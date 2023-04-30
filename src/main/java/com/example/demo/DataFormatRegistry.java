package com.example.demo;

import com.example.demo.formats.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty("bench-server.enabled")
public class DataFormatRegistry {
    private static final Logger log = LoggerFactory.getLogger(DataFormatRegistry.class);
    private final List<DataFormat> formats;
    private final DataFormatBenchmark benchmark;

    @Autowired
    DataFormatRegistry(
            List<DataFormat> formats,
            DataFormatBenchmark benchmark
    ) {
        this.formats = formats;
        this.benchmark = benchmark;
        log.info(
                "supported formats: {}",
                formats.stream().map(DataFormat::name).collect(Collectors.joining(", "))
        );
    }

    public DataFormatBenchmark.BenchResult bench(String name) {
        DataFormat format = formats
                .stream()
                .filter((f) -> f.name().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("unknown data format " + name));
        return benchmark.runBenchmark(format);
    }


}
