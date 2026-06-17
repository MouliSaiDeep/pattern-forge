package com.patternforge.pattern.adapter;

import java.util.Arrays;

public class LegacyGraphLib {
    private double[] lastData;
    private String lastTitle = "Untitled Chart";
    private int width = 400;
    private int height = 300;

    public void plot(double[] dataPoints, String title) {
        this.lastData = dataPoints;
        this.lastTitle = title;
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String exportAsHtml() {
        String dataStr = lastData != null ? Arrays.toString(lastData) : "[]";
        return String.format(
            "<div class='legacy-chart-lib-container border-2 border-red-500/40 p-4 rounded-xl bg-slate-950/60 font-mono text-xs text-red-400 w-full'>" +
            "  <div class='flex justify-between border-b border-red-500/20 pb-1 mb-2'>" +
            "    <span class='font-bold'>LegacyGraphLib (Adaptee)</span>" +
            "    <span>Resolution: %dx%d</span>" +
            "  </div>" +
            "  <div class='text-sm font-semibold mb-1 text-slate-200'>Title: %s</div>" +
            "  <div>Raw Double Array Plotted: <span class='text-white'>%s</span></div>" +
            "  <div class='mt-2 h-16 flex items-end gap-1 bg-red-950/20 p-2 rounded border border-red-500/20'>" +
            "    %s" +
            "  </div>" +
            "</div>",
            width, height, lastTitle, dataStr, generateBars()
        );
    }

    private String generateBars() {
        if (lastData == null || lastData.length == 0) return "No data";
        double max = Arrays.stream(lastData).max().orElse(1.0);
        if (max <= 0) max = 1.0;
        StringBuilder sb = new StringBuilder();
        for (double val : lastData) {
            double percent = (val / max) * 100;
            sb.append(String.format("<div style='height: %.1f%%' class='w-full bg-red-500 rounded-t' title='%.2f'></div>", percent, val));
        }
        return sb.toString();
    }
}
