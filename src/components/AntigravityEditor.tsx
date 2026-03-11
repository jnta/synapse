import { useState } from "react";
import CodeMirror from "@uiw/react-codemirror";
import { markdown, markdownLanguage } from "@codemirror/lang-markdown";
import { languages } from "@codemirror/language-data";
import { useTheme } from "../ThemeContext";

export function AntigravityEditor() {
  const { theme } = useTheme();
  const [doc, setDoc] = useState<string>("# Welcome to Cerebro\n\nThis is the high-friction cognitive gym. Friction is a feature.\n");

  return (
    <div className="flex-1 h-full flex flex-col bg-white dark:bg-[#09090b] transition-colors duration-200">
      <div className="h-10 border-b border-zinc-200 dark:border-zinc-800 flex items-center px-4 bg-zinc-50 dark:bg-zinc-950/50">
        <span className="text-sm font-medium text-zinc-500 dark:text-zinc-400">
          2026-03-11.md
        </span>
      </div>
      <div className="flex-1 overflow-hidden relative">
        <CodeMirror
          value={doc}
          height="100%"
          theme={theme}
          extensions={[markdown({ base: markdownLanguage, codeLanguages: languages })]}
          onChange={(value) => setDoc(value)}
          className="h-full text-base"
        />
        <style>
          {`
            .cm-editor {
              height: 100%;
            }
            .cm-scroller {
              font-family: inherit !important;
            }
            .cm-content {
              padding: 2rem !important;
              max-width: 800px;
              margin: 0 auto;
            }
          `}
        </style>
      </div>
    </div>
  );
}
