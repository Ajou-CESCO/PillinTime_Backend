package com.cesco.pillintime.dto;

import lombok.Data;

@Data
public class UserDto {

    int id;
    String uuid;
    String name;
    String phone_number;
    String ssn;
    int gender;
    boolean is_manager;
    boolean is_subscriber;
    boolean has_case;

}