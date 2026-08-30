package it.uninsubria.laboratoriob.server.utils;

import it.uninsubria.laboratoriob.api.utils.HeartbeatChannel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-side setup for the shared HeartbeatChannel: binds and accepts
 * on a background thread so a failure never blocks application startup.
 */
public class HeartbeatServer {

    private final int port;
    private final long intervalMinutes;

    private volatile ServerSocket serverSocket;
    private final CopyOnWriteArrayList<HeartbeatChannel> channels = new CopyOnWriteArrayList<>();
    private volatile boolean shuttingDown = false;

    /**
     * Crea un nuovo server di heartbeat.
     *
     * @param port            porta TCP su cui ascoltare
     * @param intervalMinutes intervallo di heartbeat in minuti
     */
    public HeartbeatServer(int port, long intervalMinutes) {
        this.port = port;
        this.intervalMinutes = intervalMinutes;
    }

    /**
     * Avvia il server di heartbeat su un thread daemon.
     */
    public void start() {
        Thread setupThread = new Thread(this::acceptLoop, "heartbeat-setup");
        setupThread.setDaemon(true);
        setupThread.start();
    }

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
                newChannel.setOnDisconnect(() -> {
                    channels.remove(newChannel);
                    newChannel.shutdown();
                });
                channels.add(newChannel);
                newChannel.start();
            } catch (IOException e) {
                if (!shuttingDown) {
                    System.err.println("Heartbeat accept failed, retrying: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Invia un segnale di risveglio a tutti i canali di heartbeat attivi.
     */
    public void wakeUp() {
        for (HeartbeatChannel c : channels) {
            c.wakeUp();
        }
    }

    /**
     * Chiude tutti i canali di heartbeat e il server socket.
     */
    public void shutdown() {
        shuttingDown = true;
        for (HeartbeatChannel c : channels) {
            c.shutdown();
        }
        channels.clear();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
    }
}
