package com.app.authsystem.dto;

import java.time.LocalDateTime;

public class ApiUsageView {
    private final String identifier;
    private final String endpoint;
    private final LocalDateTime timestamp;

    public ApiUsageView(String identifier, String endpoint, LocalDateTime timestamp) {
        this.identifier = identifier;
        this.endpoint = endpoint;
        this.timestamp = timestamp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

