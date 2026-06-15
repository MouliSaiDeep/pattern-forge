package com.patternforge.domain;

import java.util.Map;

public record WidgetFlyweight(
    String widgetType,        // e.g., "STOCK_TICKER"
    String templateHtml,      // shared HTML template
    String iconSet,           // shared SVG icons
    Map<String, String> defaultStyles,  // shared CSS
    long estimatedSizeBytes   // for memory estimation
) {
    public WidgetFlyweight {
        defaultStyles = defaultStyles != null ? Map.copyOf(defaultStyles) : Map.of();
    }
}
