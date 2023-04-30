package com.example.demo.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public interface UdpHandler {
    void process(DatagramPacket packet, DatagramSocket socket) throws IOException;
}
