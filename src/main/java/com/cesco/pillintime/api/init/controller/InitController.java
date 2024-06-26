package com.cesco.pillintime.api.init.controller;

import com.cesco.pillintime.api.init.dto.InitDto;
import com.cesco.pillintime.response.dto.ResponseDto;
import com.cesco.pillintime.api.init.service.InitService;
import com.cesco.pillintime.response.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitController {

    private final InitService initService;

    @GetMapping
    public ResponseEntity<ResponseDto> initClient() {
        InitDto initDto = initService.getInitialInfo();
        return ResponseUtil.makeResponse(200, "Success get initial info", initDto);
    }


}
