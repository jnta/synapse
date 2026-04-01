.PHONY: all dev dev-frontend dev-backend build-all build-frontend build-backend release clean start

# Default target
all: dev

# --- Local Development (Browser + Quarkus:Dev) ---

# Starts the Vite dev server for the frontend (accessible at http://localhost:5173)
dev-frontend:
	@echo "Starting frontend dev server (Vite)..."
	cd src/main/webui && npm run dev

# Starts the Quarkus dev server for the backend
dev-backend:
	@echo "Starting backend dev server (Quarkus)..."
	./mvnw quarkus:dev

# Run both in parallel (browser development)
# This uses the '-j' flag to run concurrent processes. 
# Use Ctrl+C to kill both at once.
dev:
	@echo "Starting both frontend and backend for browser development..."
	@$(MAKE) -j2 dev-frontend dev-backend

# --- Electron Build (Production Pipeline) ---

# Build frontend production assets manually
build-frontend:
	@echo "Building frontend production assets..."
	cd src/main/webui && npm run build

# Build backend native runner (Quarkus native image)
# main.js expects target/synapse-1.0.0-SNAPSHOT-runner
build-backend:
	@echo "Building native backend executable (this may take a while)..."
	./mvnw package -Dnative -DskipTests

# Complete build pipeline: Frontend -> Backend -> Electron Package
# This creates the distributable versions of the app (AppImage, deb, etc.)
release: build-frontend build-backend
	@echo "Packaging Electron application with built binaries..."
	npm run make

# Alias for release
build: release

# --- Utilities ---

# Run the Electron wrapper in dev mode (requires backend to be running or built)
start:
	@echo "Starting Electron wrapper..."
	npm start

# Clean build artifacts to ensure a fresh start
clean:
	./mvnw clean
	rm -rf dist
	rm -rf out
	rm -rf target/*
	rm -rf src/main/webui/dist
	rm -rf src/main/webui/node_modules/.vite
