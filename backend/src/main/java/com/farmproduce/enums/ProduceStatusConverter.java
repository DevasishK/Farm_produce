package com.farmproduce.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps legacy {@code APPROVED} rows in {@code farm_produce.status} to {@link ProduceStatus#GRADED}
 * so Hibernate can load older databases after the enum rename.
 */
@Converter(autoApply = false)
public class ProduceStatusConverter implements AttributeConverter<ProduceStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProduceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ProduceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return ProduceStatus.SUBMITTED;
        }
        String v = dbData.trim();
        if ("APPROVED".equalsIgnoreCase(v)) {
            return ProduceStatus.GRADED;
        }
        return ProduceStatus.valueOf(v);
    }
}
