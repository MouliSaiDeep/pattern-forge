package com.patternforge.controller;

import com.patternforge.pattern.adapter.ChartWidget;
import com.patternforge.service.ChartAdapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/widget")
@Tag(name = "Adapter Pattern - Chart Widgets", description = "Endpoints for creating chart widgets utilizing legacy adapters and inspecting translation traces")
public class AdapterController {

    private final ChartAdapterService chartService;

    public AdapterController(ChartAdapterService chartService) {
        this.chartService = chartService;
    }

    @PostMapping("/chart")
    @Operation(summary = "Create a new chart widget using a legacy adapter or a native engine")
    public ResponseEntity<Map<String, Object>> createChart(@RequestBody ChartRequest request) {
        ChartWidget chart = chartService.createChartWidget(
            request.source(),
            request.title(),
            request.dataPoints()
        );
        return ResponseEntity.ok(Map.of(
            "id", chart.getId(),
            "name", chart.getName(),
            "type", chart.getType(),
            "patternInfo", chart.getPatternInfo(),
            "source", chart.getSource(),
            "adapterTrace", chart.getAdapterTrace(),
            "renderResult", chart.render()
        ));
    }

    @GetMapping("/{id}/adapter-trace")
    @Operation(summary = "Get the translation trace details for an adapted chart widget")
    public ResponseEntity<Map<String, String>> getAdapterTrace(@PathVariable String id) {
        ChartWidget chart = chartService.getChartWidget(id);
        if (chart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "widgetId", id,
            "source", chart.getSource(),
            "trace", chart.getAdapterTrace()
        ));
    }

    @Schema(description = "Request body for creating a chart widget")
    public record ChartRequest(
        @Schema(description = "The engine source (legacy, old, new)", example = "legacy")
        String source,
        @Schema(description = "Title of the chart", example = "Sales Performance")
        String title,
        @Schema(description = "List of double data points", example = "[10.5, 20.3, 15.8, 30.1]")
        List<Double> dataPoints
    ) {}
}
