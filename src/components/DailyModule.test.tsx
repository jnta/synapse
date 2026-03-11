import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { DailyModule } from "./DailyModule.tsx";
import type { NoteEntry } from "@/services/tauri.ts";

describe("DailyModule", () => {
  const mockNotes: NoteEntry[] = [
    { name: "2023-10-01", folder: "daily-notes", modified: 123 },
    { name: "2023-10-02", folder: "daily-notes", modified: 456 },
  ];

  it("should render daily notes header with count", () => {
    render(<DailyModule notes={mockNotes} onOpen={vi.fn()} />);
    expect(screen.getByText(/Daily Notes/i)).toBeInTheDocument();
    expect(screen.getByText("(2)")).toBeInTheDocument();
  });

  it("should display all notes when open", () => {
    render(<DailyModule notes={mockNotes} onOpen={vi.fn()} />);
    expect(screen.getByText("2023-10-01")).toBeInTheDocument();
    expect(screen.getByText("2023-10-02")).toBeInTheDocument();
  });

  it("should toggle visibility when clicking the header", () => {
    render(<DailyModule notes={mockNotes} onOpen={vi.fn()} />);
    
    // Header is open by default. Verify notes are visible.
    expect(screen.getByText("2023-10-01")).toBeInTheDocument();

    // Click to collapse
    const button = screen.getByRole("button");
    fireEvent.click(button);

    // Verify notes are no longer in the document
    expect(screen.queryByText("2023-10-01")).not.toBeInTheDocument();

    // Click to expand again
    fireEvent.click(button);
    expect(screen.getByText("2023-10-01")).toBeInTheDocument();
  });

  it("should call onOpen with the correct note when a note is clicked", () => {
    const onOpen = vi.fn();
    render(<DailyModule notes={mockNotes} onOpen={onOpen} />);
    
    const noteElement = screen.getByText("2023-10-01");
    fireEvent.click(noteElement);

    expect(onOpen).toHaveBeenCalledWith(mockNotes[0]);
  });

  it("should show empty message when no notes are provided", () => {
    render(<DailyModule notes={[]} onOpen={vi.fn()} />);
    expect(screen.getByText(/No daily notes yet/i)).toBeInTheDocument();
  });
});
