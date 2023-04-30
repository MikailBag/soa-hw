package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("bench-server.enabled")
public class BenchRequestHandler {
    private static final String GET_RESULT_BY_NAME_COMMAND_PREFIX = "get_result_by_name ";
    private static final String GET_RESULT_COMMAND = "get_result";
    private static final String HELLO_COMMAND = "hello";
    private final DataFormatRegistry dataFormats;
    private final String defaultFormat;
    @Autowired
    BenchRequestHandler(
            DataFormatRegistry dataFormats,
            @Value("${benchmark.default-data-format}") String defaultFormat
    ) {
        this.dataFormats = dataFormats;
        this.defaultFormat = defaultFormat;
    }

    private String makeResponse(String dataFormatName, DataFormatBenchmark.BenchResult result) {
        return String.format(
                "%s - %d - %dns - %dns\n",
                dataFormatName,
                result.binarySize(),
                result.averageSerializationDuration().toNanos(),
                result.averageDeserializationDuration().toNanos()
        );
    }

    public String handle(String request) {
        if (request.startsWith(GET_RESULT_BY_NAME_COMMAND_PREFIX)) {
            String dataFormatName = request.substring(GET_RESULT_BY_NAME_COMMAND_PREFIX.length()).trim();
            DataFormatBenchmark.BenchResult result = dataFormats.bench(dataFormatName);
            return makeResponse(dataFormatName, result);
        }
        if (request.trim().equals(GET_RESULT_COMMAND)) {
            DataFormatBenchmark.BenchResult result = dataFormats.bench(defaultFormat);
            return makeResponse(defaultFormat, result);
        }
        if (request.trim().equals(HELLO_COMMAND)) {
            return defaultFormat;
        }
        return "unknown command\n";
    }
}
