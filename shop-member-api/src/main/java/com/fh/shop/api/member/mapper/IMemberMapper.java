package com.fh.shop.api.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fh.shop.api.member.po.Member;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface IMemberMapper extends BaseMapper<Member> {
    Member findByPhone(String phone);

    void updateScore(@Param("memberId") Long memberId, @Param("score") BigDecimal score);
}
