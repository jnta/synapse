import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { type VaultInfo, vaultApi } from '../api/vaultApi';

interface VaultState {
  currentVault: VaultInfo | null;
  recentVaults: VaultInfo[];
  isLoading: boolean;
  error: string | null;

  setCurrentVault: (vault: VaultInfo | null) => void;
  fetchCurrentVault: () => Promise<void>;
  fetchRecentVaults: () => Promise<void>;
  openVault: (path: string, name?: string) => Promise<void>;
}

export const useVaultStore = create<VaultState>()(
  persist(
    (set) => ({
      currentVault: null,
      recentVaults: [],
      isLoading: false,
      error: null,

      setCurrentVault: (vault) => set({ currentVault: vault }),

      fetchCurrentVault: async () => {
        set({ isLoading: true });
        try {
          const vault = await vaultApi.getCurrent();
          set({ currentVault: vault, isLoading: false });
        } catch (err) {
          set({ error: (err as Error).message, isLoading: false });
        }
      },

      fetchRecentVaults: async () => {
        try {
          const recent = await vaultApi.getRecent();
          set({ recentVaults: recent });
        } catch (err) {
          console.error('Failed to fetch recent vaults', err);
        }
      },

      openVault: async (path, name) => {
        set({ isLoading: true, error: null });
        try {
          const vault = await vaultApi.open({ path, name });
          set({ currentVault: vault, isLoading: false });
          // Force a reload of notes in the app
          window.location.reload(); 
        } catch (err) {
          set({ error: (err as Error).message, isLoading: false });
        }
      }
    }),
    {
      name: 'vault-storage',
      partialize: (state) => ({ currentVault: state.currentVault }),
    }
  )
);
