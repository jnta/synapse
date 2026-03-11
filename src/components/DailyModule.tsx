import { Sunrise, Plus } from "lucide-react";

export function DailyModule() {
  return (
    <div className="flex flex-col gap-2 p-3 border-b border-zinc-200 dark:border-zinc-800">
      <div className="flex items-center justify-between text-zinc-500 dark:text-zinc-400">
        <div className="flex items-center gap-2 font-medium text-xs uppercase tracking-wider">
          <Sunrise size={14} />
          <span>Daily</span>
        </div>
        <button className="hover:text-amber-600 dark:hover:text-amber-400 transition-colors">
          <Plus size={14} />
        </button>
      </div>
      <div className="flex flex-col gap-1 text-sm">
        <button className="text-left px-2 py-1.5 rounded-md bg-amber-100/50 text-amber-900 dark:bg-amber-900/20 dark:text-amber-200 font-medium truncate">
          2026-03-11.md
        </button>
        <button className="text-left px-2 py-1.5 text-zinc-600 hover:bg-zinc-100 dark:text-zinc-300 dark:hover:bg-zinc-800/50 rounded-md truncate transition-colors">
          2026-03-10.md
        </button>
      </div>
    </div>
  );
}
