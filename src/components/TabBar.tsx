import type { NoteEntry } from "@/services/tauri.ts";

interface Props {
  tabs: NoteEntry[];
  activeTab: string | null;
  onSelect: (tab: NoteEntry) => void;
  onClose: (tab: NoteEntry) => void;
}

export function TabBar({ tabs, activeTab, onSelect, onClose }: Props) {
  if (tabs.length === 0) return null;

  return (
    <div className="flex items-end overflow-x-auto scrollbar-hide border-b border-synapse-border shrink-0 bg-background-light dark:bg-[#0d0d0d]">
      {tabs.map((tab) => {
        const isActive = tab.name === activeTab;
        return (
          <div
            key={`${tab.folder}/${tab.name}`}
            onClick={() => onSelect(tab)}
            className={`group flex items-center gap-2 px-4 py-2.5 text-sm cursor-pointer border-r border-synapse-border shrink-0 transition-colors select-none ${
              isActive
                ? "bg-background-light dark:bg-background-dark text-slate-900 dark:text-slate-100 border-t-2 border-t-primary"
                : "text-slate-500 hover:text-slate-700 dark:hover:text-slate-300 bg-background-light/50 dark:bg-[#0d0d0d] border-t-2 border-t-transparent"
            }`}
          >
            <span className="material-symbols-outlined text-sm text-slate-400">description</span>
            <span className="max-w-[120px] truncate">{tab.name}</span>
            <button
              onClick={(e) => { e.stopPropagation(); onClose(tab); }}
              className="opacity-0 group-hover:opacity-100 hover:text-primary transition-opacity ml-1 flex items-center"
            >
              <span className="material-symbols-outlined text-sm">close</span>
            </button>
          </div>
        );
      })}
    </div>
  );
}
