package com.yern.service.messaging;

import java.util.List;

@FunctionalInterface
public
interface ListProcessor<T> {
    void process(List<T> list);
}