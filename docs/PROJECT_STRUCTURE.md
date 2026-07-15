# Project Structure

```
smart-city-route-navigator/
├── pom.xml                          Maven build: Java 17, JUnit 5, shade plugin for a runnable fat JAR
├── .gitignore
├── README.md                        Project overview and quick start
├── docs/                            Full documentation set (this folder)
│   ├── INSTALLATION.md              Setup, build, run, troubleshoot
│   ├── PROJECT_STRUCTURE.md         This file
│   ├── ALGORITHM.md                 Dijkstra explanation + worked example
│   ├── UML_DIAGRAMS.md              Use case, class, sequence, activity, component diagrams
│   ├── FLOWCHARTS.md                Application, graph creation, route search, Dijkstra, GUI flowcharts
│   └── screenshots/                 Screenshot placeholders referenced by README.md
└── src/
    ├── main/
    │   ├── java/com/smartcity/navigator/
    │   │   ├── Main.java             Application entry point (splash -> load graph -> main window)
    │   │   │
    │   │   ├── model/                Plain data classes — no business logic, no Swing/graph dependencies
    │   │   │   ├── Location.java     Graph vertex: id, name, x/y map coordinates
    │   │   │   ├── Edge.java         Weighted, undirected road between two location ids
    │   │   │   ├── Node.java         Comparable (id, distance) pair used only by Dijkstra's priority queue
    │   │   │   └── PathResult.java   Algorithm outcome: route, distance, success/failure + message
    │   │   │
    │   │   ├── graph/                The city road network as a data structure
    │   │   │   ├── CityGraph.java        Adjacency-list graph: add/remove locations & roads, queries
    │   │   │   ├── GraphBuilder.java     Fluent construction API wrapping CityGraph
    │   │   │   ├── GraphLoader.java      Parses/writes the custom .dat text format; bundles the default city
    │   │   │   └── GraphLoadException.java  Single checked exception type for all load/save failures
    │   │   │
    │   │   ├── algorithm/            Pathfinding — no knowledge of the UI or file I/O
    │   │   │   ├── MinPriorityQueue.java   Hand-built binary min-heap (offer/poll/peek, O(log n))
    │   │   │   └── DijkstraAlgorithm.java  Shortest-path search: distance map, parent map, visited set
    │   │   │
    │   │   ├── service/              The single bridge between UI and algorithm/graph
    │   │   │   └── RouteService.java     findRoute(), load/save/reset graph, add/remove location & road
    │   │   │
    │   │   ├── ui/                   Swing presentation layer — contains no business logic
    │   │   │   ├── MainFrame.java        Menu bar, toolbar, layout, status bar; wires every action to RouteService
    │   │   │   ├── RoutePanel.java       Source/destination dropdowns, Find Route / Reset buttons
    │   │   │   ├── ResultPanel.java      Distance/route display + scrollable timestamped activity log
    │   │   │   ├── MapPanel.java         Draws the graph and highlights the computed route; zoom support
    │   │   │   ├── AboutDialog.java      Help > About
    │   │   │   ├── SettingsDialog.java   Dark mode + distance unit (km/mi) preferences
    │   │   │   ├── SplashScreen.java     Non-blocking startup splash (Swing Timer, not Thread.sleep)
    │   │   │   └── IconFactory.java      Procedurally drawn toolbar/menu icons (no external image assets)
    │   │   │
    │   │   └── utils/                Small, shared, stateless helpers
    │   │       ├── Constants.java        App title, version, window size, distance unit, id prefix
    │   │       ├── Validators.java       Input validation returning Optional<String> error messages
    │   │       ├── Formatter.java        PathResult/distance -> display strings (unit-aware)
    │   │       ├── Helpers.java          Id generation, null-safe trim, safe double parsing
    │   │       └── AppSettings.java       Singleton user preference store (dark mode, distance unit)
    │   │
    │   └── resources/
    │       ├── data/
    │       │   └── default-city.dat  Bundled sample city: 8 locations, 12 roads, loaded on first launch
    │       └── icons/                 Reserved for future externally-authored icon assets, if ever needed
    │
    └── test/
        └── java/com/smartcity/navigator/
            ├── algorithm/
            │   ├── MinPriorityQueueTest.java
            │   └── DijkstraAlgorithmTest.java   Includes a regression test against the real bundled dataset
            ├── graph/
            │   └── CityGraphTest.java
            └── service/
                └── RouteServiceTest.java         Includes a real file save/load round-trip test
```

## Dependency direction

Each layer only depends on the layers below it — never sideways or
upward — which is what keeps the codebase modular and testable in
isolation:

```
ui  ─────────────►  service  ─────────────►  algorithm ─────────────►  model
                         │                        │
                         └────────► graph ◄───────┘
                                       │
                                       ▼
                                     model

utils is a leaf: everything may depend on it, it depends on nothing else in the project.
```

- `model` has zero dependencies on any other package in this project.
- `graph` depends only on `model`.
- `algorithm` depends only on `graph` and `model`.
- `service` depends on `algorithm`, `graph`, `model`, and `utils` — it is the only layer the UI is allowed to call into for business logic.
- `ui` depends on `service`, `model` (to read `Location`/`PathResult` fields for display), and `utils` (formatting/validation display, settings, icons). It never calls into `graph` or `algorithm` directly.
