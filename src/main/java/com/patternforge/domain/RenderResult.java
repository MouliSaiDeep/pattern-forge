package com.patternforge.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record RenderResult(
    String widgetId,
    String htmlContent,
    List<String> cssStyles,
    Map<String, String> metadata,
    List<String> renderTrace
) {
    public RenderResult {
        // Ensure lists and maps are immutable and non-null
        cssStyles = cssStyles != null ? List.copyOf(cssStyles) : List.of();
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        renderTrace = renderTrace != null ? List.copyOf(renderTrace) : List.of();
    }

    // Returns a NEW RenderResult with the style appended — never mutates
    public RenderResult withCssStyle(String style) {
        var newStyles = new ArrayList<>(this.cssStyles);
        newStyles.add(style);
        return new RenderResult(widgetId, htmlContent, List.copyOf(newStyles), metadata, renderTrace);
    }

    public RenderResult withHtmlContent(String html) {
        return new RenderResult(widgetId, html, cssStyles, metadata, renderTrace);
    }

    public RenderResult withTraceEntry(String entry) {
        var newTrace = new ArrayList<>(this.renderTrace);
        newTrace.add(entry);
        return new RenderResult(widgetId, htmlContent, cssStyles, metadata, List.copyOf(newTrace));
    }
}
