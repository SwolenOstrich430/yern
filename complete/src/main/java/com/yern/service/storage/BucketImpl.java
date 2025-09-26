package com.yern.service.storage;

import com.google.cloud.storage.Bucket;

import lombok.Getter;

@Getter
public class BucketImpl {
    private String name;

    public static BucketImpl from(Bucket gcsBucket) {
        return new BucketImpl(gcsBucket.getName());
    }

    public BucketImpl(String name) {
        this.name = name;
    }
}
