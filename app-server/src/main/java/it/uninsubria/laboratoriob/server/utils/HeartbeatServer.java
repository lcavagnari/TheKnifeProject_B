package it.uninsubria.laboratoriob.server.utils;

import it.uninsubria.laboratoriob.api.utils.HeartbeatChannel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server-side setup for the shared HeartbeatChannel: binds and accepts
 * on a background thread so a failure never blocks application startup.
 */
public class HeartbeatServer {

    private final int port;
    private final long intervalMinutes;

    private volatile ServerSocket serverSocket;
    private volatile HeartbeatChannel channel;

    public HeartbeatServer(int port, long intervalMinutes) {
        this.port = port;
        this.intervalMinutes = intervalMinutes;
    }

    public void start() {
        Thread setupThread = new Thread(this::acceptAndRun, "heartbeat-setup");
        setupThread.setDaemon(true);
        setupThread.start();
    }

    private void acceptAndRun() {
        try {
            serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            channel = new HeartbeatChannel(socket, intervalMinutes);
            channel.start();
        } catch (IOException e) {
            System.err.println("Heartbeat connection setup failed, continuing without it: " + e.getMessage());
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
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
    }
}
