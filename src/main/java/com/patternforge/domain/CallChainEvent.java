package com.patternforge.domain;

import java.util.Map;

public record CallChainEvent(
    String className,
    String methodName,
    String widgetId,
    String eventType,   // "ENTER" or "EXIT" or "ERROR"
    long timestamp,
    Map<String, Object> metadata
) {
    public CallChainEvent {
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
}
