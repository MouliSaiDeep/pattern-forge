package com.patternforge.pattern.flyweight;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;
import com.patternforge.domain.WidgetContext;
import com.patternforge.domain.WidgetFlyweight;

import java.util.List;
import java.util.Map;

public class StockTickerWidget implements DashboardComponent {
    private final WidgetFlyweight flyweight;
    private final WidgetContext context;

    public StockTickerWidget(WidgetFlyweight flyweight, WidgetContext context) {
        this.flyweight = flyweight;
        this.context = context;
    }

    @Override
    public String getId() {
        return context.instanceId();
    }

    @Override
    public String getName() {
        return context.label();
    }

    @Override
    public String getType() {
        return "WIDGET";
    }

    @Override
    public String getPatternInfo() {
        return "Flyweight Pattern - Client/Context: Combines shared intrinsic state with unique extrinsic context.";
    }

    public WidgetFlyweight getFlyweight() {
        return flyweight;
    }

    public WidgetContext getContext() {
        return context;
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("StockTickerWidget", "render", context.instanceId());
        String template = flyweight.templateHtml();
        String html = template
            .replace("{{id}}", context.instanceId())
            .replace("{{label}}", context.label())
            .replace("{{position}}", context.position())
            .replace("{{value}}", String.valueOf(context.currentValue()))
            .replace("{{icon}}", flyweight.iconSet());

        RenderResult result = new RenderResult(
            context.instanceId(),
            html,
            List.of(".stock-ticker-flyweight { font-weight: bold; }"),
            Map.of("widgetType", flyweight.widgetType(), "instanceId", context.instanceId()),
            List.of("Flyweight Rendered widget ID: " + context.instanceId() + " (Type: " + flyweight.widgetType() + ")")
        );
        com.patternforge.inspector.ManualTracer.exit("StockTickerWidget", "render[flyweight=" + flyweight.widgetType() + "]", context.instanceId());
        return result;
    }
}
