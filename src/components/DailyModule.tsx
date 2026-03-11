import { useState } from "react";
import type { NoteEntry } from "@/services/tauri.ts";

interface Props {
  notes: NoteEntry[];
  onOpen: (note: NoteEntry) => void;
}

export function DailyModule({ notes, onOpen }: Props) {
  const [open, setOpen] = useState(true);

  return (
    <section>
      <button
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center justify-between px-2 py-1 group"
      >
        <h3 className="text-[10px] font-bold text-slate-500 dark:text-slate-600 tracking-[0.2em] uppercase">
          Daily Notes
          <span className="ml-1.5 text-[9px] opacity-60">({notes.length})</span>
        </h3>
        <span className={`material-symbols-outlined text-sm text-slate-500 transition-transform duration-200 ${open ? "rotate-180" : ""}`}>
          expand_more
        </span>
      </button>

      {open && (
        <div className="space-y-0.5 mt-1">
          {notes.length === 0 ? (
            <p className="px-2 text-[11px] text-slate-600 italic">No daily notes yet.</p>
          ) : (
            notes.map((note, i) => (
              <div
                key={i}
                onClick={() => onOpen(note)}
                className="flex items-center gap-3 p-2 rounded cursor-pointer transition-all hover:bg-slate-200 dark:hover:bg-white/5 group"
              >
                <span className="material-symbols-outlined text-lg text-slate-500 group-hover:text-primary shrink-0">calendar_today</span>
                <span className="text-sm font-medium truncate">{note.name}</span>
              </div>
            ))
          )}
        </div>
      )}
    </section>
  );
}
