package it.uninsubria.laboratoriob.client.utils;

import it.uninsubria.laboratoriob.api.utils.HeartbeatChannel;
import it.uninsubria.laboratoriob.client.cli.IO;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Client-side setup for the shared HeartbeatChannel: connects out on a
 * background thread so a failure never blocks application startup, and
 * keeps retrying the connection if the server drops it instead of giving
 * up after the first disconnect.
 */
public class HeartbeatClient {

    private static final long RECONNECT_DELAY_SECONDS = 10;

    private final String host;
    private final int port;
    private final long intervalMinutes;

    private volatile HeartbeatChannel channel;
    private volatile boolean shuttingDown = false;
    private volatile boolean shownErrorMesage = false;

    /**
     * Costruisce il client di heartbeat con i parametri di connessione specificati.
     *
     * @param host            hostname del server heartbeat
     * @param port            porta TCP del server heartbeat
     * @param intervalMinutes intervallo in minuti tra un heartbeat e l'altro
     */
    public HeartbeatClient(String host, int port, long intervalMinutes) {
        this.host = host;
        this.port = port;
        this.intervalMinutes = intervalMinutes;
    }

    /**
     * Avvia la connessione di heartbeat in background. Se la connessione fallisce,
     * il client riprova automaticamente ogni 10 secondi.
     */
    public void start() {
        connect();
    }

    private void connect() {
        if (shuttingDown) return;
        try {
            Socket socket = new Socket(host, port);
            HeartbeatChannel newChannel = new HeartbeatChannel(socket, intervalMinutes);
            newChannel.setOnDisconnect(this::handleDisconnect);
            channel = newChannel;
            newChannel.start();

            shownErrorMesage = false;
        } catch (IOException e) {
            if (!shownErrorMesage) {
                IO.printErrorMessage("Heartbeat connection setup failed, retrying: " + e.getMessage());
                shownErrorMesage = true;
            }
            scheduleReconnect();
        }
    }

    private synchronized void handleDisconnect() {
        if (shuttingDown) return;
        HeartbeatChannel dead = channel;
        channel = null;
        if (dead != null) dead.shutdown();
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (shuttingDown) return;
        Thread retryThread = new Thread(() -> {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(RECONNECT_DELAY_SECONDS));
            } catch (InterruptedException ignored) {
            }
            connect();
        }, "heartbeat-reconnect");
        retryThread.setDaemon(true);
        retryThread.start();
    }

    public void wakeUp() {
        HeartbeatChannel c = channel;
        if (c != null)
            c.wakeUp();
    }

    /**
     * Interrompe la connessione di heartbeat e impedisce ulteriori tentativi di riconnessione.
     */
    public void shutdown() {
        shuttingDown = true;
        HeartbeatChannel c = channel;
        channel = null;
        if (c != null)
            c.shutdown();
    }
}
