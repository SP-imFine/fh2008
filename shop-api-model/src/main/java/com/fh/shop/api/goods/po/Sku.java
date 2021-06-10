package com.fh.shop.api.goods.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel
public class Sku implements Serializable {
    @ApiModelProperty(value = "分类id",example = "0")
    private Long id;
    //spuName 红色 64G 标准版
    @ApiModelProperty(value = "商品名")
    private String skuName;
    @ApiModelProperty(value = "spuId",example = "0")
    private Long spuId;
    @ApiModelProperty(value = "价格",example = "0")
    private BigDecimal price;
    @ApiModelProperty(value = "库存",example = "1")
    private Integer stock;
    @ApiModelProperty(value = "规格信息")
    private String specInfo;
    @ApiModelProperty(value = "颜色id",example = "0")
    private Long colorId;
    @ApiModelProperty(value = "图片")
    private String image;
    @ApiModelProperty(value = "推荐")
    private String recommend;
    @ApiModelProperty(value = "上架")
    private String status;
    @ApiModelProperty(value = "新品")
    private String newProduct;

    private int sale = 0;
}
