import React, { useState } from 'react'
import type { Tab } from '@/core/types/Tab'
import { IconFile, IconSplitRight, IconCloseGroup } from '@/core/components/Icons'
import { useEditorStore } from '@/features/editor/store/useEditorStore'

interface TabItemProps {
  groupId: string
  tab: Tab
  index: number
  isActive: boolean
  onClose?: (id: string) => void
}

function TabItem({ groupId, tab, index, isActive, onClose }: TabItemProps) {
  const { setActiveTab, closeTab, moveTab, setIsDraggingTab } = useEditorStore()
  const [isDragOver, setIsDragOver] = useState(false)

  const handleDragStart = (e: React.DragEvent) => {
    e.dataTransfer.setData('application/json', JSON.stringify({ groupId, tabIndex: index, tabId: tab.id }))
    e.dataTransfer.effectAllowed = 'move'
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
        'group relative flex items-center gap-2 px-4 min-w-0 max-w-[240px] flex-1 h-full cursor-pointer',
        'text-[12px] border-r whitespace-nowrap overflow-hidden select-none',
        'transition-all duration-[var(--duration-normal)]',
        'border-r-[var(--color-border-subtle)]',
        isActive
          ? 'bg-[var(--color-editor-bg)] text-[var(--color-text-primary)] font-semibold shadow-[0_-1px_0_inset_white/5]'
          : 'bg-[var(--color-tab-bg)] text-[var(--color-text-muted)] hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-text-secondary)]',
        isDragOver ? 'border-l-2 border-l-[var(--color-accent)]' : ''
      ].join(' ')}
    >
      <div className={`transition-all duration-[var(--duration-normal)] ${isActive ? 'text-[var(--color-accent)] scale-110' : 'opacity-50 group-hover:opacity-80'}`}>
        <IconFile className="shrink-0 size-3.5" />
      </div>
      
      <span className="flex-1 overflow-hidden text-ellipsis text-left pt-0.5 tracking-tight">
        {tab.name.replace(/\.md$/, '')}
      </span>

      <span
        role="button"
        aria-label={`Close ${tab.name}`}
        onClick={(e) => {
          e.stopPropagation()
          if (onClose) {
            onClose(tab.id)
          } else {
            closeTab(groupId, tab.id)
          }
        }}
        className={[
          'flex items-center justify-center w-4 h-4 rounded-md text-[14px] leading-none shrink-0',
          'transition-all duration-[var(--duration-fast)]',
          'hover:bg-red-500/20 hover:text-red-500',
          isActive ? 'opacity-60' : 'opacity-0 group-hover:opacity-60',
        ].join(' ')}
      >
        ×
      </span>
      
      {isActive && (
        <span className="absolute top-0 left-0 right-0 h-[2px] bg-[var(--color-accent)] shadow-[0_1px_3px_var(--color-accent)]" />
      )}
    </button>
  )
}

interface TabBarProps {
  groupId: string
  tabs: Tab[]
  activeId: string | null
  showCloseGroup?: boolean
  onCloseTab?: (id: string) => void
}

export function TabBar({ groupId, tabs, activeId, showCloseGroup, onCloseTab }: TabBarProps) {
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
        className="flex-1 flex items-stretch h-full overflow-x-auto overflow-y-hidden scrollbar-none"
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
            onClose={onCloseTab}
          />
        ))}
        {/* Drop zone for empty space after tabs */}
        <div className="flex-1 min-w-[32px] h-full" />
      </div>
      
      {/* Actions */}
      <div className="flex items-center px-1.5 shrink-0 h-full gap-0.5 border-l border-[var(--color-border-subtle)] bg-[var(--color-tab-bg)] shadow-[-4px_0_8px_-4px_rgba(0,0,0,0.1)]">
        {activeId && (
          <button
            onClick={() => splitGroup(groupId, activeId)}
            className="p-1 px-1.5 flex items-center justify-center text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] hover:bg-[var(--color-surface-hover)] rounded transition-all duration-[var(--duration-fast)]"
            title="Split Right"
          >
            <IconSplitRight className="size-4" />
          </button>
        )}
        {showCloseGroup && (
          <button
            onClick={() => closeGroup(groupId)}
            className="p-1 px-1.5 flex items-center justify-center text-[var(--color-text-muted)] hover:text-red-500 hover:bg-red-500/10 rounded transition-all duration-[var(--duration-fast)]"
            title="Close Group"
          >
            <IconCloseGroup className="size-4" />
          </button>
        )}
      </div>
    </div>
  )
}
