import React, { useState, useEffect, useRef } from 'react';
import { 
  Layers, ChevronRight, ChevronLeft, X, AlertTriangle, Lock, Check
} from 'lucide-react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title as ChartTitle,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ChartTitle,
  Tooltip,
  Legend,
  Filler
);

declare global {
  interface Window {
    SockJS: any;
    Stomp: any;
    loadLazyWidget?: (id: string) => void;
  }
}

interface RenderResult {
  widgetId: string;
  htmlContent: string;
  cssStyles: string[];
  metadata: Record<string, string>;
  renderTrace: string[];
}

interface DashboardComponent {
  id: string;
  name: string;
  type: string;
  children?: DashboardComponent[];
  widgetType?: string;
  config?: Record<string, any>;
}

interface CallChainEvent {
  className: string;
  methodName: string;
  widgetId: string;
  eventType: string;
  timestamp: number;
  metadata: Record<string, any>;
}

interface AuditEvent {
  userId: string;
  widgetId: string;
  timestamp: number;
  method: string;
}

export default function App() {
  const [viewportWidth, setViewportWidth] = useState<number>(window.innerWidth);
  const [activeTab, setActiveTab] = useState<string>('dashboard');
  const [tree, setTree] = useState<DashboardComponent | null>(null);
  const [canvasHtml, setCanvasHtml] = useState<string>('');
  const [traceLogs, setTraceLogs] = useState<CallChainEvent[]>([]);
  const [isInspectorOpen, setIsInspectorOpen] = useState<boolean>(true);
  const [websocketConnected, setWebsocketConnected] = useState<boolean>(false);
  const logsEndRef = useRef<HTMLDivElement>(null);

  // Modal State for adding nodes
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalParentId, setModalParentId] = useState('root');
  const [modalNodeType, setModalNodeType] = useState<'container' | 'widget'>('container');
  const [modalNodeName, setModalNodeName] = useState('');
  const [modalWidgetType, setModalWidgetType] = useState('TEXT');
  const [modalWidgetContent, setModalWidgetContent] = useState('');

  // Decorator Screen States
  const [selectedDecoratorWidget, setSelectedDecoratorWidget] = useState<string>('');
  const [decoratorStack, setDecoratorStack] = useState<string[]>([]);
  const [themeName, setThemeName] = useState('Classic');

  // Adapter Screen States
  const [adapterSource, setAdapterSource] = useState('legacy');
  const [adapterTitle, setAdapterTitle] = useState('System Utilization');
  const [adapterDataPoints, setAdapterDataPoints] = useState('24.2, 55.1, 78.4, 91.0');
  const [adapterTraceHtml, setAdapterTraceHtml] = useState<string>('');
  const [adapterTraceMsg, setAdapterTraceMsg] = useState('');

  // Facade Screen States
  const [facadeName, setFacadeName] = useState('Production Facade');
  const [facadeTheme, setFacadeTheme] = useState('dark');
  const [facadeCols, setFacadeCols] = useState(4);
  const [facadeRows, setFacadeRows] = useState(3);
  const [facadeThemeId, setFacadeThemeId] = useState('root');
  const [facadeThemeName, setFacadeThemeName] = useState('classic');
  const [facadeCallLogs, setFacadeCallLogs] = useState<string[]>([]);
  const [facadeToast, setFacadeToast] = useState<string | null>(null);

  // Proxy Screen States
  const [currentRole, setCurrentRole] = useState('GUEST');
  const [auditLogs, setAuditLogs] = useState<AuditEvent[]>([]);
  const [proxyStates, setProxyStates] = useState<Record<string, string>>({});

  // Bridge Screen States
  const [activeRenderer, setActiveRenderer] = useState('html');
  const [bridgeClassCount, setBridgeClassCount] = useState<{ withoutBridge: number; withBridge: number } | null>(null);

  // Flyweight Screen States
  const [flyweightCount, setFlyweightCount] = useState(1000);
  const [flyweightPool, setFlyweightPool] = useState<any[]>([]);
  const [flyweightEstimates, setFlyweightEstimates] = useState<any>(null);

  // Composite Specific state
  const [compositeError, setCompositeError] = useState<string | null>(null);
  const [renderLoading, setRenderLoading] = useState(false);
  const [previewBorderSuccess, setPreviewBorderSuccess] = useState(false);

  // Viewport resize listener
  useEffect(() => {
    const handleResize = () => setViewportWidth(window.innerWidth);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // WebSockets setup
  useEffect(() => {
    let stompClient: any = null;
    const socket = new window.SockJS('/ws');
    stompClient = window.Stomp.over(socket);
    stompClient.debug = null; // suppress logs

    stompClient.connect({}, () => {
      setWebsocketConnected(true);
      stompClient.subscribe('/topic/call-chain', (message: any) => {
        const event: CallChainEvent = JSON.parse(message.body);
        setTraceLogs(prev => [...prev, event]);
      });
    }, (err: any) => {
      console.error("STOMP connection error", err);
      setWebsocketConnected(false);
    });

    return () => {
      if (stompClient) stompClient.disconnect();
    };
  }, []);

  // Sync scroll on logs
  useEffect(() => {
    logsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [traceLogs]);

  // Initial data loading
  useEffect(() => {
    refreshTree();
    fetchClassCounts();
    fetchFlyweightPool();
    fetchMemoryEstimates(1000);
    fetchAuditLogs();
    fetchFacadeLogs();
  }, []);

  const refreshTree = () => {
    setRenderLoading(true);
    fetch('/api/dashboard/tree')
      .then(res => res.json())
      .then(data => setTree(data))
      .catch(err => console.error(err));

    fetch('/api/dashboard/render-all', { method: 'POST' })
      .then(res => res.json())
      .then((data: RenderResult) => {
        setCanvasHtml(data.htmlContent);
        setRenderLoading(false);
        setPreviewBorderSuccess(true);
        setTimeout(() => setPreviewBorderSuccess(false), 300);
      })
      .catch(err => {
        console.error(err);
        setRenderLoading(false);
      });
  };

  const getWidgetsList = (node: DashboardComponent | null): DashboardComponent[] => {
    if (!node) return [];
    const list: DashboardComponent[] = [];
    const traverse = (n: DashboardComponent) => {
      if (n.type === 'WIDGET') list.push(n);
      if (n.children) n.children.forEach(traverse);
    };
    traverse(node);
    return list;
  };

  const widgetsList = getWidgetsList(tree);

  // Auto-select first decorator widget
  useEffect(() => {
    if (widgetsList.length > 0) {
      const isValid = widgetsList.some(w => w.id === selectedDecoratorWidget);
      if (!isValid) {
        setSelectedDecoratorWidget(widgetsList[0].id);
      }
    }
  }, [widgetsList, selectedDecoratorWidget]);

  // Fetch stack when decorator widget selection changes
  useEffect(() => {
    if (selectedDecoratorWidget) {
      fetch(`/api/widget/${selectedDecoratorWidget}/decorators`)
        .then(res => res.json())
        .then(data => setDecoratorStack(data))
        .catch(err => console.error(err));
    }
  }, [selectedDecoratorWidget]);

  // Fetch states of proxy components
  useEffect(() => {
    if (activeTab === 'proxy') {
      ['lazy-video-1', 'sensitive-1'].forEach(id => {
        fetch(`/api/widget/${id}/proxy-state`)
          .then(res => res.json())
          .then(data => {
            setProxyStates(prev => ({ ...prev, [id]: data.state }));
          });
      });
    }
  }, [activeTab, canvasHtml]);

  // Composite Node deletions and creations
  const handleDeleteNode = (id: string) => {
    fetch(`/api/dashboard/node/${id}`, { method: 'DELETE' })
      .then(() => refreshTree());
  };

  const handleCreateNode = () => {
    setCompositeError(null);
    const url = modalNodeType === 'container' ? '/api/dashboard/container' : '/api/dashboard/widget';
    const payload = modalNodeType === 'container' 
      ? { parentId: modalParentId, name: modalNodeName }
      : { 
          parentId: modalParentId, 
          name: modalNodeName, 
          widgetType: modalWidgetType, 
          config: { content: modalWidgetContent, bgColor: 'bg-pf-surface' } 
        };

    fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    }).then(res => {
      if (res.ok) {
        setIsModalOpen(false);
        refreshTree();
      } else {
        res.json().then(err => setCompositeError(err.message || "Failed to create node."));
      }
    });
  };

  const handleTreeDrop = (parentId: string, childId: string) => {
    setCompositeError(null);
    fetch(`/api/dashboard/${parentId}/add/${childId}`, { method: 'PUT' })
      .then(res => {
        if (res.ok) {
          refreshTree();
        } else {
          res.json().then(err => {
            setCompositeError(`Circular Reference Blocked: ${err.message}`);
          });
        }
      });
  };

  // Decorator actions
  const handleAddDecorator = (decoratorType: string) => {
    const config = decoratorType === 'THEME' ? { themeName } : {};
    fetch(`/api/widget/${selectedDecoratorWidget}/decorators`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type: decoratorType, config })
    }).then(() => {
      refreshTree();
      fetch(`/api/widget/${selectedDecoratorWidget}/decorators`)
        .then(res => res.json())
        .then(data => setDecoratorStack(data));
    });
  };

  const handleRemoveDecorator = (type: string) => {
    fetch(`/api/widget/${selectedDecoratorWidget}/decorators/${type}`, {
      method: 'DELETE'
    }).then(() => {
      refreshTree();
      fetch(`/api/widget/${selectedDecoratorWidget}/decorators`)
        .then(res => res.json())
        .then(data => setDecoratorStack(data));
    });
  };

  const handleResetDecorators = () => {
    fetch(`/api/widget/${selectedDecoratorWidget}/decorators`, { method: 'DELETE' })
      .then(() => {
        refreshTree();
        setDecoratorStack([]);
      });
  };

  const handleReorderDecorators = (newStack: string[]) => {
    fetch(`/api/widget/${selectedDecoratorWidget}/decorators/reorder`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderedTypes: newStack })
    }).then(() => {
      refreshTree();
      fetch(`/api/widget/${selectedDecoratorWidget}/decorators`)
        .then(res => res.json())
        .then(data => setDecoratorStack(data));
    });
  };

  // Adapter actions
  const handleCreateChart = (e: React.FormEvent) => {
    e.preventDefault();
    const dataPoints = adapterDataPoints.split(',').map(x => parseFloat(x.trim())).filter(x => !isNaN(x));
    fetch('/api/widget/chart', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ source: adapterSource, title: adapterTitle, dataPoints })
    })
      .then(res => res.json())
      .then(widget => {
        setAdapterTraceHtml(widget.renderResult.htmlContent);
        fetch(`/api/widget/${widget.id}/adapter-trace`)
          .then(res => res.json())
          .then(traceData => setAdapterTraceMsg(traceData.trace));
        refreshTree();
      });
  };

  // Facade actions
  const handleFacadeCreate = (e: React.FormEvent) => {
    e.preventDefault();
    fetch('/api/facade/create-dashboard', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: facadeName, columns: facadeCols, rows: facadeRows, theme: facadeTheme })
    })
      .then(res => res.json())
      .then(() => {
        fetchFacadeLogs();
        triggerToast("Dashboard created successfully via Facade orchestrator.");
      });
  };

  const handleFacadeTheme = (e: React.FormEvent) => {
    e.preventDefault();
    fetch('/api/facade/apply-theme', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ dashboardId: facadeThemeId, themeName: facadeThemeName })
    })
      .then(res => res.json())
      .then(() => {
        fetchFacadeLogs();
        triggerToast("Theme applied successfully via Facade orchestrator.");
      });
  };

  const triggerToast = (msg: string) => {
    setFacadeToast(msg);
    setTimeout(() => setFacadeToast(null), 3000);
  };

  const fetchFacadeLogs = () => {
    fetch('/api/facade/call-log')
      .then(res => res.json())
      .then(logs => setFacadeCallLogs(logs));
  };

  // Proxy actions
  const handleRoleChange = (role: string) => {
    setCurrentRole(role);
    fetch('/api/session/role', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ role })
    }).then(() => {
      refreshTree();
      fetchAuditLogs();
    });
  };

  const handleLoadProxyWidget = (id: string) => {
    // Trigger the load
    fetch(`/api/widget/${id}/load`, { method: 'POST' }).then(() => {
      // Update state to LOADING immediately in the canvas by calling a partial re-render
      fetch(`/api/dashboard/render-all`, { method: 'POST' })
        .then(res => res.json())
        .then((data: RenderResult) => {
          setCanvasHtml(data.htmlContent);
        });

      // Poll until state becomes LOADED, then do a final refresh
      let attempts = 0;
      const maxAttempts = 10; // 5 seconds total
      const poll = setInterval(() => {
        attempts++;
        fetch(`/api/widget/${id}/proxy-state`)
          .then(res => res.json())
          .then(data => {
            setProxyStates(prev => ({ ...prev, [id]: data.state }));
            if (data.state === 'LOADED') {
              clearInterval(poll);
              // Final render after fully loaded
              fetch(`/api/dashboard/render-all`, { method: 'POST' })
                .then(res => res.json())
                .then((renderData: RenderResult) => {
                  setCanvasHtml(renderData.htmlContent);
                  fetchAuditLogs();
                });
              refreshTree();
            } else if (attempts >= maxAttempts) {
              clearInterval(poll);
              refreshTree();
              fetchAuditLogs();
            }
          })
          .catch(() => {
            clearInterval(poll);
          });
      }, 500);
    });
  };

  // Expose loadLazyWidget on window so inline onclick handlers in dangerouslySetInnerHTML can call it
  useEffect(() => {
    window.loadLazyWidget = (id: string) => {
      handleLoadProxyWidget(id);
    };
    return () => {
      delete window.loadLazyWidget;
    };
  });  // no deps — re-bind on every render so it always has the latest closure



  const fetchAuditLogs = () => {
    fetch('/api/audit-log')
      .then(res => res.json())
      .then(logs => setAuditLogs(logs));
  };

  // Fetch facade or audit logs on tab switch or active log events
  useEffect(() => {
    if (activeTab === 'facade') {
      fetchFacadeLogs();
    }
  }, [activeTab]);

  useEffect(() => {
    if (activeTab === 'proxy') {
      fetchAuditLogs();
    }
  }, [activeTab, canvasHtml]);

  // Bridge actions
  const handleRendererChange = (renderer: string) => {
    setActiveRenderer(renderer);
    fetch('/api/dashboard/renderer', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ renderer })
    }).then(() => refreshTree());
  };

  const fetchClassCounts = () => {
    fetch('/api/bridge/class-count')
      .then(res => res.json())
      .then(data => setBridgeClassCount(data));
  };

  // Flyweight actions
  const handleFlyweightGenerate = () => {
    fetch('/api/flyweight/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ count: flyweightCount, widgetType: "STOCK_TICKER" })
    }).then(() => {
      fetchFlyweightPool();
      fetchMemoryEstimates(flyweightCount);
    });
  };

  const getFlyweightChartData = () => {
    const baseCounts = [100, 1000, 2500, 5000, 7500, 10000];
    const counts = [...baseCounts];
    if (!counts.includes(flyweightCount)) {
      counts.push(flyweightCount);
      counts.sort((a, b) => a - b);
    }

    const numFlyweights = flyweightPool.length || 1;
    const withoutData = counts.map(c => (c * (1024 + 128)) / 1024);
    const withData = counts.map(c => ((numFlyweights * 1024) + (c * 128)) / 1024);

    return {
      labels: counts.map(c => c.toString()),
      datasets: [
        {
          label: 'Without Flyweight',
          data: withoutData,
          borderColor: '#F85149',
          backgroundColor: 'rgba(248, 81, 73, 0.05)',
          borderWidth: 2,
          fill: true,
          pointRadius: counts.map(c => c === flyweightCount ? 6 : 3),
          pointBackgroundColor: counts.map(c => c === flyweightCount ? '#F85149' : '#8B949E'),
          pointBorderWidth: counts.map(c => c === flyweightCount ? 3 : 1),
          pointBorderColor: counts.map(c => c === flyweightCount ? '#FFFFFF' : '#30363D'),
        },
        {
          label: 'With Flyweight',
          data: withData,
          borderColor: '#3FB950',
          backgroundColor: 'rgba(63, 185, 80, 0.05)',
          borderWidth: 2,
          fill: true,
          pointRadius: counts.map(c => c === flyweightCount ? 6 : 3),
          pointBackgroundColor: counts.map(c => c === flyweightCount ? '#3FB950' : '#8B949E'),
          pointBorderWidth: counts.map(c => c === flyweightCount ? 3 : 1),
          pointBorderColor: counts.map(c => c === flyweightCount ? '#FFFFFF' : '#30363D'),
        }
      ]
    };
  };

  const fetchFlyweightPool = () => {
    fetch('/api/flyweight/pool')
      .then(res => res.json())
      .then(data => setFlyweightPool(data));
  };

  const fetchMemoryEstimates = (count: number) => {
    fetch(`/api/flyweight/memory-estimate?count=${count}`)
      .then(res => res.json())
      .then(data => setFlyweightEstimates(data));
  };

  // Tree visualizer component
  const renderTree = (node: DashboardComponent) => {
    const isContainer = node.type === 'CONTAINER';
    return (
      <div 
        key={node.id} 
        className="pl-5 py-1 text-[13px] select-none"
        draggable
        onDragStart={(e) => {
          e.dataTransfer.setData("text/plain", node.id);
          e.stopPropagation();
        }}
        onDragOver={(e) => {
          if (isContainer) e.preventDefault();
        }}
        onDrop={(e) => {
          e.preventDefault();
          e.stopPropagation();
          const childId = e.dataTransfer.getData("text/plain");
          if (childId && childId !== node.id) {
            handleTreeDrop(node.id, childId);
          }
        }}
      >
        <div className="flex items-center justify-between group py-1 pr-3">
          <span className="flex items-center gap-2">
            {isContainer ? '📁' : '📄'}
            <span className="text-pf-text font-mono font-medium">{node.name}</span>
            <span className="text-[12px] text-pf-text-muted font-mono ml-2">{node.id}</span>
          </span>
          {node.id !== 'root' && (
            <button 
              onClick={() => handleDeleteNode(node.id)}
              className="text-[12px] text-pf-danger hover:underline font-medium opacity-0 group-hover:opacity-100 transition-opacity"
            >
              Delete
            </button>
          )}
        </div>
        {isContainer && node.children && (
          <div className="flex flex-col">
            {node.children.map(renderTree)}
          </div>
        )}
      </div>
    );
  };

  // Viewport width guard
  if (viewportWidth < 1200) {
    return (
      <div className="h-screen w-screen flex flex-col items-center justify-center bg-[#0D1117] text-[#8B949E] p-6 text-center font-sans">
        <AlertTriangle className="w-8 h-8 text-pf-warning mb-3" />
        <p className="text-[15px] font-semibold text-pf-text mb-1">Incompatible Viewport</p>
        <p className="text-[13px] text-pf-text-secondary max-w-sm">
          PatternForge requires a minimum viewport width of 1200px.
        </p>
      </div>
    );
  }

  // Get active tab visual title
  const getActiveTabTitle = () => {
    switch(activeTab) {
      case 'dashboard': case 'composite': return 'Composite';
      case 'decorator': return 'Decorator';
      case 'adapter': return 'Adapter';
      case 'facade': return 'Facade';
      case 'proxy': return 'Proxy';
      case 'bridge': return 'Bridge';
      case 'flyweight': return 'Flyweight';
      default: return 'Workbench';
    }
  };

  const getActiveTabDesc = () => {
    switch(activeTab) {
      case 'dashboard': case 'composite': return 'Component Tree & Render Preview';
      case 'decorator': return 'Dynamic Styling Wrappers';
      case 'adapter': return 'Bridge Incompatible APIs';
      case 'facade': return 'Simplify Complex Subsystems';
      case 'proxy': return 'Control Access & Audit';
      case 'bridge': return 'Decouple Abstraction from Implementation';
      case 'flyweight': return 'Share Intrinsic State';
      default: return 'Software Engineering Pattern Laboratory';
    }
  };

  return (
    <div className="flex-1 flex flex-col overflow-hidden h-screen bg-pf-base text-pf-text-secondary font-sans">
      
      {/* APP BAR (48px) */}
      <header className="h-[48px] min-h-[48px] border-b border-pf-border bg-pf-surface px-4 flex items-center justify-between select-none z-40">
        <div className="flex items-center gap-3">
          <Layers className="w-4 h-4 text-pf-accent" />
          <span className="text-[13px] font-semibold text-pf-text tracking-tight">PatternForge</span>
          <span className="w-[1px] h-3 bg-pf-border"></span>
          <span className="text-[13px] text-pf-text-secondary font-medium">{getActiveTabTitle()}</span>
        </div>

        <div className="flex items-center gap-4">
          {/* Segmented Control for Roles */}
          <div className="flex bg-pf-base border border-pf-border rounded-input p-0.5">
            {['GUEST', 'EDITOR', 'ADMIN'].map(role => (
              <button
                key={role}
                onClick={() => handleRoleChange(role)}
                className={`px-3 py-1 text-[11px] rounded-[4px] font-mono font-medium transition-all ${
                  currentRole === role
                    ? 'bg-pf-elevated border border-pf-accent text-pf-text'
                    : 'text-pf-text-secondary hover:text-pf-text'
                }`}
              >
                {role}
              </button>
            ))}
          </div>

          <a 
            href="/swagger-ui.html" 
            target="_blank" 
            className="text-[13px] text-pf-text-secondary hover:text-pf-text transition-colors"
          >
            Swagger Docs
          </a>

          {/* Sidebar Toggle */}
          <button 
            onClick={() => setIsInspectorOpen(!isInspectorOpen)}
            className="p-1 border border-pf-border rounded hover:bg-pf-elevated text-pf-text-secondary"
          >
            {isInspectorOpen ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>
      </header>

      {/* Main body shell */}
      <div className="flex-1 flex overflow-hidden">
        
        {/* NAVIGATION SIDEBAR (160px) */}
        <aside className="w-[160px] border-r border-pf-border bg-pf-surface flex flex-col shrink-0 select-none">
          <div className="py-4 flex flex-col gap-1">
            {[
              { id: 'dashboard', name: 'Composite', dot: '#A371F7' },
              { id: 'decorator', name: 'Decorator', dot: '#2F81F7' },
              { id: 'adapter', name: 'Adapter', dot: '#D29922' },
              { id: 'facade', name: 'Facade', dot: '#3FB950' },
              { id: 'proxy', name: 'Proxy', dot: '#F778BA' },
              { id: 'bridge', name: 'Bridge', dot: '#56D4DD' },
              { id: 'flyweight', name: 'Flyweight', dot: '#79C0FF' }
            ].map(tab => {
              const isActive = activeTab === tab.id || (tab.id === 'dashboard' && activeTab === 'composite');
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 h-9 px-3 text-left transition-all w-full border-l-2 ${
                    isActive 
                      ? 'bg-pf-elevated text-pf-text border-pf-accent font-medium' 
                      : 'text-pf-text-secondary hover:bg-pf-elevated hover:text-pf-text border-transparent'
                  }`}
                >
                  <span style={{ backgroundColor: tab.dot }} className="h-1.5 w-1.5 rounded-full shrink-0"></span>
                  <span className="text-[13px]">{tab.name}</span>
                </button>
              );
            })}
          </div>
        </aside>

        {/* Dynamic canvas workspace */}
        <main className="flex-1 p-6 overflow-y-auto bg-pf-base flex flex-col gap-6 relative">
          
          {/* Unified Pattern Header */}
          <div className="border-b border-pf-border pb-4 flex justify-between items-center">
            <div>
              <h1 className="text-title text-pf-text">{getActiveTabTitle()}</h1>
              <p className="text-[13px] text-pf-text-secondary mt-1">{getActiveTabDesc()}</p>
            </div>
            {(activeTab === 'dashboard' || activeTab === 'composite') && (
              <button 
                onClick={refreshTree} 
                className="pf-btn-primary"
              >
                {renderLoading ? 'Rendering...' : 'Render All'}
              </button>
            )}
          </div>

          {/* Composite Pattern view */}
          {(activeTab === 'dashboard' || activeTab === 'composite') && (
            <div className="flex flex-col gap-5 flex-grow">
              {compositeError && (
                <div className="flex items-center gap-3 p-3 bg-[rgba(248,81,73,0.05)] border-l-3 border-pf-danger text-pf-danger rounded-r-[6px]">
                  <AlertTriangle className="w-4 h-4 shrink-0" />
                  <span className="text-[13px]">{compositeError}</span>
                  <button onClick={() => setCompositeError(null)} className="ml-auto text-pf-text-muted hover:text-pf-text">✕</button>
                </div>
              )}

              <div className="flex gap-6 items-stretch">
                {/* 35% left column */}
                <div className="w-[35%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col">
                  <div className="border-b border-pf-border pb-2 mb-3 flex items-center justify-between">
                    <h3 className="text-panel text-pf-text">TREE HIERARCHY</h3>
                    <div className="flex gap-1.5">
                      <button 
                        onClick={() => { setModalNodeType('container'); setModalParentId('root'); setIsModalOpen(true); }} 
                        className="pf-btn-secondary py-1 text-[11px]"
                      >
                        + Container
                      </button>
                      <button 
                        onClick={() => { setModalNodeType('widget'); setModalParentId('root'); setIsModalOpen(true); }} 
                        className="pf-btn-secondary py-1 text-[11px]"
                      >
                        + Widget
                      </button>
                    </div>
                  </div>
                  <div className="flex-1 overflow-y-auto min-h-[350px]">
                    {tree ? renderTree(tree) : <div className="text-pf-text-muted py-4 text-center">Loading components...</div>}
                  </div>
                </div>

                {/* 65% right column */}
                <div className={`w-[65%] bg-pf-surface border rounded-card p-4 flex flex-col transition-all duration-300 ${previewBorderSuccess ? 'border-pf-success' : 'border-pf-border'}`}>
                  <div className="border-b border-pf-border pb-2 mb-3 flex justify-between items-center">
                    <h3 className="text-panel text-pf-text">RENDER PREVIEW</h3>
                  </div>
                  <div className="flex-1 bg-white p-4 rounded-[6px] overflow-y-auto text-black min-h-[350px]">
                    <div dangerouslySetInnerHTML={{ __html: canvasHtml || '<div class="text-gray-400 py-12 text-center">No HTML rendered. Press "Render All" to generate output.</div>' }} />
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Decorator Pattern view */}
          {activeTab === 'decorator' && (
            <div className="flex flex-col gap-6">
              <div className="flex gap-6">
                {/* 60% Left Controls */}
                <div className="w-[60%] flex flex-col gap-6">
                  <div className="bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col gap-3">
                    <h3 className="text-panel text-pf-text border-b border-pf-border pb-2">DECORATOR STACK</h3>
                    <div className="flex flex-col gap-2 min-h-[100px] justify-center">
                      {decoratorStack.length === 0 ? (
                        <div className="text-pf-text-muted text-center py-6 text-[13px]">No decorators applied. Click available items below to build stack.</div>
                      ) : (
                        decoratorStack.map((type, index) => (
                          <div key={index} className="flex items-center justify-between p-2.5 bg-pf-elevated border border-pf-border rounded-button text-[12px] font-mono">
                            <span className="text-pf-text">{index + 1}. {type}</span>
                            <div className="flex items-center gap-3">
                              <div className="flex items-center gap-1.5 border-r border-pf-border pr-2">
                                {index > 0 && (
                                  <button 
                                    onClick={() => {
                                      const newStack = [...decoratorStack];
                                      const temp = newStack[index];
                                      newStack[index] = newStack[index - 1];
                                      newStack[index - 1] = temp;
                                      handleReorderDecorators(newStack);
                                    }}
                                    className="text-pf-text-secondary hover:text-pf-accent transition-colors text-[10px]"
                                    title="Move Up"
                                  >
                                    ▲
                                  </button>
                                )}
                                {index < decoratorStack.length - 1 && (
                                  <button 
                                    onClick={() => {
                                      const newStack = [...decoratorStack];
                                      const temp = newStack[index];
                                      newStack[index] = newStack[index + 1];
                                      newStack[index + 1] = temp;
                                      handleReorderDecorators(newStack);
                                    }}
                                    className="text-pf-text-secondary hover:text-pf-accent transition-colors text-[10px]"
                                    title="Move Down"
                                  >
                                    ▼
                                  </button>
                                )}
                              </div>
                              <button onClick={() => handleRemoveDecorator(type)} className="text-[12px] text-pf-danger hover:underline font-bold">✕</button>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  <div className="bg-pf-surface border border-pf-border rounded-card p-4">
                    <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">AVAILABLE DECORATORS</h3>
                    <div className="grid grid-cols-2 gap-2">
                      {[
                        { type: 'BORDER', label: 'BorderDecorator', desc: 'Adds outline border' },
                        { type: 'SHADOW', label: 'ShadowDecorator', desc: 'Applies outer shadow' },
                        { type: 'PADDING', label: 'PaddingDecorator', desc: 'Increases element spacing' },
                        { type: 'THEME', label: 'ThemeDecorator', desc: 'Applies specified theme color' }
                      ].map(dec => (
                        <button
                          key={dec.type}
                          onClick={() => handleAddDecorator(dec.type)}
                          className="flex flex-col items-start p-2.5 border border-pf-border hover:bg-pf-elevated rounded-button text-left transition-all"
                        >
                          <span className="text-[12px] font-medium text-pf-text">{dec.label}</span>
                          <span className="text-[10px] text-pf-text-muted leading-tight mt-0.5">{dec.desc}</span>
                        </button>
                      ))}
                    </div>
                    {decoratorStack.includes('THEME') && (
                      <div className="mt-3 pt-3 border-t border-pf-border flex items-center gap-3">
                        <label className="text-[11px] font-mono text-pf-text-secondary uppercase">Theme Value:</label>
                        <input 
                          type="text" 
                          value={themeName} 
                          onChange={(e) => setThemeName(e.target.value)} 
                          className="pf-input py-1.5 flex-1" 
                        />
                      </div>
                    )}
                  </div>
                </div>

                {/* 40% Right Preview & CSS */}
                <div className="w-[40%] flex flex-col gap-6">
                  <div className="bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col min-h-[220px]">
                    <div className="flex justify-between items-center border-b border-pf-border pb-2 mb-4">
                      <h3 className="text-panel text-pf-text">LIVE PREVIEW</h3>
                      <button onClick={handleResetDecorators} className="text-[12px] text-pf-accent hover:underline">Reset</button>
                    </div>
                    <div className="flex-grow flex items-center justify-center p-4 bg-white rounded border border-pf-border text-black">
                      <div dangerouslySetInnerHTML={{ 
                        __html: canvasHtml ? (
                          new DOMParser().parseFromString(canvasHtml, 'text/html').getElementById(selectedDecoratorWidget || 'root')?.outerHTML || 
                          '<div class="text-gray-400">Select widget and render</div>'
                        ) : '' 
                      }} />
                    </div>
                  </div>

                  <div className="bg-pf-surface border border-pf-border rounded-card p-4">
                    <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-2">CSS OUTPUT</h3>
                    <pre className="bg-pf-base p-3 rounded-[6px] border border-pf-border font-mono text-[12px] text-pf-text-secondary overflow-x-auto">
                      <code>
                        <span className="text-[#D2A8FF]">.widget-box-{selectedDecoratorWidget}</span> {"{\n"}
                        {decoratorStack.some(x => x.toUpperCase().includes('BORDER')) && <span className="text-[#8B949E]">  <span className="text-[#79C0FF]">border</span>: 2px solid <span className="text-[#A5D6FF]">var(--pf-accent)</span>;\n</span>}
                        {decoratorStack.some(x => x.toUpperCase().includes('SHADOW')) && <span className="text-[#8B949E]">  <span className="text-[#79C0FF]">box-shadow</span>: 0 10px 15px -3px <span className="text-[#A5D6FF]">rgba(0,0,0,0.5)</span>;\n</span>}
                        {decoratorStack.some(x => x.toUpperCase().includes('PADDING')) && <span className="text-[#8B949E]">  <span className="text-[#79C0FF]">padding</span>: <span className="text-[#A5D6FF]">24px</span>;\n</span>}
                        {decoratorStack.some(x => x.toUpperCase().includes('THEME')) && <span className="text-[#8B949E]">  <span className="text-[#79C0FF]">theme-class</span>: <span className="text-[#A5D6FF]">"{themeName.toLowerCase()}"</span>;\n</span>}
                        {"}"}
                      </code>
                    </pre>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Adapter Pattern view */}
          {activeTab === 'adapter' && (
            <div className="flex flex-col gap-6">
              {/* Full Width Top Configuration */}
              <div className="bg-pf-surface border border-pf-border rounded-card p-4">
                <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">CONFIGURATION</h3>
                <form onSubmit={handleCreateChart} className="flex flex-col gap-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="text-[12px] font-semibold text-pf-text-secondary block mb-1">Chart Title</label>
                      <input 
                        type="text" 
                        value={adapterTitle} 
                        onChange={(e) => setAdapterTitle(e.target.value)} 
                        required 
                        className="pf-input w-full" 
                      />
                    </div>
                    <div>
                      <label className="text-[12px] font-semibold text-pf-text-secondary block mb-1">Data Points (Comma Separated)</label>
                      <input 
                        type="text" 
                        value={adapterDataPoints} 
                        onChange={(e) => setAdapterDataPoints(e.target.value)} 
                        required 
                        className="pf-input w-full" 
                      />
                    </div>
                  </div>

                  <div>
                    <label className="text-[12px] font-semibold text-pf-text-secondary block mb-1.5">Source Library</label>
                    <div className="flex bg-pf-base p-0.5 border border-pf-border rounded-[6px] max-w-md">
                      {[
                        { id: 'legacy', label: 'LegacyGraphLib' },
                        { id: 'old', label: 'OldChartLib' },
                        { id: 'new', label: 'ModernChart' }
                      ].map(src => (
                        <button
                          key={src.id}
                          type="button"
                          onClick={() => setAdapterSource(src.id)}
                          className={`flex-1 px-3 py-1 text-[11px] rounded-[4px] font-mono font-medium transition-all ${
                            adapterSource === src.id
                              ? 'bg-pf-elevated border border-pf-border text-pf-text'
                              : 'text-pf-text-secondary hover:text-pf-text'
                          }`}
                        >
                          {src.label}
                        </button>
                      ))}
                    </div>
                  </div>

                  <button type="submit" className="pf-btn-primary w-full">
                    Create Adapted Widget
                  </button>
                </form>
              </div>

              {/* 60/40 splits below */}
              <div className="flex gap-6">
                <div className="w-[60%] bg-pf-surface border border-pf-border rounded-card p-4 min-h-[220px] flex flex-col">
                  <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-4">RENDER OUTPUT</h3>
                  <div className="flex-grow flex items-center justify-center p-4 bg-white rounded border border-pf-border text-black">
                    <div dangerouslySetInnerHTML={{ __html: adapterTraceHtml || '<div class="text-gray-400">Configure parameters above to render chart.</div>' }} />
                  </div>
                </div>

                <div className="w-[40%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col">
                  <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-4">TRANSLATION TRACE</h3>
                  <div className="flex-grow flex flex-col gap-3 justify-center pl-6 border-l border-pf-border ml-4 relative">
                    {adapterTraceMsg ? (
                      <>
                        <div className="relative">
                          <span className="absolute left-[-32px] top-0 w-5 h-5 bg-pf-accent text-white rounded-full flex items-center justify-center text-[10px] font-bold">1</span>
                          <div className="text-[12px] font-semibold text-pf-text">Input: List&lt;Double&gt;</div>
                          <div className="text-[11px] font-mono text-pf-text-secondary bg-pf-base p-1.5 rounded mt-1 border border-pf-border">
                            [{adapterDataPoints}]
                          </div>
                        </div>
                        <div className="text-pf-text-muted text-[12px] pl-2">↓</div>
                        <div className="relative">
                          <span className="absolute left-[-32px] top-0 w-5 h-5 bg-pf-accent text-white rounded-full flex items-center justify-center text-[10px] font-bold">2</span>
                          <div className="text-[12px] font-semibold text-pf-text">Converted: {adapterSource === 'legacy' ? 'double[]' : 'CSV String'}</div>
                          <div className="text-[11px] font-mono text-pf-text-secondary bg-pf-base p-1.5 rounded mt-1 border border-pf-border">
                            {adapterSource === 'legacy' ? `[${adapterDataPoints}]` : `"${adapterDataPoints}"`}
                          </div>
                        </div>
                        <div className="text-pf-text-muted text-[12px] pl-2">↓</div>
                        <div className="relative">
                          <span className="absolute left-[-32px] top-0 w-5 h-5 bg-pf-accent text-white rounded-full flex items-center justify-center text-[10px] font-bold">3</span>
                          <div className="text-[12px] font-semibold text-pf-text">Called target interface</div>
                          <div className="text-[11px] font-mono text-pf-text-secondary bg-pf-base p-1.5 rounded mt-1 border border-pf-border">
                            {adapterTraceMsg}
                          </div>
                        </div>
                      </>
                    ) : (
                      <div className="text-pf-text-muted text-center py-8">Generate an adapted chart widget to view step pipeline.</div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Facade Pattern view */}
          {activeTab === 'facade' && (
            <div className="flex flex-col gap-6">
              {facadeToast && (
                <div className="fixed bottom-6 left-6 bg-pf-success text-[#FFFFFF] text-[13px] font-semibold px-4 py-2 rounded-button shadow-lg flex items-center gap-2 z-50">
                  <Check className="w-4 h-4" /> {facadeToast}
                </div>
              )}

              <div className="flex gap-6">
                {/* 60% Left Facade Panel */}
                <div className="w-[60%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col gap-4">
                  <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-2">DASHBOARDMANAGER FACADE (1 call each)</h3>

                  <form onSubmit={handleFacadeCreate} className="flex flex-col gap-3 p-3 bg-pf-base rounded-button border border-pf-border">
                    <div className="text-[12px] font-semibold text-pf-text">Pipeline: Create Dashboard</div>
                    <div className="grid grid-cols-2 gap-2">
                      <input 
                        type="text" 
                        placeholder="Dashboard Name" 
                        value={facadeName} 
                        onChange={(e) => setFacadeName(e.target.value)} 
                        className="pf-input w-full" 
                      />
                      <select 
                        value={facadeTheme} 
                        onChange={(e) => setFacadeTheme(e.target.value)} 
                        className="pf-input w-full"
                      >
                        <option value="classic">Classic Theme</option>
                        <option value="neon">Neon Theme</option>
                        <option value="cyberpunk">Cyberpunk Theme</option>
                      </select>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <input 
                        type="number" 
                        placeholder="Cols (4)" 
                        value={facadeCols} 
                        onChange={(e) => setFacadeCols(parseInt(e.target.value))} 
                        className="pf-input w-full" 
                      />
                      <input 
                        type="number" 
                        placeholder="Rows (3)" 
                        value={facadeRows} 
                        onChange={(e) => setFacadeRows(parseInt(e.target.value))} 
                        className="pf-input w-full" 
                      />
                    </div>
                    <button type="submit" className="pf-btn-primary w-full py-1.5">
                      Orchestrate Create
                    </button>
                  </form>

                  <form onSubmit={handleFacadeTheme} className="flex flex-col gap-3 p-3 bg-pf-base rounded-button border border-pf-border">
                    <div className="text-[12px] font-semibold text-pf-text">Pipeline: Apply Theme</div>
                    <div className="grid grid-cols-2 gap-2">
                      <input 
                        type="text" 
                        placeholder="Dashboard ID" 
                        value={facadeThemeId} 
                        onChange={(e) => setFacadeThemeId(e.target.value)} 
                        className="pf-input w-full" 
                      />
                      <input 
                        type="text" 
                        placeholder="Theme Name" 
                        value={facadeThemeName} 
                        onChange={(e) => setFacadeThemeName(e.target.value)} 
                        className="pf-input w-full" 
                      />
                    </div>
                    <button type="submit" className="pf-btn-primary w-full py-1.5">
                      Orchestrate Theme
                    </button>
                  </form>

                  <div className="p-2.5 bg-pf-base rounded-button border border-pf-border flex justify-between items-center text-[11px] font-mono">
                    <span className="text-pf-text-secondary">Last call efficiency:</span>
                    <span className="text-pf-accent font-semibold">1 Facade Call → 4 Subsystems</span>
                  </div>
                </div>

                {/* 40% Right Raw Subsystems */}
                <div className="w-[40%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col gap-4">
                  <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-2">RAW SUBSYSTEMS (4 calls to match)</h3>

                  <div className="flex flex-col gap-3">
                    {[
                      { name: 'LayoutEngine', detail: '├─ calculateGrid()\n└─ applyConstraints()', action: 'calculate-grid', payload: { columns: 4, rows: 3 } },
                      { name: 'ThemeEngine', detail: '├─ loadTheme()\n└─ applyTheme()', action: 'theme', query: 'themeName=classic' },
                      { name: 'RenderEngine', detail: '├─ prepareRenderContext()\n└─ executeRender()', action: 'render', query: 'widgetId=widget-welcome' }
                    ].map((sub, i) => (
                      <div key={i} className="flex flex-col gap-2 p-3 bg-pf-base border border-pf-border rounded-button">
                        <div className="flex justify-between items-center">
                          <span className="text-[12px] font-semibold text-pf-text font-mono">{sub.name}</span>
                          <button
                            onClick={() => {
                              const endpoint = sub.action === 'calculate-grid' 
                                ? '/api/subsystems/layout/calculate-grid' 
                                : sub.action === 'theme' 
                                ? '/api/subsystems/theme/load' 
                                : '/api/subsystems/render/execute';
                              
                              const options: RequestInit = { method: 'POST' };
                              let fetchUrl = endpoint;
                              if (sub.payload) {
                                options.headers = { 'Content-Type': 'application/json' };
                                options.body = JSON.stringify(sub.payload);
                              } else {
                                fetchUrl += `?${sub.query}`;
                              }

                              fetch(fetchUrl, options).then(() => {
                                fetchFacadeLogs();
                                triggerToast(`Direct subsystem trigger executed.`);
                              });
                            }}
                            className="pf-btn-secondary py-1 text-[11px]"
                          >
                            Trigger
                          </button>
                        </div>
                        <pre className="text-[10px] text-pf-text-muted font-mono leading-tight whitespace-pre-wrap">{sub.detail}</pre>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* Live Call Log */}
              <div className="bg-pf-surface border border-pf-border rounded-card p-4">
                <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">LIVE CALL LOG</h3>
                <div className="flex flex-col gap-2 font-mono text-[12px] text-pf-text-secondary bg-pf-base p-3 rounded-button border border-pf-border max-h-[160px] overflow-y-auto">
                  {facadeCallLogs.length === 0 ? (
                    <div className="text-pf-text-muted py-2">No subsystem events recorded in log session.</div>
                  ) : (
                    facadeCallLogs.map((log, i) => (
                      <div key={i} className="flex gap-2">
                        <span className="text-pf-text-muted">[{new Date().toTimeString().split(' ')[0]}]</span>
                        <span className="text-pf-text">{log}</span>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Proxy Pattern view */}
          {activeTab === 'proxy' && (
            <div className="flex flex-col gap-6">
              <div className="flex gap-6">
                {/* 60% Left Widgets grid */}
                <div className="w-[60%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col gap-4">
                  <h3 className="text-panel text-pf-text border-b border-pf-border pb-2">WIDGETS</h3>
                  <div className="grid grid-cols-2 gap-4">
                    {/* Lazy Video Card */}
                    <div className="bg-pf-base border border-pf-border rounded-button p-4 flex flex-col gap-3 justify-between">
                      <div>
                        <div className="text-[13px] font-semibold text-pf-text">Space Shuttle Video Player</div>
                        <span className="inline-block px-2 py-0.5 rounded-full text-[10px] bg-pf-elevated text-pf-text-secondary border border-dashed border-pf-border mt-1">
                          {proxyStates['lazy-video-1'] || 'UNLOADED'}
                        </span>
                      </div>
                      <div className="min-h-[60px] flex items-center justify-center text-center">
                        {proxyStates['lazy-video-1'] === 'LOADED' ? (
                          <span className="text-[12px] text-pf-success">🎬 Active stream payload loaded</span>
                        ) : (
                          <button
                            onClick={() => handleLoadProxyWidget('lazy-video-1')}
                            className="pf-btn-secondary text-[11px] py-1"
                          >
                            Click to Load
                          </button>
                        )}
                      </div>
                    </div>

                    {/* Security Card */}
                    <div className="bg-pf-base border border-pf-border rounded-button p-4 flex flex-col gap-3 justify-between">
                      <div>
                        <div className="text-[13px] font-semibold text-pf-text">Confidential Projections</div>
                        <span className={`inline-block px-2 py-0.5 rounded-full text-[10px] mt-1 ${
                          currentRole === 'ADMIN' 
                            ? 'bg-pf-elevated text-pf-success border border-pf-success/20' 
                            : 'bg-pf-elevated text-pf-danger border border-pf-danger/20'
                        }`}>
                          {currentRole === 'ADMIN' ? 'AUTHORIZED' : 'RESTRICTED'}
                        </span>
                      </div>
                      <div className="min-h-[60px] flex items-center justify-center text-center text-[12px]">
                        {currentRole === 'ADMIN' ? (
                          <span className="font-mono text-pf-text">Forecast Q4: +42.1M USD</span>
                        ) : (
                          <span className="text-pf-text-muted flex items-center gap-1"><Lock className="w-3 h-3" /> Locked</span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* 40% Right Audit Logs Table */}
                <div className="w-[40%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col">
                  <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">AUDIT LOG</h3>
                  <div className="flex-1 overflow-y-auto max-h-[350px] bg-pf-base rounded-button border border-pf-border">
                    <table className="w-full text-left text-[11px] font-mono border-collapse text-pf-text-secondary">
                      <thead>
                        <tr className="border-b border-pf-border bg-pf-elevated text-pf-text-muted">
                          <th className="py-2 px-3">Time</th>
                          <th className="py-2 px-3">Action</th>
                          <th className="py-2 px-3">Target</th>
                          <th className="py-2 px-3">User</th>
                        </tr>
                      </thead>
                      <tbody>
                        {auditLogs.map((log, i) => (
                          <tr key={i} className="border-b border-pf-border hover:bg-pf-elevated">
                            <td className="py-2 px-3 text-pf-text-muted">{new Date(log.timestamp).toTimeString().split(' ')[0]}</td>
                            <td className="py-2 px-3">{log.method}</td>
                            <td className="py-2 px-3 font-semibold">{log.widgetId}</td>
                            <td className="py-2 px-3 text-pf-text-muted">{log.userId}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Bridge Pattern view */}
          {activeTab === 'bridge' && (
            <div className="flex flex-col gap-6">
              {/* Renderer Switcher */}
              <div className="bg-pf-surface border border-pf-border rounded-card p-4 flex justify-between items-center">
                <span className="text-panel text-pf-text">RENDERER MODE</span>
                <div className="flex bg-pf-base p-0.5 border border-pf-border rounded-[6px]">
                  {['html', 'json', 'svg'].map(renderer => (
                    <button 
                      key={renderer}
                      onClick={() => handleRendererChange(renderer)}
                      className={`px-4 py-1 text-[11px] rounded-[4px] font-mono font-medium transition-all ${
                        activeRenderer === renderer 
                          ? 'bg-pf-elevated border border-pf-border text-pf-text' 
                          : 'text-pf-text-secondary hover:text-pf-text'
                      }`}
                    >
                      {renderer.toUpperCase()}
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex gap-6">
                {/* 60% Left Preview */}
                <div className="w-[60%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col min-h-[300px]">
                  <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">WIDGET PREVIEW</h3>
                  <div className="flex-1 bg-white p-4 rounded-[6px] text-black overflow-auto">
                    <div dangerouslySetInnerHTML={{ 
                      __html: canvasHtml ? (
                        new DOMParser().parseFromString(canvasHtml, 'text/html').getElementById('bridge-text-1')?.outerHTML || 
                        '<div class="text-gray-400">Empty</div>'
                      ) : '' 
                    }} />
                  </div>
                </div>

                {/* 40% Right Footprint explainer */}
                <div className="w-[40%] bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col justify-between">
                  <div>
                    <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">CLASS EXPLOSION EXPLAINER</h3>
                    {bridgeClassCount && (
                      <table className="w-full text-left text-[12px] border-collapse font-mono mt-2">
                        <thead>
                          <tr className="border-b border-pf-border text-pf-text-muted">
                            <th className="pb-1.5">Architecture</th>
                            <th className="pb-1.5 text-right">Classes</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr className="border-b border-pf-border">
                            <td className="py-2">Without Bridge</td>
                            <td className="py-2 text-right text-pf-danger font-semibold">{bridgeClassCount.withoutBridge}</td>
                          </tr>
                          <tr className="border-b border-pf-border">
                            <td className="py-2">With Bridge</td>
                            <td className="py-2 text-right text-pf-success font-semibold">{bridgeClassCount.withBridge}</td>
                          </tr>
                        </tbody>
                      </table>
                    )}
                  </div>

                  {bridgeClassCount && (
                    <div className="mt-4 flex flex-col gap-2">
                      <div className="flex justify-between text-[11px] text-pf-text-secondary font-mono">
                        <span>Comparison footprint</span>
                      </div>
                      <div className="flex h-3 bg-pf-base border border-pf-border rounded overflow-hidden">
                        <div 
                          style={{ width: `${(bridgeClassCount.withBridge / bridgeClassCount.withoutBridge) * 100}%` }}
                          className="bg-pf-accent"
                          title="With Bridge"
                        ></div>
                        <div 
                          style={{ width: `${(1 - (bridgeClassCount.withBridge / bridgeClassCount.withoutBridge)) * 100}%` }}
                          className="bg-pf-danger"
                          title="Explosion Overload"
                        ></div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Flyweight Pattern view */}
          {activeTab === 'flyweight' && (
            <div className="flex flex-col gap-6">
              {/* Generator Panel */}
              <div className="bg-pf-surface border border-pf-border rounded-card p-4">
                <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">GENERATOR</h3>
                <div className="flex items-center gap-6">
                  <div className="flex-1 flex flex-col gap-2">
                    <div className="flex justify-between items-center text-[12px] font-mono">
                      <span className="text-pf-text-secondary">Instance Count: {flyweightCount}</span>
                    </div>
                    <input 
                      type="range" 
                      min="100" 
                      max="10000" 
                      step="100" 
                      value={flyweightCount} 
                      onChange={(e) => {
                        const val = parseInt(e.target.value);
                        setFlyweightCount(val);
                        fetchMemoryEstimates(val);
                      }} 
                      className="w-full h-1 bg-pf-base rounded-lg appearance-none cursor-pointer accent-pf-accent" 
                    />
                  </div>
                  <button 
                    onClick={handleFlyweightGenerate}
                    className="pf-btn-primary"
                  >
                    Generate Bulk Instances
                  </button>
                </div>
              </div>

              {/* Memory Footprint Panel */}
              <div className="bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col min-h-[300px]">
                <div className="flex justify-between items-center border-b border-pf-border pb-2 mb-4">
                  <h3 className="text-panel text-pf-text">MEMORY FOOTPRINT</h3>
                  {flyweightEstimates && (
                    <span className="text-[11px] text-pf-success font-bold font-mono">
                      Saved: {flyweightEstimates.savingsPercent.toFixed(1)}% | Without: {(flyweightEstimates.withoutFlyweightBytes / 1024 / 1024).toFixed(2)} MB | With: {(flyweightEstimates.withFlyweightBytes / 1024 / 1024).toFixed(2)} MB
                    </span>
                  )}
                </div>
                <div className="flex-grow relative min-h-[200px]">
                  {flyweightEstimates && (
                    <Line 
                      data={getFlyweightChartData()}
                      options={{
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                          x: { 
                            grid: { color: '#30363D' }, 
                            ticks: { color: '#8B949E' },
                            title: {
                              display: true,
                              text: 'Number of Widgets',
                              color: '#8B949E'
                            }
                          },
                          y: { 
                            grid: { color: '#30363D' }, 
                            ticks: { 
                              color: '#8B949E',
                              callback: (value) => value + ' KB'
                            },
                            title: {
                              display: true,
                              text: 'Memory Footprint (KB)',
                              color: '#8B949E'
                            }
                          }
                        },
                        plugins: {
                          legend: { labels: { color: '#8B949E' } }
                        }
                      }}
                    />
                  )}
                </div>
              </div>

              {/* Pool Registry Table */}
              <div className="bg-pf-surface border border-pf-border rounded-card p-4 flex flex-col">
                <h3 className="text-panel text-pf-text border-b border-pf-border pb-2 mb-3">FLYWEIGHT POOL</h3>
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-[11px] font-mono text-pf-text-secondary border-collapse">
                    <thead>
                      <tr className="border-b border-pf-border bg-pf-elevated text-pf-text-muted">
                        <th className="py-2 px-3">Type</th>
                        <th className="py-2 px-3">References</th>
                        <th className="py-2 px-3">Size</th>
                        <th className="py-2 px-3">Template</th>
                      </tr>
                    </thead>
                    <tbody>
                      {flyweightPool.map((item, i) => (
                        <tr key={i} className="border-b border-pf-border hover:bg-pf-elevated">
                          <td className="py-2 px-3 font-bold text-pf-text">{item.type}</td>
                          <td className="py-2 px-3">{item.instanceCount}</td>
                          <td className="py-2 px-3 text-pf-text-muted">{item.flyweightSizeBytes} bytes</td>
                          <td className="py-2 px-3 text-pf-text-muted">Shared Stock widget layout template</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </main>

        {/* COLLAPSIBLE INSPECTOR SIDEBAR (280px) */}
        <aside 
          className={`border-l border-pf-border bg-pf-surface flex flex-col shrink-0 transition-all duration-200 select-none ${
            isInspectorOpen ? 'w-[280px]' : 'w-0 overflow-hidden border-l-0'
          }`}
        >
          <div className="p-4 border-b border-pf-border flex justify-between items-center bg-[#161B22]">
            <div className="flex items-center gap-2">
              <span className={`h-2 w-2 rounded-full ${websocketConnected ? 'bg-pf-success animate-pulse' : 'bg-pf-danger'}`}></span>
              <span className="text-[12px] font-semibold text-pf-text uppercase tracking-wider">Inspector Logs</span>
            </div>
            <button 
              onClick={() => setTraceLogs([])} 
              className="text-[11px] text-pf-text-secondary hover:text-pf-text hover:underline font-mono"
            >
              CLEAR
            </button>
          </div>

          <div className="flex-grow overflow-y-auto p-3 flex flex-col gap-2 font-mono text-[11px] text-pf-text-secondary">
            {traceLogs.length === 0 ? (
              // Contextual Help to avoid boring "Waiting for events..." empty message
              <div className="text-pf-text-secondary py-6 px-2 flex flex-col gap-4 font-sans text-[13px]">
                <div className="text-[12px] font-semibold text-pf-text uppercase tracking-wider border-b border-pf-border pb-1">Contextual Info</div>
                <div>
                  <span className="font-semibold text-pf-text">Active Tab: </span>
                  <span className="font-mono text-pf-accent">{getActiveTabTitle()}</span>
                </div>
                <div className="text-[12px] text-pf-text-secondary leading-relaxed space-y-2">
                  <p>This panel shows real-time WebSocket traces of class/method entry and exit points intercepted by Spring AOP.</p>
                  <p className="bg-pf-elevated p-2 rounded border border-pf-border font-mono text-[11px] text-pf-text-muted">
                    Tip: Click "Render All" or make dashboard adjustments to stream logs here.
                  </p>
                </div>
              </div>
            ) : (
              traceLogs.map((event, i) => {
                const isEnter = event.eventType === 'ENTER';
                const isError = event.eventType === 'ERROR';
                let statusColor = "text-pf-accent";
                let prefix = "▶";
                let indent = "pl-0";
                if (!isEnter) {
                  statusColor = "text-pf-success";
                  prefix = "◀";
                  indent = "pl-3";
                }
                if (isError) {
                  statusColor = "text-pf-danger";
                  prefix = "✖";
                }
                return (
                  <div key={i} className="p-2 border-b border-pf-border hover:bg-pf-elevated transition-all">
                    <div className="flex items-center justify-between mb-1">
                      <span className={`font-bold ${statusColor} ${indent}`}>{prefix} [{event.eventType}]</span>
                      <span className="text-pf-text-muted text-[10px]">{new Date(event.timestamp).toTimeString().split(' ')[0]}</span>
                    </div>
                    <div className="text-pf-text font-semibold">{event.className}.{event.methodName}()</div>
                    <div className="text-pf-text-muted text-[10px]">Widget ID: {event.widgetId || 'global'}</div>
                    {event.metadata && Object.keys(event.metadata).length > 0 && (
                      <div className="text-pf-accent/80 text-[10px] mt-1 break-all bg-pf-base p-1.5 rounded border border-pf-border">
                        Meta: {JSON.stringify(event.metadata)}
                      </div>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </aside>
      </div>

      {/* Node Create Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-[#0D1117]/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-pf-surface border border-pf-border rounded-card p-6 w-96 shadow-2xl flex flex-col gap-4 relative">
            <button onClick={() => setIsModalOpen(false)} className="absolute top-4 right-4 text-pf-text-secondary hover:text-pf-text">
              <X className="w-4 h-4" />
            </button>
            <h3 className="text-[13px] font-semibold text-pf-text uppercase tracking-wider border-b border-pf-border pb-2">Add Component</h3>
            <div className="flex flex-col gap-3">
              <label className="text-[11px] text-pf-text-secondary font-bold uppercase">Parent Container ID</label>
              <input 
                type="text" 
                value={modalParentId} 
                onChange={(e) => setModalParentId(e.target.value)} 
                className="pf-input w-full" 
              />
              
              <label className="text-[11px] text-pf-text-secondary font-bold uppercase">Node Type</label>
              <select 
                value={modalNodeType} 
                onChange={(e) => setModalNodeType(e.target.value as any)} 
                className="pf-input w-full"
              >
                <option value="container">Container Node (Composite)</option>
                <option value="widget">Widget Node (Leaf)</option>
              </select>

              <label className="text-[11px] text-pf-text-secondary font-bold uppercase">Node Name</label>
              <input 
                type="text" 
                value={modalNodeName} 
                onChange={(e) => setModalNodeName(e.target.value)} 
                placeholder="E.g. Database View" 
                className="pf-input w-full" 
              />

              {modalNodeType === 'widget' && (
                <div className="flex flex-col gap-3">
                  <label className="text-[11px] text-pf-text-secondary font-bold uppercase">Widget Code Type</label>
                  <input 
                    type="text" 
                    value={modalWidgetType} 
                    onChange={(e) => setModalWidgetType(e.target.value)} 
                    className="pf-input w-full" 
                  />
                  
                  <label className="text-[11px] text-pf-text-secondary font-bold uppercase">Initial Text Content</label>
                  <input 
                    type="text" 
                    value={modalWidgetContent} 
                    onChange={(e) => setModalWidgetContent(e.target.value)} 
                    placeholder="Content text..." 
                    className="pf-input w-full" 
                  />
                </div>
              )}
            </div>
            <div className="flex justify-end gap-2 mt-2">
              <button 
                onClick={() => setIsModalOpen(false)} 
                className="pf-btn-secondary"
              >
                Cancel
              </button>
              <button 
                onClick={handleCreateNode} 
                className="pf-btn-primary"
              >
                Add Node
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
