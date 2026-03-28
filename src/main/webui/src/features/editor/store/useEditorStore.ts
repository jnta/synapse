import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type { Tab } from '@/core/types/Tab'

export interface TabGroup {
  id: string
  tabs: Tab[]
  activeTabId: string | null
}

interface EditorUIState {
  groups: TabGroup[]
  activeGroupId: string
  selectedPath: string | null
  contents: Record<string, string>
  expandedFolders: string[]
  
  openNote: (id: string, title: string, content: string) => void
  closeTab: (groupId: string, tabId: string) => void
  setActiveTab: (groupId: string, tabId: string) => void
  setActiveGroup: (groupId: string) => void
  moveTab: (sourceGroupId: string, sourceTabIndex: number, targetGroupId: string, targetTabIndex: number) => void
  moveTabToNewGroup: (sourceGroupId: string, sourceTabIndex: number, afterGroupId: string) => void
  splitGroup: (groupId: string, tabId: string) => void
  closeGroup: (groupId: string) => void

  isDraggingTab: boolean
  setIsDraggingTab: (isDragging: boolean) => void

  setSelectedPath: (path: string | null) => void
  setContent: (id: string, content: string) => void
  setContentOnSave: (id: string, title: string) => void
  expandFolder: (path: string) => void
  toggleFolder: (path: string) => void
  collapseAllFolders: () => void
  removeDeletedNoteContext: (id: string) => void
}

export const useEditorStore = create<EditorUIState>()(
  immer((set) => ({
    groups: [{ id: 'main', tabs: [], activeTabId: null }],
    activeGroupId: 'main',
    selectedPath: null,
    contents: {},
    expandedFolders: [],
    isDraggingTab: false,

    setIsDraggingTab: (isDragging) => set((state) => { state.isDraggingTab = isDragging }),

    openNote: (id, title, content) => {
      set((state) => {
        let group = state.groups.find(g => g.id === state.activeGroupId)
        if (!group) {
          group = state.groups[0]
          state.activeGroupId = group.id
        }
        
        if (!group.tabs.find(t => t.id === id)) {
          group.tabs.push({ id, name: title })
        }
        group.activeTabId = id
        state.contents[id] = content
      })
    },

    closeTab: (groupId, tabId) =>
      set((state) => {
        const group = state.groups.find(g => g.id === groupId)
        if (!group) return
        
        const idx = group.tabs.findIndex((t) => t.id === tabId)
        if (idx !== -1) {
          group.tabs.splice(idx, 1)
          if (group.activeTabId === tabId) {
            group.activeTabId = group.tabs.length > 0 ? group.tabs[Math.max(0, idx - 1)].id : null
          }
        }
        
        if (group.tabs.length === 0 && state.groups.length > 1) {
          const gIdx = state.groups.findIndex(g => g.id === groupId)
          state.groups.splice(gIdx, 1)
          if (state.activeGroupId === groupId) {
             state.activeGroupId = state.groups[Math.max(0, gIdx - 1)].id
          }
        }

        const isTabOpenAnywhere = state.groups.some(g => g.tabs.some(t => t.id === tabId))
        if (!isTabOpenAnywhere) {
          delete state.contents[tabId]
        }
      }),

    setActiveTab: (groupId, tabId) =>
      set((state) => {
        const group = state.groups.find(g => g.id === groupId)
        if (group) {
          group.activeTabId = tabId
          state.activeGroupId = groupId
          state.selectedPath = tabId
        }
      }),
      
    setActiveGroup: (groupId) =>
      set((state) => {
        state.activeGroupId = groupId
      }),

    moveTab: (sourceGroupId, sourceTabIndex, targetGroupId, targetTabIndex) =>
      set((state) => {
        const sourceGroup = state.groups.find(g => g.id === sourceGroupId)
        const targetGroup = state.groups.find(g => g.id === targetGroupId)
        if (!sourceGroup || !targetGroup) return

        const [tab] = sourceGroup.tabs.splice(sourceTabIndex, 1)
        
        if (sourceGroup.activeTabId === tab.id) {
           sourceGroup.activeTabId = sourceGroup.tabs.length > 0 ? sourceGroup.tabs[Math.max(0, sourceTabIndex - 1)]?.id || sourceGroup.tabs[0]?.id : null
        }

        targetGroup.tabs.splice(targetTabIndex, 0, tab)
        targetGroup.activeTabId = tab.id
        state.activeGroupId = targetGroupId

        if (sourceGroup.tabs.length === 0 && state.groups.length > 1) {
          const gIdx = state.groups.findIndex(g => g.id === sourceGroupId)
          state.groups.splice(gIdx, 1)
        }
      }),

    moveTabToNewGroup: (sourceGroupId, sourceTabIndex, afterGroupId) =>
      set((state) => {
        const sourceGroup = state.groups.find(g => g.id === sourceGroupId)
        if (!sourceGroup) return
        
        const tab = sourceGroup.tabs[sourceTabIndex]
        if (!tab) return

        sourceGroup.tabs.splice(sourceTabIndex, 1)

        if (sourceGroup.activeTabId === tab.id) {
           sourceGroup.activeTabId = sourceGroup.tabs.length > 0 ? sourceGroup.tabs[Math.max(0, sourceTabIndex - 1)]?.id || sourceGroup.tabs[0]?.id : null
        }

        const newGroupId = Math.random().toString(36).substring(7)
        const newGroup: TabGroup = {
          id: newGroupId,
          tabs: [{ ...tab }],
          activeTabId: tab.id
        }

        const insertIdx = state.groups.findIndex(g => g.id === afterGroupId)
        if (insertIdx !== -1) {
           state.groups.splice(insertIdx + 1, 0, newGroup)
        } else {
           state.groups.push(newGroup)
        }

        state.activeGroupId = newGroupId

        if (sourceGroup.tabs.length === 0 && state.groups.length > 1) {
          const gIdx = state.groups.findIndex(g => g.id === sourceGroupId)
          state.groups.splice(gIdx, 1)
        }
      }),

    splitGroup: (groupId, tabId) =>
      set((state) => {
        const sourceGroup = state.groups.find(g => g.id === groupId)
        if (!sourceGroup) return

        const tab = sourceGroup.tabs.find(t => t.id === tabId)
        if (!tab) return

        const newGroupId = Math.random().toString(36).substring(7)
        const newGroup: TabGroup = {
          id: newGroupId,
          tabs: [{ ...tab }],
          activeTabId: tabId
        }

        const gIdx = state.groups.findIndex(g => g.id === groupId)
        state.groups.splice(gIdx + 1, 0, newGroup)
        state.activeGroupId = newGroupId
      }),

    closeGroup: (groupId) =>
      set((state) => {
        if (state.groups.length <= 1) return
        const gIdx = state.groups.findIndex(g => g.id === groupId)
        if (gIdx !== -1) {
          state.groups.splice(gIdx, 1)
          if (state.activeGroupId === groupId) {
             state.activeGroupId = state.groups[Math.max(0, gIdx - 1)].id
          }
        }
      }),

    removeDeletedNoteContext: (id) =>
      set((state) => {
        state.groups.forEach(g => {
          const idx = g.tabs.findIndex(t => t.id === id)
          if (idx !== -1) {
            g.tabs.splice(idx, 1)
            if (g.activeTabId === id) {
              g.activeTabId = g.tabs.length > 0 ? g.tabs[Math.max(0, idx - 1)].id : null
            }
          }
        })
        
        state.groups = state.groups.filter(g => g.tabs.length > 0 || state.groups.length === 1)
        if (!state.groups.find(g => g.id === state.activeGroupId) && state.groups.length > 0) {
           state.activeGroupId = state.groups[0].id
        }

        delete state.contents[id]
        if (state.selectedPath === id) {
          state.selectedPath = null
        }
      }),

    setSelectedPath: (path) =>
      set((state) => {
        state.selectedPath = path
      }),

    setContent: (id, content) =>
      set((state) => {
        state.contents[id] = content
      }),
      
    setContentOnSave: (id, title) =>
      set((state) => {
        state.groups.forEach(g => {
           const tab = g.tabs.find(t => t.id === id)
           if (tab) tab.name = title
        })
      }),

    expandFolder: (path) => 
      set((state) => {
        if (!state.expandedFolders.includes(path)) {
           state.expandedFolders.push(path)
        }
      }),
      
    toggleFolder: (path) =>
      set((state) => {
        const idx = state.expandedFolders.indexOf(path)
        if (idx === -1) {
          state.expandedFolders.push(path)
        } else {
          state.expandedFolders.splice(idx, 1)
        }
      }),
      
    collapseAllFolders: () =>
      set((state) => {
        state.expandedFolders = []
      }),
  })),
)
