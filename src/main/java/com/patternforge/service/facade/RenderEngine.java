package com.patternforge.service.facade;

import com.patternforge.domain.RenderResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RenderEngine {
    private final FacadeCallLogger callLogger;

    public RenderEngine(FacadeCallLogger callLogger) {
        this.callLogger = callLogger;
    }

    public void prepareRenderContext(String dashboardId) {
        callLogger.logCall("RenderEngine: Preparing rendering context for dashboard '" + dashboardId + "'");
    }

    public RenderResult executeRender(String widgetId) {
        callLogger.logCall("RenderEngine: Executing rendering for widget '" + widgetId + "'");
        return new RenderResult(
            widgetId,
            String.format("<div class='rendered-widget'>Widget %s Rendered</div>", widgetId),
            List.of(),
            Map.of(),
            List.of("RenderEngine: executeRender for " + widgetId)
        );
    }

    public void clearCache() {
        callLogger.logCall("RenderEngine: Clearing rendering caches");
    }
}
