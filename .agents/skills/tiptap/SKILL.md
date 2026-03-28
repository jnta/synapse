---
name: Tiptap Markdown Editor
description: Expertise in Tiptap-based Markdown editing, including custom extensions, synchronization, and layout integration for Synapse.
---

# 🧠 Synaptic Skill: Tiptap Markdown Editor


## 🔗 Synaptic Trigger: When to Use
This skill must be activated when:
- **Modifying Editor Features:** Working on `MarkdownEditor.tsx` or any component in `src/main/webui/src/features/editor/`.
- **Handling Synchronization:** Debugging or implementing Markdown input/output, file saving logic, or content reconciliation.
- **Implementing Split Panes/Tabs:** Managing multiple editor instances and ensuring each maintains its correct state and focus.
- **Visual Design:** Adjusting the styling, min-height, or scrolling behavior of the editor within the Electron shell.
- **Adding Extensions:** Incorporating new Tiptap functionality like math support, code blocks, or custom wikilink rendering.

Tiptap is a headless rich-text editor framework built on top of ProseMirror that lets you build custom editors tailored to your needs.
 It provides a modular, framework-agnostic API with React, Vue, Svelte, and vanilla JavaScript support, allowing developers to add only the extensions they need while keeping bundle sizes small. The platform includes the open-source Tiptap Editor core, plus paid cloud services for real-time collaboration, comments, AI features, and document conversion.

The Tiptap ecosystem consists of several integrated products: the Editor (core rich-text editing), Collaboration (real-time multi-user editing via Yjs), Comments (threaded discussions), Content AI (AI-powered content generation and editing), and Conversion (import/export to DOCX, PDF, Markdown). All cloud services are accessed through REST APIs with JWT authentication, enabling seamless integration into any application stack.

## Core Patterns

## Editor Installation (React)

Install the Tiptap Editor with React bindings and the StarterKit which includes commonly used extensions like paragraphs, headings, bold, italic, and more.

```bash
npm install @tiptap/react @tiptap/pm @tiptap/starter-kit
```

```tsx
// src/Tiptap.tsx
import { useEditor, EditorContent } from '@tiptap/react'
import { FloatingMenu, BubbleMenu } from '@tiptap/react/menus'
import StarterKit from '@tiptap/starter-kit'

const Tiptap = () => {
  const editor = useEditor({
    extensions: [StarterKit],
    content: '<p>Hello World!</p>',
  })

  return (
    <>
      <EditorContent editor={editor} />
      <FloatingMenu editor={editor}>This is the floating menu</FloatingMenu>
      <BubbleMenu editor={editor}>This is the bubble menu</BubbleMenu>
    </>
  )
}

export default Tiptap
```

## Editor Instance API

The Editor class is the central building block that creates the ProseMirror editor view and manages state. It provides methods for content manipulation, state queries, and lifecycle management.

```javascript
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'

const editor = new Editor({
  element: document.querySelector('.editor'),
  extensions: [StarterKit],
  content: '<p>Initial content</p>',
  editable: true,
  autofocus: 'end',

  // Event handlers
  onCreate({ editor }) {
    console.log('Editor ready')
  },
  onUpdate({ editor }) {
    console.log('Content changed:', editor.getHTML())
  },
  onSelectionUpdate({ editor }) {
    console.log('Selection:', editor.state.selection)
  },
})

// Get content in different formats
const html = editor.getHTML()
const json = editor.getJSON()
const text = editor.getText({ blockSeparator: '\n\n' })

// Check states
editor.isActive('bold')
editor.isActive('heading', { level: 2 })
editor.can().undo()

// Execute commands
editor.chain().focus().toggleBold().run()
editor.commands.setContent('<p>New content</p>')

// Make read-only
editor.setEditable(false)

// Cleanup
editor.destroy()
```

## Editor Events

Hook into various editor lifecycle and interaction events to respond to changes. Events can be registered via configuration, binding, or within custom extensions.

```javascript
const editor = new Editor({
  extensions: [StarterKit],

  onBeforeCreate({ editor }) {
    // Before the view is created
  },
  onCreate({ editor }) {
    // Editor is fully initialized
  },
  onUpdate({ editor }) {
    // Content changed - save to database
    saveDocument(editor.getJSON())
  },
  onSelectionUpdate({ editor }) {
    // Selection changed - update toolbar state
    updateToolbar(editor)
  },
  onTransaction({ editor, transaction }) {
    // Any state change
  },
  onFocus({ editor, event }) {
    // Editor gained focus
  },
  onBlur({ editor, event }) {
    // Editor lost focus
  },
  onDestroy() {
    // Editor being destroyed
  },
  onPaste(event, slice) {
    // Content pasted
  },
  onDrop(event, slice, moved) {
    // Content dropped
  },
})

// Dynamic event binding
editor.on('update', ({ editor }) => {
  console.log('Updated:', editor.getJSON())
})

// Unbind events
const onUpdate = () => console.log('update')
editor.on('update', onUpdate)
editor.off('update', onUpdate)
```

## Creating Custom Extensions

Extensions add new capabilities to Tiptap. Create custom nodes, marks, or functionality extensions to extend the editor.

```javascript
import { Extension, Node, Mark } from '@tiptap/core'

// Custom functionality extension
const CustomExtension = Extension.create({
  name: 'customExtension',

  addOptions() {
    return {
      myOption: 'default',
    }
  },

  addCommands() {
    return {
      myCommand: () => ({ commands }) => {
        return commands.insertContent('Hello!')
      },
    }
  },

  addKeyboardShortcuts() {
    return {
      'Mod-Shift-x': () => this.editor.commands.myCommand(),
    }
  },

  onCreate({ editor }) {
    console.log('Extension initialized')
  },

  onUpdate({ editor }) {
    console.log('Content updated')
  },
})

// Custom node extension
const CustomNode = Node.create({
  name: 'customNode',
  group: 'block',
  content: 'inline*',

  parseHTML() {
    return [{ tag: 'custom-node' }]
  },

  renderHTML({ HTMLAttributes }) {
    return ['custom-node', HTMLAttributes, 0]
  },
})

// Register extensions
const editor = new Editor({
  extensions: [
    StarterKit,
    CustomExtension.configure({ myOption: 'custom' }),
    CustomNode,
  ],
})
```

## Collaboration Setup

Enable real-time collaborative editing using the TiptapCollabProvider which synchronizes document state across multiple users via WebSocket connections to Tiptap Cloud.

```bash
npm install @tiptap/extension-collaboration @tiptap/y-tiptap yjs y-protocols
npm install @tiptap-pro/provider
```

```tsx
import { useEditor, EditorContent } from '@tiptap/react'
import { useEffect } from 'react'
import Collaboration from '@tiptap/extension-collaboration'
import { TiptapCollabProvider } from '@tiptap-pro/provider'
import * as Y from 'yjs'

const doc = new Y.Doc()

export default function CollaborativeEditor() {
  const editor = useEditor({
    extensions: [
      Document,
      Paragraph,
      Text,
      Collaboration.configure({
        document: doc,
      }),
    ],
  })

  useEffect(() => {
    const provider = new TiptapCollabProvider({
      name: 'my-document-id',
      appId: 'YOUR_APP_ID',
      token: 'YOUR_JWT_TOKEN',
      document: doc,

      onSynced() {
        // Set initial content only once
        if (!doc.getMap('config').get('initialContentLoaded') && editor) {
          doc.getMap('config').set('initialContentLoaded', true)
          editor.commands.setContent('<p>Welcome to collaborative editing!</p>')
        }
      },
    })

    return () => provider.destroy()
  }, [editor])

  return <EditorContent editor={editor} />
}
```

## Document Management REST API

The Collaboration Management API provides RESTful endpoints for creating, reading, updating, and deleting documents stored on Tiptap Cloud.

```bash
# Create a document with JSON content
curl -X POST "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "doc",
    "content": [
      {
        "type": "paragraph",
        "content": [{"type": "text", "text": "Hello World"}]
      }
    ]
  }' \
  --url-query "format=json"

# Get a document as JSON
curl "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"

# Get document as Yjs binary
curl "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document?format=yjs" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  --output document.yjs

# List all documents with pagination
curl "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents?take=100&skip=0" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"

# Delete a document
curl -X DELETE "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"
```

## Document Version Management API

Create and manage document versions (snapshots) with the version management endpoints. Versions capture document state at specific points in time.

```bash
# Create a new version
curl -X POST "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/versions" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Version 1.0",
    "meta": {"author": "user123", "notes": "Initial release"}
  }'

# List all versions
curl "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/versions" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"

# Get a specific version
curl "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/versions/VERSION_ID" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"

# Update version metadata
curl -X PATCH "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/versions/VERSION_ID" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Version Name"}'

# Revert document to a previous version
curl -X POST "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/versions/VERSION_ID/revertTo" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"
```

## Batch Import Documents API

Import multiple documents in bulk with version history preserved using the batch import endpoint.

```bash
curl -X PUT "https://YOUR_APP_ID.collab.tiptap.cloud/api/admin/batch-import" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  -H "Content-Type: application/json" \
  -d '[
    [
      {
        "created_at": "2024-05-01T10:00:00Z",
        "version": 0,
        "name": "document-1",
        "tiptap_json": {
          "type": "doc",
          "content": [{"type": "paragraph", "content": [{"type": "text", "text": "Version 0"}]}]
        }
      },
      {
        "created_at": "2024-05-01T11:00:00Z",
        "version": 1,
        "name": "document-1",
        "tiptap_json": {
          "type": "doc",
          "content": [{"type": "paragraph", "content": [{"type": "text", "text": "Version 1"}]}]
        }
      }
    ],
    [
      {
        "created_at": "2024-06-01T10:00:00Z",
        "version": 0,
        "name": "document-2",
        "tiptap_json": {
          "type": "doc",
          "content": [{"type": "paragraph", "content": [{"type": "text", "text": "Document 2"}]}]
        }
      }
    ]
  ]'
```

## Comments REST API

The Comments API enables managing threaded discussions on documents. Create, update, and delete comment threads and individual comments.

```bash
# Create a new thread
curl -X POST "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/threads" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This paragraph needs revision",
    "data": {"userId": "user123", "position": {"from": 10, "to": 25}}
  }'

# Get all threads for a document
curl "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/threads" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"

# Get a specific thread
curl "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/threads/THREAD_ID" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"

# Add a comment to a thread
curl -X POST "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/threads/THREAD_ID/comments" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  -H "Content-Type: application/json" \
  -d '{"content": "I agree, let me fix this", "data": {"userId": "user456"}}'

# Resolve a thread
curl -X PATCH "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/threads/THREAD_ID" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS" \
  -H "Content-Type: application/json" \
  -d '{"resolvedAt": "2024-01-15T10:30:00Z"}'

# Delete a comment
curl -X DELETE "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/threads/THREAD_ID/comments/COMMENT_ID" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"

# Delete entire thread
curl -X DELETE "https://YOUR_APP_ID.collab.tiptap.cloud/api/documents/my-document/threads/THREAD_ID" \
  -H "Authorization: YOUR_SECRET_FROM_SETTINGS"
```

## Webhooks Configuration

Configure webhooks to receive notifications when documents change, users connect/disconnect, or versions are created. Webhooks are signed with your secret for verification.

```javascript
// Example webhook payload for document changes
{
  "appName": "my-app",
  "name": "my-document",
  "time": "2024-01-15T10:30:00.000Z",
  "tiptapJson": {
    "type": "doc",
    "content": [{"type": "paragraph", "content": [{"type": "text", "text": "Updated content"}]}]
  },
  "clientsCount": 3,
  "type": "DOCUMENT",
  "trigger": "document.saved",
  "users": ["user1", "user2"]
}

// Example webhook handler (Node.js/Express)
const crypto = require('crypto')

app.post('/webhook', (req, res) => {
  const signature = req.headers['x-hocuspocus-signature-256']
  const payload = JSON.stringify(req.body)

  const expectedSignature = crypto
    .createHmac('sha256', process.env.WEBHOOK_SECRET)
    .update(payload)
    .digest('hex')

  if (signature !== `sha256=${expectedSignature}`) {
    return res.status(401).send('Invalid signature')
  }

  const { trigger, type, name, tiptapJson } = req.body

  if (trigger === 'document.saved') {
    // Process document update
    saveToDatabase(name, tiptapJson)
  } else if (trigger === 'user.connected') {
    // Track user presence
    notifyUserJoined(req.body.user)
  } else if (trigger === 'version.created') {
    // Version was created
    logVersionCreation(req.body.version, req.body.versionName)
  }

  res.status(200).send('OK')
})
```

## DOCX Import REST API

Convert Microsoft Word (.docx) files into Tiptap JSON format for editing. The API handles text formatting, tables, images, headers, footers, and footnotes.

```bash
# Basic DOCX import
curl -X POST "https://api.tiptap.dev/v2/convert/import/docx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -F "file=@/path/to/document.docx"

# Import with image upload callback
curl -X POST "https://api.tiptap.dev/v2/convert/import/docx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -F "file=@/path/to/document.docx" \
  -F "imageUploadCallbackUrl=https://your-api.com/upload-image"

# Import with custom node mapping
curl -X POST "https://api.tiptap.dev/v2/convert/import/docx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -F "file=@/path/to/document.docx" \
  -F 'prosemirrorNodes={"paragraph":"customParagraph","heading":"customHeading"}' \
  -F 'prosemirrorMarks={"bold":"strong","italic":"em"}'

# Import with verbose logging
curl -X POST "https://api.tiptap.dev/v2/convert/import/docx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -F "file=@/path/to/document.docx" \
  -F "verbose=7"

# Response includes content, headers, footers, and footnotes
# {
#   "data": {
#     "content": {"type": "doc", "content": [...]},
#     "header": {"type": "doc", "content": [...]},
#     "footer": {"type": "doc", "content": [...]},
#     "footnotes": {"1": {"type": "doc", "content": [...]}},
#     "endnotes": {}
#   }
# }
```

## DOCX Export REST API

Export Tiptap JSON documents to Microsoft Word (.docx) format with custom page layouts, headers, footers, and styling.

```bash
# Basic DOCX export
curl --output document.docx -X POST "https://api.tiptap.dev/v2/convert/export/docx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "doc": "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Hello World\"}]}]}",
    "exportType": "blob"
  }'

# Export with custom page size and margins
curl --output document.docx -X POST "https://api.tiptap.dev/v2/convert/export/docx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "doc": "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Document with custom layout\"}]}]}",
    "pageSize": {"width": "21.0cm", "height": "29.7cm"},
    "pageMargins": {"top": "2.0cm", "bottom": "2.0cm", "left": "1.5cm", "right": "1.5cm"}
  }'

# Export with headers and footers
curl --output document.docx -X POST "https://api.tiptap.dev/v2/convert/export/docx" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "doc": "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Main content\"}]}]}",
    "headers": {
      "evenAndOddHeaders": true,
      "differentFirstPage": true,
      "default": "Document Title",
      "first": "Cover Page",
      "even": "Even Page Header"
    },
    "footers": {
      "default": "Page {page} of {total}",
      "first": ""
    }
  }'
```

## PDF Export REST API

Export Tiptap JSON documents to PDF format with customizable page layout, headers, footers, and fonts.

```bash
# Basic PDF export
curl --output document.pdf -X POST "https://api.tiptap.dev/v2/convert/export/pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "doc": "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Hello World\"}]}]}"
  }'

# Export with custom layout
curl --output document.pdf -X POST "https://api.tiptap.dev/v2/convert/export/pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "doc": "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Custom layout PDF\"}]}]}",
    "pageSize": {"width": "210mm", "height": "297mm"},
    "pageMargins": {"top": "20mm", "bottom": "20mm", "left": "15mm", "right": "15mm"},
    "headers": {"default": "Report Title"},
    "footers": {"default": "Confidential - Page {page}"}
  }'

# Export with custom fonts (on-premises only)
curl --output document.pdf -X POST "https://api.tiptap.dev/v2/convert/export/pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "doc": "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Custom font document\"}]}]}",
    "customFonts": [
      {"url": "https://cdn.example.com/fonts/CustomFont.ttf", "fontFamily": "Custom Font"}
    ]
  }'
```

## Server AI Toolkit REST API

The Server AI Toolkit provides REST endpoints for AI-powered document operations. Execute tools to read, edit, and manipulate Tiptap documents using AI.

```bash
# Generate JWT with document server credentials
JWT_TOKEN=$(node -e "
const jwt = require('jsonwebtoken');
const token = jwt.sign({
  experimental_document_server_id: 'your-doc-server-id',
  experimental_document_server_management_api_secret: 'your-management-secret'
}, 'your-ai-secret-key', { expiresIn: '1h' });
console.log(token);
")

# Get available tool definitions
curl -X POST "https://api.tiptap.dev/v3/ai/toolkit/tools" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "schemaAwarenessData": {},
    "tools": {"tiptapRead": true, "tiptapEdit": true},
    "format": "json"
  }'

# Execute tiptapRead tool on a Cloud document
curl -X POST "https://api.tiptap.dev/v3/ai/toolkit/execute-tool" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "tiptapRead",
    "input": {"startIndex": 0, "endIndex": 1000},
    "experimental_documentOptions": {"documentId": "my-document"},
    "schemaAwarenessData": {},
    "chunkSize": 1000,
    "format": "json"
  }'

# Execute tiptapEdit tool with direct document
curl -X POST "https://api.tiptap.dev/v3/ai/toolkit/execute-tool" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "tiptapEdit",
    "input": {"operations": [{"type": "insert", "content": "New paragraph"}]},
    "document": {"type": "doc", "content": []},
    "schemaAwarenessData": {},
    "format": "shorthand"
  }'

# Get schema awareness prompt for AI models
curl -X POST "https://api.tiptap.dev/v3/ai/toolkit/schema-awareness-prompt" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "X-App-Id: YOUR_APP_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "schemaAwarenessData": {},
    "format": "json"
  }'
```

## AI Toolkit Extension Installation

Install and configure the AI Toolkit extension to add AI-powered editing capabilities directly to your Tiptap editor.

```bash
npm install @tiptap-pro/ai-toolkit
```

```tsx
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'
import { AiToolkit, getAiToolkit } from '@tiptap-pro/ai-toolkit'

const editor = new Editor({
  extensions: [
    StarterKit,
    AiToolkit,
  ],
  content: '<p>Start writing...</p>',
})

// Get the AI Toolkit instance for programmatic access
const toolkit = getAiToolkit(editor)

// Use AI features through the toolkit
// toolkit.generateText({ prompt: 'Continue this paragraph...' })
// toolkit.proofread()
// toolkit.translate({ targetLanguage: 'spanish' })
```

## React Context and State Management

Use React Context to share the editor instance across components and useEditorState to efficiently react to editor changes without unnecessary re-renders.

```tsx
import { useEditor, EditorContent, EditorContext, useCurrentEditor, useEditorState } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import { useMemo } from 'react'

// Main editor component with context provider
const TiptapEditor = () => {
  const editor = useEditor({
    extensions: [StarterKit],
    content: '<p>Hello World!</p>',
  })

  const providerValue = useMemo(() => ({ editor }), [editor])

  return (
    <EditorContext.Provider value={providerValue}>
      <Toolbar />
      <EditorContent editor={editor} />
      <JSONPreview />
    </EditorContext.Provider>
  )
}

// Toolbar component using context
const Toolbar = () => {
  const { editor } = useCurrentEditor()

  const { isBold, isItalic } = useEditorState({
    editor,
    selector: ({ editor }) => ({
      isBold: editor?.isActive('bold') ?? false,
      isItalic: editor?.isActive('italic') ?? false,
    }),
  })

  return (
    <div>
      <button
        onClick={() => editor?.chain().focus().toggleBold().run()}
        className={isBold ? 'active' : ''}
      >
        Bold
      </button>
      <button
        onClick={() => editor?.chain().focus().toggleItalic().run()}
        className={isItalic ? 'active' : ''}
      >
        Italic
      </button>
    </div>
  )
}

// Preview component using context
const JSONPreview = () => {
  const { editor } = useCurrentEditor()

  const content = useEditorState({
    editor,
    selector: ({ editor }) => editor?.getJSON(),
  })

  return <pre>{JSON.stringify(content, null, 2)}</pre>
}
```

## SSR Support (Server-Side Rendering)

Configure Tiptap for server-side rendering in Next.js or other SSR frameworks by disabling immediate rendering to prevent hydration mismatches.

```tsx
'use client'

import { useEditor, EditorContent } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'

export function MyEditor() {
  const editor = useEditor({
    extensions: [StarterKit],
    content: '<p>Hello World!</p>',
    // Disable immediate rendering for SSR
    immediatelyRender: false,
  })

  if (!editor) {
    return null // Prevent rendering until client-side initialization
  }

  return <EditorContent editor={editor} />
}

// Server-side usage without DOM (for content processing)
import { Editor } from '@tiptap/core'
import StarterKit from '@tiptap/starter-kit'

const editor = new Editor({
  element: null, // Opt-in to SSR mode without DOM
  content: {
    type: 'doc',
    content: [
      { type: 'paragraph', content: [{ type: 'text', text: 'Server processed' }] }
    ],
  },
  extensions: [StarterKit],
})

const json = editor.getJSON()
const html = editor.getHTML()
```

Tiptap serves as a complete rich-text editing solution for modern web applications, from simple content entry to complex collaborative document editing. The modular extension system allows developers to start with basic functionality and progressively add features like real-time collaboration, AI assistance, and document conversion as needed. The headless architecture provides complete styling freedom while the cloud services handle the infrastructure complexity of real-time sync, version history, and document storage.

Integration patterns typically involve setting up the core editor with React/Vue bindings, adding collaboration through the TiptapCollabProvider for multi-user editing, implementing webhooks for backend synchronization, and using the conversion APIs for document import/export workflows. The REST APIs follow consistent patterns with JWT authentication and the `X-App-Id` header, making it straightforward to integrate Tiptap cloud services into any backend stack.
