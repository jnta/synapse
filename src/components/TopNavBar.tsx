export function TopNavBar() {
  return (
    <header className="flex items-center justify-between whitespace-nowrap border-b border-synapse-border bg-background-light dark:bg-background-dark px-4 py-2.5 shrink-0">
      <div className="flex items-center gap-2">
        <span className="material-symbols-outlined text-2xl text-primary">hub</span>
        <h2 className="text-base font-bold leading-tight tracking-tight dark:text-slate-100">Synapse</h2>
      </div>
      <div className="flex gap-2">
        <button className="flex items-center justify-center rounded-xl h-8 w-8 bg-primary/5 text-primary hover:bg-primary/10 transition-all border border-primary/10">
          <span className="material-symbols-outlined text-lg">settings</span>
        </button>
        <button className="flex items-center justify-center rounded-xl h-8 w-8 bg-primary text-white hover:opacity-90 transition-all shadow-lg shadow-primary/20">
          <span className="material-symbols-outlined text-lg">person</span>
        </button>
      </div>
    </header>
  );
}
