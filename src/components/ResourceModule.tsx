import { useState } from "react";
import { FileText, Search } from "lucide-react";

interface Resource {
  title: string;
  date: string;
}

export function ResourceModule() {
  const [search, setSearch] = useState("");

  const resources: Resource[] = [
    { title: "Fog of War Concept", date: "2h ago" },
    { title: "React Context API", date: "yesterday" },
    { title: "Rust Ownership model", date: "3 days ago" },
  ];

  return (
    <div className="flex flex-1 flex-col gap-2 p-3 border-b border-zinc-200 dark:border-zinc-800 overflow-hidden">
      <div className="flex items-center justify-between text-zinc-500 dark:text-zinc-400 mb-1">
        <div className="flex items-center gap-2 font-medium text-xs uppercase tracking-wider">
          <FileText size={14} />
          <span>Resources</span>
        </div>
      </div>
      
      <div className="relative">
        <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-zinc-400" />
        <input 
          type="text" 
          placeholder="Search semantic vault..." 
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full bg-zinc-100 dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 rounded-md py-1.5 pl-8 pr-3 text-sm text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-500 focus:outline-none focus:ring-1 focus:ring-amber-500 transition-shadow"
        />
      </div>

      <div className="flex-1 overflow-y-auto no-scrollbar flex flex-col gap-1 mt-1">
        {resources.map((res, i) => (
          <button key={i} className="text-left group flex flex-col p-2 rounded-md hover:bg-zinc-100 dark:hover:bg-zinc-800/50 transition-colors">
            <span className="text-sm font-medium text-zinc-700 dark:text-zinc-200 truncate group-hover:text-amber-600 dark:group-hover:text-amber-400 transition-colors">
              {res.title}
            </span>
            <span className="text-xs text-zinc-400 dark:text-zinc-500">
              {res.date}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
}
