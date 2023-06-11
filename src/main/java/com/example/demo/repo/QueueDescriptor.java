package com.example.demo.repo;

import java.io.IOException;

public interface QueueDescriptor<T> {
    String name();

    enum Policy {
        PUB_SUB,
        WORK_QUEUE
    }

    Policy policy();

    byte[] serialize(T value) throws IOException;
    T deserialize(byte[] data) throws IOException;
}
