package com.cesco.pillintime.api.log.controller;

import com.cesco.pillintime.response.dto.ResponseDto;
import com.cesco.pillintime.api.log.dto.LogDto;
import com.cesco.pillintime.api.log.service.LogService;
import com.cesco.pillintime.response.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dose/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping
    public ResponseEntity<ResponseDto> getDoseLogByMemberId(@RequestParam Long memberId) {
        List<LogDto> logDtoList = logService.getDoseLogByMemberId(memberId);
        return ResponseUtil.makeResponse(200, "Success get dose log", logDtoList);
    }
}