package com.fh.shop.api.sku.biz;

import com.fh.shop.common.ServerResponse;

public interface ISkuService {

    public ServerResponse findRecommendNewProductList();

    ServerResponse findSku(Long id);

//    public List<SkuMailVo> findSkuList(int stockLimit);

}
