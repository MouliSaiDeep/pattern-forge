package com.patternforge.service;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.pattern.bridge.*;
import com.patternforge.pattern.decorator.WidgetDecorator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BridgeService {
    private final DashboardTreeService treeService;
    private WidgetRenderer activeRenderer;

    public BridgeService(DashboardTreeService treeService) {
        this.treeService = treeService;
        this.activeRenderer = new HtmlRenderer();
    }

    public synchronized WidgetRenderer getActiveRenderer() {
        return activeRenderer;
    }

    public synchronized void setActiveRenderer(String type) {
        switch (type.toLowerCase()) {
            case "json":
                activeRenderer = new JsonRenderer();
                break;
            case "svg":
                activeRenderer = new SvgRenderer();
                break;
            case "html":
            default:
                activeRenderer = new HtmlRenderer();
                break;
        }

        // Apply new renderer to all Bridge widgets in the composite tree
        updateTreeRenderers(treeService.getTree(), activeRenderer);
    }

    private void updateTreeRenderers(DashboardComponent node, WidgetRenderer renderer) {
        if (node == null) {
            return;
        }

        if (node instanceof BridgeWidget bw) {
            bw.setRenderer(renderer);
        } else if (node instanceof WidgetDecorator decorator) {
            updateTreeRenderers(decorator.getWrapped(), renderer);
        }

        // Always traverse children for containers or composites
        for (DashboardComponent child : node.getChildren()) {
            updateTreeRenderers(child, renderer);
        }
    }

    public Map<String, Object> getClassCount() {
        int abstractions = 3;  // Chart, Table, Text
        int implementors = 3;  // Html, Json, Svg

        int withoutBridge = abstractions * implementors;
        int withBridge = abstractions + implementors;

        Map<String, Object> result = new HashMap<>();
        result.put("abstractions", abstractions);
        result.put("implementors", implementors);
        result.put("withoutBridge", withoutBridge);
        result.put("withBridge", withBridge);
        return result;
    }
}
