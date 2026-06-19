package com.patternforge.service;

import com.patternforge.domain.WidgetContext;
import com.patternforge.domain.WidgetFlyweight;
import com.patternforge.pattern.adapter.LegacyGraphAdapter;
import com.patternforge.pattern.adapter.LegacyGraphLib;
import com.patternforge.pattern.bridge.TextBridgeWidget;
import com.patternforge.pattern.composite.WidgetNode;
import com.patternforge.pattern.flyweight.StockTickerWidget;
import com.patternforge.pattern.flyweight.WidgetFlyweightFactory;
import com.patternforge.pattern.proxy.AccessControlProxy;
import com.patternforge.pattern.proxy.AuditWidgetProxy;
import com.patternforge.pattern.proxy.LazyWidgetProxy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DemoInitializer implements CommandLineRunner {

    private final DashboardTreeService treeService;
    private final ChartAdapterService chartAdapterService;
    private final SessionService sessionService;
    private final AuditLogService auditLogService;
    private final BridgeService bridgeService;
    private final WidgetFlyweightFactory flyweightFactory;

    public DemoInitializer(DashboardTreeService treeService,
                           ChartAdapterService chartAdapterService,
                           SessionService sessionService,
                           AuditLogService auditLogService,
                           BridgeService bridgeService,
                           WidgetFlyweightFactory flyweightFactory) {
        this.treeService = treeService;
        this.chartAdapterService = chartAdapterService;
        this.sessionService = sessionService;
        this.auditLogService = auditLogService;
        this.bridgeService = bridgeService;
        this.flyweightFactory = flyweightFactory;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Add standard welcome widget (Composite Leaf)
        treeService.addWidget(
            "root",
            "TEXT",
            "Welcome to PatternForge",
            Map.of("content", "Explore structural patterns interactively in real time!", "bgColor", "from-indigo-600 to-indigo-900")
        );

        // 2. Add an Adapted Chart Widget (Adapter Pattern)
        chartAdapterService.createChartWidget("legacy", "Q2 Financial Analytics", List.of(12.5, 45.2, 33.1, 78.4));

        // 3. Add a Virtual Video Proxy wrapped inside an Audit Smart Proxy (Proxy Pattern)
        LazyWidgetProxy videoProxy = new LazyWidgetProxy("lazy-video-1", "Space Shuttle Launch Stream", "https://example.com/space.mp4");
        AuditWidgetProxy auditProxy = new AuditWidgetProxy(videoProxy, auditLogService);
        treeService.getTree().add(auditProxy);

        // 4. Add a Protection Proxy (Proxy Pattern)
        WidgetNode sensitiveWidget = new WidgetNode(
            "sensitive-1",
            "Confidential Admin Projections",
            "TEXT",
            Map.of("content", "Sales projections: $4.5M Q3 target", "bgColor", "from-rose-600 to-rose-900")
        );
        AccessControlProxy protectionProxy = new AccessControlProxy(sensitiveWidget, "ADMIN", sessionService);
        treeService.getTree().add(protectionProxy);

        // 5. Add a Bridge Widget (Bridge Pattern)
        TextBridgeWidget textBridge = new TextBridgeWidget(
            "bridge-text-1",
            "Subsystem Health",
            "All systems are operating within normal parameters.",
            bridgeService.getActiveRenderer()
        );
        treeService.getTree().add(textBridge);

        // 6. Add a Stock Ticker Widget (Flyweight Pattern)
        WidgetFlyweight stockFlyweight = flyweightFactory.getFlyweight("STOCK_TICKER");
        WidgetContext stockContext = new WidgetContext("stock-1", "row:1,col:2", 148.52, "AAPL Ticker", Map.of());
        StockTickerWidget stockWidget = new StockTickerWidget(stockFlyweight, stockContext);
        flyweightFactory.registerUsage("STOCK_TICKER");
        treeService.getTree().add(stockWidget);
    }
}
