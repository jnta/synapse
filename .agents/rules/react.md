---
trigger: manual
---

# React + TypeScript Rules
- Use Functional Components with explicit 'interface Props'.
- Prefer 'interface' for object definitions and 'type' for unions/aliases.
- Enforce strict null checks; never use the non-null assertion operator (!).
- Hooks returning >2 values must return a named object.
- Directory structure: Follow "Vertical Slices" (feature-based).
- Styling: Use Tailwind CSS with a focus on "Monochrome Editorial" (B&W).
- All API calls must have defined 'Request' and 'Response' types.

# Import & Structure Rules
- Use absolute imports with `@/`.
- No `export default`; use named exports for better TS intellisense.
- Group imports: External > Core > Feature > Assets.

# State Management Rules
- Server State: Use TanStack Query for all Quarkus API interactions.
- Global State: Use Zustand for cross-feature state (Theme, Active Vault).
- Local State: Limit 'useState' to UI-only toggles.
- Immutability: Use Immer for complex state updates.