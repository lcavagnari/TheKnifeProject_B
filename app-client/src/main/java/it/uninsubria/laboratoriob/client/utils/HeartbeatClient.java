package it.uninsubria.laboratoriob.client.utils;

import it.uninsubria.laboratoriob.api.utils.HeartbeatChannel;
import it.uninsubria.laboratoriob.client.ui.IO;

import java.io.IOException;
import java.net.Socket;

/**
 * Client-side setup for the shared HeartbeatChannel: connects out on a
 * background thread so a failure never blocks application startup.
 */
public class HeartbeatClient {

    private final String host;
    private final int port;
    private final long intervalMinutes;

    private volatile HeartbeatChannel channel;

    public HeartbeatClient(String host, int port, long intervalMinutes) {
        this.host = host;
        this.port = port;
        this.intervalMinutes = intervalMinutes;
    }

    public void start() {
        try {
            Socket socket = new Socket(host, port);
            System.out.println("[Heartbeat] Client connected to " + host + ":" + port);
            channel = new HeartbeatChannel(socket, intervalMinutes);
            channel.start();
        } catch (IOException e) {
            IO.printErrorMessage("Heartbeat connection setup failed, continuing without it: " + e.getMessage());
        }
    }

    public void wakeUp() {
        if (channel != null) {
            channel.wakeUp();
        }
    }

    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}
