package com.patternforge.controller;

import com.patternforge.service.DecoratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/widget/{id}/decorators")
@Tag(name = "Decorator Pattern - Widget Decorators", description = "Endpoints for inspecting, wrapping, and reordering decorators on dashboard widgets")
public class PatternController {

    private final DecoratorService decoratorService;

    public PatternController(DecoratorService decoratorService) {
        this.decoratorService = decoratorService;
    }

    @GetMapping
    @Operation(summary = "Get the current decorator stack for a widget")
    public ResponseEntity<List<String>> getDecoratorStack(@PathVariable String id) {
        List<String> stack = decoratorService.getDecoratorStack(id);
        return ResponseEntity.ok(stack);
    }

    @PostMapping
    @Operation(summary = "Wrap a widget in a new decorator")
    public ResponseEntity<Void> addDecorator(
            @PathVariable String id,
            @RequestBody DecoratorRequest request) {
        decoratorService.addDecorator(id, request.type(), request.config());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{decoratorType}")
    @Operation(summary = "Remove the outermost decorator of a specific type from a widget")
    public ResponseEntity<Void> removeDecorator(
            @PathVariable String id,
            @PathVariable String decoratorType) {
        decoratorService.removeDecorator(id, decoratorType);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Reset and remove all decorators from a widget")
    public ResponseEntity<Void> resetDecorators(@PathVariable String id) {
        decoratorService.resetDecorators(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reorder")
    @Operation(summary = "Rebuild and reorder the decorator stack for a widget")
    public ResponseEntity<Void> reorderDecorators(
            @PathVariable String id,
            @RequestBody ReorderRequest request) {
        decoratorService.reorderDecorators(id, request.orderedTypes());
        return ResponseEntity.ok().build();
    }

    @Schema(description = "Request body for adding a decorator")
    public record DecoratorRequest(
        @Schema(description = "Type of decorator (BORDER, SHADOW, PADDING, LOGGING, THEME)", example = "BORDER")
        String type,
        @Schema(description = "Optional configuration values (e.g. themeName for THEME)")
        Map<String, Object> config
    ) {}

    @Schema(description = "Request body for reordering decorators")
    public record ReorderRequest(
        @Schema(description = "Ordered list of decorator types from outermost to innermost", example = "[\"BorderDecorator\", \"ShadowDecorator\"]")
        List<String> orderedTypes
    ) {}
}
