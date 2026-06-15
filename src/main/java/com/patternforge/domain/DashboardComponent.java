package com.patternforge.domain;

import java.util.Collections;
import java.util.List;

public interface DashboardComponent {
    String getId();
    String getName();
    RenderResult render();
    String getPatternInfo();
    String getType(); // "WIDGET" or "CONTAINER"

    default void add(DashboardComponent child) {
        throw new UnsupportedOperationException("Leaf nodes cannot have children");
    }
    default void remove(DashboardComponent child) {
        throw new UnsupportedOperationException("Leaf nodes cannot have children");
    }
    default List<DashboardComponent> getChildren() {
        return Collections.emptyList();
    }
}
