package com.patternforge.service;

import com.patternforge.pattern.flyweight.WidgetFlyweightFactory;
import org.springframework.stereotype.Service;

@Service
public class MemoryEstimationService {
    private final WidgetFlyweightFactory factory;

    public MemoryEstimationService(WidgetFlyweightFactory factory) {
        this.factory = factory;
    }

    public record MemoryEstimate(
        int widgetCount,
        long withFlyweightBytes,
        long withoutFlyweightBytes,
        long savedBytes,
        double savingsPercent
    ) {}

    public MemoryEstimate estimate(int count) {
        long flyweightSize = 1024; // Estimated intrinsic size of WidgetFlyweight in bytes
        long contextSize = 128;    // Estimated extrinsic size of WidgetContext in bytes

        long withoutFlyweightBytes = (long) count * (flyweightSize + contextSize);

        int numFlyweights = Math.max(factory.getPool().size(), 1);
        long withFlyweightBytes = (numFlyweights * flyweightSize) + ((long) count * contextSize);

        long savedBytes = withoutFlyweightBytes - withFlyweightBytes;
        double savingsPercent = withoutFlyweightBytes > 0 
            ? ((double) savedBytes / withoutFlyweightBytes) * 100.0 
            : 0.0;

        return new MemoryEstimate(count, withFlyweightBytes, withoutFlyweightBytes, savedBytes, savingsPercent);
    }
}
