package com.yern.service.storage;

import java.io.IOException;
import java.util.List;

public class NotUniqueException extends IOException {

    public NotUniqueException(List<FileProcessor> processor) {
        super(processor.toString());
    }
}
