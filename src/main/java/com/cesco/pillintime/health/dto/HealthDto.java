package com.cesco.pillintime.health.dto;

import lombok.Data;

import java.sql.Time;

@Data
public class HealthDto {

    private long id;

    private Integer steps;

    private double cal;

    private Time sleepTime;

}