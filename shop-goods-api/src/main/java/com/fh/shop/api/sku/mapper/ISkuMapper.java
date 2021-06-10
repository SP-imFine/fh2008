package com.fh.shop.api.sku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fh.shop.api.goods.po.Sku;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ISkuMapper extends BaseMapper<Sku> {
    List<Sku> findRecommendNewProductList();

//    List<SkuMailVo> findSkuList(int stockLimit);

//    int updateSkuStock(CartItemVo cartItemVo);

    void updateStockAdd(@Param("skuId") Long skuId, @Param("skuCount") Long skuCount);

    void updateSale(@Param("skuId") Long skuId, @Param("skuCount") Long skuCount);
}
