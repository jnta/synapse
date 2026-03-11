import CodeMirror from "@uiw/react-codemirror";
import { markdown, markdownLanguage } from "@codemirror/lang-markdown";
import { languages } from "@codemirror/language-data";
import { useTheme } from "@/ThemeContext.tsx";
import type { NoteEntry } from "@/services/tauri.ts";

interface Props {
  activeTab: NoteEntry | null;
  content: string;
  onChange: (value: string) => void;
  onCreateNote: () => void;
  onOpenNote: () => void;
}

function WelcomeScreen({ onCreateNote, onOpenNote }: { onCreateNote: () => void; onOpenNote: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center h-full gap-8 text-center px-8">
      <div>
        <span className="material-symbols-outlined text-6xl text-primary/30 mb-4 block">hub</span>
        <h1 className="text-2xl font-bold dark:text-white mb-2">No file open</h1>
        <p className="text-sm text-slate-500">Open a note from the explorer or create a new one.</p>
      </div>
      <div className="flex gap-3">
        <button
          onClick={onCreateNote}
          className="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:opacity-90 transition-all text-sm font-medium shadow-lg shadow-primary/20"
        >
          <span className="material-symbols-outlined text-sm">add</span> Create Note
        </button>
        <button
          onClick={onOpenNote}
          className="flex items-center gap-2 px-4 py-2 border border-synapse-border dark:text-slate-300 rounded-lg hover:border-primary/50 transition-colors text-sm font-medium"
        >
          <span className="material-symbols-outlined text-sm">folder_open</span> Open Note
        </button>
      </div>
      <div className="flex flex-col gap-2 text-xs text-slate-600">
        <kbd className="px-2 py-1 bg-slate-100 dark:bg-white/5 rounded border border-slate-200 dark:border-synapse-border font-mono">Ctrl+P</kbd>
        <span>Quick open</span>
      </div>
    </div>
  );
}

export function EditorModule({ activeTab, content, onChange, onCreateNote, onOpenNote }: Props) {
  const { theme } = useTheme();

  if (!activeTab) {
    return <WelcomeScreen onCreateNote={onCreateNote} onOpenNote={onOpenNote} />;
  }

  return (
    <div className="flex flex-col h-full">
      <div className="px-8 pt-8 pb-4 border-b border-synapse-border/50 shrink-0">
        <div className="flex items-center gap-2 text-slate-400 text-xs font-bold tracking-[0.15em] uppercase mb-3">
          <span className="material-symbols-outlined text-sm">description</span>
          {activeTab.folder} / {activeTab.name}
        </div>
        <h1 className="text-3xl font-bold tracking-tight dark:text-white">{activeTab.name}</h1>
      </div>

      <div className="markdown-editor flex-1 overflow-hidden px-8 py-6">
        <CodeMirror
          key={`${activeTab.folder}/${activeTab.name}`}
          value={content}
          height="100%"
          theme={theme === "dark" ? "dark" : "light"}
          extensions={[markdown({ base: markdownLanguage, codeLanguages: languages })]}
          onChange={onChange}
          className="h-full text-base bg-transparent"
        />
        <style>{`
          .cm-editor { height: 100%; background-color: transparent; }
          .cm-scroller { font-family: inherit !important; }
          .cm-content { padding: 0 !important; }
          .cm-activeLine { background-color: transparent !important; }
          .cm-gutters { background-color: transparent !important; border-right: none !important; opacity: 0.3; }
        `}</style>
      </div>
    </div>
  );
}
