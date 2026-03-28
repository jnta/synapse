import React, { useState, useEffect, useMemo } from 'react'
import { TabBar } from '@/features/editor/components/TabBar'
import { MarkdownEditor } from '@/features/editor/components/MarkdownEditor'
import { useEditorStore } from '@/features/editor/store/useEditorStore'
import { useTheme } from '@/core/hooks/useTheme'
import { useNotesList, useFoldersList, useCreateNote, useCreateFolder, useUpdateNote, useDeleteNote, useMoveNode } from '@/features/notes/hooks/useNotes'

function IconExplorer() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
    </svg>
  )
}

function IconSun() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="5"></circle>
      <line x1="12" y1="1" x2="12" y2="3"></line>
      <line x1="12" y1="21" x2="12" y2="23"></line>
      <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
      <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
      <line x1="1" y1="12" x2="3" y2="12"></line>
      <line x1="21" y1="12" x2="23" y2="12"></line>
      <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
      <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
    </svg>
  )
}

function IconMoon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
    </svg>
  )
}

function IconTrash() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <polyline points="3 6 5 6 21 6"></polyline>
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
    </svg>
  )
}

function IconFile() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className="shrink-0 text-[var(--color-text-muted)] group-hover:text-[var(--color-text-secondary)] transition-colors">
      <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
      <polyline points="13 2 13 9 20 9"></polyline>
    </svg>
  )
}

function IconNewFile() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
      <polyline points="14 2 14 8 20 8"></polyline>
      <line x1="12" y1="18" x2="12" y2="12"></line>
      <line x1="9" y1="15" x2="15" y2="15"></line>
    </svg>
  )
}

function IconNewFolder() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path>
      <line x1="12" y1="11" x2="12" y2="17"></line>
      <line x1="9" y1="14" x2="15" y2="14"></line>
    </svg>
  )
}

function IconRefresh() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <polyline points="23 4 23 10 17 10"></polyline>
      <polyline points="1 20 1 14 7 14"></polyline>
      <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
    </svg>
  )
}

function IconCollapseAll() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <polyline points="4 14 12 6 20 14"></polyline>
      <line x1="12" y1="6" x2="12" y2="22"></line>
      <line x1="4" y1="2" x2="20" y2="2"></line>
    </svg>
  )
}

function IconChevronRight({ className = '' }: { className?: string }) {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={`shrink-0 ${className}`}>
      <polyline points="9 18 15 12 9 6"></polyline>
    </svg>
  )
}

// Tree helper types and functions
interface FileNode {
  type: 'file' | 'folder'
  name: string
  path: string
  note?: any
  children?: FileNode[]
}

function buildTree(notes: any[], folders: string[]): FileNode[] {
  const root: FileNode = { type: 'folder', name: 'root', path: '', children: [] }

  for (const folder of folders) {
    const parts = folder.split('/')
    let current = root
    let currentPath = ''
    for (const part of parts) {
      if (!part) continue
      currentPath = currentPath ? currentPath + '/' + part : part
      let node = current.children!.find(c => c.name === part && c.type === 'folder')
      if (!node) {
        node = { type: 'folder', name: part, path: currentPath, children: [] }
        current.children!.push(node)
      }
      current = node
    }
  }

  for (const note of notes) {
    const parts = note.id.split('/')
    let current = root
    let currentPath = ''
    for (let i = 0; i < parts.length; i++) {
      currentPath = currentPath ? currentPath + '/' + parts[i] : parts[i]
      const isFile = i === parts.length - 1
      
      let node = current.children!.find(c => c.name === parts[i] && c.type === (isFile ? 'file' : 'folder'))
      if (!node) {
        node = {
          type: isFile ? 'file' : 'folder',
          name: parts[i],
          path: currentPath,
          children: isFile ? undefined : [],
          note: isFile ? note : undefined
        }
        current.children!.push(node)
      }
      if (!isFile) {
        current = node
      }
    }
  }

  const sortTree = (node: FileNode) => {
    if (node.children) {
      node.children.sort((a, b) => {
        if (a.type === b.type) return a.name.localeCompare(b.name)
        return a.type === 'folder' ? -1 : 1
      })
      node.children.forEach(sortTree)
    }
  }
  sortTree(root)
  return root.children!
}

// Inline input component for creating nodes
function InlineInput({ type, depth, onCommit, onCancel }: { type: 'file' | 'folder', depth: number, onCommit: (name: string) => void, onCancel: () => void }) {
  const [value, setValue] = useState('')
  const [error, setError] = useState(false)

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') onCancel()
    if (e.key === 'Enter') {
      if (!value.trim()) {
        setError(true)
        return
      }
      onCommit(value.trim())
    }
  }

  return (
    <div className="flex flex-col py-1" style={{ paddingLeft: `${depth * 12 + 8}px` }}>
      <div className="flex items-center gap-1.5 bg-[var(--color-bg-secondary)] border border-[var(--color-accent)] rounded px-1.5 py-1 mr-2 shadow-sm">
        <span className="text-[var(--color-accent)] shrink-0">
          {type === 'file' ? <IconNewFile /> : <IconNewFolder />}
        </span>
        <input 
          autoFocus 
          value={value} 
          onChange={e => { setValue(e.target.value); setError(false) }}
          onKeyDown={handleKeyDown}
          onBlur={onCancel}
          className="flex-1 bg-transparent text-[13px] text-[var(--color-text-primary)] outline-none w-full min-w-0"
          placeholder={`Enter Name...`}
        />
      </div>
      {error && <span className="text-[10px] text-red-500 font-medium mt-0.5 ml-1">Name cannot be blank</span>}
    </div>
  )
}

function TreeNode({ node, depth = 0, creatingState, onCommitCreate, onCancelCreate }: { 
  node: FileNode, 
  depth?: number,
  creatingState: { parentPath: string, type: 'file' | 'folder' } | null,
  onCommitCreate: (path: string) => void,
  onCancelCreate: () => void
}) {
  const { openNote, expandedFolders, toggleFolder, selectedPath, setSelectedPath, removeDeletedNoteContext } = useEditorStore()
  const { mutateAsync: deleteNoteMutate } = useDeleteNote()
  const { mutateAsync: moveNodeMutate } = useMoveNode()
  const [isDragOver, setIsDragOver] = useState(false)
  
  const isSelected = selectedPath === node.path

  const handleDragStart = (e: React.DragEvent) => {
    e.dataTransfer.setData('text/plain', node.path)
    e.stopPropagation()
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragOver(true)
  }

  const handleDragLeave = () => {
    setIsDragOver(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragOver(false)
    const source = e.dataTransfer.getData('text/plain')
    if (!source || source === node.path) return

    let targetPath = node.path
    if (node.type === 'file') {
      const parts = node.path.split('/')
      parts.pop()
      targetPath = parts.join('/')
    }
    
    const sourceName = source.split('/').pop()
    const target = targetPath ? `${targetPath}/${sourceName}` : sourceName!
    
    if (source !== target && !target.startsWith(source + '/')) {
      moveNodeMutate({ source, target })
    }
  }

  if (node.type === 'file') {
    return (
      <div 
        draggable
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={`group flex justify-between items-center text-[13px] text-[var(--color-text-primary)] cursor-pointer transition-colors duration-[var(--duration-fast)] ${isDragOver ? 'bg-[var(--color-accent)]/20 shadow-[inset_0_0_0_1px_var(--color-accent)]' : (isSelected ? 'bg-[var(--color-surface-hover)]' : 'hover:bg-[var(--color-surface-hover)]')}`}
        onClick={(e) => { e.stopPropagation(); setSelectedPath(node.path); openNote(node.note.id, node.note.title, node.note.content) }}
      >
        <div className="flex-1 py-1.5 flex items-center gap-1.5 truncate" style={{ paddingLeft: `${depth * 12 + 8}px` }}>
          <IconFile />
          <span className="truncate flex-1">{node.name.replace(/\.md$/, '')}</span>
        </div>
        <button 
          className="opacity-0 group-hover:opacity-100 p-1 mr-2 text-[var(--color-text-muted)] hover:text-red-400 transition-opacity"
          onClick={async (e) => {
            e.stopPropagation()
            await deleteNoteMutate(node.note.id)
            removeDeletedNoteContext(node.note.id)
          }}
          title="Delete File"
        >
          <IconTrash />
        </button>
      </div>
    )
  }

  const isExpanded = expandedFolders.includes(node.path)
  const isCreatingHere = creatingState?.parentPath === node.path

  return (
    <>
      <div 
        draggable
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={`group flex justify-between items-center text-[13px] font-semibold text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] cursor-pointer transition-colors duration-[var(--duration-fast)] select-none ${isDragOver ? 'bg-[var(--color-accent)]/20 shadow-[inset_0_0_0_1px_var(--color-accent)] text-[var(--color-text-primary)]' : (isSelected ? 'bg-[var(--color-surface-hover)] text-[var(--color-text-primary)]' : 'hover:bg-[var(--color-surface-hover)]')}`}
        onClick={(e) => { e.stopPropagation(); setSelectedPath(node.path); toggleFolder(node.path) }}
      >
        <div className="flex-1 py-1.5 flex items-center gap-1.5 truncate" style={{ paddingLeft: `${depth * 12 + 8}px` }}>
          <IconChevronRight className={`transition-transform duration-150 ${isExpanded ? 'rotate-90' : ''}`} />
          <span className="truncate flex-1">{node.name}</span>
        </div>
      </div>
      
      {isExpanded && (
        <>
          {isCreatingHere && (
            <InlineInput 
              type={creatingState.type} 
              depth={depth + 1} 
              onCommit={(name) => onCommitCreate(node.path ? `${node.path}/${name}` : name)}
              onCancel={onCancelCreate}
            />
          )}
          {node.children?.map(child => (
            <TreeNode 
              key={child.path} 
              node={child} 
              depth={depth + 1} 
              creatingState={creatingState}
              onCommitCreate={onCommitCreate}
              onCancelCreate={onCancelCreate}
            />
          ))}
        </>
      )}
      
      {/* Fallback to show creating state if expanded but empty */}
      {isExpanded && node.children?.length === 0 && isCreatingHere && (
        <div className="text-[11px] text-[var(--color-text-muted)] px-3 py-1 italic" style={{ paddingLeft: `${(depth + 1) * 12 + 8}px` }}>
          Directory empty...
        </div>
      )}
    </>
  )
}

export function AppShell() {
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const { groups, contents, setContent, openNote, collapseAllFolders, setSelectedPath, setContentOnSave, setActiveGroup, isDraggingTab, setIsDraggingTab, moveTabToNewGroup } = useEditorStore()
  const [dragSplitTarget, setDragSplitTarget] = useState<string | null>(null)
  const { data: notesData, refetch: fetchNotes } = useNotesList()
  const { data: foldersData } = useFoldersList()
  const notes = notesData || []
  const folders = foldersData || []
  
  const { mutateAsync: createNewNoteMutate } = useCreateNote()
  const { mutateAsync: createFolderMutate } = useCreateFolder()
  const { mutateAsync: saveNoteMutate } = useUpdateNote()

  const { theme, toggleTheme } = useTheme()
  const [creating, setCreating] = useState<{ type: 'file' | 'folder', parentPath: string } | null>(null)

  // Simple debounce logic for saving
  useEffect(() => {
    const handles: number[] = []

    groups.forEach(g => {
      if (!g.activeTabId) return
      const currentContent = contents[g.activeTabId]
      if (currentContent === undefined) return
      
      const existingNote = notes.find(n => n.id === g.activeTabId)
      if (!existingNote) return // The note was deleted or moved; do not attempt ghost saves.
      
      let title = g.activeTabId.split('/').pop() || g.activeTabId
      const firstLineMatch = currentContent.match(/^#?\s*(.+)$/m)
      if (firstLineMatch) {
        title = firstLineMatch[1].trim()
      }

      if (existingNote.content === currentContent && existingNote.title === title) {
        return
      }

      const handler = window.setTimeout(() => {
        saveNoteMutate({ id: g.activeTabId!, req: { title, content: currentContent } }).then(() => {
          setContentOnSave(g.activeTabId!, title)
        })
      }, 1000)
      handles.push(handler)
    })

    return () => handles.forEach(h => clearTimeout(h))
  }, [groups, contents, saveNoteMutate, notes, setContentOnSave])


  const treeNodes = useMemo(() => buildTree(notes, folders), [notes, folders])

  // Determine where to create new files by default
  const getActiveParentPath = () => {
    const { selectedPath } = useEditorStore.getState()
    if (!selectedPath) return ''
    if (folders.includes(selectedPath)) {
      return selectedPath
    }
    const parts = selectedPath.split('/')
    parts.pop() // remove file name
    return parts.join('/')
  }

  const handleStartCreate = (type: 'file' | 'folder') => {
    const parentPath = getActiveParentPath()
    
    // Automatically expand parent path if not empty
    if (parentPath) {
      useEditorStore.getState().toggleFolder(parentPath) // Ensure it's inside expandedFolders
      // We push strictly if missing to avoid toggling off
      const { expandedFolders } = useEditorStore.getState()
      if (!expandedFolders.includes(parentPath)) {
         useEditorStore.setState(s => { s.expandedFolders.push(parentPath) })
      }
    }
    
    setCreating({ type, parentPath })
  }

  const handleCommitCreate = async (path: string) => {
    if (creating?.type === 'file') {
      const content = '# ' + path.split('/').pop() + '\n\n'
      const note = await createNewNoteMutate({ title: path, content })
      openNote(note.id, note.title, note.content)
      // Auto-expand the path created
      const parts = note.id.split('/')
      let currentPath = ''
      for (let i = 0; i < parts.length - 1; i++) {
        currentPath = currentPath ? currentPath + '/' + parts[i] : parts[i]
        useEditorStore.getState().expandFolder(currentPath)
      }
    } else if (creating?.type === 'folder') {
      await createFolderMutate({ path })
      useEditorStore.getState().expandFolder(path)
    }
    setCreating(null)
  }

  return (
    <div className="grid h-full w-full overflow-hidden" style={{ gridTemplateColumns: 'var(--width-activitybar) auto 1fr' }}>
      <nav
        className="flex flex-col items-center py-2 gap-0.5 bg-[var(--color-bg-secondary)] border-r border-[var(--color-border)] z-10 justify-between"
        aria-label="Activity bar"
      >
        <div className="flex flex-col gap-0.5 w-full items-center">
          <button
            className={[
              'relative flex items-center justify-center w-9 h-9 rounded transition-colors duration-[var(--duration-fast)]',
              'hover:bg-[var(--color-surface-hover)]',
              sidebarOpen
                ? 'text-[var(--color-text-primary)] before:absolute before:left-[-3px] before:top-1/2 before:-translate-y-1/2 before:w-0.5 before:h-4 before:bg-[var(--color-accent)] before:rounded-r'
                : 'text-[var(--color-text-secondary)]',
            ].join(' ')}
            onClick={() => setSidebarOpen((o) => !o)}
            aria-label="Toggle Explorer"
            title="Explorer"
          >
            <IconExplorer />
          </button>
        </div>

        <div className="flex flex-col gap-0.5 w-full items-center">
          <button
            className={[
              'relative flex items-center justify-center w-9 h-9 rounded transition-colors duration-[var(--duration-fast)]',
              'hover:bg-[var(--color-surface-hover)] text-[var(--color-text-secondary)]'
            ].join(' ')}
            onClick={toggleTheme}
            aria-label={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
            title={`Switch to ${theme === 'light' ? 'dark' : 'light'} theme`}
          >
            {theme === 'light' ? <IconMoon /> : <IconSun />}
          </button>
        </div>
      </nav>

      <aside
        className={[
          'group/sidebar flex flex-col bg-[var(--color-sidebar-bg)] border-r border-[var(--color-border)] overflow-hidden transition-all duration-[var(--duration-slow)]',
          sidebarOpen ? 'w-[var(--width-sidebar)] opacity-100' : 'w-0 opacity-0 border-r-0',
        ].join(' ')}
        aria-label="Explorer"
        aria-hidden={!sidebarOpen}
      >
        <div className="flex justify-between items-center px-4 py-3 shrink-0 border-b border-[var(--color-border-subtle)] z-10 bg-[var(--color-sidebar-bg)]">
          <span className="text-[11px] font-semibold tracking-widest uppercase text-[var(--color-text-secondary)] whitespace-nowrap overflow-hidden">
            Synapse Vault
          </span>
          <div className="flex items-center gap-1.5 opacity-0 group-hover/sidebar:opacity-100 transition-opacity">
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] p-1 rounded hover:bg-[var(--color-surface-hover)] transition-colors" onClick={() => handleStartCreate('file')} title="New File">
              <IconNewFile />
            </button>
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] p-1 rounded hover:bg-[var(--color-surface-hover)] transition-colors" onClick={() => handleStartCreate('folder')} title="New Folder">
              <IconNewFolder />
            </button>
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] p-1 rounded hover:bg-[var(--color-surface-hover)] transition-colors" onClick={() => fetchNotes()} title="Refresh Explorer">
              <IconRefresh />
            </button>
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] p-1 rounded hover:bg-[var(--color-surface-hover)] transition-colors" onClick={() => collapseAllFolders()} title="Collapse Folders">
              <IconCollapseAll />
            </button>
          </div>
        </div>
        <div className="flex-1 overflow-y-auto overflow-x-hidden py-2" onClick={() => setSelectedPath(null)}>
          {creating?.parentPath === '' && (
            <InlineInput 
              type={creating.type} 
              depth={0} 
              onCommit={handleCommitCreate} 
              onCancel={() => setCreating(null)} 
            />
          )}
          {treeNodes.length === 0 && !creating ? (
            <div className="flex flex-col items-center justify-center pt-8 px-4 gap-3 text-center">
               <p className="text-[12px] text-[var(--color-text-muted)]">You have not opened a folder.</p>
               <button onClick={() => handleStartCreate('file')} className="px-3 py-1.5 bg-[var(--color-accent)] text-white text-[12px] rounded font-medium hover:opacity-90 transition-opacity w-full">Create Note</button>
            </div>
          ) : (
            <div className="flex flex-col">
              {treeNodes.map(node => (
                <TreeNode 
                  key={node.path} 
                  node={node} 
                  creatingState={creating}
                  onCommitCreate={handleCommitCreate}
                  onCancelCreate={() => setCreating(null)}
                />
              ))}
            </div>
          )}
        </div>
      </aside>

      <main className="flex flex-col overflow-hidden bg-[var(--color-editor-bg)] min-w-0">
        <div className="flex-1 flex overflow-hidden">
           {groups.map((group, index) => {
             const activeContent = group.activeTabId ? (contents[group.activeTabId] ?? '') : ''
             return (
               <div 
                 key={group.id} 
                 className={`relative flex-1 flex flex-col min-w-0 ${index > 0 ? 'border-l border-[var(--color-border)]' : ''}`}
                 onClick={() => setActiveGroup(group.id)}
               >
                 <TabBar 
                   groupId={group.id}
                   tabs={group.tabs} 
                   activeId={group.activeTabId} 
                   showCloseGroup={groups.length > 1}
                 />
                 {group.activeTabId ? (
                   <MarkdownEditor
                     key={group.activeTabId}
                     content={activeContent}
                     onChange={(v) => setContent(group.activeTabId!, v)}
                   />
                 ) : (
                   <div className="flex-1 flex flex-col items-center justify-center text-[13px] text-[var(--color-text-muted)] gap-4">
                      <div className="opacity-50">
                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2-2h12a2 2 0 0 0 2-2V7.5L14.5 2z"></path>
                          <polyline points="14 2 14 8 20 8"></polyline>
                        </svg>
                      </div>
                      Click a note in the explorer or press <button className="font-semibold text-[color:var(--color-accent)] hover:underline" onClick={() => handleStartCreate('file')}>create a new file</button>.
                   </div>
                 )}

                 {/* Edge Drop Zone for Split Right */}
                 {isDraggingTab && (
                   <div
                     className="absolute right-0 top-0 bottom-0 w-1/4 z-50"
                     onDragOver={(e) => {
                       e.preventDefault()
                       e.dataTransfer.dropEffect = 'move'
                       setDragSplitTarget(group.id)
                     }}
                     onDragLeave={() => setDragSplitTarget(null)}
                     onDrop={(e) => {
                       e.preventDefault()
                       setDragSplitTarget(null)
                       setIsDraggingTab(false)
                       try {
                         const data = JSON.parse(e.dataTransfer.getData('application/json'))
                         if (data.groupId && typeof data.tabIndex === 'number') {
                           moveTabToNewGroup(data.groupId, data.tabIndex, group.id)
                         }
                       } catch (err) {}
                     }}
                   >
                     {dragSplitTarget === group.id && (
                       <div className="absolute inset-0 bg-[var(--color-accent)]/20 border-l-2 border-[var(--color-accent)] pointer-events-none transition-opacity" />
                     )}
                   </div>
                 )}
               </div>
             )
           })}
        </div>
      </main>
    </div>
  )
}
