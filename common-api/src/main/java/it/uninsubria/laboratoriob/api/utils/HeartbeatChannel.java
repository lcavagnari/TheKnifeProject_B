package it.uninsubria.laboratoriob.api.utils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Shared bidirectional TCP heartbeat over an already-connected socket.
 * Identical for server and client - only how the socket gets established
 * (accept vs. connect) differs between them, which lives outside this class.
 */
public class HeartbeatChannel {

    private static final byte PING = 0;
    private static final byte PONG = 1;

    private static volatile boolean serverReachable = false;

    public static boolean isServerReachable() {
        return serverReachable;
    }

    public synchronized static void setServerReachable(boolean value) {
        serverReachable = value;
    }

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final long intervalMinutes;
    private final Object outLock = new Object();

    private volatile boolean running = true;
    private volatile Thread readerThread;
    private volatile Thread pingerThread;
    private volatile Runnable onDisconnect;

    private final BlockingQueue<Integer> pongQueue = new ArrayBlockingQueue<>(1);

    /** socket must already be connected. */
    public HeartbeatChannel(Socket socket, long intervalMinutes) throws IOException {
        this.socket = socket;
        this.intervalMinutes = intervalMinutes;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /** Invoked once, on a dead-socket disconnect only (never on a graceful {@link #shutdown()}). */
    public void setOnDisconnect(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public void start() {
        serverReachable = true;

        readerThread = new Thread(this::readLoop, "heartbeat-reader");
        readerThread.setDaemon(true);
        readerThread.start();

        pingerThread = new Thread(this::pingLoop, "heartbeat-pinger");
        pingerThread.setDaemon(true);
        pingerThread.start();
    }

    private void readLoop() {
        try {
            while (running) {
                byte tag = in.readByte();
                int value = in.readInt();
                if (tag == PING) {
                    synchronized (outLock) {
                        out.writeByte(PONG);
                        out.writeInt(value);
                        out.flush();
                    }
                } else if (tag == PONG) {
                    pongQueue.offer(value);
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Heartbeat reader stopped: " + e.getMessage());
                serverReachable = false;
                running = false;
                if (pingerThread != null) pingerThread.interrupt();
                Runnable callback = onDisconnect;
                if (callback != null) callback.run();
            }
        }
    }

    private void pingLoop() {
        int counter = 0;
        while (running) {
            try {
                synchronized (outLock) {
                    out.writeByte(PING);
                    out.writeInt(counter);
                    out.flush();
                }
            } catch (IOException e) {
                System.err.println("Heartbeat ping failed: " + e.getMessage());
                serverReachable = false;
                running = false;
                if (readerThread != null) readerThread.interrupt();
                Runnable callback = onDisconnect;
                if (callback != null) callback.run();
                return;
            }

            Integer reply = null;
            try {
                reply = pongQueue.poll(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // interrupted while waiting for a reply - treat as no reply, keep going
            }
            if (reply == null) {
                System.err.println("Heartbeat: no pong received in time");
            }
            counter++;

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(intervalMinutes));
            } catch (InterruptedException e) {
                // woken early - loop back and ping again immediately
            }
        }
    }

    /** Call when an operation elsewhere in the app fails, to trigger an immediate check. */
    public void wakeUp() {
        if (pingerThread != null) {
            pingerThread.interrupt();
        }
    }

    public void shutdown() {
        serverReachable = false;
        running = false;
        if (pingerThread != null) pingerThread.interrupt();
        if (readerThread != null) readerThread.interrupt();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
