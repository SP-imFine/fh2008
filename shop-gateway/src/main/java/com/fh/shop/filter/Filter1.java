package com.fh.shop.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

@Slf4j
public class Filter1 extends ZuulFilter {

    /**
     * pre  访问微服务前执行
     * post  访问微服务后执行
     *
     * @return
     */

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    /**
     * 同类型，值越小，执行优先级越高
     * @return
     */
    @Override
    public int filterOrder() {
        return 10;
    }

    /**
     * true开启 false关闭
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 核心业务逻辑，返回null
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        log.info("执行==============================pre");
        return null;
    }
}
