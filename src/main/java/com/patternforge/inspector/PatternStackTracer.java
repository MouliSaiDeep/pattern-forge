package com.patternforge.inspector;

import com.patternforge.domain.CallChainEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatternStackTracer {
    private final SimpMessagingTemplate messagingTemplate;
    private final ThreadLocal<Deque<String>> callStack = ThreadLocal.withInitial(ArrayDeque::new);

    // Track pending AOP enter events to deduplicate when manual trace is used
    private final ThreadLocal<Deque<CallChainEvent>> pendingAopEnters = ThreadLocal.withInitial(ArrayDeque::new);

    public PatternStackTracer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void push(CallChainEvent event) {
        String source = (String) event.metadata().getOrDefault("source", "manual");
        if ("aop".equals(source)) {
            // Buffer the AOP event
            pendingAopEnters.get().push(event);
        } else {
            // Flush any older/outer pending AOP events first to preserve order
            flushPendingAopEnters(event.className(), event.methodName());
            
            // Remove the matching AOP enter event from pending stack if it matches
            CallChainEvent matchingAop = pendingAopEnters.get().peek();
            if (matchingAop != null && matchingAop.className().equals(event.className())) {
                pendingAopEnters.get().pop(); // Discard the duplicate AOP enter
            }
            
            // Push manual event
            callStack.get().push(event.className() + "." + event.methodName());
            messagingTemplate.convertAndSend("/topic/call-chain", event);
        }
    }

    public void pop(CallChainEvent event) {
        String source = (String) event.metadata().getOrDefault("source", "manual");
        if ("aop".equals(source)) {
            // Check if this AOP exit has a corresponding pending AOP enter
            CallChainEvent pendingEnter = pendingAopEnters.get().peek();
            if (pendingEnter != null && pendingEnter.className().equals(event.className())) {
                // The method had no manual tracing! Send the buffered enter first, then the exit.
                pendingAopEnters.get().pop();
                
                // Push AOP enter to callStack and send
                callStack.get().push(pendingEnter.className() + "." + pendingEnter.methodName());
                messagingTemplate.convertAndSend("/topic/call-chain", pendingEnter);
                
                // Pop AOP exit and send
                callStack.get().poll();
                messagingTemplate.convertAndSend("/topic/call-chain", event);
            }
            // If the pending enter was already consumed, it means manual trace handled it, so skip this AOP exit!
        } else {
            // Pop manual event
            callStack.get().poll();
            messagingTemplate.convertAndSend("/topic/call-chain", event);
        }
    }

    private void flushPendingAopEnters(String currentClass, String currentMethod) {
        Deque<CallChainEvent> pending = pendingAopEnters.get();
        if (pending.isEmpty()) {
            return;
        }
        
        // We want to flush all pending entries EXCEPT the one matching the current class (if any)
        List<CallChainEvent> toFlush = new ArrayList<>();
        Iterator<CallChainEvent> it = pending.iterator();
        while (it.hasNext()) {
            CallChainEvent p = it.next();
            if (p.className().equals(currentClass)) {
                // Stop at the current class (it will be matched/popped next)
                break;
            }
            toFlush.add(p);
        }
        
        // Send them in FIFO order (oldest first, so we reverse the list since we walked from top to bottom)
        Collections.reverse(toFlush);
        for (CallChainEvent p : toFlush) {
            pending.remove(p);
            callStack.get().push(p.className() + "." + p.methodName());
            messagingTemplate.convertAndSend("/topic/call-chain", p);
        }
    }

    public List<String> getCurrentStack() {
        return new ArrayList<>(callStack.get());
    }
}
