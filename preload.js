const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electron', {
  getBackendPort: () => ipcRenderer.invoke('get-backend-port'),
  getApiUrl: () => {
    return window.__BACKEND_URL__ || '';
  },
  onBackendReady: (callback) => ipcRenderer.on('backend-port', (event, port) => callback(port))
});

contextBridge.exposeInMainWorld('electronAPI', {
  onPortDetected: (callback) => ipcRenderer.on('backend-port', (_event, value) => callback(value))
});

contextBridge.exposeInMainWorld('ELECTRON_API', {
  selectDirectory: () => ipcRenderer.invoke('dialog:open-directory'),
});

// Listen for port updates and set a global for easy access
ipcRenderer.on('backend-port', (event, port) => {
  window.__BACKEND_URL__ = `http://127.0.0.1:${port}`;
  console.log(`[Preload] Backend URL set to: ${window.__BACKEND_URL__}`);
});
