package com.yern.service.storage;

import java.io.IOException;
import java.util.List;

// TODO: figure out how this works 
// TODO: add actual constuctor 
public class NotUniqueException extends IOException {

    public NotUniqueException(List<FileProcessor> processor) {
        super(processor.toString());
    }
}
