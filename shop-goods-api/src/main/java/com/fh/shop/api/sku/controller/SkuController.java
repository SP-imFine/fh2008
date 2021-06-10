package com.fh.shop.api.sku.controller;

import com.fh.shop.api.sku.biz.ISkuService;
import com.fh.shop.common.ServerResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/skus")
public class SkuController {

    @Resource(name="skuService")
    private ISkuService skuService;

    @GetMapping("/findList")
    public ServerResponse findList(){
        return skuService.findRecommendNewProductList();
    }

    @GetMapping("/findSku")
    public ServerResponse findSku(@RequestParam("id") Long id){
        return skuService.findSku(id);
    }

}
