package com.example.demo.formats;

import com.example.demo.formats.protobuf.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProtobufDataFormat implements DataFormat {
    @Override
    public String name() {
        return "protobuf";
    }

    @Override
    public byte[] serialize(EnterpriseLevelCustomer value) throws Exception {
        Customer.CustomerStatus statusDto = switch (value.status()) {
            case ALIVE -> Customer.CustomerStatus.ALIVE;
            case DEAD -> Customer.CustomerStatus.DEAD;
            case DEAD_INSIDE -> Customer.CustomerStatus.DEAD_INSIDE;
            case UNKNOWN -> Customer.CustomerStatus.UNKNOWN;
        };
        Stream<Customer.HistoryItem> history = value
                .history()
                .entrySet()
                .stream()
                .map(
                        (entry) -> Customer.HistoryItem
                                .newBuilder()
                                .setKey(entry.getKey())
                                .addAllValues(entry.getValue())
                                .build()
                );
        Customer.EnterpriseLevelCustomer dto = Customer.EnterpriseLevelCustomer.newBuilder()
                .setName(value.name())
                .setAge(value.age())
                .setBalance(value.balance())
                .setStatus(statusDto)
                .addAllCats(value.cats())
                .addAllHistory(history.toList())
                .build();
        return dto.toByteArray();
    }

    @Override
    public EnterpriseLevelCustomer deserialize(byte[] repr) throws Exception {
        Customer.EnterpriseLevelCustomer dto = Customer.EnterpriseLevelCustomer.parseFrom(repr);
        CustomerStatus status = switch (dto.getStatus()) {
            case DEAD -> CustomerStatus.DEAD;
            case ALIVE -> CustomerStatus.ALIVE;
            case DEAD_INSIDE -> CustomerStatus.DEAD_INSIDE;
            default -> CustomerStatus.UNKNOWN;
        };
        Map<String, List<String>> history = dto
                .getHistoryList()
                .stream()
                .collect(
                        Collectors.toMap(Customer.HistoryItem::getKey, Customer.HistoryItem::getValuesList)
                );
        return new EnterpriseLevelCustomer(
                dto.getName(),
                dto.getAge(),
                dto.getBalance(),
                status,
                dto.getCatsList(),
                history
        );
    }
}
