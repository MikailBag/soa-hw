package com.example.demo.formats;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class XmlDataFormat implements DataFormat {
    static class StringList {
        public List<String> items;
        public StringList() {

        }
        public StringList(List<String> items) {
            this.items = items;
        }

        List<String> items() {
            return items;
        }
    }
    @JacksonXmlRootElement
    static class Dto {
        @JacksonXmlProperty(isAttribute = true)
        String name;
        @JacksonXmlProperty(isAttribute = true)
        int age;
        @JacksonXmlProperty(isAttribute = true)
        double balance;
        @JacksonXmlProperty(isAttribute = true)
        CustomerStatus status;
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<String> cats;
        @JacksonXmlElementWrapper(useWrapping = false)
        public Map<String, StringList> history;

        Dto() {
        }

        Dto(EnterpriseLevelCustomer data) {
            this.name = data.name();
            this.age = data.age();
            this.balance = data.balance();
            this.status = data.status();
            this.cats = data.cats();
            this.history = data
                    .history()
                    .entrySet()
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    (e) -> new StringList(e.getValue())
                            )
                    );
        }

        EnterpriseLevelCustomer data() {
            Map<String, List<String>> history = this.history
                    .entrySet()
                    .stream()
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    (e) -> e.getValue().items()
                            )
                    );
            return new EnterpriseLevelCustomer(name, age, balance, status, cats, history);
        }
    }


    private static final Logger log = LoggerFactory.getLogger(XmlDataFormat.class);
    private final XmlMapper mapper;

    XmlDataFormat() {
        mapper = new XmlMapper();
    }

    @Override
    public String name() {
        return "xml";
    }

    @Override
    public byte[] serialize(EnterpriseLevelCustomer value) throws Exception {
        return mapper.writeValueAsBytes(new Dto(value));
    }

    @Override
    public EnterpriseLevelCustomer deserialize(byte[] repr) throws Exception {
        return mapper.readValue(repr, Dto.class).data();
    }
}
