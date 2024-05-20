package com.cesco.pillintime.request.mapper;

import com.cesco.pillintime.request.entity.Request;
import com.cesco.pillintime.request.dto.RequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RequestMapper {

    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    Request toEntity(RequestDto requestDto);

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", source = "sender.name")
    @Mapping(target = "senderPhone", source = "sender.phone")
    RequestDto toDto(Request request);
}
