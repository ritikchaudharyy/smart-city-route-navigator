# Algorithm Explanation — Dijkstra's Shortest Path

This document explains how `DijkstraAlgorithm` (in
`com.smartcity.navigator.algorithm`) computes the shortest route between
two locations, and walks through a real, hand-verified trace using the
project's bundled default city data.

## 1. Why Dijkstra's Algorithm

The city road network is modeled as an **undirected, weighted graph**:
locations are vertices, roads are edges, and every edge weight is a
positive distance (kilometers). Dijkstra's algorithm is the standard
choice for single-source shortest paths on graphs with **non-negative**
edge weights, which is guaranteed here — `Edge`'s constructor rejects
any non-positive weight, so the algorithm's core greedy assumption is
enforced by the data model itself, not just by convention.

## 2. Data structures used

| Concept (spec requirement) | Concrete type in code | Why |
|---|---|---|
| Distance Array | `Map<String, Double> distances` | Location ids are Strings, not contiguous integer indices, so a hash map plays the role of an array while keeping O(1) average access. |
| Visited Array | `Set<String> finalized` | Tracks which locations' shortest distance is already guaranteed correct. |
| Parent Array | `Map<String, String> parents` | Records how each location was reached, enabling path reconstruction. |
| Priority Queue | `MinPriorityQueue<Node>` | A hand-built binary min-heap (see `MinPriorityQueue.java`) rather than `java.util.PriorityQueue`, so the heap mechanics are fully inspectable. |

`Node` (in `model`) is a minimal `(locationId, distance)` pair used only
by the heap — kept separate from `Location` so the algorithm never
mutates domain objects while searching.

## 3. Algorithm steps (matching the code, method by method)

1. **Guard clauses** — unknown source/destination → immediate failure. Identical source and destination → trivial success with distance `0`.
2. **`initializeDistances`** — every location starts at `+∞` except the source, which starts at `0`.
3. **Main loop** — repeatedly:
   a. Pop the location with the smallest tentative distance from the heap.
   b. If it's already finalized, it's a stale duplicate left over from an earlier relaxation — skip it (**lazy deletion**, see below).
   c. Otherwise, finalize it. If it's the destination, stop immediately — its distance can never improve further.
   d. Otherwise, call **`relaxNeighbors`**: for every road out of this location, check whether reaching the neighbor through here is cheaper than the neighbor's current best distance; if so, update its distance and parent, and push a fresh heap entry.
4. **Failure check** — if the destination's distance is still `+∞` after the loop, the two locations are disconnected.
5. **`reconstructPath`** — walk `parents` backward from destination to source, inserting each location at the front of a list so it ends up ordered source → destination without a separate reverse pass.

### Why "lazy deletion" instead of decrease-key

A textbook decrease-key operation would update an existing heap entry
in place. `MinPriorityQueue` doesn't support that (nor does
`java.util.PriorityQueue`), so instead, every time a shorter distance is
found, a **new** entry is pushed rather than updating the old one. When
an entry is popped, the algorithm checks whether that location is
already finalized; if so, it's a stale leftover from a distance that's
since been improved upon, and it's simply discarded. This is standard
practice for heap-based Dijkstra and does not change correctness or the
`O((V + E) log V)` complexity, since each edge still triggers at most
one useful push.

## 4. Complexity

**O((V + E) log V)**, where V = number of locations, E = number of roads:

- Each location is popped from the heap and finalized at most once: `O(V log V)`.
- Each road is relaxed at most once, and each successful relaxation costs `O(log V)` to push: `O(E log V)`.

## 5. Worked example: the bundled default city

Using `resources/data/default-city.dat` (8 locations, 12 roads),
finding the shortest route from **Home (L1)** to **Mall (L4)**. This is
the exact spec example (`Home → Market → Hospital → Mall`, 14 km),
traced step-by-step through the real algorithm mechanics, including one
genuine priority-queue tie:

| Step | Popped (id, dist) | Action | Distance updates |
|---|---|---|---|
| 1 | (L1, 0) | Finalize L1 | L2=5 (via L1), L5=3 (via L1), L6=12 (via L1) |
| 2 | (L5, 3) | Finalize L5 | L6 improved 12→9 (via L5), L3=12 (via L5) |
| 3 | (L2, 5) | Finalize L2 | L3 improved 12→9 (via L2) |
| 4 | (L6, 9) | Finalize L6 | L4=19 (via L6) |
| 5 | (L3, 9) | Finalize L3 (tie with a stale L3 entry, correctly resolved by lazy deletion) | L4 improved 19→14 (via L3), L7=16 (via L3) |
| 6 | (L3, 12) — stale | **Skipped**: L3 already finalized | — |
| 7 | (L4, 14) | Finalize L4 — **this is the destination, so the search stops here** | — |

Final distance to Mall: **14 km**. Walking `parents` backward:
`L4 → L3 → L2 → L1`, reversed to `L1 → L2 → L3 → L4`, i.e.
**Home → Market → Hospital → Mall**, exactly matching the spec's
example — and this project ships a regression test
(`DijkstraAlgorithmTest.findShortestPath_defaultCity_matchesSpecExample`)
that fails loudly if the bundled dataset or algorithm ever drift from
this result.

Note step 6: a stale queue entry for L3 (pushed at distance 12 back in
step 2, before its distance was improved to 9 in step 3) gets popped
later and is correctly discarded because L3 was already finalized —
this is lazy deletion working exactly as intended, with zero impact on
the final answer.
