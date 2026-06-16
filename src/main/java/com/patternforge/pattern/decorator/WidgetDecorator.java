package com.patternforge.pattern.decorator;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

import java.util.List;

public abstract class WidgetDecorator implements DashboardComponent {
    protected final DashboardComponent wrapped;

    protected WidgetDecorator(DashboardComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String getType() {
        return wrapped.getType();
    }

    @Override
    public String getPatternInfo() {
        return "Decorator Pattern - Abstract Decorator: Wraps a component and matches its interface while dynamically adding behaviors.";
    }

    @Override
    public void add(DashboardComponent child) {
        wrapped.add(child);
    }

    @Override
    public void remove(DashboardComponent child) {
        wrapped.remove(child);
    }

    @Override
    public List<DashboardComponent> getChildren() {
        return wrapped.getChildren();
    }

    public DashboardComponent getWrapped() {
        return wrapped;
    }
}
