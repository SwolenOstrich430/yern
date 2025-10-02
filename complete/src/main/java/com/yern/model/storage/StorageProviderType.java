package com.yern.model.storage;

import com.google.api.services.storage.Storage;

public enum StorageProviderType {
    GCS("gcs");

    private final String providerIdentifier;

    StorageProviderType(String providerIdentifier) {
        this.providerIdentifier = providerIdentifier;
    }

    public static StorageProviderType defaultOr(StorageProviderType value) {
        return value != null ? value : GCS;
    }

    public static StorageProviderType defaultOr(String providerIdentifier) {
        StorageProviderType[] values = StorageProviderType.values();

        for (StorageProviderType type: values) {
            if (type.providerIdentifier.equals(providerIdentifier)) {
                return type;
            }
        }

        return GCS;
    }
}
