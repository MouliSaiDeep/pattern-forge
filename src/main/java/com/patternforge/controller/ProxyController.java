package com.patternforge.controller;

import com.patternforge.domain.AuditEvent;
import com.patternforge.domain.DashboardComponent;
import com.patternforge.pattern.proxy.HeavyWidget;
import com.patternforge.pattern.proxy.LazyWidgetProxy;
import com.patternforge.service.AuditLogService;
import com.patternforge.service.DashboardTreeService;
import com.patternforge.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Proxy Pattern - Proxy Control", description = "Endpoints demonstrating virtual proxies, access protection proxies, and audit logs")
public class ProxyController {

    private final DashboardTreeService treeService;
    private final SessionService sessionService;
    private final AuditLogService auditLogService;

    public ProxyController(DashboardTreeService treeService,
                           SessionService sessionService,
                           AuditLogService auditLogService) {
        this.treeService = treeService;
        this.sessionService = sessionService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/api/widget/{id}/load")
    @Operation(summary = "Trigger asynchronous loading of a virtual proxy widget")
    public ResponseEntity<Void> loadProxyWidget(@PathVariable String id) {
        com.patternforge.inspector.ManualTracer.enter("ProxyController", "loadProxyWidget", id);
        DashboardComponent node = treeService.findNode(treeService.getTree(), id);
        DashboardComponent unwrapped = unwrap(node);
        if (unwrapped instanceof LazyWidgetProxy proxy) {
            proxy.load();
            com.patternforge.inspector.ManualTracer.exit("ProxyController", "loadProxyWidget", id);
            return ResponseEntity.ok().build();
        }
        com.patternforge.inspector.ManualTracer.exit("ProxyController", "loadProxyWidget[ERROR]", id);
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/api/widget/{id}/proxy-state")
    @Operation(summary = "Get the loading state of a heavy/virtual proxy widget")
    public ResponseEntity<Map<String, String>> getProxyState(@PathVariable String id) {
        DashboardComponent node = treeService.findNode(treeService.getTree(), id);
        DashboardComponent unwrapped = unwrap(node);
        if (unwrapped instanceof HeavyWidget proxy) {
            return ResponseEntity.ok(Map.of("state", proxy.getProxyState().name()));
        }
        return ResponseEntity.ok(Map.of("state", "LOADED")); // Standard components are considered LOADED
    }

    private DashboardComponent unwrap(DashboardComponent component) {
        DashboardComponent current = component;
        while (current != null) {
            try {
                java.lang.reflect.Field field = findField(current.getClass(), "real");
                if (field == null) {
                    field = findField(current.getClass(), "wrapped");
                }
                if (field != null) {
                    field.setAccessible(true);
                    current = (DashboardComponent) field.get(current);
                } else {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return current;
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @PutMapping("/api/session/role")
    @Operation(summary = "Set the current user role for security testing (GUEST, EDITOR, ADMIN)")
    public ResponseEntity<Void> setSessionRole(@RequestBody RoleRequest request) {
        com.patternforge.inspector.ManualTracer.enter("ProxyController", "setSessionRole[" + request.role() + "]", "session");
        sessionService.setRole(request.role());
        com.patternforge.inspector.ManualTracer.exit("ProxyController", "setSessionRole[" + request.role() + "]", "session");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/audit-log")
    @Operation(summary = "Retrieve all logged audit events from smart proxies")
    public ResponseEntity<List<AuditEvent>> getAuditLog() {
        return ResponseEntity.ok(auditLogService.getLog());
    }

    @Schema(description = "Request body for setting the session role")
    public record RoleRequest(
        @Schema(description = "Role name (GUEST, EDITOR, ADMIN)", example = "ADMIN")
        String role
    ) {}
}
