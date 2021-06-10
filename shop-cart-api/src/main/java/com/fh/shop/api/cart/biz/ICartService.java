package com.fh.shop.api.cart.biz;

import com.fh.shop.common.ServerResponse;

public interface ICartService {
    ServerResponse add(Long memberVoId, Long skuId, Long count);

    ServerResponse showCart(Long memberVoId);

    ServerResponse showCartCount(Long memberVoId);

    ServerResponse deleteCartItem(Long memberVoId, Long skuId);

    ServerResponse deleteBatch(Long memberVoId, String ids);
}
