package com.patternforge.inspector;

import com.patternforge.domain.CallChainEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ManualTracer implements ApplicationContextAware {
    private static PatternStackTracer tracerInstance;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        tracerInstance = applicationContext.getBean(PatternStackTracer.class);
    }

    public static void enter(String className, String methodName, String widgetId) {
        if (tracerInstance != null) {
            tracerInstance.push(new CallChainEvent(
                className, methodName, widgetId, "ENTER", System.currentTimeMillis(), Map.of("source", "manual")
            ));
        }
    }

    public static void exit(String className, String methodName, String widgetId) {
        if (tracerInstance != null) {
            tracerInstance.pop(new CallChainEvent(
                className, methodName, widgetId, "EXIT", System.currentTimeMillis(), Map.of("source", "manual")
            ));
        }
    }
}
