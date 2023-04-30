package com.example.demo.udp;

import jdk.incubator.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Optional;

public class UdpServer implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(UdpServer.class);
    private final DatagramSocket socket;
    private final Thread listener;
    private final UdpHandler handler;
    private volatile boolean stopping = false;

    public record Options(
            int port,
            Optional<InetAddress> multicast
    ) {
    }

    public UdpServer(UdpHandler handler, Options options) throws IOException {
        log.info("Starting server with options {}", options);
        if (options.multicast().isPresent()) {
            MulticastSocket sock = new MulticastSocket(options.port);
            NetworkInterface iface = NetworkInterface.getByInetAddress(InetAddress.getByName("0.0.0.0"));
            sock.joinGroup(new InetSocketAddress(options.multicast().get(), options.port), iface);
            this.socket = sock;
        } else {
            this.socket = new DatagramSocket(options.port);
        }
        this.handler = handler;
        this.listener = Thread.ofVirtual().start(this::serve);
    }

    public static DatagramPacket preparePacketForReceive() {
        // way more than usual MTUs, should work
        byte[] buffer = new byte[2048];
        return new DatagramPacket(buffer, buffer.length);
    }

    private void serve() {
        StructuredTaskScope<Void> scope = new StructuredTaskScope<>();
        try (scope) {
            while (!stopping) {
                try {
                    DatagramPacket p = preparePacketForReceive();
                    socket.receive(p);
                    scope.fork(() -> {
                        handler.process(p, socket);
                        return null;
                    });
                } catch (IOException ex) {
                    log.warn("failed to process packet", ex);
                }
            }
            log.info("shutting down");
            scope.shutdown();
            log.info("waiting for requests to stop");
        }
        log.info("server stopped");
    }

    @Override
    public void close() {
        stopping = true;
        listener.interrupt();
        boolean interrupted = false;
        while (true) {
            try {
                listener.join();
                break;
            } catch (InterruptedException ignored) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
