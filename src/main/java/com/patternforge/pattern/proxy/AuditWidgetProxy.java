package com.patternforge.pattern.proxy;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;
import com.patternforge.service.AuditLogService;
import java.util.List;

public class AuditWidgetProxy implements DashboardComponent {
    private final DashboardComponent real;
    private final AuditLogService auditLogService;

    public AuditWidgetProxy(DashboardComponent real, AuditLogService auditLogService) {
        this.real = real;
        this.auditLogService = auditLogService;
    }

    @Override
    public String getId() {
        return real.getId();
    }

    @Override
    public String getName() {
        return real.getName();
    }

    @Override
    public String getType() {
        return real.getType();
    }

    @Override
    public String getPatternInfo() {
        return "Proxy Pattern - Smart Reference Proxy: Intercepts operations to add side-effects (like logging audit events).";
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("AuditWidgetProxy", "render", real.getId());
        if (auditLogService != null) {
            auditLogService.logEvent("user-session", real.getId(), System.currentTimeMillis(), "render");
        }
        RenderResult result = real.render().withTraceEntry("AuditWidgetProxy: logged render event to AuditLogService.");
        com.patternforge.inspector.ManualTracer.exit("AuditWidgetProxy", "render", real.getId());
        return result;
    }
}
