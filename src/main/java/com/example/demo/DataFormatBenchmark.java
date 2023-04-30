package com.example.demo;

import com.example.demo.formats.CustomerStatus;
import com.example.demo.formats.DataFormat;
import com.example.demo.formats.EnterpriseLevelCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty("bench-server.enabled")
public class DataFormatBenchmark {
    private static final Logger log = LoggerFactory.getLogger(DataFormatBenchmark.class);
    private final int iterationCount;
    private final boolean verifyCorrectness;

    @Autowired
    DataFormatBenchmark(
            @Value("${benchmark.iteration-count}") int iterationCount,
            @Value("${benchmark.verify-correctness}") boolean verifyCorrectness
    ) {
        this.iterationCount = iterationCount;
        this.verifyCorrectness = verifyCorrectness;
    }

    private EnterpriseLevelCustomer prepareData() {
        var history = Map.of(
                "today", List.of("milk", "sugar"),
                "yesterday", List.of("eggs", "sausages")
        );
        return new EnterpriseLevelCustomer(
                "jon snow",
                20,
                35.348,
                CustomerStatus.ALIVE,
                List.of("balerion", "wolf"),
                history
        );
    }

    // I hope this is enough to trick the optimizer
    public volatile boolean printAllToStdout = false;

    private void iterationE2e(EnterpriseLevelCustomer input, DataFormat dataFormat) throws Exception {
        byte[] repr = dataFormat.serialize(input);
        if (printAllToStdout) {
            System.out.write(repr);
        }
        var output = dataFormat.deserialize(repr);
        if (verifyCorrectness && !output.equals(input)) {
            throw new RuntimeException("data format " + dataFormat.name() + " corrupted data");
        }
    }

    private void iterationSerialize(EnterpriseLevelCustomer input, DataFormat dataFormat) throws Exception {
        byte[] repr = dataFormat.serialize(input);
        if (printAllToStdout) {
            System.out.write(repr);
        }
    }

    private void iterationDeserialize(byte[] repr, DataFormat dataFormat) throws Exception {
        var output = dataFormat.deserialize(repr);
        if (printAllToStdout) {
            System.out.print(output);
        }
    }

    private void runBatchE2e(EnterpriseLevelCustomer input, DataFormat dataFormat) throws Exception {
        for (int i = 0; i < iterationCount; ++i) {
            iterationE2e(input, dataFormat);
        }
    }

    private void runBatchSerialize(EnterpriseLevelCustomer input, DataFormat dataFormat) throws Exception {
        for (int i = 0; i < iterationCount; ++i) {
            iterationSerialize(input, dataFormat);
        }
    }

    private void runBatchDeserialize(byte[] repr, DataFormat dataFormat) throws Exception {
        for (int i = 0; i < iterationCount; ++i) {
            iterationDeserialize(repr, dataFormat);
        }
    }

    private Duration measureRoundTripDuration(DataFormat dataFormat, EnterpriseLevelCustomer data) {
        long begin;
        long end;
        try {
            // warm-up
            runBatchE2e(data, dataFormat);
            begin = System.nanoTime();
            runBatchE2e(data, dataFormat);
            end = System.nanoTime();
        } catch (Exception ex) {
            throw new RuntimeException("unexpected exception", ex);
        }

        long average = (end - begin) / iterationCount;
        return Duration.ofNanos(average);
    }
    private Duration measureSerializationDuration(DataFormat dataFormat, EnterpriseLevelCustomer data) {
        long begin;
        long end;
        try {
            // warm-up
            runBatchSerialize(data, dataFormat);
            begin = System.nanoTime();
            runBatchSerialize(data, dataFormat);
            end = System.nanoTime();
        } catch (Exception ex) {
            throw new RuntimeException("unexpected exception", ex);
        }

        long average = (end - begin) / iterationCount;
        return Duration.ofNanos(average);
    }

    private Duration measureDeserializationDuration(DataFormat dataFormat, byte[] repr) {
        long begin;
        long end;
        try {
            // warm-up
            runBatchDeserialize(repr, dataFormat);
            begin = System.nanoTime();
            runBatchDeserialize(repr, dataFormat);
            end = System.nanoTime();
        } catch (Exception ex) {
            throw new RuntimeException("unexpected exception", ex);
        }

        long average = (end - begin) / iterationCount;
        return Duration.ofNanos(average);
    }



    private int measureBinarySize(DataFormat dataFormat, EnterpriseLevelCustomer data) {
        try {
            byte[] res = dataFormat.serialize(data);
            EnterpriseLevelCustomer restored = dataFormat.deserialize(res);
            if (!restored.equals(data)) {
                log.error("was {}, got {}", data, restored);
                throw new RuntimeException("data format lost data");
            }

            return res.length;
        } catch (Exception ex) {
            throw new RuntimeException("unexpected exception", ex);
        }
    }

    public record BenchResult(
            Duration averageRoundTripDuration,
            int binarySize,
            Duration averageSerializationDuration,
            Duration averageDeserializationDuration
    ) {}

    public BenchResult runBenchmark(DataFormat dataFormat) {
        EnterpriseLevelCustomer data = prepareData();
        byte[] repr;
        try {
            repr = dataFormat.serialize(data);
        } catch (Exception ex) {
            throw new RuntimeException("unexpected exception", ex);
        }
        return new BenchResult(
                measureRoundTripDuration(dataFormat, data),
                measureBinarySize(dataFormat, data),
                measureSerializationDuration(dataFormat, data),
                measureDeserializationDuration(dataFormat, repr)
        );
    }
}
