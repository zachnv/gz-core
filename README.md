# Ground Zero Project by Zach Vaughn

Ground Zero as of now, is a 2D top down rogue-like shooter that features AI enemies, and a pre-made tile map.
You can collect items such as MedKits, or Ammo in your inventory as of now, and collect a score for more enemies you kill.
The starting weapon is currently an AR, but later on the plan is to make it so that a pistol with unlimited ammo is the starting weapon,
and you will be able to find better weapons in certain areas such as houses, or AI enemies can drop them.

# Instructions on how to run the game
To run the game, clone into IntelliJ and then scroll down to
"lwjgl3\src\main\java\io.github.groundzero\Lwjgl3Launcher"
and click the run button in the top right as you would to run any other program.

# Instructions on how to play the game
Launch the game, click play and then it takes you into the game screen where you spawn with an AR, 120 bullets,
and can walk around using (W, A, S, D) and left-click on mouse to shoot, when your mag is empty, the gun will automatically reload,
or if you would like to reload when needed, just press "R", and also this is self explanatory,
but the bullets go wherever your crosshair is aiming.

Spawns basic AI enemies as well. When an enemy dies, they also have a chance of dropping a medkit which will heal you to full health.
When the player dies, it displays the dead texture and sends you back to the main menu screen where you can play again or exit.

Pre-made tile-map and adding in other weapons and scoring/progression will be added later on.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
