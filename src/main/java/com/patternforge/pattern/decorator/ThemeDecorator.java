package com.patternforge.pattern.decorator;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

public class ThemeDecorator extends WidgetDecorator {
    private final String themeName;

    public ThemeDecorator(DashboardComponent wrapped, String themeName) {
        super(wrapped);
        this.themeName = themeName;
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("ThemeDecorator", "render", getId());
        try {
            RenderResult result = wrapped.render();
            String themedHtml = String.format(
                "<div id='%s' class='theme-%s rounded-xl overflow-hidden' data-theme='%s'>%s</div>",
                getId(),
                themeName.toLowerCase(), themeName, result.htmlContent()
            );
            return result.withHtmlContent(themedHtml)
                         .withCssStyle("theme-" + themeName.toLowerCase())
                         .withTraceEntry("Applied ThemeDecorator: " + themeName + " to " + getId());
        } finally {
            com.patternforge.inspector.ManualTracer.exit("ThemeDecorator", "render", getId());
        }
    }

    public String getThemeName() {
        return themeName;
    }
}
