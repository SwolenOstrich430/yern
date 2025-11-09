package com.yern.service.messaging;

@FunctionalInterface
public
interface Processor<T> {
    void process(T convertedMessage);
}