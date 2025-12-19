# gz-core
gz-core is a personal portfolio project built in Java using the libGDX framework. It is a small 2D top-down shooter developed to explore game architecture, real-time input handling, enemy AI behavior, and state-driven gameplay systems.  

This project is not intended to be a production-ready game or reusable library. It was created to demonstrate design decisions, implementation skills, and familiarity with game development concepts in Java.

## Gameplay Overview
- 2D top-down perspective
- W/A/S/D movement
- Shooting via left mouse button
- Hostile AI-controlled enemies
- Enemy drops (medkits, ammo packs)
- Health and ammo restoration via pickups
- Game resets to the main menu on player death

## Screenshots

### Main Menu
![Main Menu](assets/screenshots/main-menu.png)

### Gameplay
![Gameplay](assets/screenshots/gameplay.png)

## Technologies Used
- Java 17+
- libGDX
- Gradle
- LWJGL3 (desktop backend)

## Project Structure
gz-core/  
├── core/    # Core game logic, entities, systems, screens  
├── lwjgl3/  # Desktop launcher (LWJGL3)  
├── assets/  # Textures, sounds, and other resources  
├── build.gradle  
└── settings.gradle  

## Running the Project
To build a runnable desktop distribution, run from the source:
```bash
git clone https://github.com/zachnv/gz-core.git
cd gz-core
./gradlew lwjgl3:run
```

On Windows:
```bash
gradlew lwjgl3:run
```

## Background
This project was originally developed as an academic project and
later refined, cleaned up, and published as a personal project.

- Experimentation with game systems and mechanics
- Iterative development rather than feature completeness
- A focus on code structure and learning outcomes

## Credits:
- https://szadiart.itch.io/postapo-lands-set1 - Tileset for map  
- https://blackhunterdev.itch.io/stlaker-like-pixel-art-pack - All other textures
