package co.edu.uniquindio.sameday.shipmentsuqsameday.util;

import java.util.*;

/**
 * Implementación del algoritmo de Dijkstra para calcular la ruta más corta.
 * Utiliza un grafo ponderado representando calles y coordenadas.
 * 
 * Patrón Strategy: implementación de algoritmo de búsqueda de caminos.
 * 
 * @author ShipmentsUQ Team
 * @version 1.0
 */
public class DijkstraRoutePlanner {
    
    /**
     * Representa un nodo en el grafo (coordenada/intersección)
     */
    public static class Node {
        private final int id;
        private final double x;
        private final double y;
        
        public Node(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
        
        public int getId() { return id; }
        public double getX() { return x; }
        public double getY() { return y; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node node = (Node) o;
            return id == node.id;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
    
    /**
     * Representa una arista (conexión entre nodos con peso/distancia)
     */
    public static class Edge {
        private final Node destination;
        private final double weight;
        
        public Edge(Node destination, double weight) {
            this.destination = destination;
            this.weight = weight;
        }
        
        public Node getDestination() { return destination; }
        public double getWeight() { return weight; }
    }
    
    /**
     * Representa el resultado de una ruta calculada
     */
    public static class RouteResult {
        private final List<Node> path;
        private final double totalDistance;
        
        public RouteResult(List<Node> path, double totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }
        
        public List<Node> getPath() { return path; }
        public double getTotalDistance() { return totalDistance; }
        public boolean isValid() { return path != null && !path.isEmpty(); }
    }
    
    private final Map<Node, List<Edge>> adjacencyList;
    
    public DijkstraRoutePlanner() {
        this.adjacencyList = new HashMap<>();
    }
    
    /**
     * Añade un nodo al grafo
     */
    public void addNode(Node node) {
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }
    
    /**
     * Añade una arista bidireccional entre dos nodos
     */
    public void addEdge(Node source, Node destination, double weight) {
        adjacencyList.putIfAbsent(source, new ArrayList<>());
        adjacencyList.putIfAbsent(destination, new ArrayList<>());
        
        adjacencyList.get(source).add(new Edge(destination, weight));
        adjacencyList.get(destination).add(new Edge(source, weight)); // Bidireccional
    }
    
    /**
     * Calcula la distancia euclidiana entre dos nodos
     */
    private double calculateDistance(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Encuentra el nodo más cercano a unas coordenadas dadas
     */
    public Node findNearestNode(double x, double y) {
        Node nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Node node : adjacencyList.keySet()) {
            double distance = calculateDistance(node, new Node(-1, x, y));
            if (distance < minDistance) {
                minDistance = distance;
                nearest = node;
            }
        }
        
        return nearest;
    }
    
    /**
     * Calcula la ruta más corta usando el algoritmo de Dijkstra
     * 
     * @param startX coordenada X de inicio
     * @param startY coordenada Y de inicio
     * @param endX coordenada X de destino
     * @param endY coordenada Y de destino
     * @return resultado con la ruta y distancia total
     */
    public RouteResult calculateShortestPath(double startX, double startY, double endX, double endY) {
        Node start = findNearestNode(startX, startY);
        Node end = findNearestNode(endX, endY);
        
        if (start == null || end == null) {
            return new RouteResult(Collections.emptyList(), Double.MAX_VALUE);
        }
        
        return dijkstra(start, end);
    }
    
    /**
     * Implementación del algoritmo de Dijkstra
     */
    private RouteResult dijkstra(Node start, Node end) {
        // Mapa de distancias mínimas desde el nodo de inicio
        Map<Node, Double> distances = new HashMap<>();
        // Mapa para reconstruir el camino
        Map<Node, Node> previous = new HashMap<>();
        // Cola de prioridad para procesar nodos por distancia
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        // Conjunto de nodos visitados
        Set<Node> visited = new HashSet<>();
        
        // Inicializar distancias
        for (Node node : adjacencyList.keySet()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        
        pq.offer(new NodeDistance(start, 0.0));
        
        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            Node currentNode = current.node;
            
            if (visited.contains(currentNode)) {
                continue;
            }
            
            visited.add(currentNode);
            
            // Si llegamos al destino, terminamos
            if (currentNode.equals(end)) {
                break;
            }
            
            // Explorar vecinos
            List<Edge> edges = adjacencyList.get(currentNode);
            if (edges == null) continue;
            
            for (Edge edge : edges) {
                Node neighbor = edge.getDestination();
                
                if (visited.contains(neighbor)) {
                    continue;
                }
                
                double newDistance = distances.get(currentNode) + edge.getWeight();
                
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, currentNode);
                    pq.offer(new NodeDistance(neighbor, newDistance));
                }
            }
        }
        
        // Reconstruir el camino
        List<Node> path = reconstructPath(previous, start, end);
        double totalDistance = distances.get(end);
        
        return new RouteResult(path, totalDistance);
    }
    
    /**
     * Reconstruye el camino desde el mapa de predecesores
     */
    private List<Node> reconstructPath(Map<Node, Node> previous, Node start, Node end) {
        List<Node> path = new ArrayList<>();
        Node current = end;
        
        while (current != null) {
            path.add(0, current);
            current = previous.get(current);
            
            // Evitar bucles infinitos
            if (path.size() > adjacencyList.size()) {
                return Collections.emptyList();
            }
        }
        
        // Verificar que el camino comienza en start
        if (path.isEmpty() || !path.get(0).equals(start)) {
            return Collections.emptyList();
        }
        
        return path;
    }
    
    /**
     * Clase auxiliar para la cola de prioridad
     */
    private static class NodeDistance {
        Node node;
        double distance;
        
        NodeDistance(Node node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }
    
    /**
     * Crea un grafo de ejemplo para Armenia, Colombia (grid simplificado)
     * En producción, esto debería cargarse desde una base de datos o archivo GeoJSON
     */
    public static DijkstraRoutePlanner createDefaultGrid() {
        DijkstraRoutePlanner planner = new DijkstraRoutePlanner();
        
        // Crear un grid 10x10 de nodos (simula calles)
        int gridSize = 10;
        Node[][] grid = new Node[gridSize][gridSize];
        
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double x = i * 10.0; // Escala: 10 unidades por cuadra
                double y = j * 10.0;
                Node node = new Node(i * gridSize + j, x, y);
                grid[i][j] = node;
                planner.addNode(node);
            }
        }
        
        // Conectar nodos adyacentes (horizontal y vertical)
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                // Conexión horizontal
                if (i < gridSize - 1) {
                    planner.addEdge(grid[i][j], grid[i + 1][j], 10.0);
                }
                // Conexión vertical
                if (j < gridSize - 1) {
                    planner.addEdge(grid[i][j], grid[i][j + 1], 10.0);
                }
                // Conexión diagonal (opcional, para rutas más flexibles)
                if (i < gridSize - 1 && j < gridSize - 1) {
                    planner.addEdge(grid[i][j], grid[i + 1][j + 1], 14.14); // sqrt(10^2 + 10^2)
                }
            }
        }
        
        return planner;
    }
}
