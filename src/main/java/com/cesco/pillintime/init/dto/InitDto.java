package com.cesco.pillintime.init.dto;

import com.cesco.pillintime.member.dto.MemberDto;
import com.cesco.pillintime.relation.dto.RelationDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({"memberId", "name", "ssn", "phone", "gender", "cabinetId", "isManager", "isSubscriber", "relationList"})
public class InitDto {

    private Long memberId;
    private String name;
    private String ssn;
    private String phone;
    private Integer gender;
    private Long cabinetId;

    @JsonProperty(value = "isManager")
    private boolean isManager;

    @JsonProperty(value = "isSubscriber")
    private boolean isSubscriber;

    private List<RelationDto> relationList;

    public InitDto(MemberDto memberDto, List<RelationDto> relationDtoList) {
        this.memberId = memberDto.getId();
        this.name = memberDto.getName();
        this.ssn = memberDto.getSsn();
        this.phone = memberDto.getPhone();
        this.gender = memberDto.getGender();
        this.cabinetId = memberDto.getCabinetId();
        this.isManager = memberDto.isManager();
        this.isSubscriber = memberDto.isSubscriber();
        this.relationList = relationDtoList;
    }
}
