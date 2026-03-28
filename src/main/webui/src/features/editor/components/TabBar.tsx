import type { Tab } from '@/core/types/Tab'
import { useEditorStore } from '@/features/editor/store/useEditorStore'

interface TabItemProps {
  tab: Tab
  isActive: boolean
  onSelect: () => void
  onClose: (e: React.MouseEvent) => void
}

function TabItem({ tab, isActive, onSelect, onClose }: TabItemProps) {
  return (
    <button
      role="tab"
      aria-selected={isActive}
      onClick={onSelect}
      className={[
        'group relative flex items-center gap-1.5 px-3 min-w-[100px] max-w-[200px] h-full',
        'text-[11px] border-r whitespace-nowrap overflow-hidden shrink-0 select-none',
        'transition-colors duration-[var(--duration-fast)]',
        'border-r-[var(--color-border)]',
        isActive
          ? 'bg-[var(--color-tab-bg-active)] text-[var(--color-text-primary)]'
          : 'bg-[var(--color-tab-bg)] text-[var(--color-text-secondary)] hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-text-primary)]',
      ].join(' ')}
    >
      <span className="flex-1 overflow-hidden text-ellipsis">{tab.name}</span>
      <span
        role="button"
        aria-label={`Close ${tab.name}`}
        onClick={onClose}
        className={[
          'flex items-center justify-center w-4 h-4 rounded text-sm leading-none shrink-0',
          'text-[var(--color-text-muted)] transition-colors duration-[var(--duration-fast)]',
          'hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-text-primary)]',
          isActive ? 'opacity-100' : 'opacity-0 group-hover:opacity-100',
        ].join(' ')}
      >
        ×
      </span>
      {isActive && (
        <span className="absolute bottom-0 left-0 right-0 h-px bg-[var(--color-accent)]" />
      )}
    </button>
  )
}

interface TabBarProps {
  tabs: Tab[]
  activeId: string | null
  onNewNote: () => void
}

export function TabBar({ tabs, activeId, onNewNote }: TabBarProps) {
  const { closeTab, setActiveId } = useEditorStore()

  return (
    <div
      role="tablist"
      className="flex items-stretch h-[var(--height-tabbar)] bg-[var(--color-tab-bg)] border-b border-[var(--color-border)] overflow-x-auto overflow-y-hidden shrink-0 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
    >
      {tabs.map((tab) => (
        <TabItem
          key={tab.id}
          tab={tab}
          isActive={tab.id === activeId}
          onSelect={() => setActiveId(tab.id)}
          onClose={(e) => {
            e.stopPropagation()
            closeTab(tab.id)
          }}
        />
      ))}
      <button
        onClick={onNewNote}
        aria-label="New tab"
        title="New tab"
        className="flex items-center justify-center w-[35px] h-full shrink-0 text-lg border-r border-[var(--color-border)] text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] hover:bg-[var(--color-surface-hover)] transition-colors duration-[var(--duration-fast)]"
      >
        +
      </button>
    </div>
  )
}
