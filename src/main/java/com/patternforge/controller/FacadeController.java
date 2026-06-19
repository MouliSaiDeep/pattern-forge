package com.patternforge.controller;

import com.patternforge.domain.RenderResult;
import com.patternforge.service.DashboardManager;
import com.patternforge.service.facade.LayoutEngine;
import com.patternforge.service.facade.RenderEngine;
import com.patternforge.service.facade.ThemeEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Facade Pattern - Dashboard Manager & Subsystems", description = "Endpoints demonstrating a simplified facade interface vs raw direct subsystem calls")
public class FacadeController {

    private final DashboardManager dashboardManager;
    private final LayoutEngine layoutEngine;
    private final ThemeEngine themeEngine;
    private final RenderEngine renderEngine;

    public FacadeController(DashboardManager dashboardManager,
                            LayoutEngine layoutEngine,
                            ThemeEngine themeEngine,
                            RenderEngine renderEngine) {
        this.dashboardManager = dashboardManager;
        this.layoutEngine = layoutEngine;
        this.themeEngine = themeEngine;
        this.renderEngine = renderEngine;
    }

    // --- FACADE ENDPOINTS ---

    @PostMapping("/api/facade/create-dashboard")
    @Operation(summary = "Orchestrate creation of a dashboard via the Facade (DashboardManager)")
    public ResponseEntity<Map<String, Object>> createDashboard(@RequestBody CreateDashboardRequest request) {
        Map<String, Object> response = dashboardManager.createDashboard(
            request.name(),
            request.columns(),
            request.rows(),
            request.theme()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/facade/apply-theme")
    @Operation(summary = "Orchestrate theme application via the Facade")
    public ResponseEntity<Map<String, Object>> applyThemeFacade(@RequestBody ApplyThemeRequest request) {
        Map<String, Object> response = dashboardManager.applyTheme(
            request.dashboardId(),
            request.themeName()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/facade/call-log")
    @Operation(summary = "Get the interaction logs of the last executed Facade operation")
    public ResponseEntity<List<String>> getFacadeCallLog() {
        return ResponseEntity.ok(dashboardManager.getLastCallLog());
    }

    // --- RAW SUBSYSTEM ENDPOINTS (Demonstration Only) ---

    @PostMapping("/api/subsystems/layout/calculate-grid")
    @Operation(summary = "Subsystem direct call: Calculate grid layout")
    public ResponseEntity<Map<String, Object>> calculateGrid(@RequestBody GridRequest request) {
        return ResponseEntity.ok(layoutEngine.calculateGrid(request.columns(), request.rows()));
    }

    @PostMapping("/api/subsystems/layout/apply-constraints")
    @Operation(summary = "Subsystem direct call: Apply constraints to a widget")
    public ResponseEntity<Void> applyConstraints(@RequestBody ConstraintsRequest request) {
        layoutEngine.applyConstraints(request.widgetId(), request.constraints());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/subsystems/theme/load")
    @Operation(summary = "Subsystem direct call: Load a theme configuration")
    public ResponseEntity<Map<String, Object>> loadTheme(@RequestParam String themeName) {
        return ResponseEntity.ok(themeEngine.loadTheme(themeName));
    }

    @PostMapping("/api/subsystems/theme/apply")
    @Operation(summary = "Subsystem direct call: Apply theme directly to a widget")
    public ResponseEntity<Void> applyThemeDirect(@RequestParam String widgetId, @RequestParam String themeName) {
        themeEngine.applyTheme(widgetId, themeName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/subsystems/render/prepare")
    @Operation(summary = "Subsystem direct call: Prepare context for rendering")
    public ResponseEntity<Void> prepareRender(@RequestParam String dashboardId) {
        renderEngine.prepareRenderContext(dashboardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/subsystems/render/execute")
    @Operation(summary = "Subsystem direct call: Execute rendering of a widget")
    public ResponseEntity<RenderResult> executeRender(@RequestParam String widgetId) {
        return ResponseEntity.ok(renderEngine.executeRender(widgetId));
    }

    // --- REQUEST DTOS ---

    @Schema(description = "Request body for creating a dashboard via facade")
    public record CreateDashboardRequest(
        @Schema(description = "Dashboard name", example = "Operations Control")
        String name,
        @Schema(description = "Number of columns", example = "4")
        int columns,
        @Schema(description = "Number of rows", example = "3")
        int rows,
        @Schema(description = "Theme name", example = "dark")
        String theme
    ) {}

    @Schema(description = "Request body for applying a theme via facade")
    public record ApplyThemeRequest(
        @Schema(description = "ID of the target dashboard", example = "dash-12345")
        String dashboardId,
        @Schema(description = "Theme name", example = "sunset")
        String themeName
    ) {}

    @Schema(description = "Request body for direct grid calculation")
    public record GridRequest(
        int columns,
        int rows
    ) {}

    @Schema(description = "Request body for direct layout constraints application")
    public record ConstraintsRequest(
        String widgetId,
        Map<String, Object> constraints
    ) {}
}
