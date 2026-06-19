package com.patternforge.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpSession;

@Service
public class SessionService {
    private static final String ROLE_KEY = "currentRole";
    private String fallbackRole = "GUEST";

    private HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            return null;
        }
        return attr.getRequest().getSession(true);
    }

    public void setRole(String role) {
        HttpSession session = getSession();
        String upperRole = role != null ? role.toUpperCase() : "GUEST";
        if (session != null) {
            session.setAttribute(ROLE_KEY, upperRole);
        } else {
            fallbackRole = upperRole;
        }
    }

    public String getCurrentRole() {
        HttpSession session = getSession();
        if (session != null) {
            String role = (String) session.getAttribute(ROLE_KEY);
            return role != null ? role : "GUEST";
        }
        return fallbackRole;
    }

    public void clear() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(ROLE_KEY);
        } else {
            fallbackRole = "GUEST";
        }
    }
}
