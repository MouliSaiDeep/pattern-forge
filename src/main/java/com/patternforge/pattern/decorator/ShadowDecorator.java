package com.patternforge.pattern.decorator;

import com.patternforge.domain.DashboardComponent;
import com.patternforge.domain.RenderResult;

public class ShadowDecorator extends WidgetDecorator {

    public ShadowDecorator(DashboardComponent wrapped) {
        super(wrapped);
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("ShadowDecorator", "render", getId());
        try {
            RenderResult result = wrapped.render();
            String wrappedHtml = String.format(
                "<div id='%s' style='box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.4); border-radius: 12px; margin: 4px;' class='decorator-shadow'>%s</div>",
                getId(),
                result.htmlContent()
            );
            return result.withHtmlContent(wrappedHtml)
                         .withCssStyle("box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.4)")
                         .withTraceEntry("Applied ShadowDecorator to " + getId());
        } finally {
            com.patternforge.inspector.ManualTracer.exit("ShadowDecorator", "render", getId());
        }
    }
}
