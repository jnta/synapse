import { useEffect, useState } from "react";
import { listVaultNotes } from "@/services/tauri.ts";
import { DailyModule } from "@/components/DailyModule.tsx";
import { ResourceModule } from "@/components/ResourceModule.tsx";
import { ProjectModule } from "@/components/ProjectModule.tsx";
import type { NoteEntry, VaultNotes } from "@/services/tauri.ts";

interface Props {
  onOpen: (note: NoteEntry) => void;
}

export function TheTopography({ onOpen }: Props) {
  const [vault, setVault] = useState<VaultNotes>({ daily: [], resources: [], projects: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    listVaultNotes()
      .then(setVault)
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <div className="flex-1 overflow-y-auto custom-scrollbar p-3 space-y-4">
        {loading ? (
          <div className="space-y-4 animate-pulse">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="space-y-1.5">
                <div className="h-2 bg-slate-200 dark:bg-white/10 rounded w-2/5 mx-2"></div>
                <div className="h-7 bg-slate-100 dark:bg-white/5 rounded"></div>
                <div className="h-7 bg-slate-100 dark:bg-white/5 rounded"></div>
              </div>
            ))}
          </div>
        ) : (
          <>
            <DailyModule notes={vault.daily} onOpen={onOpen} />
            <ResourceModule notes={vault.resources} onOpen={onOpen} />
            <ProjectModule notes={vault.projects} onOpen={onOpen} />
          </>
        )}
      </div>
    </div>
  );
}
