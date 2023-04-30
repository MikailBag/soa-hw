package com.example.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@ConditionalOnProperty("proxy-server.enabled")
public class ProxyRequestHandler {
    private static final String GET_RESULT_COMMAND_PREFIX = "get_result ";
    private static final String TARGET_BROADCAST = "all";

    private final DiscoveryClient client;

    ProxyRequestHandler(
            DiscoveryClient client
    ) {
        this.client = client;
    }

    public String handle(String request) throws IOException, InterruptedException {
        if (!request.startsWith(GET_RESULT_COMMAND_PREFIX)) {
            return "unknown command\n";
        }
        String target = request.substring(GET_RESULT_COMMAND_PREFIX.length()).trim();
        if (target.equals(TARGET_BROADCAST)) {
            List<String> result = client.multicast("get_result");
            return String.join("", result);
        }
        return client.unicast(target, "get_result");
    }
}
