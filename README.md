# Synapse 🧠
> The AI-Native Cognitive Gym (Active Learning & Desirable Difficulty).

Synapse is a local-first, high-friction knowledge management system designed to transform passive consumption into active mastery. Built on the principle of **"Friction as a Feature"**, it forces synthesis and reflection rather than simple storage.

## 🚀 Getting Started

### Prerequisites
- **JDK 21** (Required for Hot Reload)
- **Kotlin 2.1.20**
- **Gradle 8.10.2**

### Running the Application

To run the application with **Hot Reload** (supported on Desktop/JVM):

```bash
./run-hot.sh
```

Or using Gradle directly:

```bash
./gradlew :desktopApp:hotRunJvm
```

### Key Commands
- `./gradlew :desktopApp:run` - Standard run (no hot reload).
- `./gradlew :desktopApp:hotRunJvm` - Run with real-time UI updates on save.

## 🛠 Tech Stack
- **UI:** Compose Multiplatform (Kotlin)
- **Backend:** Kotlin Multiplatform / JVM
- **Database:** Local Markdown + `sqlite-vec` (Semantic Search)
- **AI:** Local LLM via Ollama / llama.cpp

## ⚖️ Friction Logic
- **Resonance Filter:** No "silent saves." Every note requires original thought.
- **Fog of War:** Progressive text masking to force active recall.
- **Semantic Decay:** Resources are pruned if not actively linked or accessed.
- **No Passive Summaries:** AI generates quizzes to unlock synthesis.

---
*Built for deep work and cognitive endurance.*