package com.yern.dto.storage;

import java.time.LocalDateTime;
import java.util.Objects;

import com.google.cloud.storage.Bucket;

import lombok.Getter;

// TODO: this should probably be moved to a factory 
// TODO: add a provider enum 
public class BucketImpl {
    private String name;

    public static BucketImpl from(Bucket gcsBucket) {
        return new BucketImpl(gcsBucket.getName());
    }

    public BucketImpl(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BucketImpl)) return false;

		BucketImpl bucket = (BucketImpl) o;

		return Objects.equals(this.name, bucket.getName());
	}
}
