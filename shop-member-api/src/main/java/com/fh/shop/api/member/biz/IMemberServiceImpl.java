package com.fh.shop.api.member.biz;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.api.member.mapper.IMemberMapper;
import com.fh.shop.api.member.po.Member;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.common.Constants;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.KeyUtil;
import com.fh.shop.util.Md5Util;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service("memberService")
@Transactional(rollbackFor = Exception.class)
public class IMemberServiceImpl implements IMemberService {

    @Autowired
    private IMemberMapper memberMapper;

    @Override
    public ServerResponse login(String memberName, String password) {
        //验证非空
        if(StringUtils.isEmpty(memberName)||StringUtils.isEmpty(password)){
            return ServerResponse.error(ResponseEnum.MEMBER_INFO_IS_NULL);
        }
        //验证用户是否存在
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("memberName",memberName);
        Member member = memberMapper.selectOne(queryWrapper);
        if(member == null){
            return ServerResponse.error(ResponseEnum.MEMBER_NAME_IS_NOT_EXIST);
        }
        //验证密码是否正确
        if(!member.getPassword().equals(Md5Util.md5(password))){
            return ServerResponse.error(ResponseEnum.PASSWORD_IS_ERROR);
        }
        //验证用户是否激活
        if(member.getStatus().equals(Constants.MEMBER_STATUS_ERROR)){
            Map map = new HashMap();
            map.put("mail",member.getEmail());
            map.put("id",member.getId()+"");
            String activeInfo = JSON.toJSONString(map);
            return ServerResponse.error(ResponseEnum.MEMBER_STATUS_ERROR,activeInfo);
        }
        //登陆之后把会员信息签名加密
        MemberVo memberVo = new MemberVo();
        Long id = member.getId();
        memberVo.setId(id);
        memberVo.setMemberName(member.getMemberName());
        memberVo.setNickName(member.getNickName());
        //转换成json字符串
        //生成签名
        String jsonString = JSON.toJSONString(memberVo);
        String sign = Md5Util.buildSign(jsonString , Constants.SECRET);
        //把用户信息和签名转换成base64编码
        String memberVoBase64 = Base64.getEncoder().encodeToString(jsonString.getBytes());
        String signBase64 = Base64.getEncoder().encodeToString(sign.getBytes());
        RedisUtil.setEx(KeyUtil.buildMemberKey(id),Constants.TOKEN_EXPIRE,"");
        return ServerResponse.success(memberVoBase64+"."+signBase64);
    }


}
