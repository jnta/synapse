import { FolderGit2 } from "lucide-react";

interface Project {
  title: string;
  active: boolean;
}

export function ProjectModule() {
  const projects: Project[] = [
    { title: "Milestone 1: Synapse Core", active: true },
    { title: "Milestone 2: Cognitive Gym", active: false },
    { title: "Milestone 3: Neural Bridge", active: false }
  ];

  return (
    <div className="flex flex-col gap-2 p-3 pb-4">
      <div className="flex items-center justify-between text-zinc-500 dark:text-zinc-400">
        <div className="flex items-center gap-2 font-medium text-xs uppercase tracking-wider">
          <FolderGit2 size={14} />
          <span>Projects</span>
        </div>
      </div>
      <div className="flex flex-col gap-1 text-sm overflow-y-auto max-h-48 no-scrollbar">
        {projects.map((proj, i) => (
          <button 
            key={i} 
            className={`text-left px-2 py-1.5 rounded-md truncate transition-colors ${
              proj.active 
                ? "bg-zinc-200 dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 font-medium" 
                : "text-zinc-600 hover:bg-zinc-100 dark:text-zinc-300 dark:hover:bg-zinc-800/50"
            }`}
          >
            {proj.title}
          </button>
        ))}
      </div>
    </div>
  );
}
