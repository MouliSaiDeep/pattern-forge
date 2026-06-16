package com.patternforge.pattern.decorator;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

public class BorderDecorator extends WidgetDecorator {

    public BorderDecorator(DashboardComponent wrapped) {
        super(wrapped);
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("BorderDecorator", "render", getId());
        try {
            RenderResult result = wrapped.render();
            String wrappedHtml = String.format(
                "<div id='%s' style='border: 2px solid #3b82f6; border-radius: 12px; margin: 4px;' class='decorator-border'>%s</div>",
                getId(),
                result.htmlContent()
            );
            return result.withHtmlContent(wrappedHtml)
                         .withCssStyle("border: 2px solid #3b82f6")
                         .withTraceEntry("Applied BorderDecorator to " + getId());
        } finally {
            com.patternforge.inspector.ManualTracer.exit("BorderDecorator", "render", getId());
        }
    }
}
