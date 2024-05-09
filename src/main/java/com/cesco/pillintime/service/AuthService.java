package com.cesco.pillintime.service;

import com.cesco.pillintime.dto.LoginDto;
import com.cesco.pillintime.dto.MemberDto;
import com.cesco.pillintime.entity.Member;
import com.cesco.pillintime.exception.CustomException;
import com.cesco.pillintime.exception.ErrorCode;
import com.cesco.pillintime.mapper.MemberMapper;
import com.cesco.pillintime.repository.MemberRepository;
import com.cesco.pillintime.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public Object login(LoginDto loginDto) {
        String name = loginDto.getName();
        String phone = loginDto.getPhone();

        Member member = memberRepository.findByNameAndPhone(name, phone)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        MemberDto memberDto = MemberMapper.INSTANCE.toDto(member);
        String token = jwtUtil.createAccessToken(memberDto);
        return Map.of("access_token", token, "user_type", member.getUserType());
    }

}
