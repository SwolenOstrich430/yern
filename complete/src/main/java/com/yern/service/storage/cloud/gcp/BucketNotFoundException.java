package com.yern.service.storage.cloud.gcp;

import java.io.IOException;

public class BucketNotFoundException extends IOException {

    public BucketNotFoundException(String bucket) {
        super(
            "Could not find bucket: " + bucket
        );
    }

}
