---
description: Generate tasks for front end
---

Act as a Senior Kotlin Multiplatform Developer. I am providing a Figma design for a [Screen Name, e.g., "Note Detail View"]. 

Your goal is to decompose this UI into a maintainable KMP structure. 
1. **Analyze the UI:** Identify reusable atomic components (Buttons, Cards, Inputs).
2. **State Management:** Define a `UiState` data class and a `UiEvent` sealed class for user actions.
3. **Architecture:** Use a ViewModel (via KMP-compatible library like Voyage or Decompose) to handle logic in `commonMain`.
4. **Best Practices:** Ensure use of Compose Multiplatform best practices (Modifiers, Material 3, and LocalProviders).

### Communication Protocol
- **Ambiguity Rule:** If a UI requirement lacks a defined "Search Trigger" or "Data Source," ASK before assuming.
- **Verification:** Always ask: "Does this component need to be accessible via keyboard-only navigation?"

Please output the result as a `TASK.md` file that breaks down the implementation into incremental, testable steps.