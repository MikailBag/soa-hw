package com.example.demo.rpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

@Component
class ApiServer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ApiServer.class);
    private final Server server;

    @Autowired
    ApiServer(
            @Value("${rpc.port}") int port,
            List<BindableService> services,
            AuthnInterceptor interceptor
    ) throws IOException {
        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        services.forEach(builder::addService);
        builder.intercept(interceptor);
        builder.executor(Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("grpc-handler-", 0)
                        .factory()
        ));
        this.server = builder.build();
        log.info("Starting server on 0.0.0.0:{}", port);
        server.start();
    }

    @Override
    public void close() {
        log.info("Shutting down server");
        server.shutdown();
        log.info("Shutdown finished");
    }
}
