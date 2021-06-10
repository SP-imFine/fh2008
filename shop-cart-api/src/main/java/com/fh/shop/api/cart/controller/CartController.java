package com.fh.shop.api.cart.controller;

import com.fh.shop.api.BaseController;
import com.fh.shop.api.cart.biz.ICartService;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.common.Constants;
import com.fh.shop.common.ServerResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/carts")
@Api(tags="购物车接口")
public class CartController extends BaseController {

    @Resource
    private ICartService cartService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping("/addCartItem")
    @ApiOperation("添加商品到购物车")
    @ApiImplicitParams({
        @ApiImplicitParam(name="count",value = "商品数量",example = "0",dataType = "java.lang.Long",required = true),
        @ApiImplicitParam(name="skuId",value = "商品ID",example = "0",dataType = "java.lang.Long",required = true),
        @ApiImplicitParam(name="x-auth",value = "头信息",dataType = "java.lang.String",required = true,paramType = "header")
    })
    public ServerResponse addCartItem(Long count,Long skuId){
        MemberVo memberVo = buildMemberVo(request);
        Long memberVoId = memberVo.getId();
        return cartService.add(memberVoId,skuId,count);
    }

    @GetMapping("/showCart")
    @ApiOperation("展示购物车中的商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name="x-auth",value = "头信息",dataType = "java.lang.String",required = true,paramType = "header")
    })
    public ServerResponse showCart(){
        MemberVo memberVo = buildMemberVo(request);
        Long memberVoId = memberVo.getId();
        return cartService.showCart(memberVoId);
    }


    @GetMapping("/showCartCount")
    @ApiOperation("购物车中的商品数量")
    @ApiImplicitParam(name="x-auth",value = "头信息",dataType = "java.lang.String",required = true,paramType = "header")
    public ServerResponse showCartCount(){
        MemberVo memberVo = buildMemberVo(request);
        Long memberVoId = memberVo.getId();
        return cartService.showCartCount(memberVoId);
    }

    @DeleteMapping("/deleteCartItem")
    @ApiOperation("删除购物车中的商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name="skuId",value = "商品id",example = "0",dataType = "java.lang.Long",required = true),
            @ApiImplicitParam(name="x-auth",value = "头信息",dataType = "java.lang.String",required = true,paramType = "header")
    })
    public ServerResponse deleteCartItem(Long skuId){
        MemberVo memberVo = buildMemberVo(request);
        Long memberVoId = memberVo.getId();
        return cartService.deleteCartItem(memberVoId,skuId);
    }

    @DeleteMapping("/deleteBatch")
    @ApiOperation("批量删除购物车中的商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name="ids",value = "商品id集合",dataType = "java.lang.String",required = true),
            @ApiImplicitParam(name="x-auth",value = "头信息",dataType = "java.lang.String",required = true,paramType = "header")
    })
    public ServerResponse deleteBatch(String ids){
        MemberVo memberVo = buildMemberVo(request);
        Long memberVoId = memberVo.getId();
        return cartService.deleteBatch(memberVoId,ids);
    }

}
