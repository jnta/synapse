interface Props {
  activeView: "files" | "search" | null;
  onViewChange: (view: "files" | "search") => void;
}

export function ActivityBar({ activeView, onViewChange }: Props) {
  const items = [
    { id: "files" as const, icon: "folder_open", label: "Explorer" },
    { id: "search" as const, icon: "search", label: "Search" },
  ];

  return (
    <div className="w-12 flex flex-col items-center py-2 gap-1 border-r border-synapse-border bg-background-light dark:bg-[#0d0d0d] shrink-0">
      {items.map(({ id, icon, label }) => (
        <button
          key={id}
          title={label}
          onClick={() => onViewChange(id)}
          className={`relative flex items-center justify-center w-10 h-10 rounded-lg transition-all ${
            activeView === id
              ? "text-primary bg-primary/10"
              : "text-slate-500 hover:text-slate-800 dark:hover:text-slate-300 hover:bg-slate-200 dark:hover:bg-white/5"
          }`}
        >
          {activeView === id && (
            <span className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 bg-primary rounded-r-full" />
          )}
          <span className="material-symbols-outlined text-xl">{icon}</span>
        </button>
      ))}
    </div>
  );
}
