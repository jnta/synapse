export interface NoteDTO {
  id: string;
  title: string;
  content: string;
  tags: string[];
  createdAt: string;
  lastAccessedAt: string;
  state: string;
}

export interface CreateNoteRequest {
  title: string;
  content: string;
}

export interface UpdateNoteRequest {
  title: string;
  content: string;
}

export interface CreateFolderRequest {
  path: string;
}

export interface MoveNodeRequest {
  source: string;
  target: string;
}

declare global {
  interface Window {
    electron?: {
      getApiUrl: () => string;
      getBackendPort: () => Promise<string | null>;
      onBackendReady: (callback: (port: string) => void) => void;
    };
    electronAPI?: {
      onPortDetected: (callback: (port: string) => void) => void;
    };
  }
}

const getBackendUrl = () => {
  if (window.electron?.getApiUrl) {
    const url = window.electron.getApiUrl();
    if (url) return url;
  }
  return ''; // Default to relative path for dev mode
};

const getHeaders = (extra: Record<string, string> = {}) => ({
  'Content-Type': 'application/json',
  'X-Trace-Id': crypto.randomUUID(),
  ...extra
});

export const notesApi = {
  getAll: async (): Promise<NoteDTO[]> => {
    const res = await fetch(`${getBackendUrl()}/api/v1/notes`, { headers: getHeaders() });
    if (!res.ok) throw new Error('Failed to fetch notes');
    return res.json();
  },
  getOne: async (id: string): Promise<NoteDTO> => {
    const encodedId = id.split('/').map(segment => encodeURIComponent(segment)).join('/');
    const res = await fetch(`${getBackendUrl()}/api/v1/notes/${encodedId}`, { headers: getHeaders() });
    if (!res.ok) throw new Error('Failed to fetch note');
    return res.json();
  },
  create: async (req: CreateNoteRequest): Promise<NoteDTO> => {
    const res = await fetch(`${getBackendUrl()}/api/v1/notes`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(req),
    });
    if (!res.ok) throw new Error('Failed to create note');
    return res.json();
  },
  update: async (id: string, req: UpdateNoteRequest): Promise<NoteDTO> => {
    const encodedId = id.split('/').map(segment => encodeURIComponent(segment)).join('/');
    const res = await fetch(`${getBackendUrl()}/api/v1/notes/${encodedId}`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify(req),
    });
    if (!res.ok) throw new Error('Failed to update note');
    return res.json();
  },
  delete: async (id: string): Promise<void> => {
    const encodedId = id.split('/').map(segment => encodeURIComponent(segment)).join('/');
    const res = await fetch(`${getBackendUrl()}/api/v1/notes/${encodedId}`, {
      method: 'DELETE',
      headers: getHeaders(),
    });
    if (!res.ok) throw new Error('Failed to delete note');
  },

  createFolder: async (req: CreateFolderRequest): Promise<void> => {
    const res = await fetch(`${getBackendUrl()}/api/v1/notes/folders`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(req),
    });
    if (!res.ok) throw new Error('Failed to create folder');
  },
  getFolders: async (): Promise<string[]> => {
    const res = await fetch(`${getBackendUrl()}/api/v1/notes/folders`, { headers: getHeaders() });
    if (!res.ok) throw new Error('Failed to fetch folders');
    return res.json();
  },
  moveNode: async (req: MoveNodeRequest): Promise<void> => {
    const res = await fetch(`${getBackendUrl()}/api/v1/notes/move`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(req),
    });
    if (!res.ok) throw new Error('Failed to move node');
  }
};
