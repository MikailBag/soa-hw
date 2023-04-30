package com.example.demo;

import com.example.demo.udp.UdpHandler;
import com.example.demo.udp.UdpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@ConditionalOnProperty("bench-server.enabled")
public class BenchmarkUdpServer implements AutoCloseable, UdpHandler {
    private final Logger log = LoggerFactory.getLogger(BenchmarkUdpServer.class);
    private final BenchRequestHandler handler;
    private final UdpServer server;

    @Autowired
    BenchmarkUdpServer(
            BenchRequestHandler handler,
            @Value("${bench-server.port}") int port,
            @Value("${bench-server.multicast-address}") String multicast
    ) throws IOException {
        this.handler = handler;
        UdpServer.Options options = new UdpServer.Options(
                port,
                Optional.of(InetAddress.getByName(multicast))
        );
        this.server = new UdpServer(this, options);
    }

    @Override
    public void process(DatagramPacket packet, DatagramSocket socket) throws IOException {
        var request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
        String response;
        // basic error reporting
        try {
            response = handler.handle(request);
        } catch (Exception ex) {
            log.warn("handler failed", ex);
            response = ex.getMessage();
        }
        byte[] resBytes = response.getBytes(StandardCharsets.UTF_8);
        var responsePacket = new DatagramPacket(resBytes, resBytes.length, packet.getAddress(), packet.getPort());
        socket.send(responsePacket);
    }

    @Override
    public void close() {
        this.server.close();
    }
}
