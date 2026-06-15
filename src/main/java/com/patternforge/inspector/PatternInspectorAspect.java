package com.patternforge.inspector;

import com.patternforge.domain.CallChainEvent;
import com.patternforge.domain.DashboardComponent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component
public class PatternInspectorAspect {
    private final PatternStackTracer tracer;

    public PatternInspectorAspect(PatternStackTracer tracer) {
        this.tracer = tracer;
    }

    // Target ALL render() methods in pattern implementations
    @Pointcut("execution(* com.patternforge.pattern..*.render(..))")
    public void renderMethods() {}

    @Around("renderMethods()")
    public Object traceRender(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        // Extract widgetId if possible via reflection on target
        String widgetId = extractWidgetId(pjp.getTarget());

        CallChainEvent enterEvent = new CallChainEvent(
            className, methodName, widgetId, "ENTER", System.currentTimeMillis(), Map.of("source", "aop")
        );
        tracer.push(enterEvent);

        try {
            Object result = pjp.proceed();
            CallChainEvent exitEvent = new CallChainEvent(
                className, methodName, widgetId, "EXIT", System.currentTimeMillis(), Map.of("source", "aop")
            );
            tracer.pop(exitEvent);
            return result;
        } catch (Throwable t) {
            tracer.pop(new CallChainEvent(
                className, methodName, widgetId, "ERROR", System.currentTimeMillis(), Map.of("source", "aop", "error", t.getMessage() != null ? t.getMessage() : "Unknown error")
            ));
            throw t;
        }
    }

    private String extractWidgetId(Object target) {
        if (target instanceof DashboardComponent dc) {
            return dc.getId();
        }
        try {
            Method getIdMethod = target.getClass().getMethod("getId");
            return (String) getIdMethod.invoke(target);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
