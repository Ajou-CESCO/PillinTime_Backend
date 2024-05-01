package com.cesco.pillintime.controller;

import com.cesco.pillintime.dto.MemberDto;
import com.cesco.pillintime.dto.ResponseDto;
import com.cesco.pillintime.entity.Member;
import com.cesco.pillintime.service.MemberService;
import com.cesco.pillintime.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping // 회원 가입
    public ResponseEntity<ResponseDto> createUser(@RequestBody MemberDto memberDto) {
        memberService.createUser(memberDto);
        return ResponseUtil.makeResponse(200, "Success create member", null);
    }

    @GetMapping // 내 정보 조회
    public ResponseEntity<ResponseDto> getUserByUuid(@RequestParam(defaultValue = "") String uuid){
        Member member = memberService.getUserByUuid(uuid);
        return ResponseUtil.makeResponse(200, "Success get member", member);
    }

    @PutMapping // 내 정보 수정
    public ResponseEntity<ResponseDto> updateUserById(@RequestParam(defaultValue = "") String uuid, @RequestBody MemberDto memberDto){
        Member member = memberService.updateUserByUuid(uuid, memberDto);
        return ResponseUtil.makeResponse(200, "Success update member", member);
    }

    @DeleteMapping // 탈퇴
    public ResponseEntity<ResponseDto> deleteUser(){
        memberService.deleteUser();
        return ResponseUtil.makeResponse(200, "Success delete member", null);
    }
}
