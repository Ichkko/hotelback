package com.example.hotelback.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoomStatusConverter implements AttributeConverter<RoomStatus, String> {

    @Override
    public String convertToDatabaseColumn(RoomStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public RoomStatus convertToEntityAttribute(String dbData) {
        return RoomStatus.fromValue(dbData);
    }
}
