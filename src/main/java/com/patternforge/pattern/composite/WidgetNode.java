package com.patternforge.pattern.composite;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

import java.util.List;
import java.util.Map;

public class WidgetNode implements DashboardComponent {
    private final String id;
    private final String name;
    private final String widgetType;
    private final Map<String, Object> config;

    public WidgetNode(String id, String name, String widgetType, Map<String, Object> config) {
        this.id = id;
        this.name = name;
        this.widgetType = widgetType;
        this.config = config != null ? Map.copyOf(config) : Map.of();
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
    public String getPatternInfo() {
        return "Composite Pattern - Leaf: Represents individual elements in the hierarchy that perform the actual work.";
    }

    @Override
    public String getType() {
        return "WIDGET";
    }

    public String getWidgetType() {
        return widgetType;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("WidgetNode", "render", id);
        try {
            String content = (String) config.getOrDefault("content", "Default Widget Content");
            String bgColor = (String) config.getOrDefault("bgColor", "from-slate-900 to-slate-950");
            String textColor = (String) config.getOrDefault("textColor", "text-white");

            String html = String.format(
                "<div class='widget-box p-6 rounded-2xl bg-gradient-to-br %s %s shadow-lg border border-slate-700/50 backdrop-blur-md transition-all duration-300 hover:shadow-xl hover:-translate-y-1' id='%s' data-widget-type='%s'>" +
                "  <div class='flex justify-between items-center mb-3'>" +
                "    <span class='text-xs font-semibold tracking-wider text-indigo-400 uppercase'>%s</span>" +
                "    <span class='px-2 py-0.5 text-[10px] font-medium rounded-full bg-slate-700/60 text-slate-300'>%s</span>" +
                "  </div>" +
                "  <h4 class='text-lg font-bold mb-2'>%s</h4>" +
                "  <p class='text-sm opacity-80'>%s</p>" +
                "</div>",
                bgColor, textColor, id, widgetType, widgetType, id.substring(0, Math.min(id.length(), 8)), name, content
            );

            return new RenderResult(
                id,
                html,
                List.of(".widget-box { min-height: 120px; }"),
                Map.of("widgetType", widgetType, "name", name),
                List.of("Rendered Leaf Node: " + name + " (ID: " + id + ")")
            );
        } finally {
            com.patternforge.inspector.ManualTracer.exit("WidgetNode", "render", id);
        }
    }
}
