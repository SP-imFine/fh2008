package com.fh.shop.api.cate.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cate.mapper.ICateMapper;
import com.fh.shop.api.cate.po.Cate;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("cateService")
@Transactional(rollbackFor = Exception.class)
public class ICateServiceImpl implements ICateService {

    @Autowired
    private ICateMapper cateMapper;

    @Override
    @Transactional(readOnly = true)
    public ServerResponse findList() {

        String cateListString = RedisUtil.get("cateList");
        //判断redis缓存服务器中是否存在数据
        if(StringUtils.isNotEmpty(cateListString)){
            List<Cate> cateListJson = JSON.parseArray(cateListString, Cate.class);
            return ServerResponse.success(cateListJson);
        }
        //如果不存在则查询数据库，并且把数据放入redis
        List<Cate> cateList = cateMapper.selectList(null);
        String jsonString = JSON.toJSONString(cateList);
        RedisUtil.setEx("cateList",5*60,jsonString);
        return ServerResponse.success(cateList);
    }
}
