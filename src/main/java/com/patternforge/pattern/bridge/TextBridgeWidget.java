package com.patternforge.pattern.bridge;

import com.patternforge.domain.RenderResult;
import java.util.List;
import java.util.Map;

public class TextBridgeWidget extends BridgeWidget {
    private final String id;
    private final String name;
    private final String content;

    public TextBridgeWidget(String id, String name, String content, WidgetRenderer renderer) {
        this.id = id;
        this.name = name;
        this.content = content;
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
        com.patternforge.inspector.ManualTracer.enter(rendererName, "renderText", id);
        String innerHtml = renderer.renderText(name, content);
        String html = String.format("<div id='%s'>%s</div>", id, innerHtml);
        com.patternforge.inspector.ManualTracer.exit(rendererName, "renderText", id);
        return new RenderResult(
            id,
            html,
            List.of(),
            Map.of("renderer", renderer.getRendererName(), "type", "text"),
            List.of("TextBridgeWidget displayed using " + renderer.getRendererName())
        );
    }
}
