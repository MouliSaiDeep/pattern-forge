package com.patternforge.pattern.bridge;

import com.patternforge.domain.RenderResult;
import java.util.List;
import java.util.Map;

public class TableBridgeWidget extends BridgeWidget {
    private final String id;
    private final String name;
    private final List<Map<String, Object>> rows;

    public TableBridgeWidget(String id, String name, List<Map<String, Object>> rows, WidgetRenderer renderer) {
        this.id = id;
        this.name = name;
        this.rows = rows != null ? List.copyOf(rows) : List.of();
        this.renderer = renderer;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public RenderResult display() {
        String rendererName = renderer.getClass().getSimpleName();
        com.patternforge.inspector.ManualTracer.enter(rendererName, "renderTable", id);
        String innerHtml = renderer.renderTable(name, rows);
        String html = String.format("<div id='%s'>%s</div>", id, innerHtml);
        com.patternforge.inspector.ManualTracer.exit(rendererName, "renderTable", id);
        return new RenderResult(
            id,
            html,
            List.of(),
            Map.of("renderer", renderer.getRendererName(), "type", "table"),
            List.of("TableBridgeWidget displayed using " + renderer.getRendererName())
        );
    }
}
