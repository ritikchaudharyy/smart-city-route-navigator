# Flowcharts

Five flowcharts as required by the project spec. `UML_DIAGRAMS.md`
already covers the end-to-end user workflow as an activity diagram;
these flowcharts each zoom into one specific process in more technical
detail.

## 1. Application Flow (lifecycle, launch to shutdown)

```mermaid
flowchart TD
    A([main invoked]) --> B[SwingUtilities.invokeLater]
    B --> C[SplashScreen shown]
    C --> D["new RouteService() - loads default-city.dat"]
    D --> E{Load succeeded?}
    E -->|No| F[Show startup error dialog]
    F --> G([System.exit 1])
    E -->|Yes| H[SplashScreen disposed]
    H --> I[MainFrame constructed & shown]
    I --> J[User interacts: menus, toolbar, panels]
    J --> K{Exit requested?}
    K -->|No| J
    K -->|Yes| L[Confirm exit dialog]
    L --> M{Confirmed?}
    M -->|No| J
    M -->|Yes| N([dispose + System.exit 0])
```

## 2. Graph Creation (GraphLoader parsing a data file)

```mermaid
flowchart TD
    A([loadFromFile / loadDefaultCity called]) --> B[Open file or bundled resource stream]
    B --> C{Stream opened OK?}
    C -->|No| D([throw GraphLoadException])
    C -->|Yes| E[Read next line]
    E --> F{End of file?}
    F -->|Yes| G{Graph has at least one location?}
    G -->|No| D
    G -->|Yes| H([return completed CityGraph])
    F -->|No| I{Blank or comment line?}
    I -->|Yes| E
    I -->|No| J{Directive type?}
    J -->|LOCATION| K["Parse id, name, x, y -> graph.addLocation(...)"]
    J -->|ROAD| L{Both endpoint locations already declared?}
    L -->|No| D
    L -->|Yes| M["Parse sourceId, destId, weight -> graph.addRoad(...)"]
    J -->|Unknown directive| D
    K --> E
    M --> E
```

## 3. Route Search (RouteService.findRoute)

```mermaid
flowchart TD
    A([findRoute sourceId, destinationId]) --> B["Validators.validateRouteSelection(...)"]
    B --> C{Validation error?}
    C -->|Yes| D(["return PathResult.failure(message)"])
    C -->|No| E["new DijkstraAlgorithm(graph)"]
    E --> F["algorithm.findShortestPath(sourceId, destinationId)"]
    F --> G{Path found?}
    G -->|No| H(["return PathResult.failure('disconnected')"])
    G -->|Yes| I(["return PathResult.success(route, distance)"])
    D --> J[MainFrame displays result via ResultPanel + JOptionPane]
    H --> J
    I --> K[MainFrame displays result via ResultPanel + MapPanel highlight]
```

## 4. Dijkstra's Algorithm (detailed)

```mermaid
flowchart TD
    A([findShortestPath sourceId, destId]) --> B{Source/dest exist in graph?}
    B -->|No| C(["return PathResult.failure"])
    B -->|Yes| D{source equals destination?}
    D -->|Yes| E(["return PathResult.success single-node, distance 0"])
    D -->|No| F["Initialize: distances all = infinity except source = 0"]
    F --> G["priorityQueue.offer(Node(source, 0))"]
    G --> H{Queue empty?}
    H -->|Yes| I{destination distance still infinity?}
    I -->|Yes| J(["return PathResult.failure - disconnected"])
    I -->|No| K["reconstructPath via parents map"]
    K --> L(["return PathResult.success(route, distance)"])
    H -->|No| M["current = priorityQueue.poll()"]
    M --> N{current already finalized?}
    N -->|Yes, stale entry| H
    N -->|No| O["mark current as finalized"]
    O --> P{current equals destination?}
    P -->|Yes| K
    P -->|No| Q["for each road out of current"]
    Q --> R{neighbor already finalized?}
    R -->|Yes| Q
    R -->|No| S{"candidate = dist(current) + weight < dist(neighbor)?"}
    S -->|No| Q
    S -->|Yes| T["update distance & parent, priorityQueue.offer(Node(neighbor, candidate))"]
    T --> Q
    Q -->|all roads processed| H
```

## 5. GUI Workflow (event dispatch across MainFrame)

```mermaid
flowchart TD
    A([MainFrame idle, awaiting input]) --> B{Which control did the user use?}

    B -->|"Find Route (toolbar or panel)"| C[onFindRoute]
    C --> D["routeService.findRoute(...)"]
    D --> E["resultPanel.displayResult + mapPanel.highlightRoute or clearHighlight"]
    E --> A

    B -->|Clear toolbar / Reset panel button| F[onClear]
    F --> G["Clear selections, result panel, map highlight, status bar"]
    G --> A

    B -->|Refresh toolbar button| H[onRefresh]
    H --> I["Repopulate dropdowns and map from current graph"]
    I --> A

    B -->|File > New City| J[onNewCity]
    J --> K[Confirm dialog]
    K -->|Yes| L["routeService.resetToDefaultCity() -> refresh + clear"]
    K -->|No| A
    L --> A

    B -->|File > Load Graph| M[onLoadGraph]
    M --> N[JFileChooser open dialog]
    N -->|File chosen| O["routeService.loadGraphFromFile(file) -> refresh + clear"]
    N -->|Cancelled| A
    O --> A

    B -->|File > Save Graph| P[onSaveGraph]
    P --> Q[JFileChooser save dialog]
    Q -->|File chosen| R["routeService.saveGraphToFile(file)"]
    Q -->|Cancelled| A
    R --> A

    B -->|View > Zoom In/Out/Reset| S["mapPanel.zoomIn / zoomOut / resetZoom"]
    S --> A

    B -->|View > Settings| T[onSettings]
    T --> U[SettingsDialog shown modally]
    U -->|OK| V["AppSettings updated -> mapPanel.repaint()"]
    U -->|Cancel| A
    V --> A

    B -->|Help > About| W["AboutDialog shown modally"]
    W --> A

    B -->|Exit / window close| X[confirmExit]
    X --> Y[Confirm dialog]
    Y -->|Yes| Z([dispose + System.exit])
    Y -->|No| A
```
