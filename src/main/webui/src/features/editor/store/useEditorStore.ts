import { create } from 'zustand'
import { immer } from 'zustand/middleware/immer'
import type { Tab } from '@/core/types/Tab'

interface EditorUIState {
  tabs: Tab[]
  activeId: string | null
  selectedPath: string | null
  contents: Record<string, string>
  expandedFolders: string[]
  
  openNote: (id: string, title: string, content: string) => void
  closeTab: (id: string) => void
  setActiveId: (id: string) => void
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
    tabs: [],
    activeId: null,
    selectedPath: null,
    contents: {},
    expandedFolders: [],

    openNote: (id, title, content) => {
      set((state) => {
        if (!state.tabs.find(t => t.id === id)) {
          state.tabs.push({ id, name: title })
          state.contents[id] = content
        }
        state.activeId = id
      })
    },

    removeDeletedNoteContext: (id) =>
      set((state) => {
        const idx = state.tabs.findIndex((t) => t.id === id)
        if (idx !== -1) {
          state.tabs.splice(idx, 1)
          if (state.activeId === id) {
            state.activeId = state.tabs.length > 0 ? state.tabs[Math.max(0, idx - 1)].id : null
          }
        }
        delete state.contents[id]
        if (state.selectedPath === id) {
          state.selectedPath = null
        }
      }),

    closeTab: (id) =>
      set((state) => {
        const idx = state.tabs.findIndex((t) => t.id === id)
        if (idx !== -1) {
          state.tabs.splice(idx, 1)
          if (state.activeId === id) {
            state.activeId = state.tabs.length > 0 ? state.tabs[Math.max(0, idx - 1)].id : null
          }
          delete state.contents[id]
        }
      }),

    setActiveId: (id) =>
      set((state) => {
        state.activeId = id
        state.selectedPath = id
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
        const tab = state.tabs.find(t => t.id === id)
        if (tab) tab.name = title
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
