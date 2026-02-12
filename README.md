# PhysicsEngine

JavaFX-based 2D physics simulation and multiplayer playground built with Maven.

## Overview
PhysicsEngine includes:
- A single-player physics sandbox with multiple motion models (linear, projectile, freefall, circular, pendulum, and simple harmonic).
- A multiplayer mode with client/server networking, state synchronization, and collision handling.
- A modular core with reusable physics primitives (`Particle`, `Vector2D`, `PhysicsWorld`) and rendering/UI layers.

## Tech Stack
- Java 21
- JavaFX 21.0.1
- Maven

## Project Structure
- `src/main/java/it/unibs/pajc/core`: physics model, constants, and simulation world.
- `src/main/java/it/unibs/pajc/core/motion`: motion calculators and motion types.
- `src/main/java/it/unibs/pajc/collision`: collision detection and resolution.
- `src/main/java/it/unibs/pajc/network`: multiplayer server/client and protocol messages.
- `src/main/java/it/unibs/pajc/ui`: JavaFX application, scenes, and rendering components.
- `src/main/java/it/unibs/pajc/Main.java`: application entry point.

## Requirements
- JDK 21 installed and available on `PATH`
- Maven 3.9+

## Run
From the project root:

```bash
mvn clean javafx:run
```

Alternative:

```bash
mvn clean package
mvn javafx:run
```

## Multiplayer Notes
- Default server port: `5555`
- Tick rate: `60`
- Max players: `4`

These values are defined in `PhysicsConstants`.

## Build Artifact
To generate the jar:

```bash
mvn clean package
```

The artifact is produced under `target/`.

## Entry Point
Configured main class in `pom.xml`:
- `it.unibs.pajc.Main`
