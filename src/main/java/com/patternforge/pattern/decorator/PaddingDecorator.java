package com.patternforge.pattern.decorator;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

public class PaddingDecorator extends WidgetDecorator {

    public PaddingDecorator(DashboardComponent wrapped) {
        super(wrapped);
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("PaddingDecorator", "render", getId());
        try {
            RenderResult result = wrapped.render();
            String wrappedHtml = String.format(
                "<div id='%s' style='padding: 20px;' class='decorator-padding'>%s</div>",
                getId(),
                result.htmlContent()
            );
            return result.withHtmlContent(wrappedHtml)
                         .withCssStyle("padding: 20px")
                         .withTraceEntry("Applied PaddingDecorator to " + getId());
        } finally {
            com.patternforge.inspector.ManualTracer.exit("PaddingDecorator", "render", getId());
        }
    }
}
