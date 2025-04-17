package edu.upenn.cit594.logging;

import java.io.*;

/**
 * singleton logger for recording application events and messages.
 * The logger can write to either standard error or a specified log file,
 * and automatically includes timestamps with each log message.
 */
public class Logger {
    // Singleton instance, initialized eagerly
    private static final Logger instance = new Logger();
    private PrintWriter writer;

    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the logger to write to System.err by default.
     */
    private Logger() {
        writer = new PrintWriter(System.err, true);
    }

    /**
     * Returns the singleton instance of the Logger.
     *
     * @return the singleton Logger instance
     */
    public static Logger getInstance() {
        return instance;
    }

    /**
     * Sets the output destination for the logger.
     * If the filename is null or empty, logs will be written to System.err.
     * If the file cannot be opened, logs will continue to be written to the previous destination.
     *
     * @param filename the path to the log file, or null/empty to use System.err
     * @throws SecurityException if a security manager exists and denies file operations
     */
    public synchronized void setDestination(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty()) {
                writer = new PrintWriter(System.err, true);
            } else {
                writer = new PrintWriter(new FileWriter(filename, true), true);
            }
        } catch (IOException e) {
            System.err.println("Failed to set log destination: " + e.getMessage());
        }
    }

    /**
     * Logs a message with a timestamp prefix.
     * The message will be immediately flushed to the output destination.
     *
     * @param message the message to be logged
     * @throws NullPointerException if the message parameter is null
     */
    public synchronized void log(String message) {
        if (message == null) {
            throw new NullPointerException("Message cannot be null");
        }
        writer.printf("%d %s%n", System.currentTimeMillis(), message);
        writer.flush();
    }
}