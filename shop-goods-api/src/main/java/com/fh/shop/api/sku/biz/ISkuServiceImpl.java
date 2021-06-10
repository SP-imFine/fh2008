package com.fh.shop.api.sku.biz;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.api.goods.po.Sku;
import com.fh.shop.api.sku.mapper.ISkuMapper;
import com.fh.shop.api.sku.vo.SkuVo;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("skuService")
@Transactional(rollbackFor = Exception.class)
public class ISkuServiceImpl implements ISkuService{

    @Autowired
    private ISkuMapper skuMapper;

    @Override
    @Transactional(readOnly = true)
    public ServerResponse findRecommendNewProductList() {

        String skuListString = RedisUtil.get("skuList");
        //从redis缓存中获取
        if(StringUtils.isNotEmpty(skuListString)){
            //从缓存中取数据，不必访问数据库
            List<SkuVo> skuVoList = JSON.parseArray(skuListString, SkuVo.class);
            return ServerResponse.success(skuVoList);
        }
        //如果缓存中不存在，则从数据库中取数据，并且往redis中存一份
        QueryWrapper<Sku> skuQueryWrapper = new QueryWrapper<>();
        skuQueryWrapper.eq("status","1");
        skuQueryWrapper.eq("recommend","1");
        skuQueryWrapper.eq("newProduct","1");
        List<Sku> skuList = skuMapper.selectList(skuQueryWrapper);
        List<SkuVo> skuVoList = skuList.stream().map(a -> {
            SkuVo skuVo = new SkuVo();
            skuVo.setId(a.getId());
            skuVo.setSkuName(a.getSkuName());
            skuVo.setPrice(a.getPrice().toString());
            skuVo.setImage(a.getImage());
            return skuVo;
        }).collect(Collectors.toList());
        String jsonString = JSON.toJSONString(skuVoList);
        RedisUtil.setEx("skuList",20,jsonString);
        return ServerResponse.success(skuVoList);
    }

    @Override
    public ServerResponse findSku(Long id) {
        Sku sku = skuMapper.selectById(id);
        ServerResponse<Sku> success = ServerResponse.success(sku);
        success.getData()
        return ServerResponse.success(sku);
    }

}
