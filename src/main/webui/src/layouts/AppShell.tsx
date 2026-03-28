import React, { useState, useEffect, useMemo, useRef } from 'react'
import { TabBar } from '@/features/editor/components/TabBar'
import { MarkdownEditor } from '@/features/editor/components/MarkdownEditor'
import { useEditorStore } from '@/features/editor/store/useEditorStore'
import { useTheme } from '@/core/hooks/useTheme'
import { useNotesList, useFoldersList, useCreateNote, useCreateFolder, useUpdateNote, useDeleteNote, useMoveNode } from '@/features/notes/hooks/useNotes'

import { IconExplorer, IconSun, IconMoon, IconTrash, IconFile, IconNewFile, IconEdit, IconNewFolder, IconRefresh, IconCollapseAll, IconChevronRight } from '@/core/components/Icons'

// UI Helpers for Context Menu
function ContextMenuItem({ onClick, icon, label, danger = false }: { onClick: () => void, icon?: React.ReactNode, label: string, danger?: boolean }) {
  return (
    <button 
      className={[
        "w-full flex items-center gap-2.5 text-left px-2 py-1.5 mx-1 rounded-sm transition-colors",
         danger 
           ? "hover:bg-red-500 hover:text-white text-red-500" 
           : "hover:bg-[var(--color-accent)] hover:text-white text-[var(--color-text-primary)]"
      ].join(' ')}
      style={{ width: 'calc(100% - 8px)' }}
      onClick={onClick}
    >
      {icon && <span className="opacity-80 shrink-0">{icon}</span>}
      <span className="flex-1">{label}</span>
    </button>
  )
}

function ContextMenuSeparator() {
  return <div className="h-px bg-[var(--color-border-subtle)] my-1 mx-1.5" />
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
      let node = (current.children || []).find(c => c.name === part && c.type === 'folder')
      if (!node) {
        node = { type: 'folder', name: part, path: currentPath, children: [] }
        if (!current.children) current.children = []
        current.children.push(node)
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
      
      let node = (current.children || []).find(c => c.name === parts[i] && c.type === (isFile ? 'file' : 'folder'))
      if (!node) {
        node = {
          type: isFile ? 'file' : 'folder',
          name: parts[i],
          path: currentPath,
          children: isFile ? undefined : [],
          note: isFile ? note : undefined
        }
        if (!current.children) current.children = []
        current.children.push(node)
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
  return root.children || []
}

// Inline input component for creating or renaming nodes
function InlineInput({ type, depth, initialValue = '', onCommit, onCancel }: { type: 'file' | 'folder', depth: number, initialValue?: string, onCommit: (name: string) => void, onCancel: () => void }) {
  const [value, setValue] = useState(initialValue)
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
        <span className="text-[var(--color-text-muted)] shrink-0 opacity-60">
          {type === 'file' ? <IconNewFile size={13} /> : <IconNewFolder size={13} />}
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

function TreeNode({ node, depth = 0, creatingState, renamingState, onCommitCreate, onCancelCreate, onCommitRename, onCancelRename, onContextMenu }: { 
  node: FileNode, 
  depth?: number,
  creatingState: { parentPath: string, type: 'file' | 'folder' } | null,
  renamingState: { path: string, type: 'file'|'folder' } | null,
  onCommitCreate: (path: string) => void,
  onCancelCreate: () => void,
  onCommitRename: (oldPath: string, newName: string, type: 'file'|'folder') => void,
  onCancelRename: () => void,
  onContextMenu: (e: React.MouseEvent, type: 'file' | 'folder', path: string) => void
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
    const target = targetPath ? `${targetPath}/${sourceName}` : (sourceName || '')
    
    if (source !== target && !target.startsWith(source + '/')) {
      moveNodeMutate({ source, target })
    }
  }

  if (renamingState?.path === node.path) {
    return (
      <InlineInput 
        type={node.type} 
        depth={depth} 
        initialValue={node.type === 'file' ? node.name.replace(/\.md$/, '') : node.name}
        onCommit={(newName) => onCommitRename(node.path, newName, node.type)}
        onCancel={onCancelRename}
      />
    )
  }

  if (node.type === 'file') {
    return (
      <div 
        draggable
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onContextMenu={(e) => onContextMenu(e, 'file', node.path)}
        className={[
          "group flex justify-between items-center text-[13px] text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] cursor-pointer transition-all duration-[var(--duration-fast)]",
          isDragOver ? "bg-[var(--color-accent)]/10 shadow-[inset_2px_0_0_var(--color-accent)]" : "",
          isSelected ? "bg-[var(--color-surface-hover)] text-[var(--color-text-primary)] shadow-[inset_2px_0_0_var(--color-accent)]" : "hover:bg-[var(--color-surface-hover)]/60"
        ].join(' ')}
        onClick={(e) => { 
          e.stopPropagation(); 
          if (node.note) {
            setSelectedPath(node.path); 
            openNote(node.note.id, node.note.title, node.note.content);
          }
        }}
      >
        <div className="flex-1 py-1.5 flex items-center gap-2 truncate" style={{ paddingLeft: `${depth * 12 + 8}px` }}>
          <IconFile 
            size={13.5}
            className={[
              "shrink-0 transition-opacity duration-[var(--duration-normal)]",
              isSelected ? "text-[var(--color-text-primary)] opacity-100" : "text-[var(--color-text-secondary)] opacity-40 group-hover:opacity-80"
            ].join(' ')} 
          />
          <span className={`truncate flex-1 tracking-tight ${isSelected ? 'text-[var(--color-text-primary)] font-medium' : 'text-[var(--color-text-secondary)]'}`}>{node.name.replace(/\.md$/, '')}</span>
        </div>
        <button 
          className="opacity-0 group-hover:opacity-100 p-1 mr-2 text-[var(--color-text-muted)] hover:text-red-500 hover:bg-red-500/10 rounded-md transition-all"
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
        className={[
          "group flex justify-between items-center text-[13px] font-semibold text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] cursor-pointer transition-all duration-[var(--duration-fast)] select-none",
          isDragOver ? "bg-[var(--color-accent)]/10 shadow-[inset_2px_0_0_var(--color-accent)] text-[var(--color-text-primary)]" : "",
          isSelected ? "bg-[var(--color-surface-hover)] text-[var(--color-text-primary)] shadow-[inset_2px_0_0_var(--color-accent)]" : "hover:bg-[var(--color-surface-hover)]/60"
        ].join(' ')}
        onClick={(e) => { e.stopPropagation(); setSelectedPath(node.path); toggleFolder(node.path) }}
        onContextMenu={(e) => onContextMenu(e, 'folder', node.path)}
      >
        <div className="flex-1 py-1.5 flex items-center gap-2 truncate" style={{ paddingLeft: `${depth * 12 + 8}px` }}>
          <IconChevronRight 
            size={13}
            className={[
              "shrink-0 transition-transform duration-[var(--duration-normal)] text-[var(--color-text-secondary)] opacity-40 group-hover:opacity-100",
              isExpanded ? "rotate-90 opacity-100" : ""
            ].join(' ')} 
          />
          <span className="truncate flex-1 tracking-tight text-[var(--color-text-secondary)] group-hover:text-[var(--color-text-primary)]">{node.name}</span>
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
              renamingState={renamingState}
              onCommitCreate={onCommitCreate}
              onCancelCreate={onCancelCreate}
              onCommitRename={onCommitRename}
              onCancelRename={onCancelRename}
              onContextMenu={onContextMenu}
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
  const { groups, contents, setContent, openNote, closeTab, collapseAllFolders, setSelectedPath, setContentOnSave, setActiveGroup, isDraggingTab, setIsDraggingTab, moveTabToNewGroup, removeDeletedNoteContext, renameNodeContext, dirtyNotes, setNoteDirty } = useEditorStore()
  const [dragSplitTarget, setDragSplitTarget] = useState<string | null>(null)
  const { data: notesData, refetch: fetchNotes } = useNotesList()
  const { data: foldersData } = useFoldersList()
  const notes = notesData || []
  const folders = foldersData || []
  
  const { mutateAsync: createNewNoteMutate } = useCreateNote()
  const { mutateAsync: createFolderMutate } = useCreateFolder()
  const { mutateAsync: saveNoteMutate } = useUpdateNote()
  const { mutateAsync: deleteNoteMutate } = useDeleteNote()
  const { mutateAsync: moveNodeMutate } = useMoveNode()

  const { theme, toggleTheme } = useTheme()
  const [creating, setCreating] = useState<{ type: 'file' | 'folder', parentPath: string } | null>(null)
  const [renaming, setRenaming] = useState<{ path: string, type: 'file'|'folder' } | null>(null)
  const [contextMenu, setContextMenu] = useState<{ x: number, y: number, type: 'file' | 'folder' | 'root', path: string } | null>(null)

  useEffect(() => {
    const hideMenu = () => setContextMenu(null)
    window.addEventListener('click', hideMenu)
    return () => window.removeEventListener('click', hideMenu)
  }, [])

  // Debounced Auto-save for all dirty notes
  const timeoutsRef = useRef<Record<string, number>>({})
  const lastContentsRef = useRef<Record<string, string>>({})

  // Helper to save a single note immediately
  const flushNote = async (id: string) => {
    if (!dirtyNotes[id]) {
      return
    }
    
    const content = contents[id]
    if (content === undefined) {
      return
    }
    
    const title = id.split('/').pop() || id
    
    // Clear pending timeout if any
    if (timeoutsRef.current[id]) {
      window.clearTimeout(timeoutsRef.current[id])
      delete timeoutsRef.current[id]
    }
    
    try {
      await saveNoteMutate({ id, req: { title, content } })
      setContentOnSave(id, title)
      setNoteDirty(id, false)
      delete lastContentsRef.current[id]
    } catch (err) {
      console.error('[AutoSave] Failed to flush note:', id, err)
    }
  }

  useEffect(() => {
    const dirtyIds = Object.keys(dirtyNotes)
    
    // 1. Cleanup timeouts for notes that are no longer dirty
    Object.keys(timeoutsRef.current).forEach(id => {
      if (!dirtyIds.includes(id)) {
        window.clearTimeout(timeoutsRef.current[id])
        delete timeoutsRef.current[id]
        delete lastContentsRef.current[id]
      }
    })

    // 2. Set or refresh timeouts for all dirty notes
    dirtyIds.forEach(id => {
      const currentContent = contents[id]
      if (currentContent === undefined) {
        return
      }
      
      const title = id.split('/').pop() || id

      // Refresh timer ONLY if content changed or no timer exists
      if (currentContent !== lastContentsRef.current[id] || !timeoutsRef.current[id]) {
        if (timeoutsRef.current[id]) {
          window.clearTimeout(timeoutsRef.current[id])
        }

        timeoutsRef.current[id] = window.setTimeout(() => {
          saveNoteMutate({ id, req: { title, content: currentContent } }).then(() => {
            setContentOnSave(id, title)
            setNoteDirty(id, false)
            delete timeoutsRef.current[id]
            delete lastContentsRef.current[id]
          }).catch(err => {
            console.error('[AutoSave] Save failed:', id, err)
          })
        }, 800)
        
        lastContentsRef.current[id] = currentContent
      }
    })
  }, [contents, dirtyNotes, saveNoteMutate, setContentOnSave, setNoteDirty])

  // Component unmount cleanup - flush all dirty notes
  useEffect(() => {
    return () => {
      const ids = Object.keys(timeoutsRef.current)
      ids.forEach(id => {
        window.clearTimeout(timeoutsRef.current[id])
        // On unmount we can't easily wait for async save, 
        // but the app state is going away anyway.
        // In a real desktop app we might want to block unmount,
        // but here we just cleanup.
      })
    }
  }, [])


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

  const handleCommitRename = async (oldPath: string, newName: string, type: 'file' | 'folder') => {
    let parentPath = ''
    if (oldPath.includes('/')) {
      const parts = oldPath.split('/')
      parts.pop()
      parentPath = parts.join('/')
    }
    
    // Auto appends extension if needed
    const finalName = (type === 'file' && !newName.endsWith('.md')) ? `${newName}.md` : newName
    const target = parentPath ? `${parentPath}/${finalName}` : finalName
    
    if (oldPath !== target) {
      await moveNodeMutate({ source: oldPath, target })
      renameNodeContext(oldPath, target)
    }
    setRenaming(null)
  }

  const handleDeleteNode = async (path: string) => {
    // Both files and folders can be removed via the same mutate endpoint.
    await deleteNoteMutate(path)
    removeDeletedNoteContext(path)
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
        <div className="flex justify-between items-center px-4 py-3 shrink-0 border-b border-[var(--color-border-subtle)] z-10 bg-[var(--color-sidebar-bg)] shadow-[0_4px_12px_rgba(0,0,0,0.02)]">
          <span className="text-[10px] font-bold tracking-[0.2em] uppercase text-[var(--color-text-secondary)] whitespace-nowrap overflow-hidden">
            Synapse Vault
          </span>
          <div className="flex items-center gap-1 opacity-0 group-hover/sidebar:opacity-100 transition-all duration-[var(--duration-normal)]">
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-accent)] p-1 rounded hover:bg-[var(--color-accent-subtle)] transition-colors" onClick={() => handleStartCreate('file')} title="New File">
              <IconNewFile size={16} />
            </button>
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-accent)] p-1 rounded hover:bg-[var(--color-accent-subtle)] transition-colors" onClick={() => handleStartCreate('folder')} title="New Folder">
              <IconNewFolder size={16} />
            </button>
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] p-1 rounded hover:bg-[var(--color-surface-hover)] transition-colors" onClick={() => fetchNotes()} title="Refresh Explorer">
              <IconRefresh size={16} />
            </button>
            <button className="text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] p-1 rounded hover:bg-[var(--color-surface-hover)] transition-colors" onClick={() => collapseAllFolders()} title="Collapse Folders">
              <IconCollapseAll size={16} />
            </button>
          </div>
        </div>
        <div 
          className="flex-1 overflow-y-auto overflow-x-hidden py-2" 
          onClick={() => setSelectedPath(null)}
          onContextMenu={(e) => {
            e.preventDefault()
            e.stopPropagation()
            setContextMenu({ x: e.clientX, y: e.clientY, type: 'root', path: '' })
          }}
        >
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
                  renamingState={renaming}
                  onCommitCreate={handleCommitCreate}
                  onCancelCreate={() => setCreating(null)}
                  onCommitRename={handleCommitRename}
                  onCancelRename={() => setRenaming(null)}
                  onContextMenu={(e, type, path) => {
                    e.preventDefault()
                    e.stopPropagation()
                    setSelectedPath(path)
                    setContextMenu({ x: e.clientX, y: e.clientY, type, path })
                  }}
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
                    onCloseTab={(id) => {
                      console.log("[AppShell] Tab close requested:", id)
                      flushNote(id).then(() => {
                        console.log("[AppShell] Flush done, closing tab:", id)
                        closeTab(group.id, id)
                      })
                    }}
                 />
                 {group.activeTabId ? (
                    <MarkdownEditor
                      key={group.activeTabId}
                      content={activeContent}
                      onChange={(v) => { 
                        if (group.activeTabId) {
                          const current = useEditorStore.getState().contents[group.activeTabId]
                          if (current !== v) {
                            setContent(group.activeTabId, v)
                            setNoteDirty(group.activeTabId, true)
                          }
                        }
                      }}
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

      {/* Custom Context Menu */}
      {contextMenu && (
        <div 
          className="fixed z-[100] bg-[var(--color-bg-secondary)] border border-[var(--color-border)] rounded-md py-1.5 flex flex-col min-w-[200px] shadow-2xl text-[13px] backdrop-blur-md bg-opacity-95"
          style={{ top: contextMenu.y, left: contextMenu.x }}
          onClick={(e) => e.stopPropagation()}
        >
          {contextMenu.type === 'file' ? (
            <>
              <ContextMenuItem 
                label="Rename" 
                icon={<IconEdit />} 
                onClick={() => { setRenaming({ path: contextMenu.path, type: 'file' }); setContextMenu(null) }} 
              />
              <ContextMenuSeparator />
              <ContextMenuItem 
                label="Delete" 
                danger 
                icon={<IconTrash />} 
                onClick={() => { handleDeleteNode(contextMenu.path); setContextMenu(null) }} 
              />
            </>
          ) : contextMenu.type === 'folder' ? (
            <>
              <ContextMenuItem 
                label="New File" 
                icon={<IconNewFile />} 
                onClick={() => {
                  useEditorStore.getState().expandFolder(contextMenu.path)
                  setCreating({ type: 'file', parentPath: contextMenu.path })
                  setContextMenu(null)
                }} 
              />
              <ContextMenuSeparator />
              <ContextMenuItem 
                label="Rename" 
                icon={<IconEdit />} 
                onClick={() => { setRenaming({ path: contextMenu.path, type: 'folder' }); setContextMenu(null) }} 
              />
              <ContextMenuItem 
                label="Delete" 
                danger 
                icon={<IconTrash />} 
                onClick={() => { handleDeleteNode(contextMenu.path); setContextMenu(null) }} 
              />
            </>
          ) : contextMenu.type === 'root' ? (
            <ContextMenuItem 
              label="New File" 
              icon={<IconNewFile />} 
              onClick={() => { setCreating({ type: 'file', parentPath: '' }); setContextMenu(null) }} 
            />
          ) : null}
        </div>
      )}
    </div>
  )
}
