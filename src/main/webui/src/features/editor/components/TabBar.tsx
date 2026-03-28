import type { Tab } from '@/core/types/Tab'
import { useEditorStore } from '@/features/editor/store/useEditorStore'
import { useState } from 'react'

interface TabItemProps {
  groupId: string
  tab: Tab
  index: number
  isActive: boolean
}

function IconFile() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className="shrink-0 opacity-60 group-hover:opacity-100 transition-opacity">
      <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
      <polyline points="13 2 13 9 20 9"></polyline>
    </svg>
  )
}

function TabItem({ groupId, tab, index, isActive }: TabItemProps) {
  const { setActiveTab, closeTab, moveTab, setIsDraggingTab } = useEditorStore()
  const [isDragOver, setIsDragOver] = useState(false)

  const handleDragStart = (e: React.DragEvent) => {
    e.dataTransfer.setData('application/json', JSON.stringify({ groupId, tabIndex: index, tabId: tab.id }))
    e.dataTransfer.effectAllowed = 'move'
    
    // Use setTimeout so the drag image is rendered properly before state updates
    setTimeout(() => setIsDraggingTab(true), 0)
  }

  const handleDragEnd = () => {
    setIsDraggingTab(false)
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'move'
    setIsDragOver(true)
  }

  const handleDragLeave = () => {
    setIsDragOver(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
    try {
      const data = JSON.parse(e.dataTransfer.getData('application/json'))
      if (data.groupId && typeof data.tabIndex === 'number') {
        if (data.groupId !== groupId || data.tabIndex !== index) {
          moveTab(data.groupId, data.tabIndex, groupId, index)
        }
      }
    } catch (e) {}
  }

  return (
    <button
      role="tab"
      aria-selected={isActive}
      onClick={() => setActiveTab(groupId, tab.id)}
      draggable
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      className={[
        'group relative flex items-center gap-2 px-3.5 min-w-[120px] max-w-[220px] h-full cursor-pointer',
        'text-[12px] border-r whitespace-nowrap overflow-hidden shrink-0 select-none',
        'transition-all duration-200',
        'border-r-[var(--color-border)]',
        isActive
          ? 'bg-[var(--color-editor-bg)] text-[var(--color-text-primary)] font-medium'
          : 'bg-transparent text-[var(--color-text-muted)] hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-text-primary)]',
        isDragOver ? 'border-l-2 border-l-[var(--color-accent)]' : ''
      ].join(' ')}
    >
      <div className={`transition-colors ${isActive ? 'text-[var(--color-accent)]' : ''}`}>
        <IconFile />
      </div>
      <span className="flex-1 overflow-hidden text-ellipsis text-left pt-0.5">{tab.name.replace(/\.md$/, '')}</span>
      <span
        role="button"
        aria-label={`Close ${tab.name}`}
        onClick={(e) => {
          e.stopPropagation()
          closeTab(groupId, tab.id)
        }}
        className={[
          'flex items-center justify-center w-5 h-5 rounded-md text-[14px] leading-none shrink-0',
          'text-[var(--color-text-muted)] transition-all duration-200',
          'hover:bg-red-500/10 hover:text-red-400',
          isActive ? 'opacity-100' : 'opacity-0 group-hover:opacity-100',
        ].join(' ')}
      >
        ×
      </span>
      {isActive && (
        <span className="absolute top-0 left-0 right-0 h-0.5 bg-[var(--color-accent)]" />
      )}
    </button>
  )
}

function IconSplitRight() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
      <line x1="12" y1="3" x2="12" y2="21"></line>
    </svg>
  )
}

function IconCloseGroup() {
   return (
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="18" y1="6" x2="6" y2="18"></line>
      <line x1="6" y1="6" x2="18" y2="18"></line>
    </svg>
  )
}


interface TabBarProps {
  groupId: string
  tabs: Tab[]
  activeId: string | null
  showCloseGroup?: boolean
}

export function TabBar({ groupId, tabs, activeId, showCloseGroup }: TabBarProps) {
  const { moveTab, splitGroup, closeGroup } = useEditorStore()

  const handleDragOverLast = (e: React.DragEvent) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'move'
  }

  const handleDropLast = (e: React.DragEvent) => {
    e.preventDefault()
    try {
      const data = JSON.parse(e.dataTransfer.getData('application/json'))
      if (data.groupId && typeof data.tabIndex === 'number') {
        moveTab(data.groupId, data.tabIndex, groupId, tabs.length)
      }
    } catch (e) {}
  }

  return (
    <div
      role="tablist"
      className="flex items-center h-[var(--height-tabbar)] bg-[var(--color-tab-bg)] border-b border-[var(--color-border)] overflow-hidden shrink-0"
    >
      <div 
        className="flex-1 flex items-stretch h-full overflow-x-auto overflow-y-hidden [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        onDragOver={handleDragOverLast}
        onDrop={handleDropLast}
      >
        {tabs.map((tab, idx) => (
          <TabItem
            key={tab.id}
            groupId={groupId}
            index={idx}
            tab={tab}
            isActive={tab.id === activeId}
          />
        ))}
        {/* Drop zone for empty space after tabs */}
        <div className="flex-1 min-w-[20px] h-full" />
      </div>
      
      {/* Actions */}
      <div className="flex items-center px-2 shrink-0 h-full gap-1">
        {activeId && (
          <button
            onClick={() => splitGroup(groupId, activeId)}
            className="p-1.5 flex items-center justify-center text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] hover:bg-[var(--color-surface-hover)] rounded transition-all duration-200"
            title="Split Right"
          >
            <IconSplitRight />
          </button>
        )}
        {showCloseGroup && (
          <button
            onClick={() => closeGroup(groupId)}
            className="p-1.5 flex items-center justify-center text-[var(--color-text-muted)] hover:text-red-400 hover:bg-red-400/10 rounded transition-all duration-200"
            title="Close Group"
          >
            <IconCloseGroup />
          </button>
        )}
      </div>
    </div>
  )
}
