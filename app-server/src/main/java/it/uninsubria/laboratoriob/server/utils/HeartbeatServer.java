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
    private volatile boolean shuttingDown = false;

    public HeartbeatServer(int port, long intervalMinutes) {
        this.port = port;
        this.intervalMinutes = intervalMinutes;
    }

    public void start() {
        Thread setupThread = new Thread(this::acceptLoop, "heartbeat-setup");
        setupThread.setDaemon(true);
        setupThread.start();
    }

    /**
     * Keeps accepting new heartbeat connections for the life of the server,
     * so a client that reconnects after a drop is picked back up instead of
     * the server having stopped listening after the first connection.
     */
    private void acceptLoop() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Heartbeat server bind failed, continuing without it: " + e.getMessage());
            return;
        }

        while (!shuttingDown) {
            try {
                Socket socket = serverSocket.accept();
                HeartbeatChannel newChannel = new HeartbeatChannel(socket, intervalMinutes);
                newChannel.setOnDisconnect(newChannel::shutdown);
                channel = newChannel;
                newChannel.start();
            } catch (IOException e) {
                if (!shuttingDown) {
                    System.err.println("Heartbeat accept failed, retrying: " + e.getMessage());
                }
            }
        }
    }

    public void wakeUp() {
        HeartbeatChannel c = channel;
        if (c != null) {
            c.wakeUp();
        }
    }

    public void shutdown() {
        shuttingDown = true;
        HeartbeatChannel c = channel;
        if (c != null) {
            c.shutdown();
        }
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
    }
}
