package com.yern.mapper.storage.file;

import com.yern.model.storage.StorageProviderType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StorageProviderTypeConverter implements AttributeConverter<StorageProviderType, String> {
    public String convertToDatabaseColumn(StorageProviderType attribute) {
        return StorageProviderType.defaultOr(attribute).name().toLowerCase(); 
    }

    @Override
    public StorageProviderType convertToEntityAttribute(String dbData) {
        return StorageProviderType.defaultOr(dbData); 
    }
}