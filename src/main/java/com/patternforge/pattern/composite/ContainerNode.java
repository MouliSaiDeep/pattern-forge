package com.patternforge.pattern.composite;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class ContainerNode implements DashboardComponent {
    private final String id;
    private final String name;
    private final List<DashboardComponent> children = new CopyOnWriteArrayList<>();

    public ContainerNode(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPatternInfo() {
        return "Composite Pattern - Composite: Represents a container that can hold both leaves and other containers.";
    }

    @Override
    public String getType() {
        return "CONTAINER";
    }

    @Override
    public void add(DashboardComponent child) {
        if (child.getId().equals(this.id)) {
            throw new CircularReferenceException("Circular reference detected: Cannot add container to itself (ID: " + this.id + ")");
        }
        if (containsDescendant(child, this.id)) {
            throw new CircularReferenceException("Circular reference detected: Child tree contains parent container (ID: " + this.id + ")");
        }
        children.add(child);
    }

    private boolean containsDescendant(DashboardComponent parent, String targetId) {
        if (parent.getId().equals(targetId)) {
            return true;
        }
        for (DashboardComponent child : parent.getChildren()) {
            if (containsDescendant(child, targetId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void remove(DashboardComponent child) {
        children.removeIf(c -> c.getId().equals(child.getId()));
    }

    @Override
    public List<DashboardComponent> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("ContainerNode", "render", id);
        try {
            StringBuilder aggregatedHtml = new StringBuilder();
            aggregatedHtml.append(String.format(
                "<div class='container-box p-6 rounded-3xl bg-slate-900/40 border border-slate-800/80 shadow-2xl backdrop-blur-lg mb-6' id='%s' data-node-type='CONTAINER'>" +
                "  <div class='flex items-center justify-between mb-4 border-b border-slate-800/60 pb-3'>" +
                "    <div class='flex items-center gap-2'>" +
                "      <span class='h-3 w-3 rounded-full bg-indigo-500 animate-pulse'></span>" +
                "      <h3 class='text-md font-bold text-slate-200'>%s</h3>" +
                "    </div>" +
                "    <span class='px-2 py-0.5 text-[10px] font-semibold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 rounded-md'>CONTAINER</span>" +
                "  </div>" +
                "  <div class='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6'>",
                id, name
            ));

            Set<String> styles = new LinkedHashSet<>();
            Map<String, String> meta = new HashMap<>();
            List<String> traces = new ArrayList<>();
            traces.add("Entered Container Node: " + name + " (ID: " + id + ")");

            for (DashboardComponent child : children) {
                RenderResult childResult = child.render();
                aggregatedHtml.append(childResult.htmlContent());
                styles.addAll(childResult.cssStyles());
                meta.putAll(childResult.metadata());
                traces.addAll(childResult.renderTrace());
            }

            aggregatedHtml.append("  </div></div>");
            traces.add("Exited Container Node: " + name + " (ID: " + id + ")");

            return new RenderResult(
                id,
                aggregatedHtml.toString(),
                List.copyOf(styles),
                Map.copyOf(meta),
                List.copyOf(traces)
            );
        } finally {
            com.patternforge.inspector.ManualTracer.exit("ContainerNode", "render", id);
        }
    }
}
