package com.patternforge.service;

import com.patternforge.pattern.adapter.*;
import com.patternforge.pattern.composite.ContainerNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChartAdapterService {
    private final DashboardTreeService treeService;
    private final Map<String, ChartWidget> charts = new ConcurrentHashMap<>();

    public ChartAdapterService(DashboardTreeService treeService) {
        this.treeService = treeService;
    }

    public synchronized ChartWidget createChartWidget(String source, String title, List<Double> dataPoints) {
        String id = "chart-" + UUID.randomUUID().toString().substring(0, 8);
        ChartWidget chart;

        switch (source.toLowerCase()) {
            case "legacy":
                chart = new LegacyGraphAdapter(id, title, new LegacyGraphLib());
                break;
            case "old":
                chart = new OldChartAdapter(id, title, new OldChartLib());
                break;
            case "new":
            default:
                chart = new NativeChartWidget(id, title);
                break;
        }

        chart.setDataPoints(dataPoints);
        charts.put(id, chart);

        // Add to root container of the dashboard tree
        ContainerNode root = treeService.getTree();
        root.add(chart);

        return chart;
    }

    public ChartWidget getChartWidget(String id) {
        // Fallback: look up in tree directly if not in service map
        ChartWidget cached = charts.get(id);
        if (cached != null) {
            return cached;
        }
        var found = treeService.findNode(treeService.getTree(), id);
        if (found instanceof ChartWidget cw) {
            return cw;
        }
        return null;
    }
}
