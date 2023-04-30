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
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@ConditionalOnProperty("proxy-server.enabled")
public class ProxyServer implements UdpHandler, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ProxyServer.class);
    private final UdpServer server;
    private final ProxyRequestHandler handler;

    @Autowired
    ProxyServer(
            DiscoveryClient client,
            ProxyRequestHandler handler,
            @Value("${proxy-server.listen-port}") int port
    ) throws IOException {
        UdpServer.Options options = new UdpServer.Options(port, Optional.empty());
        this.server = new UdpServer(this, options);
        this.handler = handler;
    }

    @Override
    public void process(DatagramPacket packet, DatagramSocket socket) throws IOException {
        try {
            String response = handler.handle(new String(packet.getData(), 0, packet.getLength()));
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

            socket.send(
                    new DatagramPacket(
                            responseBytes, 0, responseBytes.length,
                            packet.getAddress(),
                            packet.getPort()
                    )
            );
        } catch (InterruptedException ex) {
            throw new IOException("interrupted while communicating with upstreams", ex);
        }

    }

    @Override
    public void close() {
        this.server.close();
    }
}
