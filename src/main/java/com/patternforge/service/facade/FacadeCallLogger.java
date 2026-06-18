package com.patternforge.service.facade;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class FacadeCallLogger {
    private final List<String> logs = new CopyOnWriteArrayList<>();

    public void logCall(String call) {
        logs.add(call);
    }

    public void clear() {
        logs.clear();
    }

    public List<String> getLastCallLog() {
        return List.copyOf(logs);
    }
}
