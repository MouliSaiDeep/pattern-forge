package com.patternforge.service;

import com.patternforge.domain.AuditEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AuditLogService {
    private final List<AuditEvent> logs = new CopyOnWriteArrayList<>();

    public void logEvent(String userId, String widgetId, long timestamp, String method) {
        logs.add(new AuditEvent(userId, widgetId, timestamp, method));
    }

    public List<AuditEvent> getLog() {
        return List.copyOf(logs);
    }
}
