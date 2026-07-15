# Architecture

The project follows a lightweight MVC-like separation:

- model: domain objects such as locations, edges, and path results
- graph: graph structure and file-based persistence of city data
- algorithm: shortest-path computation using Dijkstra's algorithm
- service: orchestration layer between the UI and the core engine
- ui: Swing-based presentation and interaction layer

This structure keeps the core routing logic isolated from the interface, which makes the project easier to test and evolve.
