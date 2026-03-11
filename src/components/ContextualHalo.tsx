export function ContextualHalo() {
  return (
    <>
      <div className="p-4 border-b border-synapse-border">
        <h3 className="text-[10px] font-bold text-slate-500 tracking-[0.2em] uppercase">Connections</h3>
      </div>
      <div className="flex-1 overflow-y-auto custom-scrollbar p-4 space-y-8">
        <div className="aspect-square w-full rounded-xl bg-black/40 border border-synapse-border relative overflow-hidden group cursor-pointer">
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="material-symbols-outlined text-4xl text-primary/20 group-hover:text-primary/40 transition-colors">hub</span>
          </div>
          <div className="absolute bottom-3 left-3 right-3 text-[10px] text-center text-slate-600 font-bold uppercase tracking-widest">
            Neural Graph View
          </div>
          <div className="absolute top-1/4 left-1/3 w-1.5 h-1.5 rounded-full bg-primary shadow-[0_0_8px_rgba(236,91,19,0.5)]"></div>
          <div className="absolute top-1/2 left-1/2 w-2 h-2 rounded-full bg-primary/60"></div>
          <div className="absolute bottom-1/3 right-1/4 w-1.5 h-1.5 rounded-full bg-slate-700"></div>
        </div>

        <div>
          <h4 className="text-xs font-bold text-primary mb-4 flex items-center gap-2">
            <span className="material-symbols-outlined text-sm">link</span> Backlinks
          </h4>
          <div className="space-y-2">
            <div className="p-3 rounded-lg border border-synapse-border bg-white/5 hover:border-primary/30 transition-colors cursor-pointer group">
              <span className="text-xs font-medium group-hover:text-primary transition-colors">Entropy_Optimization</span>
              <p className="text-[10px] text-slate-500 mt-1 line-clamp-1">...applied to the design system...</p>
            </div>
            <div className="p-3 rounded-lg border border-synapse-border bg-white/5 hover:border-primary/30 transition-colors cursor-pointer group">
              <span className="text-xs font-medium group-hover:text-primary transition-colors">Low_Entropy_UX</span>
              <p className="text-[10px] text-slate-500 mt-1 line-clamp-1">...the core of the Synapse model...</p>
            </div>
          </div>
        </div>

        <div>
          <h4 className="text-xs font-bold text-slate-500 mb-4 flex items-center gap-2">
            <span className="material-symbols-outlined text-sm">label</span> Tags
          </h4>
          <div className="flex flex-wrap gap-2">
            <span className="px-2 py-1 bg-white/5 text-slate-500 text-[10px] rounded hover:text-primary hover:bg-primary/10 cursor-pointer transition-all border border-transparent hover:border-primary/20">#design</span>
            <span className="px-2 py-1 bg-white/5 text-slate-500 text-[10px] rounded hover:text-primary hover:bg-primary/10 cursor-pointer transition-all border border-transparent hover:border-primary/20">#neural</span>
            <span className="px-2 py-1 bg-white/5 text-slate-500 text-[10px] rounded hover:text-primary hover:bg-primary/10 cursor-pointer transition-all border border-transparent hover:border-primary/20">#v3-spec</span>
          </div>
        </div>
      </div>
    </>
  );
}
