# UML Diagrams

All diagrams below are written in [Mermaid](https://mermaid.js.org/),
which GitHub renders natively in Markdown — no external image files or
diagramming tool required, and they stay easy to update alongside the code.

## 1. Use Case Diagram

```mermaid
graph LR
    User((User))

    UC1[Select Source & Destination]
    UC2[Find Shortest Route]
    UC3[View Route on Map]
    UC4[Zoom Map In / Out / Reset]
    UC5[Load Graph from File]
    UC6[Save Graph to File]
    UC7[Reset to Default City]
    UC8[Add Location]
    UC9[Add Road]
    UC10[Adjust Settings]
    UC11[View About Info]

    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC8
    User --> UC9
    User --> UC10
    User --> UC11

    UC2 -.includes.-> UC1
    UC2 -.includes.-> UC3
```

## 2. Class Diagram (core classes)

The full project has 24 classes across six packages (see
`PROJECT_STRUCTURE.md` for the complete list); this diagram shows the
core pathfinding path to keep it readable.

```mermaid
classDiagram
    class Location {
        -String id
        -String name
        -double x
        -double y
        +getId() String
        +getName() String
    }

    class Edge {
        -String sourceId
        -String destinationId
        -double weight
        +getNeighbor(fromId) String
        +connects(a, b) boolean
    }

    class Node {
        -String locationId
        -double distance
        +compareTo(Node) int
    }

    class PathResult {
        -List~Location~ route
        -double totalDistance
        -boolean pathFound
        -String message
        +success(route, distance) PathResult
        +failure(message) PathResult
        +getFormattedRoute() String
    }

    class CityGraph {
        -Map~String,Location~ locations
        -Map~String,List~Edge~~ adjacencyList
        +addLocation(Location)
        +addRoad(sourceId, destId, weight)
        +removeLocation(id) boolean
        +removeRoad(sourceId, destId) boolean
        +getNeighbors(id) List~Edge~
        +getAllEdges() Set~Edge~
    }

    class GraphBuilder {
        +withLocation(...) GraphBuilder
        +withRoad(...) GraphBuilder
        +build() CityGraph
    }

    class GraphLoader {
        +loadDefaultCity() CityGraph
        +loadFromFile(File) CityGraph
        +saveToFile(CityGraph, File)
    }

    class MinPriorityQueue~T~ {
        -List~T~ heap
        +offer(T)
        +poll() T
        +peek() T
    }

    class DijkstraAlgorithm {
        -CityGraph graph
        +findShortestPath(sourceId, destId) PathResult
        -relaxNeighbors(...)
        -reconstructPath(...) List~Location~
    }

    class RouteService {
        -CityGraph graph
        +findRoute(sourceId, destId) PathResult
        +loadGraphFromFile(File)
        +saveGraphToFile(File)
        +addLocation(...)
        +addRoad(...)
    }

    class MainFrame {
        -RouteService routeService
        -RoutePanel routePanel
        -ResultPanel resultPanel
        -MapPanel mapPanel
        -onFindRoute()
    }

    CityGraph "1" o-- "many" Location
    CityGraph "1" o-- "many" Edge
    GraphBuilder ..> CityGraph : builds
    GraphLoader ..> CityGraph : builds
    DijkstraAlgorithm --> CityGraph : queries
    DijkstraAlgorithm --> MinPriorityQueue : uses
    DijkstraAlgorithm --> Node : creates
    DijkstraAlgorithm --> PathResult : returns
    RouteService --> DijkstraAlgorithm : delegates to
    RouteService --> CityGraph : owns
    RouteService --> GraphLoader : uses
    MainFrame --> RouteService : calls
    PathResult --> Location : contains
    Edge --> Location : references by id
```

## 3. Sequence Diagram — "Find Route" user action

```mermaid
sequenceDiagram
    actor User
    participant RoutePanel
    participant MainFrame
    participant RouteService
    participant Validators
    participant DijkstraAlgorithm
    participant MinPriorityQueue
    participant CityGraph
    participant ResultPanel
    participant MapPanel

    User->>RoutePanel: select source & destination, click "Find Route"
    RoutePanel->>MainFrame: onFindRoute() callback
    MainFrame->>RouteService: findRoute(sourceId, destId)
    RouteService->>Validators: validateRouteSelection(...)
    Validators-->>RouteService: Optional<String> (empty = valid)

    alt validation failed
        RouteService-->>MainFrame: PathResult.failure(message)
    else validation passed
        RouteService->>DijkstraAlgorithm: findShortestPath(sourceId, destId)
        loop until destination finalized
            DijkstraAlgorithm->>MinPriorityQueue: poll()
            DijkstraAlgorithm->>CityGraph: getNeighbors(currentId)
            DijkstraAlgorithm->>MinPriorityQueue: offer(Node) for improved distances
        end
        DijkstraAlgorithm-->>RouteService: PathResult.success(route, distance)
    end

    RouteService-->>MainFrame: PathResult
    MainFrame->>ResultPanel: displayResult(result)
    MainFrame->>MapPanel: highlightRoute(result.getRoute())
    MapPanel-->>User: repainted map with highlighted route
```

## 4. Activity Diagram — application workflow

Mirrors the workflow defined in the project spec, from launch through
displaying a result.

```mermaid
flowchart TD
    Start([Application Starts]) --> Splash[Splash Screen]
    Splash --> MainWindow[Main Window Opens]
    MainWindow --> GraphLoads[Graph Loads - default city]
    GraphLoads --> SelectSource[User Selects Source]
    SelectSource --> SelectDest[User Selects Destination]
    SelectDest --> ClickFind[Click Find Route]
    ClickFind --> Validate{Inputs Valid?}
    Validate -->|No| ShowError[Show Validation Dialog]
    ShowError --> SelectSource
    Validate -->|Yes| RunDijkstra[Run Dijkstra's Algorithm]
    RunDijkstra --> PathExists{Path Exists?}
    PathExists -->|No| ShowNoPath[Show 'No Route Found' Dialog]
    ShowNoPath --> SelectSource
    PathExists -->|Yes| GenPath[Generate Shortest Path]
    GenPath --> GenDistance[Generate Total Distance]
    GenDistance --> DisplayRoute[Display Route in Result Panel]
    DisplayRoute --> DisplayDistance[Display Total Distance]
    DisplayDistance --> HighlightMap[Highlight Route on Map]
    HighlightMap --> StatusUpdate[Status Bar Updated]
    StatusUpdate --> Idle([Ready for next action])
```

## 5. Component Diagram — package dependencies

```mermaid
flowchart TB
    subgraph UI["ui package"]
        MainFrame
        RoutePanel
        ResultPanel
        MapPanel
        Dialogs["AboutDialog / SettingsDialog / SplashScreen"]
    end

    subgraph Service["service package"]
        RouteService
    end

    subgraph Algorithm["algorithm package"]
        DijkstraAlgorithm
        MinPriorityQueue
    end

    subgraph Graph["graph package"]
        CityGraph
        GraphBuilder
        GraphLoader
    end

    subgraph Model["model package"]
        Location
        Edge
        Node
        PathResult
    end

    subgraph Utils["utils package"]
        Constants
        Validators
        Formatter
        Helpers
        AppSettings
    end

    UI --> Service
    UI --> Utils
    Service --> Algorithm
    Service --> Graph
    Service --> Utils
    Algorithm --> Graph
    Algorithm --> Model
    Graph --> Model
```
