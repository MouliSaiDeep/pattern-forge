package com.patternforge.service;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.inspector.PatternStackTracer;
import com.patternforge.pattern.composite.WidgetNode;
import com.patternforge.pattern.decorator.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DecoratorService {

    private final DashboardTreeService treeService;
    private final PatternStackTracer stackTracer;
    private final Map<String, DashboardComponent> decoratorMap = new ConcurrentHashMap<>();

    public DecoratorService(DashboardTreeService treeService, PatternStackTracer stackTracer) {
        this.treeService = treeService;
        this.stackTracer = stackTracer;
    }

    public synchronized List<String> getDecoratorStack(String widgetId) {
        List<String> stack = new ArrayList<>();
        DashboardComponent current = decoratorMap.get(widgetId);
        if (current == null) {
            current = treeService.findNode(treeService.getTree(), widgetId);
        }
        while (current instanceof WidgetDecorator decorator) {
            stack.add(decorator.getClass().getSimpleName());
            current = decorator.getWrapped();
        }
        return stack;
    }

    public synchronized void addDecorator(String widgetId, String type, Map<String, Object> config) {
        ChainData chainData = extractChain(widgetId);
        if (chainData == null) {
            throw new IllegalArgumentException("Widget not found: " + widgetId);
        }

        DecoratorInfo newInfo = new DecoratorInfo(type, config);
        chainData.infos().add(0, newInfo); // add to outer position

        DashboardComponent decorated = rebuildChain(chainData.rawWidget(), chainData.infos());
        decoratorMap.put(widgetId, decorated);
        treeService.replaceNode(widgetId, decorated);
    }

    public synchronized void removeDecorator(String widgetId, String type) {
        ChainData chainData = extractChain(widgetId);
        if (chainData == null) {
            throw new IllegalArgumentException("Widget not found: " + widgetId);
        }

        boolean removed = false;
        Iterator<DecoratorInfo> iterator = chainData.infos().iterator();
        while (iterator.hasNext()) {
            DecoratorInfo info = iterator.next();
            if (info.type().equalsIgnoreCase(type) || (info.type() + "Decorator").equalsIgnoreCase(type)) {
                iterator.remove();
                removed = true;
                break; // remove outermost only
            }
        }

        if (removed) {
            DashboardComponent decorated = rebuildChain(chainData.rawWidget(), chainData.infos());
            if (chainData.infos().isEmpty()) {
                decoratorMap.remove(widgetId);
            } else {
                decoratorMap.put(widgetId, decorated);
            }
            treeService.replaceNode(widgetId, decorated);
        }
    }

    public synchronized void resetDecorators(String widgetId) {
        ChainData chainData = extractChain(widgetId);
        if (chainData == null) {
            throw new IllegalArgumentException("Widget not found: " + widgetId);
        }
        decoratorMap.remove(widgetId);
        treeService.replaceNode(widgetId, chainData.rawWidget());
    }

    public synchronized void reorderDecorators(String widgetId, List<String> orderedTypes) {
        ChainData chainData = extractChain(widgetId);
        if (chainData == null) {
            throw new IllegalArgumentException("Widget not found: " + widgetId);
        }

        List<DecoratorInfo> newInfos = new ArrayList<>();
        for (String type : orderedTypes) {
            // Find corresponding info in existing chain
            Optional<DecoratorInfo> existing = chainData.infos().stream()
                    .filter(info -> info.type().equalsIgnoreCase(type) || (info.type() + "Decorator").equalsIgnoreCase(type))
                    .findFirst();
            if (existing.isPresent()) {
                newInfos.add(existing.get());
                // Remove to handle duplicates if any, but keep it simple
            } else {
                // Instantiation default if not in chain already
                newInfos.add(new DecoratorInfo(type, Map.of()));
            }
        }

        DashboardComponent decorated = rebuildChain(chainData.rawWidget(), newInfos);
        if (newInfos.isEmpty()) {
            decoratorMap.remove(widgetId);
        } else {
            decoratorMap.put(widgetId, decorated);
        }
        treeService.replaceNode(widgetId, decorated);
    }

    private ChainData extractChain(String widgetId) {
        DashboardComponent current = decoratorMap.get(widgetId);
        if (current == null) {
            current = treeService.findNode(treeService.getTree(), widgetId);
        }
        if (current == null) {
            return null;
        }

        List<DecoratorInfo> infos = new ArrayList<>();
        DashboardComponent temp = current;
        while (temp instanceof WidgetDecorator decorator) {
            String type = decorator.getClass().getSimpleName();
            Map<String, Object> config = new HashMap<>();
            if (decorator instanceof ThemeDecorator themeDecorator) {
                config.put("themeName", themeDecorator.getThemeName());
            }
            infos.add(new DecoratorInfo(type, config));
            temp = decorator.getWrapped();
        }

        return new ChainData(temp, infos);
    }

    private DashboardComponent rebuildChain(DashboardComponent rawWidget, List<DecoratorInfo> infos) {
        DashboardComponent current = rawWidget;
        // Rebuild from innermost (last in list) to outermost (first in list)
        for (int i = infos.size() - 1; i >= 0; i--) {
            DecoratorInfo info = infos.get(i);
            current = createDecoratorInstance(info.type(), current, info.config());
        }
        return current;
    }

    private DashboardComponent createDecoratorInstance(String type, DashboardComponent wrapped, Map<String, Object> config) {
        String cleanType = type.toLowerCase().replace("decorator", "");
        switch (cleanType) {
            case "border":
                return new BorderDecorator(wrapped);
            case "shadow":
                return new ShadowDecorator(wrapped);
            case "padding":
                return new PaddingDecorator(wrapped);
            case "logging":
                return new LoggingDecorator(wrapped, stackTracer);
            case "theme":
                String themeName = (String) config.getOrDefault("themeName", "cyberpunk");
                return new ThemeDecorator(wrapped, themeName);
            default:
                throw new IllegalArgumentException("Unknown decorator type: " + type);
        }
    }

    private record DecoratorInfo(String type, Map<String, Object> config) {}
    private record ChainData(DashboardComponent rawWidget, List<DecoratorInfo> infos) {}
}
