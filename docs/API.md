# API Overview

The application exposes its primary functionality through the service layer:

- RouteService.findRoute(sourceId, destinationId)
- RouteService.addLocation(id, name, x, y)
- RouteService.addRoad(sourceId, destinationId, weight)
- RouteService.resetToDefaultCity()
- RouteService.loadGraphFromFile(file)
- RouteService.saveGraphToFile(file)

The graph and algorithm packages remain the core engine and can be reused independently of the Swing UI.
