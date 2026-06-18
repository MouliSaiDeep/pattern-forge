package com.patternforge.pattern.proxy;

import com.patternforge.domain.DashboardComponent;

public interface HeavyWidget extends DashboardComponent {
    void loadData();
    ProxyState getProxyState();
    enum ProxyState { UNLOADED, LOADING, LOADED }
}
