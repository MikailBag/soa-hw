package com.example.demo.formats;

import com.example.demo.formats.avro.AvroCustomerStatus;
import com.example.demo.formats.avro.AvroEnterpriseLevelCustomer;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AvroDataFormat implements DataFormat {
    @Override
    public String name() {
        return "avro";
    }

    @Override
    public byte[] serialize(EnterpriseLevelCustomer value) throws Exception {
        AvroCustomerStatus status = switch (value.status()) {
            case UNKNOWN -> AvroCustomerStatus.UNKNOWN;
            case ALIVE -> AvroCustomerStatus.ALIVE;
            case DEAD -> AvroCustomerStatus.DEAD;
            case DEAD_INSIDE -> AvroCustomerStatus.DEAD_INSIDE;
        };
        Map<CharSequence, List<CharSequence>> history = value
                .history()
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                (e) -> e.getValue()
                                        .stream()
                                        .map((s) -> (CharSequence) s)
                                        .toList()
                        )
                );
        AvroEnterpriseLevelCustomer dto = AvroEnterpriseLevelCustomer.newBuilder()
                .setName(value.name())
                .setAge(value.age())
                .setBalance(value.balance())
                .setStatus(status)
                .setCats(value.cats().stream().map((s) -> (CharSequence) s).toList())
                .setHistory(history)
                .build();

        return dto.toByteBuffer().array();
    }

    @Override
    public EnterpriseLevelCustomer deserialize(byte[] repr) throws Exception {
        AvroEnterpriseLevelCustomer dto = AvroEnterpriseLevelCustomer.fromByteBuffer(ByteBuffer.wrap(repr));
        CustomerStatus status = switch (dto.getStatus()) {
            case UNKNOWN -> CustomerStatus.UNKNOWN;
            case ALIVE -> CustomerStatus.ALIVE;
            case DEAD -> CustomerStatus.DEAD;
            case DEAD_INSIDE -> CustomerStatus.DEAD_INSIDE;
        };

        Map<String, List<String>> history = dto
                .getHistory()
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                (e) -> e.getKey().toString(),
                                (e) -> e.getValue().stream().map(CharSequence::toString).toList()
                        )
                );

        return new EnterpriseLevelCustomer(
                dto.getName().toString(),
                dto.getAge(),
                dto.getBalance(),
                status,
                dto.getCats().stream().map(CharSequence::toString).toList(),
                history
        );
    }
}
