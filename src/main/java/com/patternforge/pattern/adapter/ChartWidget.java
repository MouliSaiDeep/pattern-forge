package com.patternforge.pattern.adapter;

import com.patternforge.domain.DashboardComponent;
import java.util.List;

public interface ChartWidget extends DashboardComponent {
    void setDataPoints(List<Double> data);
    String getSource(); // "legacy", "old", or "new"
    String getAdapterTrace(); // returns log of adapter translation
}
