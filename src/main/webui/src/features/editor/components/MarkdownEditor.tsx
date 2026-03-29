import { useEffect, useRef } from 'react'
import { EditorState } from '@codemirror/state'
import { 
  EditorView, 
  keymap, 
  lineNumbers, 
  highlightActiveLineGutter, 
  highlightSpecialChars, 
  drawSelection, 
  dropCursor, 
  rectangularSelection, 
  crosshairCursor, 
  highlightActiveLine, 
  scrollPastEnd,
  placeholder,
  Decoration,
  ViewUpdate,
  ViewPlugin
} from '@codemirror/view'
import type { DecorationSet } from '@codemirror/view'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { markdown, markdownLanguage } from '@codemirror/lang-markdown'
import { 
  syntaxHighlighting, 
  HighlightStyle, 
  bracketMatching, 
  indentOnInput, 
  foldGutter, 
  foldKeymap 
} from '@codemirror/language'
import { tags as t } from '@lezer/highlight'
import { searchKeymap, highlightSelectionMatches } from '@codemirror/search'
import { autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap } from '@codemirror/autocomplete'
import { lintKeymap } from '@codemirror/lint'

// Custom Highlight Style for a more modern, VS-Code/Notion hybrid look
const customHighlightStyle = HighlightStyle.define([
  { tag: t.heading1, fontSize: '1.8em', fontWeight: '700', color: 'var(--color-text-primary)', margin: '0.67em 0' },
  { tag: t.heading2, fontSize: '1.5em', fontWeight: '600', color: 'var(--color-text-primary)' },
  { tag: t.heading3, fontSize: '1.25em', fontWeight: '600', color: 'var(--color-text-primary)' },
  { tag: t.strong, fontWeight: '700', color: 'var(--color-text-primary)' },
  { tag: t.emphasis, fontStyle: 'italic', color: 'var(--color-text-secondary)' },
  { tag: t.link, textDecoration: 'underline', color: 'var(--color-accent)' },
  { tag: t.url, color: 'var(--color-text-muted)' },
  { tag: t.strikethrough, textDecoration: 'line-through' },
  { tag: t.quote, fontStyle: 'italic', color: 'var(--color-text-secondary)' },
  { tag: t.keyword, color: 'var(--color-accent)', fontWeight: '600' },
  { tag: t.comment, color: 'var(--color-text-muted)', fontStyle: 'italic' },
  { tag: t.variableName, color: 'var(--color-text-primary)' },
  { tag: t.string, color: '#448833' },
  { tag: t.number, color: '#aa1111' },
  { tag: t.bool, color: '#aa1111' },
  { tag: t.null, color: '#aa1111' },
  { tag: t.propertyName, color: 'var(--color-accent)' },
  { tag: t.atom, color: 'var(--color-accent)' },
  { tag: t.meta, color: 'var(--color-text-muted)' },
  { tag: t.list, color: 'inherit' }
])

// Extension to highlight [[wikilinks]]
const wikilinkPlugin = ViewPlugin.fromClass(class {
  decorations: DecorationSet

  constructor(view: EditorView) {
    this.decorations = this.getDecorations(view)
  }

  update(update: ViewUpdate) {
    if (update.docChanged || update.viewportChanged) {
      this.decorations = this.getDecorations(update.view)
    }
  }

  getDecorations(view: EditorView) {
    const widgets = []
    for (const { from, to } of view.visibleRanges) {
      const text = view.state.doc.sliceString(from, to)
      const regex = /\[\[(.*?)\]\]/g
      let match
      while ((match = regex.exec(text)) !== null) {
        widgets.push(Decoration.mark({
          class: "cm-wikilink text-[var(--color-accent)] font-medium cursor-pointer hover:underline underline-offset-4"
        }).range(from + match.index, from + match.index + match[0].length))
      }
    }
    return Decoration.set(widgets)
  }
}, {
  decorations: v => v.decorations
})

interface Props {
  content: string
  onChange?: (value: string) => void
}

export function MarkdownEditor({ content, onChange }: Props) {
  const editorRef = useRef<HTMLDivElement>(null)
  const viewRef = useRef<EditorView | null>(null)
  const contentRef = useRef(content)

  useEffect(() => {
    contentRef.current = content
  }, [content])

  useEffect(() => {
    if (!editorRef.current) return

    const startState = EditorState.create({
      doc: content,
      extensions: [
        lineNumbers(),
        highlightActiveLineGutter(),
        highlightSpecialChars(),
        history(),
        drawSelection(),
        dropCursor(),
        EditorState.allowMultipleSelections.of(true),
        indentOnInput(),
        syntaxHighlighting(customHighlightStyle),
        bracketMatching(),
        closeBrackets(),
        autocompletion(),
        rectangularSelection(),
        crosshairCursor(),
        highlightActiveLine(),
        highlightSelectionMatches(),
        scrollPastEnd(),
        markdown({ base: markdownLanguage }),
        foldGutter(),
        placeholder("Capture the resonance..."),
        wikilinkPlugin,
        keymap.of([
          ...closeBracketsKeymap,
          ...defaultKeymap,
          ...searchKeymap,
          ...historyKeymap,
          ...completionKeymap,
          ...lintKeymap,
          ...foldKeymap
        ]),
        EditorView.updateListener.of((update) => {
          if (update.docChanged && onChange) {
            const newValue = update.state.doc.toString()
            if (newValue !== contentRef.current) {
              onChange(newValue)
            }
          }
        }),
        EditorView.theme({
          "&": {
            height: "100%",
            fontSize: "15px",
            backgroundColor: "var(--color-editor-bg)",
            color: "var(--color-text-primary)",
          },
          "&.cm-focused": {
            outline: "none"
          },
          ".cm-content": {
            fontFamily: "var(--font-sans)",
            padding: "32px 16px",
            lineHeight: "1.7",
            minHeight: "100%",
          },
          ".cm-gutters": {
            backgroundColor: "var(--color-editor-bg)",
            color: "var(--color-text-muted)",
            border: "none",
            borderRight: "1px solid var(--color-border-subtle)",
            minWidth: "48px",
            paddingRight: "12px",
            opacity: "0.2",
            transition: "opacity var(--duration-normal)",
            userSelect: "none",
          },
          "&:hover .cm-gutters": {
            opacity: "0.8"
          },
          ".cm-activeLine": {
            backgroundColor: "var(--color-editor-line)",
          },
          ".cm-activeLineGutter": {
            backgroundColor: "var(--color-editor-line)",
            color: "var(--color-text-primary)",
            fontWeight: "bold"
          },
          ".cm-selectionBackground, ::selection": {
            backgroundColor: "var(--color-editor-selection) !important",
            borderRadius: "2px"
          },
          ".cm-cursor, .cm-dropCursor": {
            borderLeft: "2px solid var(--color-editor-cursor)",
            marginLeft: "-1px"
          },
          ".cm-placeholder": {
            fontStyle: "italic",
            color: "var(--color-text-muted)",
            opacity: "0.5"
          },
          ".cm-foldPlaceholder": {
            backgroundColor: "var(--color-bg-tertiary)",
            border: "none",
            color: "var(--color-text-muted)",
            padding: "0 6px",
            borderRadius: "4px",
            margin: "0 4px"
          },
          ".cm-scroller": {
            scrollbarWidth: "thin",
            scrollbarColor: "var(--color-border) transparent"
          },
          // Customizing panels (search/lint)
          ".cm-panels": {
            backgroundColor: "var(--color-bg-secondary)",
            color: "var(--color-text-primary)",
            borderTop: "1px solid var(--color-border)",
          },
          ".cm-search": {
            padding: "8px 12px",
          }
        }, { dark: true }),
        EditorView.lineWrapping
      ]
    })

    const view = new EditorView({
      state: startState,
      parent: editorRef.current
    })

    viewRef.current = view

    return () => {
      view.destroy()
    }
  }, [])

  useEffect(() => {
    if (viewRef.current && content !== viewRef.current.state.doc.toString()) {
      viewRef.current.dispatch({
        changes: { from: 0, to: viewRef.current.state.doc.length, insert: content }
      })
    }
  }, [content])

  return (
    <div className="flex-1 flex flex-col min-h-0 bg-[var(--color-editor-bg)] overflow-hidden animate-in">
      <div 
        ref={editorRef} 
        className="flex-1 overflow-y-auto w-full h-full custom-scrollbar" 
      />
    </div>
  )
}
