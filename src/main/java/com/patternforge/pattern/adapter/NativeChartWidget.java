package com.patternforge.pattern.adapter;

import com.patternforge.domain.RenderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NativeChartWidget implements ChartWidget {
    private final String id;
    private final String name;
    private List<Double> dataPoints = new ArrayList<>();

    public NativeChartWidget(String id, String name) {
        this.id = id;
        this.name = name;
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
    public String getType() {
        return "WIDGET";
    }

    @Override
    public String getPatternInfo() {
        return "Adapter Pattern - Native Target: Implements the modern ChartWidget interface directly without any translation.";
    }

    @Override
    public void setDataPoints(List<Double> data) {
        this.dataPoints = data != null ? data : List.of();
    }

    @Override
    public String getSource() {
        return "new";
    }

    @Override
    public String getAdapterTrace() {
        return "Native execution, no translation layer required.";
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("NativeChartWidget", "render", id);
        StringBuilder bars = new StringBuilder();
        double max = dataPoints.isEmpty() ? 1.0 : dataPoints.stream().max(Double::compareTo).orElse(1.0);
        if (max <= 0) max = 1.0;

        for (Double val : dataPoints) {
            double percent = (val / max) * 100;
            bars.append(String.format(
                "<div style='height: %.1f%%' class='w-full bg-emerald-500 rounded-t shadow-[0_0_10px_rgba(16,185,129,0.5)]' title='%.2f'></div>",
                percent, val
            ));
        }

        String html = String.format(
            "<div class='native-chart-container border-2 border-emerald-500/40 p-4 rounded-xl bg-slate-950/60 font-mono text-xs text-emerald-400 w-full'>" +
            "  <div class='flex justify-between border-b border-emerald-500/20 pb-1 mb-2'>" +
            "    <span class='font-bold'>NativeChartWidget (Target)</span>" +
            "    <span>Modern Engine</span>" +
            "  </div>" +
            "  <div class='text-sm font-semibold mb-1 text-slate-200'>Title: %s</div>" +
            "  <div>Raw List&lt;Double&gt;: <span class='text-white'>%s</span></div>" +
            "  <div class='mt-2 h-16 flex items-end gap-1 bg-emerald-950/20 p-2 rounded border border-emerald-500/20'>" +
            "    %s" +
            "  </div>" +
            "</div>",
            name, dataPoints.toString(), bars.length() > 0 ? bars.toString() : "No data"
        );

        RenderResult result = new RenderResult(
            id,
            html,
            List.of(),
            Map.of("source", "new"),
            List.of("Rendered native chart widget: " + name)
        );
        com.patternforge.inspector.ManualTracer.exit("NativeChartWidget", "render", id);
        return result;
    }
}
