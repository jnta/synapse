const { app, BrowserWindow, ipcMain } = require('electron');
const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

let backendProcess;
let backendPort = null;
let mainWindow = null;

function findBackendExecutable() {
  const backendName = 'synapse-runner';
  const backendPath = app.isPackaged
    ? path.join(process.resourcesPath, 'backend', backendName)
    : path.join(__dirname, 'target', 'synapse-1.0.0-SNAPSHOT-runner');

  console.log('Looking for backend at:', backendPath);
  return backendPath;
}

function startBackend() {
  const backendPath = findBackendExecutable();
  if (!fs.existsSync(backendPath)) {
    console.error(`Backend executable not found at ${backendPath}. Make sure to build with -Dnative first.`);
    return;
  }

  // Ensure the binary is executable on Linux when packaged
  if (app.isPackaged && process.platform !== 'win32') {
    try {
      fs.chmodSync(backendPath, 0o755);
    } catch (err) {
      console.error('Failed to set backend permissions:', err);
    }
  }

  console.log(`Starting backend: ${backendPath}`);
  backendProcess = spawn(backendPath, [], {
    stdio: 'pipe',
    env: { ...process.env, QUARKUS_HTTP_PORT: '0' }
  });

  backendProcess.stdout.on('data', (data) => {
    const output = data.toString();
    console.log(`[Quarkus] ${output}`);

    // Parse the port from Quarkus logs: "Listening on: http://127.0.0.1:PORT"
    const portMatch = output.match(/Listening on: http:\/\/127.0.0.1:(\d+)/);
    if (portMatch && !backendPort) {
      backendPort = portMatch[1];
      console.log(`Backend is ready on port: ${backendPort}`);
      
      // If window isn't created yet, create it now that we have the port
      if (!mainWindow) {
        createWindow();
      } else {
        mainWindow.webContents.send('backend-port', backendPort);
      }
    }
  });

  backendProcess.stderr.on('data', (data) => {
    console.error(`[Quarkus Error] ${data.toString()}`);
  });
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    show: false, // Don't show until ready
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });
  
  mainWindow.once('ready-to-show', () => {
    mainWindow.show();
  });

  // Determine frontend location
  const prodFrontend = path.join(process.resourcesPath, 'frontend', 'index.html');
  const devFrontend = path.join(__dirname, 'src', 'main', 'webui', 'dist', 'index.html');

  if (fs.existsSync(prodFrontend)) {
    mainWindow.loadFile(prodFrontend);
  } else if (fs.existsSync(devFrontend)) {
    mainWindow.loadFile(devFrontend);
  } else {
    mainWindow.loadURL('http://localhost:5173'); 
  }

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

app.whenReady().then(() => {
  startBackend();
  // Don't call createWindow() here, let startBackend handle it when port is ready
});

app.on('window-all-closed', () => {
  if (backendProcess) {
    console.log('Killing backend process...');
    backendProcess.kill();
  }
  if (process.platform !== 'darwin') app.quit();
});

// IPC handler to provide the port to the frontend if it asks
ipcMain.handle('get-backend-port', () => backendPort);
