package com.example.demo.repo;

import jakarta.annotation.Nullable;

public interface ObjectStorage {
    void put(String key, byte[] data);
    @Nullable
    byte[] get(String key);
}
