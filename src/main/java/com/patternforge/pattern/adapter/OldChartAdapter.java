package com.patternforge.pattern.adapter;

import com.patternforge.domain.RenderResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class OldChartAdapter implements ChartWidget {
    private final String id;
    private final String name;
    private final OldChartLib adaptee;
    private List<Double> dataPoints = new ArrayList<>();
    private String trace = "Initialized adapter";

    public OldChartAdapter(String id, String name, OldChartLib adaptee) {
        this.id = id;
        this.name = name;
        this.adaptee = adaptee;

        Properties props = new Properties();
        props.setProperty("title", name);
        props.setProperty("theme", "amber-glow");
        this.adaptee.configure(props);
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
        return "Adapter Pattern - Adapter: Converts the interface of OldChartLib into the ChartWidget interface.";
    }

    @Override
    public void setDataPoints(List<Double> data) {
        this.dataPoints = data != null ? data : List.of();
        String csv = this.dataPoints.stream().map(Object::toString).collect(Collectors.joining(","));
        this.adaptee.loadData(csv);
        this.trace = String.format("Converted List<Double>[size=%d] -> CSV String, called adaptee.loadData(csv)", this.dataPoints.size());
    }

    @Override
    public String getSource() {
        return "old";
    }

    @Override
    public String getAdapterTrace() {
        return trace;
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("OldChartAdapter", "render", id);
        String html = this.adaptee.getRenderedOutput();
        RenderResult result = new RenderResult(
            id,
            html,
            List.of(),
            Map.of("source", "old"),
            List.of("Adapter rendered: " + name + " via OldChartLib")
        );
        com.patternforge.inspector.ManualTracer.exit("OldChartAdapter", "render", id);
        return result;
    }
}
