package com.fh.shop.api.sku.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuVo implements Serializable {

    private Long id;

    private String skuName;

    private String image;

    private String price;

}
