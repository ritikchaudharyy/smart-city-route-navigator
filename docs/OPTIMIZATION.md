# Optimization Notes

This document records the final optimization pass over the project: a
complexity audit of every core operation, the one real bottleneck found
and fixed, and how it's verified.

## 1. Complexity audit

| Class | Operation | Complexity | Notes |
|---|---|---|---|
| `CityGraph` | `addLocation` | O(1) | Map insert. |
| | `removeLocation` | O(deg(v)) | Must scan and remove from each neighbor's adjacency list. |
| | `addRoad` / `removeRoad` | O(deg(v)) | Scans one endpoint's adjacency list for the duplicate/target edge. |
| | `hasRoad` | O(deg(v)) | Linear scan of one location's roads. |
| | `getNeighbors` | O(1) | Direct map lookup + unmodifiable wrap. |
| | `getAllEdges` | **O(1) amortized** (was O(E)) | See optimization below. |
| `MinPriorityQueue` | `offer` / `poll` | O(log n) | Standard binary heap sift up/down. |
| | `peek` / `isEmpty` / `size` | O(1) | |
| `DijkstraAlgorithm` | `findShortestPath` | O((V + E) log V) | Each location finalized once; each edge relaxed once; each relaxation is one O(log V) heap push. |
| `GraphLoader` | `loadFromFile` / `loadDefaultCity` | O(V + E) | Single pass over the file. |
| | `saveToFile` | O(V + E) | Now benefits from the `getAllEdges()` cache if called right after a search/render that already populated it. |
| `RouteService` | `findRoute` | O((V + E) log V) | Dominated by the Dijkstra call; validation is O(1). |

Space complexity throughout is **O(V + E)**: the adjacency list stores
each edge exactly twice (once per endpoint) but as a *shared* `Edge`
instance, not a duplicated object, so the memory cost is one `Edge`
allocation per road plus two list-slot references, not two full objects.

## 2. The optimization applied: caching `CityGraph.getAllEdges()`

**Before:** `getAllEdges()` rebuilt a fresh `LinkedHashSet<Edge>` by
scanning every adjacency list on *every single call* — an O(E)
operation. This method is called:

- Once per `MapPanel.paintComponent()` — i.e. on every repaint, which Swing triggers on window resize, zoom in/out, and every route highlight change.
- Once per `GraphLoader.saveToFile()`.
- Once per `roadCount()`.

For a UI-driven application, `getAllEdges()` was by far the
highest-frequency call against the whole codebase, yet the underlying
edge set rarely changes between those calls (typically: unchanged
across dozens of repaints between two actual road edits).

**After:** the result is cached in a `Set<Edge> cachedEdges` field,
computed lazily on first access and invalidated (set back to `null`)
by the four operations that can actually change the edge set:
`addRoad`, `removeRoad`, `removeLocation`, and `clear`. Every other
call — including every repaint — becomes an O(1) field read instead of
an O(E) rebuild.

This is a classic **memoization with explicit invalidation** pattern:
safe because every mutation path that affects edges is fully enumerated
and covered (verified by
`CityGraphTest.getAllEdges_neverReturnsStaleDataAfterMutations`, which
exercises add-road, add-then-remove-road, and cascading
location-removal, asserting the cache reflects each change correctly).

## 3. Verifying correctness at scale

`DijkstraAlgorithmScaleTest` runs the full algorithm against a
synthetic 25x25 grid graph (625 locations, ~1,200 roads — roughly 78x
larger than the bundled sample city) and asserts the result against an
independently-known ground truth (grid shortest paths are simply the
Manhattan distance, so the expected answer doesn't depend on Dijkstra
itself). This exercises `CityGraph`'s edge cache, `MinPriorityQueue`'s
heap operations, and `DijkstraAlgorithm`'s relaxation loop at a scale
well beyond manual tracing, while keeping the assertions grounded in
math rather than a snapshot of "whatever the algorithm currently
outputs."

## 4. Packaging

`pom.xml` already uses the `maven-shade-plugin` to produce a single,
dependency-bundled runnable JAR (`mvn package` → `target/smart-city-route-navigator.jar`),
avoiding classpath assembly at runtime and keeping distribution to a
single file.

## 5. What was deliberately *not* "optimized"

- `removeLocation`/`addRoad`/`removeRoad` remain O(deg(v)), not O(1),
  because doing better would require a secondary index (e.g. edge id →
  list positions) that adds real complexity for a graph of this
  project's expected size (tens of locations, not millions). This is a
  deliberate scope decision, not an oversight — see `docs/ALGORITHM.md`
  and `docs/PROJECT_STRUCTURE.md` for where the line was drawn.
- `MapPanel`'s per-repaint O(V + E) draw pass was left as-is: drawing
  every node and edge is inherent to rendering the whole map, not
  redundant work to eliminate.
