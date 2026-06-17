package com.patternforge.pattern.bridge;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

public abstract class BridgeWidget implements DashboardComponent {
    protected WidgetRenderer renderer;

    public void setRenderer(WidgetRenderer renderer) {
        this.renderer = renderer;
    }

    public WidgetRenderer getRenderer() {
        return renderer;
    }

    @Override
    public RenderResult render() {
        String widgetId = getId();
        String className = getClass().getSimpleName();
        com.patternforge.inspector.ManualTracer.enter(className, "render", widgetId);
        try {
            RenderResult result = display();
            com.patternforge.inspector.ManualTracer.exit(className, "render", widgetId);
            return result;
        } catch (Exception e) {
            com.patternforge.inspector.ManualTracer.exit(className, "render[ERROR]", widgetId);
            throw e;
        }
    }

    @Override
    public String getType() {
        return "WIDGET";
    }

    @Override
    public String getPatternInfo() {
        return "Bridge Pattern - Abstraction: Separates the widget logical abstractions from their visual rendering implementations.";
    }

    public abstract RenderResult display();
}
