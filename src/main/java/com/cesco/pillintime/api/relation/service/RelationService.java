package com.cesco.pillintime.api.relation.service;

import com.cesco.pillintime.api.member.entity.Member;
import com.cesco.pillintime.api.relation.dto.RelationDto;
import com.cesco.pillintime.api.relation.entity.Relation;
import com.cesco.pillintime.api.relation.repository.RelationRepository;
import com.cesco.pillintime.api.request.entity.Request;
import com.cesco.pillintime.api.request.repository.RequestRepository;
import com.cesco.pillintime.exception.CustomException;
import com.cesco.pillintime.exception.ErrorCode;
import com.cesco.pillintime.fcm.strategy.FcmStrategy;
import com.cesco.pillintime.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RelationService {

    private final RequestRepository requestRepository;
    private final RelationRepository relationRepository;

    private final ApplicationContext context;

    @Transactional
    public void createRelation(Long requestId) {
        Member requestMember = SecurityUtil.getCurrentMember()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_REQUEST));

        Member targetMember = request.getSender();

        // 보호자가 프리미엄 회원이 아니면서 이미 보호관계가 존재할 경우 에러 발생
        Optional<List<Relation>> relationList = relationRepository.findByManager(targetMember);
        if (relationList.isPresent() && !relationList.get().isEmpty()) {
            if (!targetMember.isSubscriber()) {
                throw new CustomException(ErrorCode.MANAGER_IS_NOT_SUBSCRIBER);
            }
        }

        Relation relation = new Relation(targetMember, requestMember);
        relationRepository.save(relation);
        requestRepository.delete(request);

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("requestMember", requestMember);
        requestParams.put("targetMember", targetMember);

        try {
            FcmStrategy relationStrategy = context.getBean("relationCreatedStrategy", FcmStrategy.class);
            relationStrategy.execute(requestParams);
        } catch (Exception ignored) {
        }

    }

    public List<RelationDto> getRelationList() {
        Member requestMember = SecurityUtil.getCurrentMember()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        List<Relation> relationList = relationRepository.findByMember(requestMember).orElse(null);
        if (relationList == null) {
            return null;
        }

        boolean isManager = requestMember.isManager();

        List<RelationDto> relationDtoList = new ArrayList<>();
        for (Relation relation : relationList) {
            RelationDto relationDto = new RelationDto();
            Member member = isManager ? relation.getClient() : relation.getManager();

            relationDto.setId(relation.getId());
            relationDto.setMemberId(member.getId());
            relationDto.setMemberName(member.getName());
            relationDto.setMemberPhone(member.getPhone());
            relationDto.setMemberSsn(member.getSsn());
            relationDto.setCabinetId(member.getCabinet() != null ? member.getCabinet().getId() : 0);

            relationDtoList.add(relationDto);
        }

        return relationDtoList;
    }

    @Transactional
    public void deleteRelation(Long relationId) {
        Member requestMember = SecurityUtil.getCurrentMember()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        Relation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RELATION));

        if (requestMember.equals(relation.getClient()) || requestMember.equals(relation.getManager())) {
            relationRepository.delete(relation);

            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("requestMember", requestMember);
            requestParams.put("relation", relation);

            try {
                FcmStrategy relationDeletedStrategy = context.getBean("relationDeletedStrategy", FcmStrategy.class);
                relationDeletedStrategy.execute(requestParams);
            } catch (Exception ignored) {
            }
        } else {
            throw new CustomException(ErrorCode.INVALID_USER_ACCESS);
        }
    }
}