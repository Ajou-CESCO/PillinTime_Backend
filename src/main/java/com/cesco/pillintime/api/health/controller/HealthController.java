package com.cesco.pillintime.api.health.controller;

import com.cesco.pillintime.api.health.service.HealthService;
import com.cesco.pillintime.api.health.dto.HealthDto;
import com.cesco.pillintime.response.dto.ResponseDto;
import com.cesco.pillintime.response.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @PostMapping
    public ResponseEntity<ResponseDto> createHealth(@RequestBody HealthDto healthDto) {
        healthService.createHealth(healthDto);
        return ResponseUtil.makeResponse(200, "Success upload health data", null);
    }

    @GetMapping("/{targetId}")
    public ResponseEntity<ResponseDto> getHealthByMemberId(@PathVariable(required = false) Long targetId) {
        List<HealthDto> healthDtoList = healthService.getHealthByMemberId(targetId);
        return ResponseUtil.makeResponse(200, "Success get health data", healthDtoList);
    }
}