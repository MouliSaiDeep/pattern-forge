package com.patternforge.pattern.bridge;

import java.util.List;
import java.util.Map;

public class SvgRenderer implements WidgetRenderer {
    @Override
    public String getRendererName() {
        return "SVG";
    }

    @Override
    public String renderChart(String title, List<Double> data) {
        StringBuilder bars = new StringBuilder();
        double max = data.stream().max(Double::compareTo).orElse(1.0);
        if (max <= 0) max = 1.0;
        
        int x = 10;
        int width = 30;
        int spacing = 15;
        
        for (Double val : data) {
            double height = (val / max) * 120;
            double y = 140 - height;
            bars.append(String.format(
                "<rect x='%d' y='%.1f' width='%d' height='%.1f' fill='#f43f5e' rx='3'/>",
                x, y, width, height
            ));
            x += width + spacing;
        }

        String svg = String.format(
            "<svg viewBox='0 0 400 160' class='w-full h-32 bg-slate-900 rounded-lg border border-slate-800'>" +
            "  <text x='10' y='25' fill='#f43f5e' font-size='12' font-family='sans-serif' font-weight='bold'>%s</text>" +
            "  %s" +
            "  <line x1='10' y1='140' x2='390' y2='140' stroke='#334155' stroke-width='2'/>" +
            "</svg>",
            title, bars
        );
        return wrapInContainer(svg);
    }

    @Override
    public String renderTable(String title, List<Map<String, Object>> rows) {
        StringBuilder tableRows = new StringBuilder();
        int y = 50;
        for (Map<String, Object> row : rows) {
            int x = 20;
            for (Object val : row.values()) {
                tableRows.append(String.format(
                    "<text x='%d' y='%d' fill='#cbd5e1' font-size='10' font-family='sans-serif'>%s</text>",
                    x, y, val
                ));
                x += 100;
            }
            tableRows.append(String.format("<line x1='10' y1='%d' x2='390' y2='%d' stroke='#334155' stroke-dasharray='3'/>", y + 5, y + 5));
            y += 25;
        }
        String svg = String.format(
            "<svg viewBox='0 0 400 160' class='w-full h-32 bg-slate-900 rounded-lg border border-slate-800'>" +
            "  <text x='10' y='25' fill='#f43f5e' font-size='12' font-family='sans-serif' font-weight='bold'>%s</text>" +
            "  %s" +
            "</svg>",
            title, tableRows
        );
        return wrapInContainer(svg);
    }

    @Override
    public String renderText(String title, String content) {
        String svg = String.format(
            "<svg viewBox='0 0 400 160' class='w-full h-32 bg-slate-900 rounded-lg border border-slate-800'>" +
            "  <text x='10' y='25' fill='#f43f5e' font-size='12' font-family='sans-serif' font-weight='bold'>%s</text>" +
            "  <rect x='10' y='45' width='380' height='90' fill='#1e293b' rx='6'/>" +
            "  <text x='20' y='75' fill='#e2e8f0' font-size='11' font-family='sans-serif'>%s</text>" +
            "</svg>",
            title, content.length() > 45 ? content.substring(0, 42) + "..." : content
        );
        return wrapInContainer(svg);
    }

    private String wrapInContainer(String svg) {
        return String.format(
            "<div class='bridge-svg-card p-4 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 w-full'>" +
            "  <div class='text-[10px] text-slate-500 mb-2'>SVG RENDERER OUTPUT</div>" +
            "  %s" +
            "</div>",
            svg
        );
    }
}
