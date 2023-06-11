package com.example.demo.repo;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemoryObjectStorage implements ObjectStorage {
    private final ConcurrentHashMap<String, byte[]> objects = new ConcurrentHashMap<>();

    @Override
    public void put(String key, byte[] data) {
        objects.put(key, data);
    }

    @Nullable
    @Override
    public byte[] get(String key) {
        return objects.get(key);
    }
}
