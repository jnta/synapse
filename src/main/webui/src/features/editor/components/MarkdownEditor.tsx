import { useEffect, useState } from 'react'
import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { Markdown } from '@tiptap/markdown'
import Placeholder from '@tiptap/extension-placeholder'
import TaskList from '@tiptap/extension-task-list'
import TaskItem from '@tiptap/extension-task-item'
import Typography from '@tiptap/extension-typography'
import Highlight from '@tiptap/extension-highlight'
import Link from '@tiptap/extension-link'

interface Props {
  content: string
  onChange?: (value: string) => void
}

export function MarkdownEditor({ content, onChange }: Props) {
  const [isReady, setIsReady] = useState(false)
  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        bulletList: {
          keepMarks: true,
          keepAttributes: false,
        },
        orderedList: {
          keepMarks: true,
          keepAttributes: false,
        },
      }),
      Markdown.configure(),

      Placeholder.configure({
        placeholder: "Synaptic spark goes here...",
      }),


      TaskList,
      TaskItem.configure({
        nested: true,
      }),
      Typography,
      Highlight.configure({
        multicolor: true,
      }),
      Link.configure({
        openOnClick: false,
        HTMLAttributes: {
          class: 'text-[var(--color-accent)] underline cursor-pointer',
        },
      }),
    ],
    content: content || '',
    // @ts-ignore
    contentType: 'markdown',
    immediatelyRender: false,
    onUpdate: ({ editor }) => {
      if (onChange) {
        const markdown = (editor.storage as any)?.markdown?.getMarkdown?.()
        if (markdown !== undefined) {
          onChange(markdown)
        }
      }
    },
    onBeforeCreate: () => {
      // Logic before creation if needed
    },
    onCreate: () => {
      setIsReady(true)
    },
    editorProps: {
      attributes: {
        class: 'tiptap focus:outline-none min-h-full whitespace-pre-wrap',
      },
    },
  })

  useEffect(() => {
    if (editor && !editor.isFocused && isReady) {
      const currentMarkdown = (editor.storage as any)?.markdown?.getMarkdown?.()
      if (content !== currentMarkdown) {
        // @ts-ignore
        editor.commands.setContent(content || '', false, { contentType: 'markdown' })
      }
    }
  }, [content, editor, isReady])

  if (!editor || !isReady) {
    return (
      <div className="flex-1 bg-[var(--color-editor-bg)] flex items-center justify-center text-[var(--color-text-muted)] text-[12px]">
        Igniting synapses...
      </div>
    )
  }

  return (
    <div className="flex-1 flex flex-col min-h-0 bg-[var(--color-editor-bg)] overflow-hidden">
      <EditorContent 
        editor={editor} 
        className="flex-1 overflow-y-auto w-full h-full" 
      />
    </div>
  )
}

