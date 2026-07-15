# Smart City Route Navigator

A desktop Java application that models a city road network as a
weighted graph and finds the shortest route between two locations
using **Dijkstra's Algorithm**, powered by a hand-built binary min-heap
priority queue.

Built as a production-quality reference project demonstrating clean
architecture, core data structures & algorithms, and a fully functional
Swing GUI — not a tutorial demo.

## Versioning

- v1.0.0 — Initial release
- v1.1.0 — Documentation, configuration, and GitHub-ready polish
- v2.0.0 — Planned major enhancement release

See [docs/CHANGELOG.md](docs/CHANGELOG.md) for the full release history.

## Features

**Core**
- Add/remove locations and roads on a weighted, undirected graph
- Shortest-route computation with total distance and full route trace
- Route reconstruction via a parent-pointer map

**GUI**
- Source/destination dropdowns, Find Route / Reset / Exit controls
- Live map visualization of the city graph with the computed route highlighted
- Menu bar (File, View, Help) and toolbar — every item is fully wired, nothing is a dummy placeholder
- Scrollable, timestamped activity log; status bar; splash screen on launch
- Settings dialog with genuinely functional preferences (dark mode, km/mi distance unit)

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| GUI | Java Swing |
| Build | Maven |
| Testing | JUnit 5 |
| Core DSA | Custom adjacency-list graph, hand-built binary min-heap, Dijkstra's Algorithm |

## Quick Start

```bash
mvn clean package
java -jar target/smart-city-route-navigator.jar
```

Full setup instructions, IDE-specific steps, and troubleshooting are in
**[docs/INSTALLATION.md](docs/INSTALLATION.md)**.

## Documentation

| Document | Contents |
|---|---|
| [docs/INSTALLATION.md](docs/INSTALLATION.md) | Prerequisites, build, run, IDE setup, troubleshooting |
| [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) | Full annotated directory tree and package dependency rules |
| [docs/ALGORITHM.md](docs/ALGORITHM.md) | Dijkstra's Algorithm explained, plus a verified step-by-step trace against the bundled sample city |
| [docs/UML_DIAGRAMS.md](docs/UML_DIAGRAMS.md) | Use case, class, sequence, activity, and component diagrams |
| [docs/FLOWCHARTS.md](docs/FLOWCHARTS.md) | Application flow, graph creation, route search, Dijkstra's algorithm, and GUI workflow flowcharts |
| [docs/OPTIMIZATION.md](docs/OPTIMIZATION.md) | Complexity audit of every core operation, the edge-caching optimization applied, and how it's verified |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | High-level architecture overview and package responsibilities |
| [docs/API.md](docs/API.md) | Main service-layer API entry points |
| [docs/KNOWN_LIMITATIONS.md](docs/KNOWN_LIMITATIONS.md) | Known gaps and future constraints |
| [docs/CHANGELOG.md](docs/CHANGELOG.md) | Release history and roadmap |

## Project Structure (summary)

```
src/main/java/com/smartcity/navigator/
├── Main.java
├── model/       Location, Edge, Node, PathResult
├── graph/       CityGraph, GraphBuilder, GraphLoader, GraphLoadException
├── algorithm/   MinPriorityQueue, DijkstraAlgorithm
├── service/     RouteService  (the single bridge between UI and algorithm/graph)
├── ui/          MainFrame, RoutePanel, ResultPanel, MapPanel, dialogs, SplashScreen, IconFactory
└── utils/       Constants, Validators, Formatter, Helpers, AppSettings
```

See [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) for the full,
annotated tree and the dependency rules between layers.

## The Algorithm, in brief

- Graph: undirected adjacency list (`CityGraph`), positive edge weights only (enforced by `Edge`).
- Search: `DijkstraAlgorithm` using a hand-built `MinPriorityQueue` (binary min-heap), a distance map, a parent map, and a finalized/visited set.
- Complexity: **O((V + E) log V)**.
- Full explanation and a verified worked example (`Home → Market → Hospital → Mall`, 14 km): [docs/ALGORITHM.md](docs/ALGORITHM.md).

## Testing

```bash
mvn test
```

Covers `MinPriorityQueue`, `CityGraph` (including edge-cache invalidation), `DijkstraAlgorithm` (including a
regression test against the real bundled city data and a 625-location
grid scale test), and `RouteService`
(including a real file save/load round-trip). See
`src/test/java/com/smartcity/navigator/`.

## Future Scope

The architecture (graph/algorithm/service/UI kept strictly separated)
was designed so these can be added without restructuring existing code:

- Live traffic and road-closure simulation
- A* and other alternative routing algorithms
- GPS and Maps API integration
- Database-backed persistence and user accounts
- JavaFX migration for a more modern UI
- Multi-city support and city switching
- Full theme-driven UI customization and richer desktop polish

## Known Limitations

- Map coordinates in `default-city.dat` are illustrative layout hints, not real-world geographic coordinates.
- The custom `.dat` graph format is intentionally simple (no JSON/XML) — hand-edit-friendly, but without schema validation beyond what `GraphLoader` checks.
- `CityGraph` and `RouteService` are not thread-safe; the application is single-threaded on the Swing Event Dispatch Thread, which is correct for this use case but would need synchronization if extended to a multi-threaded or networked context.
- No persistence beyond manual Save Graph / Load Graph — there is no auto-save or database backing yet (see Future Scope).

## License

This project is provided as an educational / portfolio reference
project. Add a license file (e.g. MIT) before publishing publicly if
you intend for others to reuse the code.