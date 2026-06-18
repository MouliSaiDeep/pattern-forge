package com.patternforge.pattern.flyweight;

import com.patternforge.domain.WidgetFlyweight;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WidgetFlyweightFactory {
    private final Map<String, WidgetFlyweight> pool = new ConcurrentHashMap<>();
    private final Map<String, Integer> refCounts = new ConcurrentHashMap<>();

    public synchronized WidgetFlyweight getFlyweight(String widgetType) {
        String cleanType = widgetType.toUpperCase();
        boolean isNew = !pool.containsKey(cleanType);
        if (isNew) {
            com.patternforge.inspector.ManualTracer.enter("WidgetFlyweightFactory", "createFlyweight[" + cleanType + "]", "factory");
        } else {
            com.patternforge.inspector.ManualTracer.enter("WidgetFlyweightFactory", "reuseFlyweight[" + cleanType + "]", "factory");
        }
        WidgetFlyweight result = pool.computeIfAbsent(cleanType, type -> {
            String template = getTemplateForType(type);
            String icons = getIconsForType(type);
            Map<String, String> styles = Map.of(
                "font-family", "monospace",
                "border-radius", "8px"
            );
            long bytes = template.length() + icons.length() + 500; // estimated intrinsic overhead
            return new WidgetFlyweight(type, template, icons, styles, bytes);
        });
        if (isNew) {
            com.patternforge.inspector.ManualTracer.exit("WidgetFlyweightFactory", "createFlyweight[" + cleanType + "]", "factory");
        } else {
            com.patternforge.inspector.ManualTracer.exit("WidgetFlyweightFactory", "reuseFlyweight[" + cleanType + "]", "factory");
        }
        return result;
    }

    public synchronized void registerUsage(String widgetType) {
        refCounts.merge(widgetType.toUpperCase(), 1, Integer::sum);
    }

    public synchronized void deregisterUsage(String widgetType) {
        refCounts.computeIfPresent(widgetType.toUpperCase(), (k, v) -> v > 1 ? v - 1 : 0);
    }

    public Map<String, WidgetFlyweight> getPool() {
        return Map.copyOf(pool);
    }

    public int getReferenceCount(String widgetType) {
        return refCounts.getOrDefault(widgetType.toUpperCase(), 0);
    }

    private String getTemplateForType(String type) {
        return "<div class='stock-ticker-flyweight p-3 bg-slate-950 border border-slate-805 rounded-lg flex items-center justify-between w-full'>" +
               "  <div class='flex items-center gap-2'>" +
               "    {{icon}}" +
               "    <div class='text-xs font-semibold text-slate-300'>{{label}}</div>" +
               "  </div>" +
               "  <div class='text-sm font-bold text-emerald-400 font-mono'>{{value}}</div>" +
               "  <div class='text-[9px] text-slate-500'>Pos: {{position}}</div>" +
               "</div>";
    }

    private String getIconsForType(String type) {
        return "<svg class='w-4 h-4 text-emerald-500' fill='none' stroke='currentColor' viewBox='0 0 24 24' style='width:16px;height:16px;'><path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M13 7h8m0 0v8m0-8l-8 8-4-4-6 6'/></svg>";
    }
}
