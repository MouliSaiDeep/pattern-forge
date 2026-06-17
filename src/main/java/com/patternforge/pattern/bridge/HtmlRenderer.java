package com.patternforge.pattern.bridge;

import java.util.List;
import java.util.Map;

public class HtmlRenderer implements WidgetRenderer {
    @Override
    public String getRendererName() {
        return "HTML";
    }

    @Override
    public String renderChart(String title, List<Double> data) {
        StringBuilder bars = new StringBuilder();
        double max = data.stream().max(Double::compareTo).orElse(1.0);
        if (max <= 0) max = 1.0;
        for (Double val : data) {
            double percent = (val / max) * 100;
            bars.append(String.format(
                "<div style='height: %.1f%%' class='w-6 bg-indigo-500 rounded-t' title='%.2f'></div>",
                percent, val
            ));
        }
        return String.format(
            "<div class='bridge-html-card p-4 rounded-xl bg-slate-950 border border-slate-800 text-slate-200'>" +
            "  <h5 class='font-bold mb-2 text-indigo-400'>%s (HTML Layout)</h5>" +
            "  <div class='flex items-end gap-2 h-20 bg-slate-900/60 p-2 rounded border border-slate-800/80'>" +
            "    %s" +
            "  </div>" +
            "</div>",
            title, bars
        );
    }

    @Override
    public String renderTable(String title, List<Map<String, Object>> rows) {
        StringBuilder tableRows = new StringBuilder();
        for (Map<String, Object> row : rows) {
            tableRows.append("<tr>");
            for (Object val : row.values()) {
                tableRows.append(String.format("<td class='px-4 py-2 border-b border-slate-800 text-xs text-slate-300'>%s</td>", val));
            }
            tableRows.append("</tr>");
        }
        return String.format(
            "<div class='bridge-html-card p-4 rounded-xl bg-slate-950 border border-slate-800 text-slate-200'>" +
            "  <h5 class='font-bold mb-2 text-indigo-400'>%s (HTML Table)</h5>" +
            "  <table class='w-full text-left border-collapse bg-slate-900/60 rounded overflow-hidden'>" +
            "    <tbody>%s</tbody>" +
            "  </table>" +
            "</div>",
            title, tableRows
        );
    }

    @Override
    public String renderText(String title, String content) {
        return String.format(
            "<div class='bridge-html-card p-4 rounded-xl bg-slate-950 border border-slate-800 text-slate-200'>" +
            "  <h5 class='font-bold mb-1 text-indigo-400'>%s (HTML Text)</h5>" +
            "  <p class='text-sm text-slate-300'>%s</p>" +
            "</div>",
            title, content
        );
    }
}
