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

  return (
    <div 
      className="absolute inset-0 z-[2000] bg-[var(--color-bg)] flex items-center justify-center p-6 md:p-12 overflow-y-auto custom-scrollbar text-[var(--color-text-primary)] font-sans"
      onClick={onClose}
    >
      {/* 1. Modal Shell: Reduced max height for better proportions */}
      <div 
        className="relative w-full max-w-[1100px] h-[75vh] min-h-[540px] max-h-[680px] bg-[var(--color-bg)] flex overflow-hidden rounded-[2rem] shadow-2xl border border-[var(--color-border-subtle)] animate-in zoom-in-95 duration-300"
        onClick={e => e.stopPropagation()}
      >
        {/* Global Modal Exit Button */}
        {onClose && currentVault && (
          <button 
            onClick={onClose}
            className="absolute top-6 right-6 p-2 text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] hover:bg-[var(--color-surface-hover)] rounded-full transition-all z-[100]"
          >
            <span className="material-symbols-outlined text-2xl">close</span>
          </button>
        )}
        
        {/* 2. Sidebar: Professional list-based layout */}
        <aside className="flex flex-col w-1/4 shrink-0 bg-[var(--color-sidebar-bg)] border-r border-[var(--color-border-subtle)]">
          <div className="flex px-6 pt-16 pb-6 flex justify-start items-start gap-2 shrink-0">
            <span className="material-symbols-outlined text-[var(--color-accent)] text-lg">history</span>
            <span className="text-[var(--color-text-primary)] font-bold text-[10px] uppercase tracking-[0.2em]">Recent Archives</span>
          </div>
          
          <nav className="flex-1 px-4 space-y-1 overflow-y-auto no-scrollbar">
            {recentVaults.length === 0 ? (
              <div className="p-6 opacity-40 text-[10px] uppercase tracking-widest text-left border border-dashed border-[var(--color-border-subtle)] rounded-xl m-2">
                Empty Vault List
              </div>
            ) : (
              recentVaults.map((vault) => (
                <button
                  key={vault.path}
                  onClick={() => openVault(vault.path, vault.name)}
                  className="w-full group px-4 py-3 rounded-xl hover:bg-[var(--color-surface-hover)] transition-all flex items-center gap-4 text-left border border-transparent hover:border-[var(--color-border-subtle)]"
                >
                  <div className="w-10 h-10 rounded-lg bg-[var(--color-bg-secondary)] flex items-center justify-center shrink-0 border border-[var(--color-border-subtle)] group-hover:border-[var(--color-accent)]/30 transition-colors">
                    <span className="material-symbols-outlined text-[var(--color-text-muted)] group-hover:text-[var(--color-accent)] text-xl">folder_managed</span>
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-semibold truncate text-[var(--color-text-primary)] group-hover:text-[var(--color-accent)] transition-colors">{vault.name}</p>
                    <p className="text-[10px] text-[var(--color-text-muted)] uppercase tracking-wider mt-0.5 truncate max-w-[150px]">{vault.path}</p>
                  </div>
                </button>
              ))
            )}
          </nav>

          <div className="p-6 border-t border-[var(--color-border-subtle)]">
            <button 
              onClick={() => fetchRecentVaults()}
              className="w-full flex items-center justify-center gap-3 px-4 py-3 rounded-xl bg-[var(--color-bg-secondary)] text-[var(--color-text-muted)] hover:text-[var(--color-accent)] hover:border-[var(--color-accent)]/30 border border-transparent text-xs uppercase tracking-widest font-bold transition-all"
            >
              <span className={`material-symbols-outlined text-lg ${isLoading ? 'animate-spin' : ''}`}>sync_saved_locally</span>
              Refresh Sync
            </button>
          </div>
        </aside>

        {/* 3. Main Content: Predictable flex-center layout */}
        <main className="flex-1 flex flex-col relative bg-[var(--color-bg)] overflow-hidden">
          
          {/* Back Button */}
          {view !== 'selection' && (
            <div className="absolute top-6 left-6 z-20">
              <button 
                onClick={() => setView('selection')}
                className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-[var(--color-text-muted)] hover:text-[var(--color-text-primary)] px-4 py-2 rounded-lg hover:bg-[var(--color-surface-hover)] transition-colors"
              >
                <span className="material-symbols-outlined text-sm">arrow_back</span> Back
              </button>
            </div>
          )}

          {/* Centered Content Wrapper */}
          <div className="flex-1 flex flex-col items-center justify-center p-6 md:p-10 relative z-10">
            
            {view === 'selection' && (
                <div className="w-full max-w-xl flex flex-col items-center animate-in slide-in-from-bottom-4 duration-500">
                
                <div className="text-center mb-10">
                  <h1 className="text-3xl md:text-4xl font-bold tracking-tight text-balance mb-3">
                    Where does your <span className="text-[var(--color-accent)]">brain live?</span>
                  </h1>
                  <p className="text-[var(--color-text-secondary)] text-xs md:text-sm max-w-sm mx-auto opacity-80">
                    Access your neural archives or initialize a new cognitive container to begin the next spark.
                  </p>
                </div>

                {/* 4. Action Buttons: Centered, smaller, and cleaner silhouette */}
                <div className="grid grid-cols-2 gap-4 w-full">
                  <button 
                    onClick={() => setView('create')}
                    className="group flex flex-col items-center justify-center text-center bg-[var(--color-bg-secondary)] p-6 rounded-3xl border border-[var(--color-border-subtle)] hover:border-[var(--color-accent)]/50 hover:shadow-xl transition-all h-[130px]"
                  >
                    <span className="material-symbols-outlined text-2xl text-[var(--color-accent)]/80 group-hover:text-[var(--color-accent)] group-hover:scale-110 mb-3 transition-all">auto_awesome</span>
                    <h3 className="text-sm font-bold white-space-nowrap">Create New</h3>
                    <span className="text-[9px] text-[var(--color-text-muted)] uppercase tracking-wider font-bold mt-1">Initialize Archive</span>
                  </button>

                  <button 
                    onClick={() => setView('open')}
                    className="group flex flex-col items-center justify-center text-center bg-[var(--color-bg-secondary)] p-6 rounded-3xl border border-[var(--color-border-subtle)] hover:border-[var(--color-text-primary)]/50 hover:shadow-xl transition-all h-[130px]"
                  >
                    <span className="material-symbols-outlined text-2xl text-[var(--color-text-secondary)] group-hover:text-[var(--color-text-primary)] group-hover:scale-110 mb-3 transition-all">folder_open</span>
                    <h3 className="text-sm font-bold white-space-nowrap">Open Existing</h3>
                    <span className="text-[9px] text-[var(--color-text-muted)] uppercase tracking-wider font-bold mt-1">Sync Local Files</span>
                  </button>
                </div>
              </div>
            )}

            {(view === 'create' || view === 'open') && (
              <div className="w-full max-w-md animate-in slide-in-from-right-8 duration-300">
                <h2 className="text-3xl font-bold mb-2">
                  {view === 'create' ? 'Initialize Archive' : 'Open Link'}
                </h2>
                <p className="text-[var(--color-text-secondary)] text-sm mb-8">
                  {view === 'create' ? 'Choose a name and a location for your new neural archive.' : 'Provide the absolute path to your markdown files.'}
                </p>

                <form onSubmit={view === 'create' ? handleCreateNew : handleOpenExisting} className="flex flex-col gap-5">
                  {view === 'create' && (
                    <div className="flex flex-col gap-2">
                      <label className="text-[10px] font-bold text-[var(--color-text-muted)] uppercase tracking-widest">Vault Name</label>
                      <input 
                        autoFocus
                        type="text" 
                        className="w-full bg-[var(--color-bg-secondary)] border border-[var(--color-border-subtle)] rounded-lg px-4 py-3.5 text-sm focus:border-[var(--color-accent)] outline-none transition-colors placeholder:text-[var(--color-text-muted)]"
                        placeholder="e.g. My Neural Map"
                        value={newVaultName}
                        onChange={(e) => setNewVaultName(e.target.value)}
                      />
                    </div>
                  )}

                  <div className="flex flex-col gap-2">
                    <label className="text-[10px] font-bold text-[var(--color-text-muted)] uppercase tracking-widest">Absolute Path</label>
                    <input 
                      type="text" 
                      className="w-full bg-[var(--color-bg-secondary)] border border-[var(--color-border-subtle)] rounded-lg px-4 py-3.5 text-sm focus:border-[var(--color-accent)] outline-none transition-colors placeholder:text-[var(--color-text-muted)]"
                      placeholder="/home/user/vault"
                      value={newVaultPath}
                      onChange={(e) => setNewVaultPath(e.target.value)}
                    />
                  </div>

                  {error && (
                    <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-lg flex items-center gap-3">
                      <span className="material-symbols-outlined text-red-500">error</span>
                      <p className="text-xs text-red-500">{error}</p>
                    </div>
                  )}

                  <button 
                    type="submit"
                    disabled={isLoading || !newVaultPath || (view === 'create' && !newVaultName)}
                    className="mt-3 w-full py-4 bg-[var(--color-accent)] hover:bg-[var(--color-accent-hover)] text-white disabled:opacity-40 disabled:cursor-not-allowed text-[11px] font-bold rounded-lg active:scale-[0.99] transition-all flex items-center justify-center gap-2.5 uppercase tracking-[0.15em]"
                  >
                    {isLoading
                      ? <span className="animate-spin material-symbols-outlined text-base">sync</span>
                      : <span className="material-symbols-outlined text-base">{view === 'create' ? 'auto_awesome' : 'folder_open'}</span>
                    }
                    {view === 'create' ? 'Initialize Map' : 'Synchronize'}
                  </button>
                </form>
              </div>
            )}
          </div>

          {/* Background Decor */}
          <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-full h-1/2 pointer-events-none opacity-[0.03]">
            <div className="w-[60vh] h-[60vh] bg-[var(--color-accent)] rounded-full blur-[100px] absolute -bottom-1/2 left-1/2 -translate-x-1/2"></div>
          </div>
        </main>
      </div>
    </div>
  );
}