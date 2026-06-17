package com.patternforge.pattern.bridge;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonRenderer implements WidgetRenderer {
    @Override
    public String getRendererName() {
        return "JSON";
    }

    @Override
    public String renderChart(String title, List<Double> data) {
        String dataStr = data.stream().map(Object::toString).collect(Collectors.joining(", "));
        String json = String.format("{\n  \"type\": \"chart\",\n  \"title\": \"%s\",\n  \"data\": [%s]\n}", title, dataStr);
        return wrapInCodeBlock(json);
    }

    @Override
    public String renderTable(String title, List<Map<String, Object>> rows) {
        String rowsJson = rows.stream()
            .map(row -> "    " + row.toString())
            .collect(Collectors.joining(",\n"));
        String json = String.format("{\n  \"type\": \"table\",\n  \"title\": \"%s\",\n  \"rows\": [\n%s\n  ]\n}", title, rowsJson);
        return wrapInCodeBlock(json);
    }

    @Override
    public String renderText(String title, String content) {
        String json = String.format("{\n  \"type\": \"text\",\n  \"title\": \"%s\",\n  \"content\": \"%s\"\n}", title, content.replace("\"", "\\\""));
        return wrapInCodeBlock(json);
    }

    private String wrapInCodeBlock(String json) {
        return String.format(
            "<div class='bridge-json-card p-4 rounded-xl bg-slate-950 border border-slate-800 text-emerald-400 font-mono text-xs w-full'>" +
            "  <div class='text-[10px] text-slate-500 mb-1'>JSON RENDERER OUTPUT</div>" +
            "  <pre class='overflow-x-auto whitespace-pre-wrap'>%s</pre>" +
            "</div>",
            json
        );
    }
}
