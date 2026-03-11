import { vi, describe, it, expect, beforeEach } from "vitest";
import { listVaultNotes, readNote, searchNotes } from "./tauri.ts";
import { invoke } from "@tauri-apps/api/core";

// Mock the Tauri invoke function
vi.mock("@tauri-apps/api/core", () => ({
  invoke: vi.fn(),
}));

describe("Tauri Services", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("listVaultNotes", () => {
    it("should call list_vault_notes command and return notes", async () => {
      const mockResult = {
        daily: [{ name: "2023-10-01", folder: "daily-notes", modified: 123456789 }],
        resources: [],
        projects: [],
      };
      (invoke as any).mockResolvedValue(mockResult);

      const result = await listVaultNotes();

      expect(invoke).toHaveBeenCalledWith("list_vault_notes");
      expect(result).toEqual(mockResult);
    });
  });

  describe("readNote", () => {
    it("should call read_note command with correct params", async () => {
      const mockContent = "# Test Note Content";
      (invoke as any).mockResolvedValue(mockContent);

      const result = await readNote("daily-notes", "test-note");

      expect(invoke).toHaveBeenCalledWith("read_note", { folder: "daily-notes", name: "test-note" });
      expect(result).toBe(mockContent);
    });
  });

  describe("searchNotes", () => {
    it("should call search_notes command with query and limit", async () => {
      const mockMatches = [["test-note.md", 0.5]];
      (invoke as any).mockResolvedValue(mockMatches);

      const result = await searchNotes("query string", 5);

      expect(invoke).toHaveBeenCalledWith("search_notes", { query: "query string", limit: 5 });
      expect(result).toEqual(mockMatches);
    });
  });
});
