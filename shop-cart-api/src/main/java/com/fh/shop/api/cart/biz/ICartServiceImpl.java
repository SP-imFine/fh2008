package com.fh.shop.api.cart.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cart.vo.CartItemVo;
import com.fh.shop.api.cart.vo.CartVo;
import com.fh.shop.api.goods.IGoodsFeignService;
import com.fh.shop.api.goods.po.Sku;
import com.fh.shop.common.Constants;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.BigDecimalUtil;
import com.fh.shop.util.KeyUtil;
import com.fh.shop.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("cartService")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ICartServiceImpl implements ICartService {

    @Autowired
    private IGoodsFeignService goodsFeignService;

    @Value("${sku.count.limit}")
    private int countLimit;

    @Override
    public ServerResponse add(Long memberVoId, Long skuId, Long count) {
        //商品每人限购十件
        if(count > countLimit){
            return ServerResponse.error(ResponseEnum.CART_SKU_COUNT_LIMIT);
        }
        //商品是否存在
        ServerResponse<Sku> skuResponse = goodsFeignService.findSku(skuId);
        Sku sku = skuResponse.getData();
        if(sku == null){
            return ServerResponse.error(ResponseEnum.CART_SKU_IS_NULL);
        }
        //商品是否上架
        if(sku.getStatus().equals(Constants.SKU_STATUS_DOWN)){
            return ServerResponse.error(ResponseEnum.CART_SKU_STATUS_DOWN);
        }
        //商品库存是否大于购买量
        if(sku.getStock().intValue()<count.intValue()){
            return ServerResponse.error(ResponseEnum.CART_SKU_STOCK_ERROR);
        }
        //需要添加到购物车的商品
        CartItemVo cartItemVo = new CartItemVo();
        cartItemVo.setSkuId(sku.getId());
        cartItemVo.setSkuName(sku.getSkuName());
        String price = sku.getPrice().toString();
        cartItemVo.setSkuPrice(price);
        cartItemVo.setSkuImage(sku.getImage());
        cartItemVo.setCount(count);
        BigDecimal mul = BigDecimalUtil.mul(price, count + "");
        cartItemVo.setSubPrice(mul.toString());
        //会员是否有购物车
        String cartKey = KeyUtil.buildCartKey(memberVoId);
        boolean exist = RedisUtil.exist(cartKey);
        //没有购物车
        if(!exist){
            if(count<0){
                return ServerResponse.error(ResponseEnum.CART_SKU_COUNT_ERROR);
            }
            //会员没有购物车，直接创建购物车，并把商品添加进去
            CartVo cartVo = new CartVo();
            cartVo.getCartItemVoList().add(cartItemVo);
            cartVo.setTotalCount(count);
            cartVo.setTotalPrice(cartItemVo.getSubPrice());
            //更新redis中的购物车
            RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTJSON,JSON.toJSONString(cartVo));
            RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTCOUNT,count+"");
        }else{
            //如果有购物车
            String cartValue = RedisUtil.hget(cartKey,Constants.CART_FIELD_CARTJSON);
            CartVo cartVo = JSON.parseObject(cartValue,CartVo.class);
            List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
            Optional<CartItemVo> item = cartItemVoList.stream().filter(a -> a.getSkuId().longValue() == skuId.longValue()).findFirst();
            if(!item.isPresent()){
                if(count<0){
                    return ServerResponse.error(ResponseEnum.CART_SKU_COUNT_ERROR);
                }
                //购物车中没有这款商品，直接添加到购物车
                cartVo.getCartItemVoList().add(cartItemVo);
                updateCart(count, mul, cartKey, cartVo);
            }else {
                //购物车有这款商品，找到这个商品，更新商品数量，小计
                CartItemVo cartItemVo1 = item.get();
                long itemCount = cartItemVo1.getCount() + count;
                //商品限购（10）
                if(itemCount > countLimit){
                    return ServerResponse.error(ResponseEnum.CART_SKU_COUNT_LIMIT);
                }
                if(itemCount <= 0){
                    //从购物车删除当前商品
                    cartItemVoList.removeIf(a -> a.getSkuId().longValue()==cartItemVo.getSkuId().longValue());
                    //购物车没有商品则删除购物车
                    if(cartItemVoList.size()==0){
                        RedisUtil.del(cartKey);
                        return ServerResponse.success();
                    }
                    //更新购物车，总数，总价
                    updateCart(count, mul, cartKey, cartVo);
                    return ServerResponse.success();
                }
                cartItemVo1.setCount(itemCount);
                BigDecimal subPrice = new BigDecimal(cartItemVo1.getSubPrice());
                cartItemVo1.setSubPrice(subPrice.add(mul).toString());
                //更新购物车，总数，总价
                updateCart(count, mul, cartKey, cartVo);
            }
        }
        return ServerResponse.success();
    }

    //新增购物车 封装方法
    private void updateCart(Long count, BigDecimal mul, String cartKey, CartVo cartVo) {
        cartVo.setTotalCount(cartVo.getTotalCount()+count);
        BigDecimal totalPrice = new BigDecimal(cartVo.getTotalPrice());
        cartVo.setTotalPrice(totalPrice.add(mul).toString());
        //更新redis中的购物车
        RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTJSON,JSON.toJSONString(cartVo));
        RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTCOUNT,cartVo.getTotalCount()+"");
    }

    //查询购物车
    @Override
    public ServerResponse showCart(Long memberVoId) {
        String cartVoKey = KeyUtil.buildCartKey(memberVoId);
        String cartVoValue = RedisUtil.hget(cartVoKey,Constants.CART_FIELD_CARTJSON);
        CartVo cartVo = JSON.parseObject(cartVoValue,CartVo.class);
        return ServerResponse.success(cartVo);
    }

    //登录后展示购物车中商品数量
    @Override
    public ServerResponse showCartCount(Long memberVoId) {
        String cartVoKey = KeyUtil.buildCartKey(memberVoId);
        String cartCount = RedisUtil.hget(cartVoKey,Constants.CART_FIELD_CARTCOUNT);
        return ServerResponse.success(cartCount);
    }

    @Override
    public ServerResponse deleteCartItem(Long memberVoId, Long skuId) {
        //获取会员对应的购物车
        String cartKey = KeyUtil.buildCartKey(memberVoId);
        String cartValue = RedisUtil.hget(cartKey,Constants.CART_FIELD_CARTJSON);
        CartVo cartVo = JSON.parseObject(cartValue,CartVo.class);
        List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
        Optional<CartItemVo> itemVo = cartItemVoList.stream().filter(a -> a.getSkuId().longValue() == skuId.longValue()).findFirst();
        //判断是否有这个商品
        if(!itemVo.isPresent()){
            return ServerResponse.error(ResponseEnum.CART_SKU_ERROR);
        }
        //从购物车删除当前商品
        cartItemVoList.removeIf(a -> a.getSkuId().longValue()==skuId.longValue());
        //购物车没有商品则删除购物车
        if(cartItemVoList.size()==0){
            RedisUtil.del(cartKey);
            return ServerResponse.success();
        }
        //更新购物车
        cartVo.setCartItemVoList(cartItemVoList);
        cartVo.setTotalCount(cartVo.getTotalCount()-itemVo.get().getCount());
        BigDecimal totalPrice = new BigDecimal(cartVo.getTotalPrice());
        cartVo.setTotalPrice(totalPrice.subtract(new BigDecimal(itemVo.get().getSubPrice())).toString());
        //更新redis中的购物车
        RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTJSON,JSON.toJSONString(cartVo));
        RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTCOUNT,cartVo.getTotalCount()+"");
        return ServerResponse.success();
    }

    @Override
    public ServerResponse deleteBatch(Long memberVoId, String ids) {
        if(StringUtils.isEmpty(ids)){
            return ServerResponse.error(ResponseEnum.CART_SKU_ID_NULL);
        }
        String cartKey = KeyUtil.buildCartKey(memberVoId);
        String cartValue = RedisUtil.hget(cartKey,Constants.CART_FIELD_CARTJSON);
        CartVo cartVo = JSON.parseObject(cartValue,CartVo.class);
        List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
        Arrays.stream(ids.split(",")).forEach(a -> cartItemVoList.removeIf(b -> b.getSkuId().longValue() == Long.parseLong(a)));
        if(cartItemVoList.size()==0){
            RedisUtil.del(cartKey);
            return ServerResponse.success();
        }
        Long totalCount = 0L;
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartItemVo cartItemVo : cartItemVoList) {
            totalCount += cartItemVo.getCount();
            totalPrice = totalPrice.add(new BigDecimal(cartItemVo.getSkuPrice()));
        }
        cartVo.setTotalCount(totalCount);
        cartVo.setTotalPrice(totalPrice.toString());
        RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTJSON,JSON.toJSONString(cartVo));
        RedisUtil.hset(cartKey,Constants.CART_FIELD_CARTCOUNT,cartVo.getTotalCount()+"");
        return ServerResponse.success();
    }
}
