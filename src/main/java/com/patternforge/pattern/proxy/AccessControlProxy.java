package com.patternforge.pattern.proxy;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;
import com.patternforge.service.SessionService;
import java.util.List;
import java.util.Map;

public class AccessControlProxy implements DashboardComponent {
    private final DashboardComponent real;
    private final String requiredRole;
    private final SessionService sessionService;

    public AccessControlProxy(DashboardComponent real, String requiredRole, SessionService sessionService) {
        this.real = real;
        this.requiredRole = requiredRole != null ? requiredRole.toUpperCase() : "ADMIN";
        this.sessionService = sessionService;
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
        return "Proxy Pattern - Protection Proxy: Controls access to the original object based on permission rules.";
    }

    @Override
    public RenderResult render() {
        String currentRole = sessionService != null ? sessionService.getCurrentRole() : "GUEST";
        boolean authorized = checkAccess(currentRole);
        com.patternforge.inspector.ManualTracer.enter("AccessControlProxy", "render", real.getId());

        if (!authorized) {
            String html = String.format(
                "<div class='access-proxy border border-red-500/50 bg-red-950/20 p-6 rounded-xl shadow-lg w-full flex flex-col items-center justify-center text-center min-h-[200px]'>" +
                "  <div class='h-12 w-12 rounded-full bg-red-500/20 text-red-500 flex items-center justify-center mb-3'>" +
                "    <svg class='w-6 h-6' fill='none' stroke='currentColor' viewBox='0 0 24 24' style='width:24px;height:24px;'>" +
                "      <path stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z'/>" +
                "    </svg>" +
                "  </div>" +
                "  <h4 class='text-sm font-bold text-red-400 mb-1'>Access Denied</h4>" +
                "  <p class='text-xs text-red-300/80 mb-2'>Required role: <span class='font-semibold text-red-400'>%s</span> (Current: %s)</p>" +
                "  <span class='text-[10px] text-slate-500'>Protection Proxy blocked direct rendering</span>" +
                "</div>",
                requiredRole, currentRole
            );
            com.patternforge.inspector.ManualTracer.exit("AccessControlProxy", "render[BLOCKED:" + currentRole + "]", real.getId());
            return new RenderResult(
                real.getId(),
                html,
                List.of(),
                Map.of("authorized", "false", "requiredRole", requiredRole, "currentRole", currentRole),
                List.of("AccessControlProxy: blocked render of " + real.getId() + " - role " + currentRole + " lacks " + requiredRole)
            );
        }

        RenderResult result = real.render().withTraceEntry("AccessControlProxy: authorized role " + currentRole + " to view " + real.getId());
        com.patternforge.inspector.ManualTracer.exit("AccessControlProxy", "render[AUTHORIZED:" + currentRole + "]", real.getId());
        return result;
    }

    private boolean checkAccess(String role) {
        if ("ADMIN".equals(role)) return true;
        if ("EDITOR".equals(role)) {
            return "EDITOR".equals(requiredRole) || "GUEST".equals(requiredRole);
        }
        return "GUEST".equals(requiredRole);
    }
}
