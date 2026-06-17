package com.patternforge.pattern.bridge;

import java.util.List;
import java.util.Map;

public interface WidgetRenderer {
    String renderChart(String title, List<Double> data);
    String renderTable(String title, List<Map<String, Object>> rows);
    String renderText(String title, String content);
    String getRendererName();
}
