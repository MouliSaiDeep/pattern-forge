package com.patternforge.pattern.proxy;

import com.patternforge.domain.RenderResult;
import java.util.List;
import java.util.Map;

public class LazyWidgetProxy implements HeavyWidget {
    private final String id;
    private final String name;
    private final VideoWidget realWidget;
    private ProxyState state = ProxyState.UNLOADED;

    public LazyWidgetProxy(String id, String name, String videoUrl) {
        this.id = id;
        this.name = name;
        this.realWidget = new VideoWidget(id, name, videoUrl);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "WIDGET";
    }

    @Override
    public String getPatternInfo() {
        return "Proxy Pattern - Virtual Proxy: Defers instantiation or loading of a resource until it is explicitly needed.";
    }

    @Override
    public void loadData() {
        load();
    }

    public synchronized void load() {
        com.patternforge.inspector.ManualTracer.enter("LazyWidgetProxy", "load", id);
        if (state != ProxyState.UNLOADED) {
            com.patternforge.inspector.ManualTracer.exit("LazyWidgetProxy", "load", id);
            return;
        }
        state = ProxyState.LOADING;
        // Start async loading thread
        new Thread(() -> {
            realWidget.loadData();
            synchronized (LazyWidgetProxy.this) {
                state = ProxyState.LOADED;
            }
        }).start();
        com.patternforge.inspector.ManualTracer.exit("LazyWidgetProxy", "load", id);
    }

    @Override
    public synchronized ProxyState getProxyState() {
        return state;
    }

    @Override
    public synchronized RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("LazyWidgetProxy", "render", id);
        RenderResult result;
        switch (state) {
            case UNLOADED:
                result = renderSkeleton();
                com.patternforge.inspector.ManualTracer.exit("LazyWidgetProxy", "render[UNLOADED]", id);
                return result;
            case LOADING:
                result = renderSpinner();
                com.patternforge.inspector.ManualTracer.exit("LazyWidgetProxy", "render[LOADING]", id);
                return result;
            case LOADED:
            default:
                result = realWidget.render().withTraceEntry("LazyWidgetProxy: delegated rendering to Real Subject.");
                com.patternforge.inspector.ManualTracer.exit("LazyWidgetProxy", "render[LOADED]", id);
                return result;
        }
    }

    private RenderResult renderSkeleton() {
        String html = String.format(
            "<div class='video-proxy border border-dashed border-slate-700 bg-slate-900/60 p-4 rounded-xl shadow w-full cursor-pointer hover:border-indigo-500/50 transition-colors' id='%s' onclick='loadLazyWidget(\"%s\")'>" +
            "  <div class='flex items-center justify-between mb-3'>" +
            "    <span class='text-xs font-semibold text-slate-500'>VIRTUAL PROXY - LAZY LOADER</span>" +
            "    <span class='px-2 py-0.5 text-[10px] bg-slate-800 text-slate-400 border border-slate-700 rounded-full font-bold'>UNLOADED</span>" +
            "  </div>" +
            "  <h4 class='text-sm font-bold text-slate-400 mb-2'>%s</h4>" +
            "  <div class='aspect-video rounded-lg bg-slate-800/40 border border-slate-800 flex flex-col justify-center items-center gap-2'>" +
            "    <span class='text-xs text-indigo-400 font-semibold underline'>Click to Load Asset</span>" +
            "    <span class='text-[10px] text-slate-500'>(Simulates 2s delay)</span>" +
            "  </div>" +
            "</div>",
            id, id, name
        );
        return new RenderResult(
            id, html, List.of(), Map.of("state", "UNLOADED"), List.of("LazyWidgetProxy: rendered placeholder skeleton (unloaded)")
        );
    }

    private RenderResult renderSpinner() {
        String html = String.format(
            "<div class='video-proxy border border-dashed border-indigo-500/40 bg-slate-900 p-4 rounded-xl shadow w-full' id='%s'>" +
            "  <div class='flex items-center justify-between mb-3'>" +
            "    <span class='text-xs font-semibold text-indigo-400'>VIRTUAL PROXY - LAZY LOADER</span>" +
            "    <span class='px-2 py-0.5 text-[10px] bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 rounded-full font-bold animate-pulse'>LOADING</span>" +
            "  </div>" +
            "  <h4 class='text-sm font-bold text-slate-300 mb-2'>%s</h4>" +
            "  <div class='aspect-video rounded-lg bg-slate-950 border border-slate-800 flex flex-col justify-center items-center gap-2'>" +
            "    <div class='h-8 w-8 rounded-full border-4 border-indigo-500/20 border-t-indigo-500 animate-spin'></div>" +
            "    <span class='text-[10px] text-slate-400'>Buffering stream data...</span>" +
            "  </div>" +
            "</div>",
            id, name
        );
        return new RenderResult(
            id, html, List.of(), Map.of("state", "LOADING"), List.of("LazyWidgetProxy: rendered loading spinner")
        );
    }
}
