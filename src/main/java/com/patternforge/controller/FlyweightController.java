package com.patternforge.controller;

import com.patternforge.domain.WidgetFlyweight;
import com.patternforge.pattern.flyweight.WidgetFlyweightFactory;
import com.patternforge.service.MemoryEstimationService;
import com.patternforge.service.MemoryEstimationService.MemoryEstimate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flyweight")
@Tag(name = "Flyweight Pattern - Shared Widgets", description = "Endpoints for generating bulk flyweights and evaluating memory footprints")
public class FlyweightController {

    private final WidgetFlyweightFactory factory;
    private final MemoryEstimationService estimationService;

    public FlyweightController(WidgetFlyweightFactory factory, MemoryEstimationService estimationService) {
        this.factory = factory;
        this.estimationService = estimationService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Bulk generate widgets using the flyweight pool")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody GenerateRequest request) {
        com.patternforge.inspector.ManualTracer.enter("FlyweightController", "generate[" + request.widgetType() + ", count=" + request.count() + "]", "flyweight");
        WidgetFlyweight flyweight = factory.getFlyweight(request.widgetType());

        int count = request.count();
        for (int i = 0; i < count; i++) {
            factory.registerUsage(request.widgetType());
        }
        com.patternforge.inspector.ManualTracer.exit("FlyweightController", "generate[" + request.widgetType() + ", count=" + request.count() + "]", "flyweight");

        return ResponseEntity.ok(Map.of(
            "widgetType", request.widgetType(),
            "generatedCount", count,
            "generated", count,
            "status", "SUCCESS",
            "flyweightSizeBytes", flyweight.estimatedSizeBytes()
        ));
    }

    @GetMapping("/pool")
    @Operation(summary = "Get the status of the flyweight pool")
    public ResponseEntity<List<PoolItem>> getPool() {
        List<PoolItem> list = new ArrayList<>();
        Map<String, WidgetFlyweight> poolMap = factory.getPool();
        for (String type : poolMap.keySet()) {
            WidgetFlyweight flyweight = poolMap.get(type);
            int count = factory.getReferenceCount(type);
            list.add(new PoolItem(type, count, flyweight.estimatedSizeBytes()));
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/memory-estimate")
    @Operation(summary = "Retrieve theoretical memory usage estimates")
    public ResponseEntity<MemoryEstimate> getMemoryEstimate(@RequestParam(defaultValue = "1000") int count) {
        return ResponseEntity.ok(estimationService.estimate(count));
    }

    public record PoolItem(String type, int instanceCount, long flyweightSizeBytes) {}

    @Schema(description = "Request body for generating bulk widgets")
    public record GenerateRequest(
        @Schema(description = "The number of instances to generate", example = "1000")
        int count,
        @Schema(description = "The widget type", example = "STOCK_TICKER")
        String widgetType
    ) {}
}
