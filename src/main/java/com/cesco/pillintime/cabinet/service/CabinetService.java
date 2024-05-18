package com.cesco.pillintime.cabinet.service;

import com.cesco.pillintime.cabinet.dto.CabinetDto;
import com.cesco.pillintime.cabinet.dto.SensorDto;
import com.cesco.pillintime.cabinet.entity.Cabinet;
import com.cesco.pillintime.cabinet.repository.CabinetRepository;
import com.cesco.pillintime.exception.CustomException;
import com.cesco.pillintime.exception.ErrorCode;
import com.cesco.pillintime.log.entity.Log;
import com.cesco.pillintime.log.entity.TakenStatus;
import com.cesco.pillintime.log.repository.LogRepository;
import com.cesco.pillintime.member.entity.Member;
import com.cesco.pillintime.member.repository.MemberRepository;
import com.cesco.pillintime.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CabinetService {

    private final CabinetRepository cabinetRepository;
    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    public void createCabinet(CabinetDto cabinetDto) {
        String serial = cabinetDto.getSerial();
        Long ownerId = cabinetDto.getOwnerId();

        Member requestMember = SecurityUtil.getCurrentMember()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        Member targetMember = memberRepository.findById(ownerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (!requestMember.equals(targetMember)) { // ??? -> requestMember.getId() == ownerId
            // targetMember = memberRepository.findById(ownerId)
            //                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
            SecurityUtil.checkPermission(requestMember, targetMember);
        } else {
            targetMember = requestMember; // ???
        }

        Cabinet cabinet = cabinetRepository.findBySerial(serial)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CABINET));

        cabinet.setOwner(targetMember);
        targetMember.setCabinet(cabinet);
        cabinetRepository.save(cabinet);
        memberRepository.save(targetMember);
    }

    public void getSensorData(SensorDto sensorDto) {
        String serial = sensorDto.getSerial();
        int sensorIndex = sensorDto.getIndex();

        // Cabinet 정보 가져오기
        Cabinet cabinet = cabinetRepository.findBySerial(serial)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CABINET));

        Optional<Member> owner = memberRepository.findByCabinet(cabinet); // ??? -> Member owner = cabinet.getOwner();

        owner.ifPresent(member -> {
            // 현재 날짜, 시각 구하기
            LocalDate today = LocalDate.now();
            LocalDateTime currentTime = LocalDateTime.now();

            // 오늘의 로그 조회
            Optional<List<Log>> logListOptional = logRepository.findByMemberAndPlannedAtAndIndex(member, today, sensorIndex); // ??? 예외처리

            // 가장 근접한 로그 찾기
            Optional<Log> nearestLogOptional = logListOptional.flatMap(logs -> logs.stream()
                    .min(Comparator.comparing(log -> Math.abs(Duration.between(log.getPlan().getTime(), currentTime).toSeconds())))
            );

            // 로그가 존재하는 경우 상태 업데이트
            nearestLogOptional.ifPresent(nearestLog -> {
                nearestLog.setTakenStatus(TakenStatus.COMPLETED);
                logRepository.save(nearestLog);
            });
        });
    }
}
