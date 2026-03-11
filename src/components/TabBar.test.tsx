import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { TabBar } from "./TabBar.tsx";
import type { NoteEntry } from "@/services/tauri.ts";

describe("TabBar", () => {
  const mockTabs: NoteEntry[] = [
    { name: "Note 1", folder: "daily", modified: 1 },
    { name: "Note 2", folder: "resource", modified: 2 },
  ];

  it("should render nothing when there are no tabs", () => {
    const { container } = render(
      <TabBar tabs={[]} activeTab={null} onSelect={vi.fn()} onClose={vi.fn()} />
    );
    expect(container.firstChild).toBeNull();
  });

  it("should render both tabs", () => {
    render(
      <TabBar tabs={mockTabs} activeTab="Note 1" onSelect={vi.fn()} onClose={vi.fn()} />
    );
    expect(screen.getByText("Note 1")).toBeInTheDocument();
    expect(screen.getByText("Note 2")).toBeInTheDocument();
  });

  it("should highlight the active tab", () => {
    render(
      <TabBar tabs={mockTabs} activeTab="Note 2" onSelect={vi.fn()} onClose={vi.fn()} />
    );
    const activeTabElement = screen.getByText("Note 2").closest("div");
    expect(activeTabElement).toHaveClass("border-t-primary");
    
    const inactiveTabElement = screen.getByText("Note 1").closest("div");
    expect(inactiveTabElement).toHaveClass("border-t-transparent");
  });

  it("should call onSelect when a tab is clicked", () => {
    const onSelect = vi.fn();
    render(
      <TabBar tabs={mockTabs} activeTab="Note 1" onSelect={onSelect} onClose={vi.fn()} />
    );
    
    fireEvent.click(screen.getByText("Note 2"));
    expect(onSelect).toHaveBeenCalledWith(mockTabs[1]);
  });

  it("should call onClose when the close button is clicked", () => {
    const onClose = vi.fn();
    render(
      <TabBar tabs={mockTabs} activeTab="Note 1" onSelect={vi.fn()} onClose={onClose} />
    );
    
    // Find the close button for Note 1
    // The button is in the same parent div as "Note 1"
    const tab1 = screen.getByText("Note 1").closest("div");
    const closeButton = tab1?.querySelector("button");
    
    if (closeButton) {
      fireEvent.click(closeButton);
    }
    
    expect(onClose).toHaveBeenCalledWith(mockTabs[0]);
  });
});
