package com.patternforge.service;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;
import com.patternforge.pattern.composite.ContainerNode;
import com.patternforge.pattern.composite.NodeNotFoundException;
import com.patternforge.pattern.composite.WidgetNode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardTreeService {
    private final ContainerNode root;

    public DashboardTreeService() {
        this.root = new ContainerNode("root", "Main Dashboard");
    }

    public synchronized ContainerNode getTree() {
        return root;
    }

    public synchronized ContainerNode addContainer(String parentId, String name) {
        DashboardComponent parent = findNode(root, parentId);
        if (parent == null) {
            throw new NodeNotFoundException("Parent container not found: " + parentId);
        }
        if (!(parent instanceof ContainerNode)) {
            throw new IllegalArgumentException("Parent must be a container: " + parentId);
        }
        String id = "container-" + UUID.randomUUID().toString().substring(0, 8);
        ContainerNode container = new ContainerNode(id, name);
        parent.add(container);
        return container;
    }

    public synchronized WidgetNode addWidget(String parentId, String widgetType, String name, Map<String, Object> config) {
        DashboardComponent parent = findNode(root, parentId);
        if (parent == null) {
            throw new NodeNotFoundException("Parent container not found: " + parentId);
        }
        if (!(parent instanceof ContainerNode)) {
            throw new IllegalArgumentException("Parent must be a container: " + parentId);
        }
        String id = "widget-" + UUID.randomUUID().toString().substring(0, 8);
        WidgetNode widget = new WidgetNode(id, name, widgetType, config);
        parent.add(widget);
        return widget;
    }

    public synchronized void move(String childId, String newParentId) {
        DashboardComponent child = findNode(root, childId);
        if (child == null) {
            throw new NodeNotFoundException("Child node not found: " + childId);
        }

        ContainerNode oldParent = findParent(root, childId);
        ContainerNode newParent = (ContainerNode) findNode(root, newParentId);
        if (newParent == null) {
            throw new NodeNotFoundException("New parent container not found: " + newParentId);
        }

        // Add to new parent first (triggers circular reference check)
        newParent.add(child);

        // Remove from old parent if it exists
        if (oldParent != null) {
            oldParent.remove(child);
        }
    }

    public synchronized void delete(String nodeId) {
        if ("root".equals(nodeId)) {
            throw new IllegalArgumentException("Cannot delete the root node");
        }
        ContainerNode parent = findParent(root, nodeId);
        if (parent == null) {
            throw new NodeNotFoundException("Node not found or has no parent: " + nodeId);
        }
        DashboardComponent node = findNode(root, nodeId);
        parent.remove(node);
    }

    public synchronized RenderResult renderAll() {
        return root.render();
    }

    // Helper to find a node by ID
    public DashboardComponent findNode(DashboardComponent current, String id) {
        if (current.getId().equals(id)) {
            return current;
        }
        for (DashboardComponent child : current.getChildren()) {
            DashboardComponent found = findNode(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // Helper to find the parent of a node by ID
    public ContainerNode findParent(DashboardComponent current, String childId) {
        for (DashboardComponent child : current.getChildren()) {
            if (child.getId().equals(childId)) {
                return (ContainerNode) current;
            }
            if (child instanceof ContainerNode) {
                ContainerNode parent = findParent(child, childId);
                if (parent != null) {
                    return parent;
                }
            }
        }
        return null;
    }

    // Replace a child node in its parent container
    public synchronized void replaceNode(String id, DashboardComponent newNode) {
        ContainerNode parent = findParent(root, id);
        if (parent != null) {
            // Find and remove the old child
            DashboardComponent oldNode = findNode(root, id);
            parent.remove(oldNode);
            parent.add(newNode);
        }
    }
}
