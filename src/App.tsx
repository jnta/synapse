import { useCallback, useRef, useState } from "react";
import { ThemeProvider } from "@/ThemeContext.tsx";
import { TheTopography } from "@/components/TheTopography.tsx";
import { EditorModule } from "@/components/EditorModule.tsx";
import { ActivityBar } from "@/components/ActivityBar.tsx";
import { TabBar } from "@/components/TabBar.tsx";
import { TopNavBar } from "@/components/TopNavBar.tsx";
import { StatusBar } from "@/components/StatusBar.tsx";
import { readNote } from "@/services/tauri.ts";
import type { NoteEntry } from "@/services/tauri.ts";

const LEFT_MIN = 160;
const LEFT_MAX = 500;

interface Tab extends NoteEntry {
  content: string;
}

function usePanelResize(initialPx: number, min: number, max: number) {
  const [width, setWidth] = useState(initialPx);
  const dragging = useRef(false);
  const startX = useRef(0);
  const startWidth = useRef(initialPx);

  const onMouseDown = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    dragging.current = true;
    startX.current = e.clientX;
    startWidth.current = width;
    document.body.style.cursor = "col-resize";
    document.body.style.userSelect = "none";

    const onMove = (ev: MouseEvent) => {
      if (!dragging.current) return;
      setWidth(Math.min(max, Math.max(min, startWidth.current + (ev.clientX - startX.current))));
    };
    const onUp = () => {
      dragging.current = false;
      document.body.style.cursor = "";
      document.body.style.userSelect = "";
      document.removeEventListener("mousemove", onMove);
      document.removeEventListener("mouseup", onUp);
    };
    document.addEventListener("mousemove", onMove);
    document.addEventListener("mouseup", onUp);
  }, [width, min, max]);

  return { width, onMouseDown };
}

function App() {
  const [sidebarView, setSidebarView] = useState<"files" | "search" | null>("files");
  const [tabs, setTabs] = useState<Tab[]>([]);
  const [activeTabName, setActiveTabName] = useState<string | null>(null);
  const left = usePanelResize(240, LEFT_MIN, LEFT_MAX);

  const activeTab = tabs.find(t => t.name === activeTabName) ?? null;
  const sidebarOpen = sidebarView !== null;

  const handleActivityClick = (view: "files" | "search") => {
    setSidebarView(prev => prev === view ? null : view);
  };

  const handleOpenNote = async (note: NoteEntry) => {
    const alreadyOpen = tabs.find(t => t.name === note.name && t.folder === note.folder);
    if (alreadyOpen) {
      setActiveTabName(note.name);
      return;
    }
    const content = await readNote(note.folder, note.name).catch(() => "");
    setTabs(prev => [...prev, { ...note, content }]);
    setActiveTabName(note.name);
  };

  const handleTabClose = (tab: NoteEntry) => {
    setTabs(prev => {
      const next = prev.filter(t => !(t.name === tab.name && t.folder === tab.folder));
      if (activeTabName === tab.name) {
        setActiveTabName(next.length > 0 ? next[next.length - 1].name : null);
      }
      return next;
    });
  };

  const handleContentChange = (value: string) => {
    setTabs(prev => prev.map(t => t.name === activeTabName ? { ...t, content: value } : t));
  };

  return (
    <ThemeProvider>
      <div className="flex flex-col h-screen overflow-hidden bg-background-light dark:bg-background-dark text-slate-900 dark:text-slate-400 font-display selection:bg-primary/30">
        <TopNavBar />

        <main className="flex flex-1 overflow-hidden">

          <ActivityBar activeView={sidebarView} onViewChange={handleActivityClick} />

          <aside
            style={{ width: sidebarOpen ? left.width : 0 }}
            className="flex flex-col bg-background-light/50 dark:bg-[#0d0d0d] shrink-0 overflow-hidden transition-[width] duration-200 ease-in-out border-r border-synapse-border"
          >
            {sidebarView === "files" && <TheTopography onOpen={handleOpenNote} />}
            {sidebarView === "search" && (
              <div className="p-3 flex-1">
                <div className="relative group">
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 text-base group-focus-within:text-primary">search</span>
                  <input
                    className="w-full bg-slate-100 dark:bg-black/20 border border-slate-200 dark:border-synapse-border rounded-lg pl-9 pr-3 py-1.5 text-sm focus:ring-1 focus:ring-primary outline-none transition-all placeholder:text-slate-500"
                    placeholder="Search across notes..."
                    type="text"
                    autoFocus
                  />
                </div>
              </div>
            )}
          </aside>

          {sidebarOpen && (
            <div
              onMouseDown={left.onMouseDown}
              className="w-[3px] shrink-0 bg-synapse-border hover:bg-primary/60 cursor-col-resize transition-colors duration-150 active:bg-primary"
            />
          )}

          <section className="flex-1 flex flex-col bg-background-light dark:bg-background-dark min-w-0 overflow-hidden">
            <TabBar
              tabs={tabs}
              activeTab={activeTabName}
              onSelect={t => setActiveTabName(t.name)}
              onClose={handleTabClose}
            />
            <div className="flex-1 overflow-y-auto custom-scrollbar">
              <EditorModule
                activeTab={activeTab}
                content={activeTab?.content ?? ""}
                onChange={handleContentChange}
                onCreateNote={() => setSidebarView("files")}
                onOpenNote={() => setSidebarView("files")}
              />
            </div>
          </section>

        </main>

        <StatusBar />
      </div>
    </ThemeProvider>
  );
}

export default App;
