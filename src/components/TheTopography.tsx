import { DailyModule } from "./DailyModule";
import { ResourceModule } from "./ResourceModule";
import { ProjectModule } from "./ProjectModule";
import { useTheme } from "../ThemeContext";
import { Moon, Sun, Settings } from "lucide-react";

export function TheTopography() {
  const { theme, toggleTheme } = useTheme();

  return (
    <aside className="w-64 h-full flex flex-col bg-zinc-50 dark:bg-zinc-950 border-r border-zinc-200 dark:border-zinc-800 transition-colors duration-200">
      <div className="p-3 border-b border-zinc-200 dark:border-zinc-800 flex items-center justify-between">
        <h1 className="font-bold text-sm tracking-widest text-zinc-900 dark:text-zinc-100 uppercase">
          Cerebro
        </h1>
        <div className="flex items-center gap-1">
          <button 
            onClick={toggleTheme}
            className="p-1.5 rounded-md text-zinc-500 hover:text-amber-600 dark:text-zinc-400 dark:hover:text-amber-400 hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors"
            title="Toggle Theme"
          >
            {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
          </button>
          <button 
            className="p-1.5 rounded-md text-zinc-500 hover:text-amber-600 dark:text-zinc-400 dark:hover:text-amber-400 hover:bg-zinc-200 dark:hover:bg-zinc-800 transition-colors"
            title="Settings"
          >
            <Settings size={16} />
          </button>
        </div>
      </div>
      
      <DailyModule />
      <ResourceModule />
      <ProjectModule />
    </aside>
  );
}
