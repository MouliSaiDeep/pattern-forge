package com.patternforge.domain;

import java.util.Map;

public record WidgetContext(
    String instanceId,
    String position,          // e.g., "row:2,col:3"
    Object currentValue,      // unique live data
    String label,
    Map<String, Object> uniqueData
) {
    public WidgetContext {
        uniqueData = uniqueData != null ? Map.copyOf(uniqueData) : Map.of();
    }
}
