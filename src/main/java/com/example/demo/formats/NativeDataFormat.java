package com.example.demo.formats;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.Map;

@Component
public class NativeDataFormat implements DataFormat {
    private static class Dto implements Serializable {
        String name;
        int age;
        double balance;
        CustomerStatus status;
        List<String> cats;
        Map<String, List<String>> history;
    }
    @Override
    public String name() {
        return "native";
    }

    @Override
    public byte[] serialize(EnterpriseLevelCustomer value) throws Exception {
        var stream = new ByteArrayOutputStream();
        var ostream = new ObjectOutputStream(stream);
        var dto = new Dto();
        dto.age = value.age();
        dto.balance = value.balance();
        dto.cats = value.cats();
        dto.status = value.status();
        dto.name = value.name();
        dto.history = value.history();
        ostream.writeObject(dto);
        ostream.flush();
        return stream.toByteArray();
    }

    @Override
    public EnterpriseLevelCustomer deserialize(byte[] repr) throws Exception {
        var stream = new ByteArrayInputStream(repr);
        var ostream = new ObjectInputStream(stream);
        var dto =  (Dto) ostream.readObject();
        return new EnterpriseLevelCustomer(dto.name, dto.age, dto.balance, dto.status, dto.cats, dto.history);
    }
}
