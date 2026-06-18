package com.patternforge.service.facade;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WidgetRegistry {
    private final FacadeCallLogger callLogger;
    private final List<String> registeredWidgetIds = new CopyOnWriteArrayList<>();

    public WidgetRegistry(FacadeCallLogger callLogger) {
        this.callLogger = callLogger;
    }

    public void register(String widgetId, String type) {
        callLogger.logCall("WidgetRegistry: Registering widget '" + widgetId + "' (Type: " + type + ")");
        if (!registeredWidgetIds.contains(widgetId)) {
            registeredWidgetIds.add(widgetId);
        }
    }

    public void deregister(String widgetId) {
        callLogger.logCall("WidgetRegistry: Deregistering widget '" + widgetId + "'");
        registeredWidgetIds.remove(widgetId);
    }

    public List<String> getRegistered() {
        callLogger.logCall("WidgetRegistry: Fetching list of registered widget IDs");
        return List.copyOf(registeredWidgetIds);
    }
}
