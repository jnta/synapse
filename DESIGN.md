# Design System Strategy: The Digital Curator

### 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Digital Curator."** In an age of information overload, this system acts as a silent, sophisticated gallery for the user’s thoughts. It moves beyond "minimalism" into the realm of **Editorial Precision**.

The design breaks the "template" look by treating every screen as a high-end publication layout. We reject the standard "sidebar-and-main-content" grid in favor of intentional asymmetry and extreme negative space. By utilizing high-contrast typography and a ruthless commitment to tonal separation over structural lines, we create an environment where the user’s content is the only ornamentation. It feels less like a utility and more like a custom-designed workspace for the mind.

---

### 2. Colors & Surface Philosophy
This system operates on a monochromatic spectrum to minimize cognitive load and maximize sophistication.

*   **Primary (#000000):** Reserved for the most critical actions and high-level typography. It provides the "ink" on the digital page.
*   **Surface (#f9f9fb):** The default canvas. It is a warm, off-white that reduces eye strain compared to pure hex white.
*   **The "No-Line" Rule:** To achieve a high-end feel, **1px solid borders are strictly prohibited** for sectioning. Structural boundaries must be defined solely through background color shifts. For example, a sidebar should be set to `surface_container_low` (#f3f3f5) against the main `surface` (#f9f9fb).
*   **Surface Hierarchy & Nesting:** Use the surface-container tiers to create depth. Treat the UI as a series of stacked sheets of fine paper. 
    *   *Example:* Place a `surface_container_lowest` (#ffffff) card on top of a `surface_container` (#eeeef0) background. This creates a "soft lift" that feels architectural rather than digital.
*   **The Glass Threshold:** While the aesthetic is flat, floating elements (like a command palette or context menu) should utilize `surface_container_lowest` at 85% opacity with a `20px` backdrop blur. This provides "visual soul" and ensures the UI feels like a modern application, not a static document.

---

### 3. Typography: The Editorial Engine
Typography is the primary vehicle for brand expression. We use **Inter** for its technological precision and neutral, clean appearance.

*   **Display & Headline (Display-LG to Headline-SM):** These are your "statement" levels. Use them with generous leading and wide margins. They should feel like titles in a contemporary art magazine.
*   **Body (Body-LG to Body-MD):** Optimized for long-form note-taking. The line height should be set to 1.6x the font size to ensure the "high negative space" feeling extends into the text itself.
*   **Monospace Utility:** For technical metadata, dates, or code snippets, use a monospace font (like SF Mono) in the `label-sm` scale. This contrasts with the Inter sans-serif to provide a "technological" edge.
*   **Intentional Asymmetry:** Align headlines to the left with significantly larger top margins than bottom margins. This "top-heavy" layout creates a sophisticated, non-template rhythm.

---

### 4. Elevation & Depth: Tonal Layering
Traditional shadows and borders are replaced by **Tonal Layering**.

*   **The Layering Principle:** Depth is achieved by stacking. A `surface_container_highest` (#e2e2e4) element should be used for the most temporary or high-priority surfaces, such as an active search bar or a focused note.
*   **Ambient Shadows:** If a floating effect is required for a modal, use a shadow with a 32px blur and 4% opacity, using the `on_surface` (#1a1c1d) color. This mimics natural, ambient gallery lighting.
*   **The "Ghost Border" Fallback:** In rare cases where accessibility requires a container boundary (e.g., a text input), use the `outline_variant` (#c6c6c6) at **15% opacity**. It should be felt, not seen.

---

### 5. Components

*   **Buttons:** 
    *   *Primary:* Solid `primary` (#000000) with `on_primary` (#e2e2e2) text. Use the `md` (0.375rem) roundedness for a sharp, modern look. 
    *   *Secondary:* No background. Use a `title-sm` weight with a simple `primary` text color.
*   **Input Fields:** No boxes. Use a `label-md` for the title and a simple baseline shift or a `surface_container_low` background fill. Avoid 4-sided borders.
*   **Cards & Lists:** **Strictly no divider lines.** Separate notes in a list using `16px` of vertical white space or a subtle shift to `surface_container_lowest` on hover.
*   **Chips:** Use `surface_container_high` (#e8e8ea) with `on_surface_variant` (#474747) text. These should be pills (`full` roundedness) to contrast with the sharper corners of the main UI.
*   **The "Synapse" Editor:** The main note-taking area should be a pure `surface` background with no visible UI elements until interaction, creating a "Zen" state for the user.

---

### 6. Do's and Don'ts

**Do:**
*   **Do** use white space as a structural element. If a section feels crowded, add more space rather than a line.
*   **Do** use `primary` (#000000) for \"destructive\" actions—black is more authoritative and sophisticated than red in this high-end context.
*   **Do** ensure text contrast meets WCAG AA standards by using the `on_surface` and `on_surface_variant` tokens appropriately.

**Don't:**
*   **Don't** use pure #000000 for body text; use `on_surface` (#1a1c1d) to maintain a soft, premium feel.
*   **Don't** use icons unless absolutely necessary. Rely on clear, well-spaced typography first.
*   **Don't** use standard \"drop shadows.\" If it looks like a default software shadow, it is wrong.
*   **Don't** use center alignment for anything other than specific, isolated \"empty state\" illustrations. Editorial design lives on the margins.

---

### Design Tokens (Hex Codes)
- **Background:** #f9f9fb
- **On Background:** #1a1c1d
- **Surface:** #f9f9fb
- **Surface Container Lowest:** #ffffff
- **Surface Container Low:** #f3f3f5
- **Surface Container:** #eeeef0
- **Surface Container High:** #e8e8ea
- **Surface Container Highest:** #e2e2e4
- **Primary:** #000000
- **On Primary:** #e2e2e2
- **Outline:** #777777
- **Outline Variant:** #c6c6c6
