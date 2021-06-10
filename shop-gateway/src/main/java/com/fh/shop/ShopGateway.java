package com.fh.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class ShopGateway {

    public static void main(String[] args) {
        SpringApplication.run(ShopGateway.class, args);
    }

}
