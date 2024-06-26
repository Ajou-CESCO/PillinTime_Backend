package com.cesco.pillintime.api.log.service;

import com.cesco.pillintime.api.log.dto.LogResponseDto;
import com.cesco.pillintime.api.log.dto.SensorDto;
import com.cesco.pillintime.api.cabinet.entity.Cabinet;
import com.cesco.pillintime.api.cabinet.repository.CabinetRepository;
import com.cesco.pillintime.api.log.dto.LogDto;
import com.cesco.pillintime.api.log.entity.Log;
import com.cesco.pillintime.api.log.entity.TakenStatus;
import com.cesco.pillintime.api.log.mapper.LogMapper;
import com.cesco.pillintime.api.log.repository.LogRepository;
import com.cesco.pillintime.api.member.entity.Member;
import com.cesco.pillintime.api.member.repository.MemberRepository;
import com.cesco.pillintime.api.plan.entity.Plan;
import com.cesco.pillintime.api.plan.repository.PlanRepository;
import com.cesco.pillintime.exception.CustomException;
import com.cesco.pillintime.exception.ErrorCode;
import com.cesco.pillintime.fcm.strategy.FcmStrategy;
import com.cesco.pillintime.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final PlanRepository planRepository;
    private final MemberRepository memberRepository;
    private final CabinetRepository cabinetRepository;
    private final SecurityUtil securityUtil;
    private final ApplicationContext context;

    @Scheduled(cron = "0 50 23 * * SUN")
    @Transactional
    public void createDoseLog() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        planRepository.findActivePlan(today).ifPresent(planList -> {
            for (Plan plan : planList) {
                LocalDate plannedDate = calculateNextPlannedDate(today, plan.getWeekday());
                LocalTime plannedTime = plan.getTime();
                LocalDateTime plannedAt = plannedDate.atTime(plannedTime);

                // 예정 시각이 실제 계획의 시작 시각보다 이를 경우 무시
                if (plannedDate.isBefore(plan.getStartAt())) {
                    continue;
                }

                // 현재 시각보다 예정 시각이 빠른 경우 무시
                if (plannedAt.isBefore(now)) {
                    continue;
                }

                LocalDate endAt = plan.getEndAt();

                // 해당 날짜 및 Plan 에 대한 Log 가 없을 경우에만 생성
                // 계산된 plannedAt이 계획의 종료일보다 작거나 같을 경우에만 생성
                boolean logExists = logRepository.existsByMemberAndPlanAndPlannedAt(plan.getMember(), plan, plannedAt);
                if (!logExists && plannedDate.isBefore(endAt) || plannedDate.isEqual(endAt)) {
                    Log log = new Log();
                    log.setMember(plan.getMember());
                    log.setPlan(plan);
                    log.setPlannedAt(plannedAt);
                    log.setMedicineId(plan.getMedicineId());
                    log.setMedicineName(plan.getMedicineName());
                    log.setTakenStatus(TakenStatus.NOT_COMPLETED);

                    logRepository.save(log);
                }
            }
        });
    }

    public LogResponseDto getDoseLogByMemberId(Long targetId, LocalDate date) {
        Member requestMember = SecurityUtil.getCurrentMember()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        Member targetMember = memberRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (!requestMember.equals(targetMember)) {
            securityUtil.checkPermission(requestMember, targetMember);
        } else {
            targetMember = requestMember;
        }

        if (targetMember.isManager()) {
            throw new CustomException(ErrorCode.INVALID_USERTYPE);
        }

        if (date == null) {
            date = LocalDate.now();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Optional<List<Log>> logListOptional = logRepository.findByMemberAndPlannedAtBetween(targetMember, startOfDay, endOfDay);

        List<LogDto> logDtoList = new ArrayList<>();
        logListOptional.ifPresent(logs -> {
            for (Log log : logs) {
                LogDto logDto = LogMapper.INSTANCE.toDto(log);
                logDtoList.add(logDto);
            }
        });

        logDtoList.sort(Comparator
                .comparing(LogDto::getCabinetIndex)
                .thenComparing(LogDto::getPlannedAt));

        List<Long> cabinetIndexList = planRepository.findUsingCabinetIndex(targetMember);

        return new LogResponseDto(cabinetIndexList, logDtoList);
    }

    @Transactional
    public void updateDoseLogByCabinet(SensorDto sensorDto) {
        String serial = sensorDto.getSerial();
        int index = sensorDto.getIndex();

        // Cabinet 정보 가져오기
        Cabinet cabinet = cabinetRepository.findBySerial(serial)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CABINET));

        System.out.println(cabinet.getSerial());
        System.out.println(index);

        Optional<Member> owner = memberRepository.findByCabinet(cabinet);

        owner.ifPresent(member -> {
            // 현재 날짜, 시각 구하기
            LocalDateTime currentTime = LocalDateTime.now();

            LocalDateTime rangeStartTime = currentTime.minusMinutes(30);
            LocalDateTime rangeEndTime = currentTime.plusMinutes(30);

            // 타겟 로그 조회 후 존재할 시 업데이트
            logRepository.findTargetLog(member, index, rangeStartTime, rangeEndTime)
                    .ifPresent(log -> {
                        log.setTakenStatus(TakenStatus.COMPLETED);
                        logRepository.save(log);
                    });
        });
    }

    @Transactional
//    @Scheduled(cron = "0 * * * * *")
    @Scheduled(cron = "0 0/30 * * * *")
    public void updateDoseLogByCurrentTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime targetTime = currentTime.minusMinutes(30);

        LocalDateTime startOfSecond = currentTime.truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endOfSecond = startOfSecond.plus(999, ChronoUnit.MILLIS);

        // 현재 시각과 일치하는 예정 계획이 있을 경우 푸시알림
        logRepository.findPlannedLogBetween(startOfSecond, endOfSecond)
                .ifPresent((plannedLogList) -> {
                    for (Log log : plannedLogList) {
                        Map<String, Object> requestParams = new HashMap<>();
                        requestParams.put("log", log);

                        try {
                            FcmStrategy clientPlanStrategy = context.getBean("clientPlanStrategy", FcmStrategy.class);
                            clientPlanStrategy.execute(requestParams);
                        } catch (Exception ignored) {
                        }

                    }
                });

        // 예정 시각보다 30분 초과한 미완료된 로그들을 조회하여 업데이트 및 푸시알림
        List<Log> incompletedLogList = logRepository.findIncompleteLog(targetTime);
        incompletedLogList.forEach(log -> {
            log.setTakenStatus(TakenStatus.TIMED_OUT);
            logRepository.save(log);

            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("log", log);

            try {
                FcmStrategy clientLogStrategy = context.getBean("clientOverLogStrategy", FcmStrategy.class);
                clientLogStrategy.execute(requestParams);

                FcmStrategy managerLogStrategy = context.getBean("managerOverLogStrategy", FcmStrategy.class);
                managerLogStrategy.execute(requestParams);
            } catch (Exception ignored) {
            }
        });
    }

    // ======================================================

    private LocalDate calculateNextPlannedDate(LocalDate today, Integer weekday) {
        DayOfWeek targetDayOfWeek = DayOfWeek.of(weekday);
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        int daysToAdd = targetDayOfWeek.getValue() - todayDayOfWeek.getValue();
        if (daysToAdd < 0) {
            daysToAdd += 7;
        }

        return today.plusDays(daysToAdd);
    }

}


