package com.fh.shop.filter;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.common.Constants;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.KeyUtil;
import com.fh.shop.util.Md5Util;
import com.fh.shop.util.RedisUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class JwtFilter extends ZuulFilter {

    @Value("${fh.shop.checkUrl}")
    private List<String> checkUrl;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @SneakyThrows
    @Override
    public Object run() throws ZuulException {
        //获取当前访问的url
        //判断当前请求的url是否包含需要传递头信息的请求url
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String requestURI = request.getRequestURI();
        boolean isCheck = false;
        for (String s : checkUrl) {
            if(requestURI.indexOf(s)>0){
                isCheck = true;//当前请求需要判断请求头信息
                break;
            }
        }
        if(!isCheck){
            return null;//放行
        }
        //eyJpZCI6NSwibWVtYmVyTmFtZSI6IuWTiOWTiOWTiCIsIm5pY2tOYW1lIjoi5Zi75Zi75Zi7In0=.ZTI4MDNmY2JiYjNlMTI2M2NiMjY0ZThlMzkxNDFiN2E=
        //判断是否有头信息
        String header = request.getHeader("x-auth");
        if(StringUtils.isEmpty(header)){
            //拦截
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_MISS);
        }
        //判断头信息格式是否正确
        String[] headerArr = header.split("\\.");
        if(headerArr.length!=2){
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_NOT_FULL);
        }
        //验签
        String memberVoJsonBase64 = headerArr[0];
        String signBase64 = headerArr[1];
        //Base64解码
        String memberJson = new String(Base64.getDecoder().decode(memberVoJsonBase64),"utf-8");
        String sign = new String(Base64.getDecoder().decode(signBase64),"utf-8");
        String newSign = Md5Util.buildSign(memberJson, Constants.SECRET);
        if(!newSign .equals(sign) ){
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_FAIL);
        }
        //转成java对象
        MemberVo memberVo = JSON.parseObject(memberJson, MemberVo.class);
        Long id = memberVo.getId();
        //判断redis缓存中是否存活
        if(!RedisUtil.exist(KeyUtil.buildMemberKey(id))){
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_TIME_OUT);
        }
        //续命
        RedisUtil.expire(KeyUtil.buildMemberKey(id),Constants.TOKEN_EXPIRE);
        //将memberVo放入request中
//        URLDecoder.decode(JSON.toJSONString())
//        request.setAttribute(Constants.CURR_MEMBER,memberVo);
//        log.info("===={}",checkUrl);
        //把要传递给后台微服务的信息放在url请求头里
        currentContext.addZuulRequestHeader(Constants.CURR_MEMBER,URLEncoder.encode(memberJson,"utf-8"));
        return null;
    }

    private Object buildResponse(RequestContext currentContext, ResponseEnum tokenException) {
        HttpServletResponse response = currentContext.getResponse();
        //解决中文乱码
        response.setContentType("application/json;charset=utf-8");
        //拦截
        currentContext.setSendZuulResponse(false);
        ServerResponse error = ServerResponse.error(tokenException);
        //向前台响应信息
        String s = JSON.toJSONString(error);
        currentContext.setResponseBody(s);
        return null;
    }
}
