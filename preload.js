const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electron', {
  getBackendPort: () => ipcRenderer.invoke('get-backend-port'),
  getApiUrl: () => {
    // Port will be injected asynchronously or via query param if needed
    // For now, we'll assume the frontend can fetch it or we use a global
    return window.__BACKEND_URL__ || '';
  },
  onBackendReady: (callback) => ipcRenderer.on('backend-port', (event, port) => callback(port))
});

// Listen for port updates and set a global for easy access
ipcRenderer.on('backend-port', (event, port) => {
  window.__BACKEND_URL__ = `http://127.0.0.1:${port}`;
  console.log(`[Preload] Backend URL set to: ${window.__BACKEND_URL__}`);
});
