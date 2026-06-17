package com.patternforge.pattern.bridge;

import com.patternforge.domain.RenderResult;
import java.util.List;
import java.util.Map;

public class ChartBridgeWidget extends BridgeWidget {
    private final String id;
    private final String name;
    private final List<Double> data;

    public ChartBridgeWidget(String id, String name, List<Double> data, WidgetRenderer renderer) {
        this.id = id;
        this.name = name;
        this.data = data != null ? List.copyOf(data) : List.of();
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
        com.patternforge.inspector.ManualTracer.enter(rendererName, "renderChart", id);
        String innerHtml = renderer.renderChart(name, data);
        String html = String.format("<div id='%s'>%s</div>", id, innerHtml);
        com.patternforge.inspector.ManualTracer.exit(rendererName, "renderChart", id);
        return new RenderResult(
            id,
            html,
            List.of(),
            Map.of("renderer", renderer.getRendererName(), "type", "chart"),
            List.of("ChartBridgeWidget displayed using " + renderer.getRendererName())
        );
    }
}
