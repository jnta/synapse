import React, { useState, useEffect } from 'react';
import { useVaultStore } from '../store/useVaultStore';

export function ManageVaultScreen({ onClose }: { onClose?: () => void }) {
  const { currentVault, recentVaults, fetchRecentVaults, openVault, error, isLoading } = useVaultStore();
  const [newVaultPath, setNewVaultPath] = useState('');
  const [newVaultName, setNewVaultName] = useState('');
  const [view, setView] = useState<'selection' | 'create' | 'open'>('selection');

  useEffect(() => {
    fetchRecentVaults();
  }, [fetchRecentVaults]);

  const handleOpenExisting = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newVaultPath) await openVault(newVaultPath);
  };

  const handleCreateNew = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newVaultPath && newVaultName) await openVault(newVaultPath, newVaultName);
  };

  const handleBrowse = async () => {
    // @ts-ignore
    const selectedPath = await window.ELECTRON_API?.selectDirectory();
    if (selectedPath) {
      setNewVaultPath(selectedPath);
    }
  };

  return (
    <div className="fixed inset-0 z-[2000] bg-[var(--color-bg)] text-[var(--color-text-primary)] font-sans flex overflow-hidden">
      {/* Global Exit Button */}
      {onClose && currentVault && (
        <button 
          onClick={onClose}
          className="absolute top-8 right-8 p-3 text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] hover:bg-[var(--color-surface-hover)] rounded-full transition-all z-[100]"
        >
          <span className="material-symbols-outlined text-2xl">close</span>
        </button>
      )}
      
      {/* Sidebar: Professional list-based layout */}
      <aside className="flex flex-col w-[300px] shrink-0 bg-[var(--color-sidebar-bg)] border-r border-[var(--color-border-subtle)] pt-8">
        <div className="w-full flex items-center gap-2 px-8 pb-8 shrink-0">
          <span className="material-symbols-outlined text-[var(--color-accent)] text-lg">history</span>
          <span className="text-[var(--color-text-primary)] font-bold text-[10px] uppercase tracking-[0.2em] whitespace-nowrap">
            Recent Archives
          </span>
        </div>
        
        <nav className="flex-1 px-4 space-y-1 overflow-y-auto no-scrollbar">
          {recentVaults.length === 0 ? (
            <div className="p-8 opacity-40 text-[10px] uppercase tracking-widest text-left border border-dashed border-[var(--color-border-subtle)] rounded-2xl m-2">
              Empty Vault List
            </div>
          ) : (
            recentVaults.map((vault) => (
              <button
                key={vault.path}
                onClick={() => openVault(vault.path, vault.name)}
                className="w-full group px-5 py-4 rounded-2xl hover:bg-[var(--color-surface-hover)] transition-all flex items-center gap-4 text-left border border-transparent hover:border-[var(--color-border-subtle)]"
              >
                <div className="w-12 h-12 rounded-xl bg-[var(--color-bg-secondary)] flex items-center justify-center shrink-0 border border-[var(--color-border-subtle)] group-hover:border-[var(--color-accent)]/30 transition-colors">
                  <span className="material-symbols-outlined text-[var(--color-text-muted)] group-hover:text(--color-accent)] text-2xl">folder_managed</span>
                </div>
                <div className="min-w-0">
                  <p className="text-sm font-semibold truncate text-[var(--color-text-primary)] group-hover:text-[var(--color-accent)] transition-colors">{vault.name}</p>
                  <p className="text-[10px] text-[var(--color-text-muted)] uppercase tracking-wider mt-0.5 truncate">{vault.path}</p>
                </div>
              </button>
            ))
          )}
        </nav>

        <div className="p-8 border-t border-[var(--color-border-subtle)]">
          <button 
            onClick={() => fetchRecentVaults()}
            className="w-full flex items-center justify-center gap-3 px-6 py-4 rounded-xl bg-[var(--color-bg-secondary)] text-[var(--color-text-muted)] hover:text-[var(--color-accent)] hover:border-[var(--color-accent)]/30 border border-transparent text-xs uppercase tracking-widest font-bold transition-all"
          >
            <span className={`material-symbols-outlined text-lg ${isLoading ? 'animate-spin' : ''}`}>sync_saved_locally</span>
            Refresh Sync
          </button>
        </div>
      </aside>

      {/* Main Content: Predictable flex-center layout */}
      <main className="flex-1 flex flex-col relative bg-[var(--color-bg)] overflow-hidden">
        
        {/* Back Button */}
        {view !== 'selection' && (
          <div className="absolute top-8 left-8 z-20">
            <button 
              onClick={() => setView('selection')}
              className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] px-6 py-3 rounded-xl hover:bg-[var(--color-surface-hover)] transition-colors"
            >
              <span className="material-symbols-outlined text-sm">arrow_back</span> Back
            </button>
          </div>
        )}

        {/* Centered Content Wrapper */}
        <div className="flex-1 flex flex-col items-center justify-center p-12 relative z-10">
          
          {view === 'selection' && (
              <div className="w-full max-w-2xl flex flex-col items-center animate-in fade-in slide-in-from-bottom-8 duration-700">
              
              <div className="text-center mb-12">
                <h1 className="text-4xl md:text-5xl font-bold tracking-tight text-balance mb-4 leading-tight">
                  Where does your <span className="text-[var(--color-accent)]">brain live?</span>
                </h1>
                <p className="text-[var(--color-text-secondary)] text-sm md:text-base max-w-md mx-auto opacity-80 leading-relaxed">
                  Access your neural archives or initialize a new cognitive container to begin the next spark.
                </p>
              </div>

              {/* Action Buttons: Centered, larger for full screen */}
              <div className="grid grid-cols-2 gap-6 w-full">
                <button 
                  onClick={() => setView('create')}
                  className="group flex flex-col items-center justify-center text-center bg-[var(--color-bg-secondary)] p-10 rounded-[2.5rem] border border-[var(--color-border-subtle)] hover:border-[var(--color-accent)]/50 hover:shadow-2xl transition-all h-[200px]"
                >
                  <div className="w-16 h-16 rounded-2xl bg-[var(--color-bg)] flex items-center justify-center mb-4 border border-[var(--color-border-subtle)] group-hover:border-[var(--color-accent)]/30 group-hover:scale-110 transition-all">
                    <span className="material-symbols-outlined text-3xl text-[var(--color-accent)]/80 group-hover:text-[var(--color-accent)]">auto_awesome</span>
                  </div>
                  <h3 className="text-lg font-bold">Create New</h3>
                  <span className="text-[10px] text-[var(--color-text-muted)] uppercase tracking-widest font-bold mt-2">Initialize Archive</span>
                </button>

                <button 
                  onClick={() => setView('open')}
                  className="group flex flex-col items-center justify-center text-center bg-[var(--color-bg-secondary)] p-10 rounded-[2.5rem] border border-[var(--color-border-subtle)] hover:border-[var(--color-text-primary)]/50 hover:shadow-2xl transition-all h-[200px]"
                >
                   <div className="w-16 h-16 rounded-2xl bg-[var(--color-bg)] flex items-center justify-center mb-4 border border-[var(--color-border-subtle)] group-hover:border-[var(--color-text-primary)]/30 group-hover:scale-110 transition-all">
                    <span className="material-symbols-outlined text-3xl text-[var(--color-text-secondary)] group-hover:text-[var(--color-text-primary)]">folder_open</span>
                  </div>
                  <h3 className="text-lg font-bold">Open Existing</h3>
                  <span className="text-[10px] text-[var(--color-text-muted)] uppercase tracking-widest font-bold mt-2">Sync Local Files</span>
                </button>
              </div>
            </div>
          )}

          {(view === 'create' || view === 'open') && (
            <div className="w-full max-w-lg animate-in fade-in slide-in-from-right-12 duration-500">
              <h2 className="text-4xl font-bold mb-3 tracking-tight">
                {view === 'create' ? 'Initialize Archive' : 'Open Link'}
              </h2>
              <p className="text-[var(--color-text-secondary)] text-base mb-10 leading-relaxed">
                {view === 'create' ? 'Choose a name and a location for your new neural archive.' : 'Provide the absolute path to your markdown files.'}
              </p>

              <form onSubmit={view === 'create' ? handleCreateNew : handleOpenExisting} className="flex flex-col gap-6">
                {view === 'create' && (
                  <div className="flex flex-col gap-3">
                    <label className="text-[11px] font-bold text-[var(--color-text-muted)] uppercase tracking-widest ml-1">Vault Name</label>
                    <input 
                      autoFocus
                      type="text" 
                      className="w-full bg-[var(--color-bg-secondary)] border border-[var(--color-border-subtle)] rounded-2xl px-6 py-4 text-base focus:border-[var(--color-accent)] outline-none transition-all placeholder:text-[var(--color-text-muted)]"
                      placeholder="e.g. My Neural Map"
                      value={newVaultName}
                      onChange={(e) => setNewVaultName(e.target.value)}
                    />
                  </div>
                )}

                <div className="flex flex-col gap-3">
                  <label className="text-[11px] font-bold text-[var(--color-text-muted)] uppercase tracking-widest ml-1">Absolute Path</label>
                  <div className="flex gap-3">
                    <input 
                      type="text" 
                      className="flex-1 bg-[var(--color-bg-secondary)] border border-[var(--color-border-subtle)] rounded-2xl px-6 py-4 text-base focus:border-[var(--color-accent)] outline-none transition-all placeholder:text-[var(--color-text-muted)] min-w-0"
                      placeholder="/home/user/vault"
                      value={newVaultPath}
                      onChange={(e) => setNewVaultPath(e.target.value)}
                    />
                    <button 
                      type="button"
                      onClick={handleBrowse}
                      className="px-6 py-4 bg-[var(--color-bg-secondary)] hover:bg-[var(--color-surface-hover)] border border-[var(--color-border-subtle)] rounded-2xl transition-all flex items-center justify-center gap-2 group shrink-0"
                      title="Select Directory"
                    >
                      <span className="material-symbols-outlined text-[var(--color-text-muted)] group-hover:text-[var(--color-accent)] transition-colors">folder_open</span>
                      <span className="text-[10px] font-bold text-[var(--color-text-muted)] group-hover:text-[var(--color-text-primary)] uppercase tracking-widest">Browse</span>
                    </button>
                  </div>
                </div>

                {error && (
                  <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-2xl flex items-center gap-4 animate-in shake-in duration-300">
                    <span className="material-symbols-outlined text-red-500">error</span>
                    <p className="text-sm text-red-500">{error}</p>
                  </div>
                )}

                <button 
                  type="submit"
                  disabled={isLoading || !newVaultPath || (view === 'create' && !newVaultName)}
                  className="mt-4 w-full py-5 bg-[var(--color-accent)] hover:bg-[var(--color-accent-hover)] text-white disabled:opacity-40 disabled:cursor-not-allowed text-xs font-bold rounded-2xl active:scale-[0.98] transition-all flex items-center justify-center gap-3 uppercase tracking-[0.2em]"
                >
                  {isLoading
                    ? <span className="animate-spin material-symbols-outlined text-base">sync</span>
                    : <span className="material-symbols-outlined text-xl">{view === 'create' ? 'auto_awesome' : 'folder_open'}</span>
                  }
                  {view === 'create' ? 'Initialize Map' : 'Synchronize'}
                </button>
              </form>
            </div>
          )}
        </div>

        {/* Background Decor */}
        <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-full h-1/2 pointer-events-none opacity-[0.05]">
          <div className="w-[80vh] h-[80vh] bg-[var(--color-accent)] rounded-full blur-[120px] absolute -bottom-1/2 left-1/2 -translate-x-1/2"></div>
        </div>
      </main>
    </div>
  );
}