package com.patternforge.service;

import com.patternforge.service.facade.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardManager {
    private final WidgetRegistry widgetRegistry;
    private final LayoutEngine layoutEngine;
    private final ThemeEngine themeEngine;
    private final RenderEngine renderEngine;
    private final FacadeCallLogger callLogger;

    public DashboardManager(WidgetRegistry widgetRegistry,
                            LayoutEngine layoutEngine,
                            ThemeEngine themeEngine,
                            RenderEngine renderEngine,
                            FacadeCallLogger callLogger) {
        this.widgetRegistry = widgetRegistry;
        this.layoutEngine = layoutEngine;
        this.themeEngine = themeEngine;
        this.renderEngine = renderEngine;
        this.callLogger = callLogger;
    }

    public synchronized Map<String, Object> createDashboard(String name, int columns, int rows, String theme) {
        callLogger.clear();
        callLogger.logCall("Facade: createDashboard started for '" + name + "'");

        String dashboardId = "dash-" + UUID.randomUUID().toString().substring(0, 8);

        // Orchestration
        Map<String, Object> grid = layoutEngine.calculateGrid(columns, rows);
        Map<String, Object> themeConfig = themeEngine.loadTheme(theme);

        // Simulating registering some default widgets for the dashboard
        String welcomeWidgetId = "widget-welcome";
        widgetRegistry.register(welcomeWidgetId, "WELCOME");
        layoutEngine.applyConstraints(welcomeWidgetId, Map.of("row", 1, "col", 1));
        themeEngine.applyTheme(welcomeWidgetId, theme);

        layoutEngine.validateLayout();
        renderEngine.prepareRenderContext(dashboardId);
        renderEngine.executeRender(welcomeWidgetId);

        callLogger.logCall("Facade: createDashboard completed for '" + name + "'");

        Map<String, Object> result = new HashMap<>();
        result.put("dashboardId", dashboardId);
        result.put("name", name);
        result.put("status", "CREATED");
        result.put("grid", grid);
        result.put("themeConfig", themeConfig);
        result.put("subsystemCalls", callLogger.getLastCallLog());
        return result;
    }

    public synchronized Map<String, Object> applyTheme(String dashboardId, String themeName) {
        callLogger.clear();
        callLogger.logCall("Facade: applyTheme started for dashboard '" + dashboardId + "' with theme '" + themeName + "'");

        // Orchestration: ThemeEngine.loadTheme -> ThemeEngine.generateCssVariables -> ThemeEngine.applyTheme -> RenderEngine.clearCache -> RenderEngine.prepareRenderContext
        Map<String, Object> themeConfig = themeEngine.loadTheme(themeName);
        String cssVars = themeEngine.generateCssVariables(themeName);

        // Get registered widgets and apply theme to all of them
        List<String> widgets = widgetRegistry.getRegistered();
        if (widgets.isEmpty()) {
            widgetRegistry.register("widget-default", "DEFAULT");
            widgets = widgetRegistry.getRegistered();
        }

        for (String widgetId : widgets) {
            themeEngine.applyTheme(widgetId, themeName);
        }

        renderEngine.clearCache();
        renderEngine.prepareRenderContext(dashboardId);

        callLogger.logCall("Facade: applyTheme completed for dashboard '" + dashboardId + "'");

        Map<String, Object> result = new HashMap<>();
        result.put("dashboardId", dashboardId);
        result.put("appliedTheme", themeName);
        result.put("cssVariables", cssVars);
        result.put("subsystemCalls", callLogger.getLastCallLog());
        return result;
    }

    public List<String> getLastCallLog() {
        return callLogger.getLastCallLog();
    }
}
