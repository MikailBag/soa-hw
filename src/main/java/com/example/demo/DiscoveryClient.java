package com.example.demo;

import com.example.demo.udp.UdpServer;
import jdk.incubator.concurrent.StructuredTaskScope;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty("proxy-server.enabled")
public class DiscoveryClient implements AutoCloseable {


    private record Upstream(InetAddress address, String format) {
    }

    private final int timeoutMs;
    private final int upstreamCount;
    private final InetAddress multicast;
    private final int upstreamPort;
    private final Duration retryPeriod;
    private Map<String, InetAddress> upstreams;
    private final BlockingQueue<DatagramSocket> sockets;

    @Autowired
    DiscoveryClient(

            @Value("${proxy-server.client-port-begin}") int clientPortBegin,
            @Value("${proxy-server.client-port-count}") int clientPortCount,
            @Value("${proxy-server.upstream-port}") int upstreamPort,
            @Value("${proxy-server.discovery.upstream-count}") int upstreamCount,
            @Value("${proxy-server.discovery.retry-period-ms}") int retryPeriodMs,
            @Value("${proxy-server.discovery.timeout-ms}") int timeoutMs,
            @Value("${proxy-server.discovery.multicast-address}") String multicastAddress
    ) throws IOException, InterruptedException {
        this.timeoutMs = timeoutMs;
        this.upstreamCount = upstreamCount;
        this.multicast = InetAddress.getByName(multicastAddress);
        this.upstreamPort = upstreamPort;
        this.retryPeriod = Duration.ofMillis(retryPeriodMs);
        this.sockets = new LinkedBlockingQueue<>();
        for (int port = clientPortBegin; port < clientPortBegin + clientPortCount; ++port) {
            sockets.add(new DatagramSocket(port));
        }
        setup();
    }

    private static final Logger log = LoggerFactory.getLogger(DiscoveryClient.class);

    private void attemptDiscovery() throws IOException, InterruptedException, TimeoutException, ExecutionException {
        log.info("Starting upstream discovery attempt");
        DatagramSocket socket = sockets.take();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            byte[] reqData = "hello".getBytes(StandardCharsets.UTF_8);
            DatagramPacket req = new DatagramPacket(reqData, reqData.length, multicast, upstreamPort);
            log.info("Sending request");
            socket.send(req);
            var responses = new ConcurrentLinkedQueue<Upstream>();
            log.info("Waiting for {} responses", upstreamCount);
            for (int i = 0; i < upstreamCount; ++i) {
                scope.fork(() -> {
                    DatagramPacket packet = UdpServer.preparePacketForReceive();
                    socket.receive(packet);
                    String format = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    log.info("Received hello response for format {}", format);
                    responses.add(new Upstream(packet.getAddress(), format));
                    return null;
                });
            }
            Instant deadline = Instant.now().plusMillis(timeoutMs);
            scope.joinUntil(deadline);
            scope.throwIfFailed();
            this.upstreams = responses.stream().collect(Collectors.toMap(
                    Upstream::format,
                    Upstream::address
            ));
        } finally {
            sockets.add(socket);
        }
    }

    void setup() throws InterruptedException {
        while (true) {
            try {
                attemptDiscovery();
                if (this.upstreams != null) {
                    return;
                }
            } catch (Exception ex) {
                log.warn("discovery attempt failed", ex);
                Thread.sleep(retryPeriod);
            }
        }
    }

    public String unicast(String upstream, String request) throws IOException, InterruptedException {
        InetAddress address = upstreams.get(upstream);
        if (address == null) {
            throw new IOException("unknown upstream " + upstream);
        }
        byte[] body = request.getBytes(StandardCharsets.UTF_8);
        DatagramPacket reqPacket = new DatagramPacket(body, body.length, address, upstreamPort);
        DatagramSocket socket = sockets.take();
        try {
            socket.send(reqPacket);
            DatagramPacket respPacket = UdpServer.preparePacketForReceive();
            socket.receive(respPacket);
            return new String(respPacket.getData(), 0, respPacket.getLength());
        } finally {
            sockets.add(socket);
        }
    }

    public List<String> multicast(String request) throws IOException, InterruptedException {
        byte[] body = request.getBytes(StandardCharsets.UTF_8);
        DatagramPacket reqPacket = new DatagramPacket(body, body.length, multicast, upstreamPort);
        DatagramSocket socket = sockets.take();
        try {
            socket.send(reqPacket);
            List<String> result = new ArrayList<>();
            for (int i = 0; i < upstreams.size(); ++i) {
                DatagramPacket respPacket = UdpServer.preparePacketForReceive();
                socket.receive(respPacket);
                result.add(new String(respPacket.getData(), 0, respPacket.getLength()));
            }
            return result;
        } finally {
            sockets.add(socket);
        }
    }

    @Override
    public void close() {
        for (DatagramSocket socket : sockets) {
            socket.close();
        }
    }
}
