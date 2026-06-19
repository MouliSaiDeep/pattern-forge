package com.patternforge.controller;

import com.patternforge.service.BridgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name = "Bridge Pattern - Visual Renderers", description = "Endpoints for configuring active widget renderers dynamically and computing bridge class counts")
public class BridgeController {

    private final BridgeService bridgeService;

    public BridgeController(BridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @PutMapping("/api/dashboard/renderer")
    @Operation(summary = "Switch the active renderer for all Bridge widgets")
    public ResponseEntity<Void> switchRenderer(@RequestBody RendererRequest request) {
        com.patternforge.inspector.ManualTracer.enter("BridgeController", "switchRenderer[" + request.renderer() + "]", "bridge");
        bridgeService.setActiveRenderer(request.renderer());
        com.patternforge.inspector.ManualTracer.exit("BridgeController", "switchRenderer[" + request.renderer() + "]", "bridge");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/bridge/class-count")
    @Operation(summary = "Get the comparison of total class count with and without the Bridge pattern")
    public ResponseEntity<Map<String, Object>> getClassCount() {
        return ResponseEntity.ok(bridgeService.getClassCount());
    }

    @Schema(description = "Request body for switching the active renderer")
    public record RendererRequest(
        @Schema(description = "The renderer type (json, html, svg)", example = "html")
        String renderer
    ) {}
}
