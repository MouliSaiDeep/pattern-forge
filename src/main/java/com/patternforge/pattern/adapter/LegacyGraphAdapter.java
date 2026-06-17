package com.patternforge.pattern.adapter;

import com.patternforge.domain.RenderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LegacyGraphAdapter implements ChartWidget {
    private final String id;
    private final String name;
    private final LegacyGraphLib adaptee;
    private List<Double> dataPoints = new ArrayList<>();
    private String trace = "Initialized adapter";

    public LegacyGraphAdapter(String id, String name, LegacyGraphLib adaptee) {
        this.id = id;
        this.name = name;
        this.adaptee = adaptee;
        this.adaptee.setResolution(400, 300);
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
        return "Adapter Pattern - Adapter: Converts the interface of LegacyGraphLib into the ChartWidget interface.";
    }

    @Override
    public void setDataPoints(List<Double> data) {
        this.dataPoints = data != null ? data : List.of();
        double[] array = this.dataPoints.stream().mapToDouble(Double::doubleValue).toArray();
        this.adaptee.plot(array, this.name);
        this.trace = String.format("Converted List<Double>[size=%d] -> double[%d], called adaptee.plot(double[], '%s')", 
                this.dataPoints.size(), array.length, this.name);
    }

    @Override
    public String getSource() {
        return "legacy";
    }

    @Override
    public String getAdapterTrace() {
        return trace;
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("LegacyGraphAdapter", "render", id);
        String html = this.adaptee.exportAsHtml();
        RenderResult result = new RenderResult(
            id,
            html,
            List.of(),
            Map.of("source", "legacy"),
            List.of("Adapter rendered: " + name + " via LegacyGraphLib")
        );
        com.patternforge.inspector.ManualTracer.exit("LegacyGraphAdapter", "render", id);
        return result;
    }
}
