package com.patternforge.domain;

public record AuditEvent(
    String userId,
    String widgetId,
    long timestamp,
    String method
) {}
