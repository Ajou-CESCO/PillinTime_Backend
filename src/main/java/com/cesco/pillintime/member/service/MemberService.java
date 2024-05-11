package com.cesco.pillintime.member.service;

import com.cesco.pillintime.member.dto.MemberDto;
import com.cesco.pillintime.member.entity.Member;
import com.cesco.pillintime.relation.entity.Relation;
import com.cesco.pillintime.exception.CustomException;
import com.cesco.pillintime.exception.ErrorCode;
import com.cesco.pillintime.member.mapper.MemberMapper;
import com.cesco.pillintime.member.repository.MemberRepository;
import com.cesco.pillintime.relation.repository.RelationRepository;
import com.cesco.pillintime.util.JwtUtil;
import com.cesco.pillintime.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RelationRepository relationRepository;
    private final JwtUtil jwtUtil;

    public String createUser(MemberDto memberDto){

        String name = memberDto.getName();
        String ssn = memberDto.getSsn();
        String phone = memberDto.getPhone();
        boolean isManager = memberDto.isManager();

        // 회원가입 여부 확인
        memberRepository.findByPhone(phone)
                .ifPresent((member) -> {
                    throw new CustomException(ErrorCode.ALREADY_EXISTS_PHONE);
                });

        // 회원가입 진행
        Member member = new Member(name, phone, ssn, isManager);
        memberRepository.save(member);

        return jwtUtil.createAccessToken(memberDto);
    }

    public MemberDto getUserById(Long targetId) {

        Long requesterId = SecurityUtil.getCurrentMemberId();

        Member requestMember = memberRepository.findById(requesterId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (targetId == null) {
            return MemberMapper.INSTANCE.toDto(requestMember);
        } else {
            Member targetMember = memberRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

            if (checkPermission(requestMember, targetMember)) {
                return MemberMapper.INSTANCE.toDto(targetMember);
            }
        }

        return null;
    }

    public MemberDto updateUserById(Long targetId, MemberDto memberDto) {
        /*
        id 값 유무에 따라 내 정보 혹은 연관된 사용자 정보 수정

        id X -> 내 정보 수정
        id O -> 연관된 사용자 정보 수정. 즉 권한 체크 필요
         */

        String ssn = memberDto.getSsn();
        String name = memberDto.getName();
        String phone = memberDto.getPhone();

        Member targetMember = null;
        Member requestMember = SecurityUtil.getCurrentMember()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (targetId == null) {
            targetMember = requestMember;
        } else {
            targetMember = memberRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

            checkPermission(requestMember, targetMember);
        }

        targetMember.setSsn(ssn);
        targetMember.setName(name);
        targetMember.setPhone(phone);

        memberRepository.save(targetMember);
        return MemberMapper.INSTANCE.toDto(targetMember);
    }

    public void deleteUser(){
        Member member = SecurityUtil.getCurrentMember()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        memberRepository.delete(member);
    }

    // ============================================================================

    private boolean checkPermission(Member requestMember, Member targetMember) {
        /*
         * 요청한 사용자가 목표로 하는 사용자에 접근 권한이 있는지 확인하기 위한 함수
         * 생성된 연관 관계가 없을 경우 에러 반환
         */

        List<Relation> relationList = relationRepository.findByMember(requestMember)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_ACCESS));

        for (Relation relation : relationList) {
            if ((relation.getClient().equals(targetMember))
                    || (relation.getManager().equals(targetMember))) {
                return true;
            }
        }

        throw new CustomException(ErrorCode.INVALID_USER_ACCESS);
    }

}