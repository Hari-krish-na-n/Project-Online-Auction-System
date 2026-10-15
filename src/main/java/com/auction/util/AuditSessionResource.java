package com.auction.util;

import java.util.List;

/**
 * Resource class demonstrating explicit AutoCloseable lifecycle and close order.
 *
 * Demonstrates:
 * - Implementing AutoCloseable.
 * - Resource cleanup when used within try-with-resources.
 * - Tracking close events for deterministic testing and training inspection.
 */
public class AuditSessionResource implements AutoCloseable {

    private final String sessionName;
    private final List<String> eventLog;
    private boolean closed = false;

    public AuditSessionResource(String sessionName, List<String> eventLog) {
        this.sessionName = sessionName;
        this.eventLog = eventLog;
        if (eventLog != null) {
            eventLog.add("OPEN:" + sessionName);
        }
    }

    public void logAction(String action) {
        if (closed) {
            throw new IllegalStateException("Audit session '" + sessionName + "' is already closed.");
        }
        if (eventLog != null) {
            eventLog.add("ACTION:" + sessionName + ":" + action);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (eventLog != null) {
                eventLog.add("CLOSE:" + sessionName);
            }
        }
    }
}
