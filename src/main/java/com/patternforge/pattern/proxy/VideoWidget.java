package com.patternforge.pattern.proxy;

import com.patternforge.domain.RenderResult;
import java.util.List;
import java.util.Map;

public class VideoWidget implements HeavyWidget {
    private final String id;
    private final String name;
    private final String videoUrl;
    private ProxyState state = ProxyState.UNLOADED;

    public VideoWidget(String id, String name, String videoUrl) {
        this.id = id;
        this.name = name;
        this.videoUrl = videoUrl;
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
        return "Proxy Pattern - Real Subject: Performs the heavy video processing and rendering once data is loaded.";
    }

    @Override
    public void loadData() {
        com.patternforge.inspector.ManualTracer.enter("VideoWidget", "loadData", id);
        state = ProxyState.LOADING;
        try {
            Thread.sleep(2000); // Simulate expensive work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        state = ProxyState.LOADED;
        com.patternforge.inspector.ManualTracer.exit("VideoWidget", "loadData", id);
    }

    @Override
    public ProxyState getProxyState() {
        return state;
    }

    @Override
    public RenderResult render() {
        com.patternforge.inspector.ManualTracer.enter("VideoWidget", "render", id);
        String html = String.format(
            "<div class='video-widget border border-indigo-500 bg-slate-950 p-4 rounded-xl shadow-lg w-full'>" +
            "  <div class='flex items-center justify-between mb-3'>" +
            "    <span class='text-xs font-semibold text-indigo-400'>REAL SUBJECT - VIDEO PLAYER</span>" +
            "    <span class='px-2 py-0.5 text-[10px] bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-full font-bold'>LOADED</span>" +
            "  </div>" +
            "  <h4 class='text-sm font-bold text-slate-200 mb-2'>%s</h4>" +
            "  <div class='relative rounded-lg overflow-hidden bg-slate-900 border border-slate-800 aspect-video flex flex-col justify-center items-center gap-2'>" +
            "    <div class='absolute inset-0 bg-gradient-to-tr from-slate-900 via-indigo-950/20 to-slate-900 opacity-60'></div>" +
            "    <div class='h-12 w-12 rounded-full bg-indigo-500/80 flex items-center justify-center text-white cursor-pointer shadow-lg hover:scale-110 transition-transform'>" +
            "      <svg class='w-6.5 h-6.5 fill-current ml-1' viewBox='0 0 24 24' style='width:24px;height:24px;'>" +
            "        <path d='M8 5v14l11-7z' fill='currentColor'/>" +
            "      </svg>" +
            "    </div>" +
            "    <span class='text-xs text-indigo-300 font-medium z-10'>Playing stream: %s</span>" +
            "  </div>" +
            "</div>",
            name, videoUrl
        );

        RenderResult result = new RenderResult(
            id,
            html,
            List.of(),
            Map.of("loaded", "true", "videoUrl", videoUrl),
            List.of("VideoWidget: rendered active video content for " + name)
        );
        com.patternforge.inspector.ManualTracer.exit("VideoWidget", "render", id);
        return result;
    }
}
