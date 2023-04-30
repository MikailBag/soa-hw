package com.example.demo.formats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class YamlDataFormat implements DataFormat {
    private static class Dto {
        public String name;
        public int age;
        public double balance;
        public CustomerStatus status;
        public List<String> cats;
        public Map<String, List<String>> history;
    }

    private final ObjectMapper mapper;

    @Autowired
    YamlDataFormat() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    @Override
    public String name() {
        return "yaml";
    }

    @Override
    public byte[] serialize(EnterpriseLevelCustomer value) throws Exception {
        var dto = new Dto();
        dto.age = value.age();
        dto.balance = value.balance();
        dto.cats = value.cats();
        dto.status = value.status();
        dto.history = value.history();
        dto.name = value.name();
        return mapper.writeValueAsBytes(dto);
    }

    @Override
    public EnterpriseLevelCustomer deserialize(byte[] repr) throws Exception {
        Dto dto = mapper.readValue(repr, Dto.class);
        return new EnterpriseLevelCustomer(dto.name, dto.age, dto.balance, dto.status, dto.cats, dto.history);
    }
}
