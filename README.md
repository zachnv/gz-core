# GZ (libGDX)
GZ is a Java-based 2D top-down roguelike shooter demo built with libGDX.
The project features AI-driven enemies, tile-based map, and designed for scalability and maintainability.

## Requirements
- Java 17 or newer

## Running the Game
To build a runnable desktop distribution, run from the source:
```bash
git clone https://github.com/zachvaughn/gz-core.git
cd gz-core
./gradlew lwjgl3:run

On Windows:
gradlew lwjgl3:run
```

## Controls
- Move: W A S D
- Shoot: Left Mouse Button
- Reload: R (automatic reload when magazine is empty)

## Gameplay Overview
The player spawns with an assault rifle and a limited ammo supply.
AI-controlled enemies spawn throughout the level and engage the player.
Defeated enemies have a chance to drop medkits that restore the player to full health.

When the player dies, the game returns to the main menu, allowing the player
to restart or exit.

## Project Structure
```core``` — Shared game logic and systems\
```lwjgl3``` — Desktop launcher using LWJGL3

## Background
This project was originally developed as an academic project and
later refined, cleaned up, and published as a personal project.

## Credits:
```https://szadiart.itch.io/postapo-lands-set1``` - Tileset for map
```https://blackhunterdev.itch.io/stlaker-like-pixel-art-pack``` - All other textures
