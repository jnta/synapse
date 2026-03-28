import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import type { CreateNoteRequest, UpdateNoteRequest, CreateFolderRequest, MoveNodeRequest } from '@/features/notes/api/notesApi'
import { notesApi } from '@/features/notes/api/notesApi'

export const useNotesList = () => {
  return useQuery({
    queryKey: ['notes'],
    queryFn: notesApi.getAll
  })
}

export const useFoldersList = () => {
  return useQuery({
    queryKey: ['folders'],
    queryFn: notesApi.getFolders
  })
}

export const useCreateNote = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateNoteRequest) => notesApi.create(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
    }
  })
}

export const useUpdateNote = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, req }: { id: string; req: UpdateNoteRequest }) => notesApi.update(id, req),
    // Optimistic updates or just invalidate:
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
    }
  })
}

export const useDeleteNote = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => notesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notes'] })
      queryClient.invalidateQueries({ queryKey: ['folders'] })
    }
  })
}

export const useCreateFolder = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: CreateFolderRequest) => notesApi.createFolder(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['folders'] })
    }
  })
}

export const useMoveNode = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (req: MoveNodeRequest) => notesApi.moveNode(req),
    onSuccess: () => {
      // both notes and folders might have structurally moved
      queryClient.invalidateQueries({ queryKey: ['notes'] })
      queryClient.invalidateQueries({ queryKey: ['folders'] })
    }
  })
}
