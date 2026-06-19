package com.patternforge.controller;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;
import com.patternforge.pattern.composite.ContainerNode;
import com.patternforge.pattern.composite.WidgetNode;
import com.patternforge.service.DashboardTreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Composite Pattern - Dashboard Controller", description = "Endpoints for managing the dashboard component tree (containers and widgets)")
public class DashboardController {

    private final DashboardTreeService treeService;

    public DashboardController(DashboardTreeService treeService) {
        this.treeService = treeService;
    }

    @GetMapping("/tree")
    @Operation(summary = "Get the full dashboard component tree structure")
    public ResponseEntity<DashboardComponent> getTree() {
        return ResponseEntity.ok(treeService.getTree());
    }

    @PostMapping("/container")
    @Operation(summary = "Create a new container node inside a parent container")
    public ResponseEntity<ContainerNode> createContainer(@RequestBody ContainerRequest request) {
        ContainerNode container = treeService.addContainer(request.parentId(), request.name());
        return ResponseEntity.ok(container);
    }

    @PostMapping("/widget")
    @Operation(summary = "Create a new widget leaf node inside a parent container")
    public ResponseEntity<WidgetNode> createWidget(@RequestBody WidgetRequest request) {
        WidgetNode widget = treeService.addWidget(
            request.parentId(),
            request.widgetType(),
            request.name(),
            request.config()
        );
        return ResponseEntity.ok(widget);
    }

    @PutMapping("/{containerId}/add/{childId}")
    @Operation(summary = "Move and nest a child node into a container node")
    public ResponseEntity<Void> moveChild(
            @PathVariable String containerId,
            @PathVariable String childId) {
        treeService.move(childId, containerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/node/{id}")
    @Operation(summary = "Remove a node and all of its descendants from the tree")
    public ResponseEntity<Void> deleteNode(@PathVariable String id) {
        treeService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/render-all")
    @Operation(summary = "Trigger full recursive rendering of the dashboard")
    public ResponseEntity<RenderResult> renderAll() {
        RenderResult result = treeService.renderAll();
        return ResponseEntity.ok(result);
    }

    @Schema(description = "Request body for creating a container")
    public record ContainerRequest(
        @Schema(description = "ID of the parent container", example = "root")
        String parentId,
        @Schema(description = "Name of the new container", example = "Analytics Panel")
        String name
    ) {}

    @Schema(description = "Request body for creating a widget")
    public record WidgetRequest(
        @Schema(description = "ID of the parent container", example = "root")
        String parentId,
        @Schema(description = "Name of the widget", example = "Clock Widget")
        String name,
        @Schema(description = "Type of the widget", example = "CLOCK")
        String widgetType,
        @Schema(description = "Configuration properties for the widget")
        Map<String, Object> config
    ) {}
}
