package com.fh.shop.api.cate.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel
public class Cate implements Serializable {
    @ApiModelProperty(value = "分类id",example = "0")
    private Long id;
    @ApiModelProperty(value = "分类名")
    private String cateName;
    @ApiModelProperty(value = "父亲id",example = "0")
    private Long fatherId;
    @ApiModelProperty(value = "类型id",example = "0")
    private Long typeId;
    @ApiModelProperty(value = "类型名")
    private String typeName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public Long getFatherId() {
        return fatherId;
    }

    public void setFatherId(Long fatherId) {
        this.fatherId = fatherId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
