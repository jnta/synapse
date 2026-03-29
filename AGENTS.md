# AGENTS.md — Synapse

> **Synapse** is a local-first, AI-augmented Markdown note editor built for connected thinking.  
> Notes are first-class citizens linked to each other via typed bidirectional links, semantic embeddings, and hybrid search.

---

## 1. Project Overview

| Dimension        | Detail                                                                 |
|------------------|------------------------------------------------------------------------|
| **Purpose**      | Linked note-taking with semantic search, graph view, and AI assistance |
| **Philosophy**   | Local-first, privacy-first, human-readable Markdown files on disk      |
| **Search**       | Hybrid: lexical (full-text) + semantic (vector) + ColBERT re-ranking   |
| **Storage**      | Markdown files on the local filesystem (source of truth)               |
| **Intelligence** | Local embeddings via sentence-transformers; no cloud calls for content |

---

## 2. Technology Stack

### Backend — Quarkus (Java 25)

| Layer          | Technology                              | Purpose                                     |
|----------------|-----------------------------------------|---------------------------------------------|
| Framework      | Quarkus 3.34.1                          | Supersonic Subatomic Java runtime           |
| REST           | `quarkus-rest` + `quarkus-rest-jackson` | JAX-RS endpoints, JSON serialization        |
| DI / CDI       | `quarkus-arc`                           | Dependency injection and bean wiring        |
| Frontend host  | `quarkus-quinoa` 2.7.2                  | Serves the Vite/React SPA via Quarkus       |
| Build          | Maven (mvnw wrapper)                    | Build, packaging, and dependency management |
| Native Image   | GraalVM                                 | Compile Quarkus app to native image         |
| Testing        | `quarkus-junit`, `rest-assured`         | Unit and integration tests                  |
| Java version   | Java 25 (`maven.compiler.release=25`)   | Modern Java features                        |

### Desktop Client — Electron

| Layer        | Technology                        | Purpose                                    |
|--------------|-----------------------------------|--------------------------------------------|
| App Wrapper  | Electron                          | Transform web app into desktop application |

### Frontend — React + Vite

| Layer        | Technology                        | Purpose                                    |
|--------------|-----------------------------------|--------------------------------------------|
| Framework    | React 19                          | UI component model                         |
| Language     | TypeScript 5.9                    | Type-safe frontend                         |
| Bundler      | Vite 8                            | Fast dev server and optimized builds       |
| Linting      | ESLint 9 + typescript-eslint      | Code quality enforcement                   |
| Dev Server   | Port 5173 (proxied by Quinoa)     | Hot Module Replacement in dev mode         |

### Infrastructure / Planned

| Concern        | Technology                              | Purpose                                       |
|----------------|-----------------------------------------|-----------------------------------------------|
| Vector search  | `sqlite-vec` + `all-MiniLM-L6-v2`      | Local semantic embeddings over Markdown vault |
| Hybrid search  | Lucene (full-text) + vector re-ranking  | Lexical + semantic hybrid retrieval           |
| File watching  | Java `WatchService` / Quarkus FS       | Detect Markdown changes on disk               |
| Embeddings     | Local sentence-transformers (ONNX)      | Offline, zero-cloud embedding generation      |

---

## 3. Running the Project

```bash
# Start Quarkus backend (also boots Vite via Quinoa)
./mvnw quarkus:dev

# Frontend only (from src/main/webui)
cd src/main/webui && npm run dev

# Run backend tests
./mvnw test

# Production package (JVM)
./mvnw package

# Production package (Native Image)
./mvnw package -Dnative
```

Quarkus DevUI: `http://localhost:8080/q/dev`  
React SPA: `http://localhost:8080` (served by Quinoa) or `http://localhost:5173` (direct Vite)

---

## 4. Architecture

Synapse follows **Clean Architecture** with **Domain-Driven Design (DDD)**.  
The rule is simple: dependencies point inward. Domain has zero outward dependencies.

### 4.1 Backend Package Structure

```
src/main/java/org/synapse/
├── domain/                         # Zero external dependencies
│   ├── model/                      # Note, Tag, Link, Vault
│   ├── service/                    # LinkIntegrityService, DecayService
│   └── repository/                 # NoteRepository (interface only)
│
├── application/                    # Orchestrates domain objects
│   ├── dto/                        # NoteDTO, SearchResultDTO (React contracts)
│   └── usecase/                    # SearchNotes, CreateNote, RenameNote,
│                                   # GetBacklinks, PruneDecayedNotes
│
├── infrastructure/                 # Framework-specific implementations
│   ├── persistence/                # LocalFileNoteRepository (reads/writes .md)
│   ├── search/                     # LuceneIndexer, VectorIndexer, HybridSearch
│   ├── embedding/                  # OnnxEmbeddingService (sqlite-vec integration)
│   ├── watcher/                    # FileSystemWatcher (triggers re-index)
│   └── rest/                       # JAX-RS Resources (Quarkus entry points)
│
└── configuration/                  # Quarkus CDI wiring, @ApplicationScoped beans
```

**Dependency rules:**
- `domain` → nothing
- `application` → `domain`
- `infrastructure` → `application` + `domain`
- `configuration` → all (wires everything together)

### 4.2 Frontend Source Structure

```
src/main/webui/src/
├── core/                           # Shared enterprise layer
│   ├── api/                        # Base fetch config, interceptors
│   ├── components/                 # Design system: Button, Input, Tooltip, Modal
│   └── types/                      # Global types: NoteId, EntityId, SearchResult
│
├── features/                       # Vertical slices by domain
│   ├── notes/                      # Note management
│   │   ├── api/                    # Note-specific endpoints (CRUD, backlinks)
│   │   ├── components/             # NoteEditor, NoteList, BacklinkPanel
│   │   ├── hooks/                  # useNoteContent, useBacklinks, useNoteSync
│   │   ├── store/                  # Note-specific state (Zustand)
│   │   └── index.ts                # Public API — only import from here
│   │
│   ├── graph-view/                 # Knowledge graph visualization
│   │   ├── components/             # GraphCanvas, NodeTooltip, DecayHeatmap
│   │   ├── hooks/                  # useGraphData, useNodeSelection
│   │   └── index.ts
│   │
│   └── search/                     # Global hybrid search
│       ├── api/                    # Search endpoints (lexical + semantic)
│       ├── components/             # SearchBar, SearchResultCard, FilterPanel
│       ├── hooks/                  # useSearch, useSearchHistory
│       └── index.ts
│
└── layouts/                        # App shell and routing
    ├── AppShell.tsx                # Sidebar + main content area
    └── Router.tsx                  # SPA routing
```

**Import discipline:**
- Features are isolated — `notes` never imports from `graph-view` directly.
- Cross-feature communication goes through `core/` or app-level state.
- Always import features through their `index.ts` barrel export.

---

## 5. Domain Model

```
Note
  ├── id: NoteId (UUID or slug derived from filename)
  ├── title: String
  ├── content: String (raw Markdown)
  ├── filePath: Path
  ├── tags: Set<Tag>
  ├── links: Set<Link>          ← outgoing [[wikilinks]]
  ├── backlinks: Set<Link>      ← computed by LinkIntegrityService
  ├── createdAt: Instant
  ├── lastAccessedAt: Instant
  └── state: NoteState          ← DORMANT | ACTIVE | MASTERED

Link
  ├── source: NoteId
  ├── target: NoteId
  └── label: LinkLabel          ← SUPPORTS | CONTRADICTS | EXTENDS | REFERENCES

Tag
  └── name: String

NoteState
  ├── DORMANT   — raw capture, hidden 48h, no links
  ├── ACTIVE    — linked to a project, has user-generated content
  └── MASTERED  — high link density, passed active recall
```

---

## 6. API Contract (REST)

All endpoints are under `/api/v1`. JSON in/out.

| Method | Path                          | Use Case           |
|--------|-------------------------------|--------------------|
| GET    | `/api/v1/notes`               | List all notes     |
| GET    | `/api/v1/notes/{id}`          | Get note by ID     |
| POST   | `/api/v1/notes`               | Create note        |
| PUT    | `/api/v1/notes/{id}`          | Update note        |
| DELETE | `/api/v1/notes/{id}`          | Delete note        |
| GET    | `/api/v1/notes/{id}/backlinks`| Get backlinks      |
| POST   | `/api/v1/search`              | Hybrid search      |
| GET    | `/api/v1/graph`               | Graph data (nodes + edges) |

Request/response shapes are defined as DTOs in `application/dto/` — these are the shared contracts between backend and frontend TypeScript types.

---

## 7. Search Architecture

Synapse uses a three-stage hybrid search pipeline:

```
Query
  │
  ├── 1. Lexical (Lucene full-text)    → scored candidate set A
  ├── 2. Semantic (sqlite-vec + ONNX)  → scored candidate set B
  │
  └── 3. Reciprocal Rank Fusion (RRF)  → merged final ranking
```

- Embeddings are generated locally using `all-MiniLM-L6-v2` in ONNX format.
- Lucene index is rebuilt on `FileSystemWatcher` events (file create/modify/delete).
- Vector index is updated asynchronously off the main thread.
- Search must respond in < 200ms for the user-visible UI path.

---

## 8. Coding Conventions

### General
- No comments explaining *what* the code does. Comments are only for *why* when non-obvious.
- All features are behind their `index.ts` barrel — no deep internal imports across features.
- DTOs are immutable records. Domain models are rich objects with behavior.
- Infrastructure classes implement domain interfaces — never the reverse.

### Backend (Java)
- Use `record` for DTOs and value objects.
- `@ApplicationScoped` for stateless services; `@Singleton` only when state is intentional.
- Repository interfaces live in `domain.repository`; implementations in `infrastructure.persistence`.
- JAX-RS resources are thin — they delegate immediately to use cases.
- Use `Uni<T>` / `Multi<T>` for async I/O at the infrastructure boundary.

### Frontend (TypeScript / React)
- Functional components only. No class components.
- State: local `useState` for UI state, Zustand for feature-level shared state.
- Async data fetching via custom hooks inside `features/*/hooks/`.
- No inline styles. CSS Modules or Vanilla CSS with design tokens.
- Component props are typed with explicit interfaces, never `any`.

---

## 9. Non-Negotiable Constraints

1. **Offline-first:** Zero network calls for note content or embeddings. All AI runs locally.
2. **Human-readable files:** Markdown on disk is the source of truth. The database is a cache.
3. **Non-blocking UI:** Embedding generation and indexing must not block the main thread.
4. **< 200ms search:** User-visible search results must be delivered within 200ms.
---

## 10. Agent Skills

Agent skills are specialized knowledge bases located in `.agents/skills/` that provide deep context for specific subsystems.

| Skill | Path | Focus |
|-------|------|-------|
| **Tiptap Markdown Editor** | `.agents/skills/tiptap/SKILL.md` | Expertise in Tiptap integration, synchronization, and Markdown conversion. |

