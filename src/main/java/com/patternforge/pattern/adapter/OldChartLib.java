package com.patternforge.pattern.adapter;

import java.util.Properties;

public class OldChartLib {
    private String csv;
    private Properties properties = new Properties();

    public void loadData(String csvData) {
        this.csv = csvData;
    }

    public void configure(Properties props) {
        if (props != null) {
            this.properties.putAll(props);
        }
    }

    public String getRenderedOutput() {
        String title = properties.getProperty("title", "Old Chart");
        String theme = properties.getProperty("theme", "classic");
        return String.format(
            "<div class='old-chart-lib-container border-2 border-amber-500/40 p-4 rounded-xl bg-slate-950/60 font-mono text-xs text-amber-400 w-full'>" +
            "  <div class='flex justify-between border-b border-amber-500/20 pb-1 mb-2'>" +
            "    <span class='font-bold'>OldChartLib (Adaptee)</span>" +
            "    <span>Theme: %s</span>" +
            "  </div>" +
            "  <div class='text-sm font-semibold mb-1 text-slate-200'>Title: %s</div>" +
            "  <div>Raw CSV String Loaded: <span class='text-white'>%s</span></div>" +
            "  <div class='mt-2 h-16 flex items-end gap-1 bg-amber-950/20 p-2 rounded border border-amber-500/20'>" +
            "    %s" +
            "  </div>" +
            "</div>",
            theme, title, csv != null ? csv : "N/A", generateBars()
        );
    }

    private String generateBars() {
        if (csv == null || csv.isEmpty()) return "No data";
        String[] parts = csv.split(",");
        double max = 1.0;
        double[] values = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                values[i] = Double.parseDouble(parts[i].trim());
                if (values[i] > max) max = values[i];
            } catch (NumberFormatException e) {
                values[i] = 0.0;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (double val : values) {
            double percent = (val / max) * 100;
            sb.append(String.format("<div style='height: %.1f%%' class='w-full bg-amber-500 rounded-t' title='%.2f'></div>", percent, val));
        }
        return sb.toString();
    }
}
