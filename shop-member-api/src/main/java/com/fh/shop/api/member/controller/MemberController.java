package com.fh.shop.api.member.controller;

import com.fh.shop.api.BaseController;
import com.fh.shop.api.member.biz.IMemberService;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.KeyUtil;
import com.fh.shop.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/members")
@Api(tags="会员接口")
@Slf4j
public class MemberController extends BaseController {

    @Resource(name="memberService")
    private IMemberService memberService;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation(value="会员登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberName",value = "会员名",dataType = "java.lang.String",required = true),
            @ApiImplicitParam(name = "password",value = "密码",dataType = "java.lang.String",required = true)
    })
    @PostMapping("/login")
    public ServerResponse login(String memberName, String password){
        log.debug("-----*****登录*****-----");
        return memberService.login(memberName,password);
    }


    @ApiOperation(value="注销")
    @GetMapping("/logout")
    public ServerResponse logout() {
        //从请求头信息中获取信息
        MemberVo memberVo = buildMemberVo(request);
        RedisUtil.del(KeyUtil.buildMemberKey(memberVo.getId()));
        return ServerResponse.success();
    }


    @ApiOperation(value="查找用户")
    @ApiImplicitParam(name = "x-auth",value = "头信息",dataType = "java.lang.String",required = true,paramType = "header")
    @GetMapping("/findMember")
    public ServerResponse findMember() {
        //从请求头信息中获取信息
        MemberVo memberVo = buildMemberVo(request);
        return ServerResponse.success(memberVo);
    }

}
