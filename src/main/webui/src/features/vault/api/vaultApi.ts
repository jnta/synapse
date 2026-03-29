export interface VaultInfo {
  path: string;
  name: string;
  lastAccessed: number;
}

const getHeaders = (extra: Record<string, string> = {}) => ({
  'Content-Type': 'application/json',
  ...extra
});

export const vaultApi = {
  getCurrent: async (): Promise<VaultInfo> => {
    const res = await fetch('/api/v1/vault', { headers: getHeaders() });
    if (!res.ok) throw new Error('Failed to fetch current vault');
    return res.json();
  },
  getRecent: async (): Promise<VaultInfo[]> => {
    const res = await fetch('/api/v1/vault/recent', { headers: getHeaders() });
    if (!res.ok) throw new Error('Failed to fetch recent vaults');
    return res.json();
  },
  open: async (vault: Partial<VaultInfo>): Promise<VaultInfo> => {
    const res = await fetch('/api/v1/vault/open', {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(vault),
    });
    if (!res.ok) throw new Error('Failed to open vault');
    return res.json();
  }
};
