package com.cesco.pillintime.api.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MemberDto {

    private Long id;
    private String name;
    private String ssn;
    private String phone;
    private Integer gender;
    private Long cabinetId;
    private String fcmToken;

    @JsonProperty(value = "isManager")
    private boolean isManager;

    @JsonProperty(value = "isSubscriber")
    private boolean isSubscriber;


}