import "@testing-library/jest-dom";
import { vi } from "vitest";

// Mock Tauri invoke
if (!(window as any).__TAURI_INTERNALS__) {
  (window as any).__TAURI_INTERNALS__ = {
    invoke: vi.fn(),
  };
}

// Mock Material Symbols to prevent warnings in RTL
const originalH = (window as any).h;
(window as any).h = vi.fn();
