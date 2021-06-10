package com.fh.shop.api.member.po;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Member implements Serializable {

    private Long id;

    private String memberName;

    private String password;

    private String nickName;

    private String phone;

    private String email;

    private String status;

    private BigDecimal score;
}
