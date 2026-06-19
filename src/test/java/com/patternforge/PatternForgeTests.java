package com.patternforge;

import com.patternforge.domain.RenderResult;
import com.patternforge.domain.WidgetFlyweight;
import com.patternforge.pattern.adapter.LegacyGraphAdapter;
import com.patternforge.pattern.adapter.LegacyGraphLib;
import com.patternforge.pattern.composite.CircularReferenceException;
import com.patternforge.pattern.composite.ContainerNode;
import com.patternforge.pattern.composite.WidgetNode;
import com.patternforge.pattern.decorator.BorderDecorator;
import com.patternforge.pattern.decorator.ShadowDecorator;
import com.patternforge.pattern.flyweight.WidgetFlyweightFactory;
import com.patternforge.pattern.proxy.AccessControlProxy;
import com.patternforge.service.SessionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class PatternForgeTests {

    @Test
    void testCircularReferenceDetection() {
        ContainerNode containerA = new ContainerNode("A", "Container A");
        ContainerNode containerB = new ContainerNode("B", "Container B");
        ContainerNode containerC = new ContainerNode("C", "Container C");

        containerA.add(containerB);
        containerB.add(containerC);

        // A -> B -> C. Adding A to C should detect circular path A -> B -> C -> A
        assertThrows(CircularReferenceException.class, () -> {
            containerC.add(containerA);
        });
    }

    @Test
    void testRenderResultImmutability() {
        RenderResult initial = new RenderResult(
            "w1",
            "<div>Initial</div>",
            List.of("color: red"),
            Map.of("key", "val"),
            List.of("Log entry")
        );

        RenderResult modified = initial.withCssStyle("margin: 10px");

        // Verify initial is unchanged (immutable)
        assertNotSame(initial, modified);
        assertEquals(1, initial.cssStyles().size());
        assertEquals("color: red", initial.cssStyles().get(0));

        // Verify modified contains both styles
        assertEquals(2, modified.cssStyles().size());
        assertTrue(modified.cssStyles().contains("color: red"));
        assertTrue(modified.cssStyles().contains("margin: 10px"));
    }

    @Test
    void testDecoratorChainOrder() {
        WidgetNode widget = new WidgetNode("w1", "Test Widget", "TEXT", Map.of("content", "Text"));

        // Chain 1: Border outer, Shadow inner -> BorderDecorator(ShadowDecorator(widget))
        BorderDecorator chain1 = new BorderDecorator(new ShadowDecorator(widget));
        String html1 = chain1.render().htmlContent();

        // Chain 2: Shadow outer, Border inner -> ShadowDecorator(BorderDecorator(widget))
        ShadowDecorator chain2 = new ShadowDecorator(new BorderDecorator(widget));
        String html2 = chain2.render().htmlContent();

        // The HTML wrapping sequence must be different due to decorator call order
        assertNotEquals(html1, html2);
        assertTrue(html1.contains("class='decorator-border'><div style='box-shadow:"));
        assertTrue(html2.contains("class='decorator-shadow'><div style='border:"));
    }

    @Test
    void testFlyweightSharing() {
        WidgetFlyweightFactory factory = new WidgetFlyweightFactory();

        WidgetFlyweight flyweight1 = factory.getFlyweight("STOCK_TICKER");
        WidgetFlyweight flyweight2 = factory.getFlyweight("STOCK_TICKER");

        // Verify it is the exact same instance in memory (shared)
        assertSame(flyweight1, flyweight2);
    }

    @Test
    void testAdapterTranslation() {
        LegacyGraphLib mockLib = Mockito.mock(LegacyGraphLib.class);
        LegacyGraphAdapter adapter = new LegacyGraphAdapter("chart-1", "Test Chart", mockLib);

        List<Double> testData = List.of(1.5, 2.5, 3.0);
        adapter.setDataPoints(testData);

        // Verify adapter translates double List to double array and plots
        double[] expectedArray = new double[]{1.5, 2.5, 3.0};
        verify(mockLib).plot(Mockito.any(double[].class), Mockito.eq("Test Chart"));
    }

    @Test
    void testAccessControlProxy() {
        SessionService sessionService = new SessionService();
        WidgetNode sensitiveWidget = new WidgetNode(
            "widget-secret",
            "Secret Info",
            "TEXT",
            Map.of("content", "super-secret-data")
        );

        AccessControlProxy proxy = new AccessControlProxy(sensitiveWidget, "ADMIN", sessionService);

        // 1. Verify GUEST gets locked HTML (no secret content leaked)
        sessionService.setRole("GUEST");
        String guestHtml = proxy.render().htmlContent();
        assertTrue(guestHtml.contains("Access Denied"));
        assertFalse(guestHtml.contains("super-secret-data"));

        // 2. Verify ADMIN gets full access
        sessionService.setRole("ADMIN");
        String adminHtml = proxy.render().htmlContent();
        assertFalse(adminHtml.contains("Access Denied"));
        assertTrue(adminHtml.contains("super-secret-data"));
    }
}
