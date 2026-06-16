package com.patternforge.pattern.decorator;

import com.patternforge.domain.CallChainEvent;
import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;
import com.patternforge.inspector.PatternStackTracer;

import java.util.Map;

public class LoggingDecorator extends WidgetDecorator {
    private final PatternStackTracer tracer;

    public LoggingDecorator(DashboardComponent wrapped, PatternStackTracer tracer) {
        super(wrapped);
        this.tracer = tracer;
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("LoggingDecorator", "render", getId());
        long start = System.currentTimeMillis();
        CallChainEvent enterEvent = new CallChainEvent(
            "LoggingDecorator", "render", getId(), "ENTER", start, Map.of("message", "Decorator logging enter")
        );
        if (tracer != null) {
            tracer.push(enterEvent);
        }

        RenderResult result;
        try {
            result = wrapped.render();
        } finally {
            long end = System.currentTimeMillis();
            CallChainEvent exitEvent = new CallChainEvent(
                "LoggingDecorator", "render", getId(), "EXIT", end, Map.of("durationMs", (end - start))
            );
            if (tracer != null) {
                tracer.pop(exitEvent);
            }
            com.patternforge.inspector.ManualTracer.exit("LoggingDecorator", "render", getId());
        }

        return result.withTraceEntry("Logged render event for " + getId());
    }
}
