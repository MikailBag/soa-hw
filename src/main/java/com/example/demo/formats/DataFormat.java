package com.example.demo.formats;

public interface DataFormat {
    String name();
    byte[] serialize(EnterpriseLevelCustomer value) throws Exception;
    EnterpriseLevelCustomer deserialize(byte[] repr) throws Exception;
}
