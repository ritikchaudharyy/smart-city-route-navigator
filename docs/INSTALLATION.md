# Installation Guide

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| JDK | 17 or newer | Required — the project uses Java 17 language features and `pom.xml` targets release 17. |
| Maven | 3.8+ | Used to compile, test, and package the project. Bundled Maven wrappers can be added if you don't have Maven installed globally. |
| IDE (optional) | VS Code or IntelliJ IDEA | Either works out of the box with a standard Maven project layout — no special plugin configuration is required. |

Verify your setup:

```bash
java -version    # should report 17 or higher
mvn -version     # should report 3.8 or higher
```

## 1. Get the project

```bash
git clone <your-fork-or-repo-url>
cd smart-city-route-navigator
```

## 2. Build

```bash
mvn clean package
```

This compiles all sources, runs the full JUnit test suite (see
`docs/`'s testing notes and the `src/test` directory), and produces a
runnable, dependency-bundled JAR at:

```
target/smart-city-route-navigator.jar
```

To skip tests during a build (not recommended, but useful for a quick
iteration cycle):

```bash
mvn clean package -DskipTests
```

## 3. Run

```bash
java -jar target/smart-city-route-navigator.jar
```

The application will show a splash screen, load the bundled default
city, and open the main window.

## 4. Run from an IDE

### IntelliJ IDEA
1. **File > Open** and select the project's root folder (the one containing `pom.xml`).
2. IntelliJ will detect the Maven project automatically and import it.
3. Open `src/main/java/com/smartcity/navigator/Main.java` and click the green run arrow next to `public static void main`.

### VS Code
1. Install the **Extension Pack for Java** (includes Maven and debugger support).
2. Open the project's root folder.
3. Open `Main.java`; VS Code shows a **Run** / **Debug** code lens above `main` — click **Run**.

## 5. Run the test suite only

```bash
mvn test
```

Runs every test under `src/test/java`, covering `CityGraph`,
`MinPriorityQueue`, `DijkstraAlgorithm`, and `RouteService`.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `error: release version 17 not supported` | An older JDK is active | Install JDK 17+ and point `JAVA_HOME` at it. |
| Blank/garbled window on Linux | Missing desktop/GUI environment | Swing requires a display; run on a machine (or remote desktop session) with a graphical environment. |
| `Failed to start Smart City Route Navigator` dialog on launch | The bundled `default-city.dat` resource failed to load — normally only possible if the JAR was built incorrectly | Rebuild with `mvn clean package` and confirm `src/main/resources/data/default-city.dat` exists. |
