// ========================
// CONFIGURACIÓN Y ESTADO
// ========================

// Centro de operaciones - Armenia, Quindío
const ARMENIA_CENTER = [4.533889, -75.681111];
const SERVICE_RADIUS_KM = 20;

// Estado global
let map;
let delivererMarker = null;
let currentDeliverer = null;
let selectedShipment = null;
let routeLine = null;
let originMarker = null;
let destinationMarker = null;

// Simulación de datos del repartidor (esto se puede cargar desde JavaFX)
let mockDeliverer = {
    id: '1',
    name: 'Juan Pérez',
    currentLat: 4.533889,
    currentLng: -75.681111,
    status: 'ACTIVO',
    totalDeliveries: 45,
    averageRating: 4.7,
    currentShipments: [
        {
            id: '001',
            status: 'PENDIENTE',
            user: { name: 'María González' },
            origin: { lat: 4.540000, lng: -75.670000, city: 'Armenia', address: 'Calle 15 #20-30' },
            destination: { lat: 4.520000, lng: -75.690000, city: 'Armenia', address: 'Carrera 14 #10-20' }
        },
        {
            id: '002',
            status: 'EN_TRANSITO',
            user: { name: 'Carlos Rodríguez' },
            origin: { lat: 4.545000, lng: -75.665000, city: 'Armenia', address: 'Avenida Bolívar #25-40' },
            destination: { lat: 4.525000, lng: -75.685000, city: 'Armenia', address: 'Calle 20 #15-10' }
        }
    ]
};

// ========================
// INICIALIZACIÓN
// ========================

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚴 Inicializando Dashboard del Repartidor...');
    
    // Verificar si hay datos desde JavaFX
    if (typeof window.delivererData !== 'undefined') {
        currentDeliverer = window.delivererData;
        console.log('✅ Datos cargados desde JavaFX:', currentDeliverer);
    } else {
        currentDeliverer = mockDeliverer;
        console.log('⚠️ Usando datos simulados');
    }
    
    initMap();
    loadDelivererData();
    loadShipments();
});

// ========================
// MAPA
// ========================

function initMap() {
    // Crear mapa centrado en la ubicación del repartidor
    const centerLat = currentDeliverer.currentLat || ARMENIA_CENTER[0];
    const centerLng = currentDeliverer.currentLng || ARMENIA_CENTER[1];
    
    map = L.map('map').setView([centerLat, centerLng], 14);

    // Añadir tiles de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);

    // Dibujar círculo de área de cobertura
    L.circle(ARMENIA_CENTER, {
        color: '#f59e0b',
        fillColor: '#fef3c7',
        fillOpacity: 0.1,
        radius: SERVICE_RADIUS_KM * 1000,
        weight: 2,
        dashArray: '10, 10'
    }).addTo(map);

    // Marcador del repartidor
    delivererMarker = L.marker([centerLat, centerLng], {
        icon: L.divIcon({
            className: 'deliverer-marker',
            html: `<div style="background: #3b82f6; color: white; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4); border: 3px solid white;">🚴</div>`,
            iconSize: [40, 40],
            iconAnchor: [20, 40]
        })
    }).addTo(map).bindPopup(`<strong>${currentDeliverer.name}</strong><br>Tu ubicación actual`);

    console.log('✅ Mapa inicializado');
}

// ========================
// CARGAR DATOS
// ========================

function loadDelivererData() {
    // Actualizar header
    document.getElementById('deliverer-name').textContent = `🚴 ${currentDeliverer.name}`;
    document.getElementById('deliverer-status').textContent = `Estado: ${currentDeliverer.status}`;
    
    // Actualizar métricas
    document.getElementById('active-shipments').textContent = currentDeliverer.currentShipments.length;
    document.getElementById('total-deliveries').textContent = currentDeliverer.totalDeliveries;
    document.getElementById('average-rating').textContent = currentDeliverer.averageRating.toFixed(1);
    document.getElementById('deliverer-status-metric').textContent = currentDeliverer.status;
    
    console.log('✅ Datos del repartidor cargados');
}

function loadShipments() {
    const shipmentsList = document.getElementById('shipments-list');
    
    if (!currentDeliverer.currentShipments || currentDeliverer.currentShipments.length === 0) {
        shipmentsList.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📭</div>
                <p>No hay envíos activos</p>
            </div>
        `;
        return;
    }
    
    shipmentsList.innerHTML = '';
    
    currentDeliverer.currentShipments.forEach(shipment => {
        const card = createShipmentCard(shipment);
        shipmentsList.appendChild(card);
    });
    
    console.log(`✅ ${currentDeliverer.currentShipments.length} envíos cargados`);
}

function createShipmentCard(shipment) {
    const card = document.createElement('div');
    card.className = 'shipment-card';
    card.onclick = () => selectShipment(shipment);
    
    const statusClass = shipment.status === 'PENDIENTE' ? 'status-pending' : 'status-in-transit';
    const statusText = shipment.status === 'PENDIENTE' ? 'Pendiente' : 'En Tránsito';
    
    card.innerHTML = `
        <div class="shipment-header">
            <span class="shipment-id">#${shipment.id}</span>
            <span class="shipment-status ${statusClass}">${statusText}</span>
        </div>
        <div class="shipment-info">
            <strong>Cliente:</strong> ${shipment.user.name}<br>
            <strong>Origen:</strong> ${shipment.origin.address || shipment.origin.city}<br>
            <strong>Destino:</strong> ${shipment.destination.address || shipment.destination.city}
        </div>
        <div class="shipment-actions">
            <button class="btn btn-route" onclick="event.stopPropagation(); calculateRoute('${shipment.id}')">
                🗺️ Calcular Ruta
            </button>
            <button class="btn btn-complete" onclick="event.stopPropagation(); completeShipment('${shipment.id}')">
                ✅ Completar
            </button>
        </div>
    `;
    
    return card;
}

// ========================
// SELECCIÓN DE ENVÍO
// ========================

function selectShipment(shipment) {
    // Deseleccionar anterior
    document.querySelectorAll('.shipment-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Seleccionar nuevo
    event.currentTarget.classList.add('selected');
    selectedShipment = shipment;
    
    // Mostrar marcadores en el mapa
    showShipmentOnMap(shipment);
    
    console.log('✅ Envío seleccionado:', shipment.id);
}

function showShipmentOnMap(shipment) {
    // Limpiar marcadores anteriores
    clearRouteMarkers();
    
    // Marcador de origen (verde)
    originMarker = L.marker([shipment.origin.lat, shipment.origin.lng], {
        icon: L.divIcon({
            className: 'origin-marker',
            html: `<div style="background: #10b981; color: white; width: 35px; height: 35px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4); border: 3px solid white;">📍</div>`,
            iconSize: [35, 35],
            iconAnchor: [17.5, 35]
        })
    }).addTo(map).bindPopup(`<strong>Origen</strong><br>${shipment.origin.address || shipment.origin.city}`);
    
    // Marcador de destino (rojo)
    destinationMarker = L.marker([shipment.destination.lat, shipment.destination.lng], {
        icon: L.divIcon({
            className: 'destination-marker',
            html: `<div style="background: #ef4444; color: white; width: 35px; height: 35px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4); border: 3px solid white;">🎯</div>`,
            iconSize: [35, 35],
            iconAnchor: [17.5, 35]
        })
    }).addTo(map).bindPopup(`<strong>Destino</strong><br>${shipment.destination.address || shipment.destination.city}`);
    
    // Ajustar vista para mostrar todos los marcadores
    const bounds = L.latLngBounds([
        [currentDeliverer.currentLat, currentDeliverer.currentLng],
        [shipment.origin.lat, shipment.origin.lng],
        [shipment.destination.lat, shipment.destination.lng]
    ]);
    map.fitBounds(bounds, { padding: [50, 50] });
}

function clearRouteMarkers() {
    if (originMarker) {
        map.removeLayer(originMarker);
        originMarker = null;
    }
    if (destinationMarker) {
        map.removeLayer(destinationMarker);
        destinationMarker = null;
    }
    if (routeLine) {
        map.removeLayer(routeLine);
        routeLine = null;
    }
    
    // Ocultar panel de ruta
    document.getElementById('route-info-panel').classList.remove('visible');
}

// ========================
// ALGORITMO DE DIJKSTRA
// ========================

class Graph {
    constructor() {
        this.nodes = new Map();
        this.edges = new Map();
    }
    
    addNode(id, lat, lng) {
        this.nodes.set(id, { id, lat, lng });
    }
    
    addEdge(from, to, weight) {
        if (!this.edges.has(from)) {
            this.edges.set(from, []);
        }
        this.edges.get(from).push({ to, weight });
    }
    
    dijkstra(startId, endId) {
        const distances = new Map();
        const previous = new Map();
        const unvisited = new Set();
        
        // Inicializar distancias
        for (let nodeId of this.nodes.keys()) {
            distances.set(nodeId, Infinity);
            previous.set(nodeId, null);
            unvisited.add(nodeId);
        }
        distances.set(startId, 0);
        
        while (unvisited.size > 0) {
            // Encontrar nodo no visitado con menor distancia
            let currentId = null;
            let minDistance = Infinity;
            for (let nodeId of unvisited) {
                if (distances.get(nodeId) < minDistance) {
                    minDistance = distances.get(nodeId);
                    currentId = nodeId;
                }
            }
            
            if (currentId === null || currentId === endId) break;
            
            unvisited.delete(currentId);
            
            // Actualizar distancias de vecinos
            const neighbors = this.edges.get(currentId) || [];
            for (let edge of neighbors) {
                if (!unvisited.has(edge.to)) continue;
                
                const alt = distances.get(currentId) + edge.weight;
                if (alt < distances.get(edge.to)) {
                    distances.set(edge.to, alt);
                    previous.set(edge.to, currentId);
                }
            }
        }
        
        // Reconstruir ruta
        const path = [];
        let current = endId;
        while (current !== null) {
            const node = this.nodes.get(current);
            path.unshift([node.lat, node.lng]);
            current = previous.get(current);
        }
        
        return {
            path: path,
            distance: distances.get(endId),
            valid: distances.get(endId) !== Infinity
        };
    }
}

function createCityGraph() {
    const graph = new Graph();
    
    // Nodos representando puntos clave de Armenia
    // Centro
    graph.addNode('center', 4.533889, -75.681111);
    
    // Norte
    graph.addNode('north1', 4.545000, -75.680000);
    graph.addNode('north2', 4.550000, -75.675000);
    graph.addNode('north3', 4.555000, -75.685000);
    
    // Sur
    graph.addNode('south1', 4.525000, -75.680000);
    graph.addNode('south2', 4.520000, -75.690000);
    graph.addNode('south3', 4.515000, -75.670000);
    
    // Este
    graph.addNode('east1', 4.535000, -75.665000);
    graph.addNode('east2', 4.540000, -75.655000);
    graph.addNode('east3', 4.530000, -75.650000);
    
    // Oeste
    graph.addNode('west1', 4.535000, -75.695000);
    graph.addNode('west2', 4.540000, -75.705000);
    graph.addNode('west3', 4.530000, -75.710000);
    
    // Conectar nodos con pesos (distancia aproximada en km)
    // Desde centro
    graph.addEdge('center', 'north1', 1.2);
    graph.addEdge('center', 'south1', 1.0);
    graph.addEdge('center', 'east1', 1.5);
    graph.addEdge('center', 'west1', 1.3);
    
    // Conexiones norte
    graph.addEdge('north1', 'center', 1.2);
    graph.addEdge('north1', 'north2', 0.8);
    graph.addEdge('north1', 'north3', 0.9);
    graph.addEdge('north2', 'north1', 0.8);
    graph.addEdge('north2', 'north3', 1.0);
    graph.addEdge('north3', 'north1', 0.9);
    graph.addEdge('north3', 'north2', 1.0);
    
    // Conexiones sur
    graph.addEdge('south1', 'center', 1.0);
    graph.addEdge('south1', 'south2', 1.1);
    graph.addEdge('south1', 'south3', 1.2);
    graph.addEdge('south2', 'south1', 1.1);
    graph.addEdge('south2', 'south3', 1.5);
    graph.addEdge('south3', 'south1', 1.2);
    graph.addEdge('south3', 'south2', 1.5);
    
    // Conexiones este
    graph.addEdge('east1', 'center', 1.5);
    graph.addEdge('east1', 'east2', 1.3);
    graph.addEdge('east1', 'east3', 1.1);
    graph.addEdge('east2', 'east1', 1.3);
    graph.addEdge('east2', 'east3', 0.9);
    graph.addEdge('east3', 'east1', 1.1);
    graph.addEdge('east3', 'east2', 0.9);
    
    // Conexiones oeste
    graph.addEdge('west1', 'center', 1.3);
    graph.addEdge('west1', 'west2', 1.2);
    graph.addEdge('west1', 'west3', 1.4);
    graph.addEdge('west2', 'west1', 1.2);
    graph.addEdge('west2', 'west3', 0.8);
    graph.addEdge('west3', 'west1', 1.4);
    graph.addEdge('west3', 'west2', 0.8);
    
    // Conexiones diagonales
    graph.addEdge('north1', 'east1', 1.8);
    graph.addEdge('east1', 'north1', 1.8);
    graph.addEdge('north1', 'west1', 1.7);
    graph.addEdge('west1', 'north1', 1.7);
    graph.addEdge('south1', 'east1', 1.6);
    graph.addEdge('east1', 'south1', 1.6);
    graph.addEdge('south1', 'west1', 1.9);
    graph.addEdge('west1', 'south1', 1.9);
    
    return graph;
}

function findNearestNode(graph, lat, lng) {
    let nearestId = null;
    let minDistance = Infinity;
    
    for (let [id, node] of graph.nodes.entries()) {
        const distance = calculateDistance(lat, lng, node.lat, node.lng);
        if (distance < minDistance) {
            minDistance = distance;
            nearestId = id;
        }
    }
    
    return nearestId;
}

function calculateDistance(lat1, lng1, lat2, lng2) {
    const R = 6371; // Radio de la Tierra en km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

// ========================
// CALCULAR RUTA
// ========================

function calculateRoute(shipmentId) {
    const shipment = currentDeliverer.currentShipments.find(s => s.id === shipmentId);
    if (!shipment) {
        alert('Envío no encontrado');
        return;
    }
    
    console.log('🗺️ Calculando ruta para envío:', shipmentId);
    
    // Crear grafo de la ciudad
    const graph = createCityGraph();
    
    // Encontrar nodos más cercanos
    const startNode = findNearestNode(graph, currentDeliverer.currentLat, currentDeliverer.currentLng);
    const endNode = findNearestNode(graph, shipment.origin.lat, shipment.origin.lng);
    
    console.log('Nodo inicial:', startNode, 'Nodo final:', endNode);
    
    // Calcular ruta con Dijkstra
    const result = graph.dijkstra(startNode, endNode);
    
    if (!result.valid) {
        alert('No se pudo calcular la ruta');
        return;
    }
    
    console.log('✅ Ruta calculada. Distancia:', result.distance.toFixed(2), 'km');
    
    // Añadir puntos inicial y final exactos
    const fullPath = [
        [currentDeliverer.currentLat, currentDeliverer.currentLng],
        ...result.path,
        [shipment.origin.lat, shipment.origin.lng]
    ];
    
    // Dibujar ruta en el mapa
    drawRoute(fullPath, result.distance, shipment);
    
    // Mostrar envío en el mapa
    if (!selectedShipment || selectedShipment.id !== shipment.id) {
        selectShipmentById(shipmentId);
    }
}

function selectShipmentById(shipmentId) {
    const shipment = currentDeliverer.currentShipments.find(s => s.id === shipmentId);
    if (shipment) {
        const cards = document.querySelectorAll('.shipment-card');
        cards.forEach((card, index) => {
            if (currentDeliverer.currentShipments[index].id === shipmentId) {
                card.classList.add('selected');
            } else {
                card.classList.remove('selected');
            }
        });
        selectedShipment = shipment;
        showShipmentOnMap(shipment);
    }
}

function drawRoute(path, distance, shipment) {
    // Limpiar ruta anterior
    if (routeLine) {
        map.removeLayer(routeLine);
    }
    
    // Dibujar nueva ruta
    routeLine = L.polyline(path, {
        color: '#f59e0b',
        weight: 4,
        opacity: 0.8,
        smoothFactor: 1
    }).addTo(map);
    
    // Ajustar vista
    map.fitBounds(routeLine.getBounds(), { padding: [50, 50] });
    
    // Actualizar panel de información
    const estimatedTime = (distance / 30) * 60; // Asumiendo 30 km/h
    document.getElementById('route-distance').textContent = `${distance.toFixed(2)} km`;
    document.getElementById('route-time').textContent = `${Math.ceil(estimatedTime)} min`;
    document.getElementById('route-destination').textContent = shipment.origin.city;
    document.getElementById('route-info-panel').classList.add('visible');
}

// ========================
// ACCIONES
// ========================

function completeShipment(shipmentId) {
    if (!confirm('¿Marcar este envío como completado?')) {
        return;
    }
    
    console.log('✅ Completando envío:', shipmentId);
    
    // Comunicar con JavaFX si está disponible
    if (typeof window.javaConnector !== 'undefined' && window.javaConnector.completeShipment) {
        window.javaConnector.completeShipment(shipmentId);
    } else {
        // Simulación: eliminar envío de la lista
        currentDeliverer.currentShipments = currentDeliverer.currentShipments.filter(s => s.id !== shipmentId);
        currentDeliverer.totalDeliveries++;
        
        loadDelivererData();
        loadShipments();
        clearRouteMarkers();
        
        alert('✅ Envío completado exitosamente');
    }
}

function startNavigation() {
    if (!selectedShipment) {
        alert('Seleccione un envío primero');
        return;
    }
    
    console.log('🧭 Iniciando navegación...');
    alert('🧭 Navegación iniciada. Sigue la ruta marcada en naranja en el mapa.');
}

function refreshData() {
    console.log('🔄 Actualizando datos...');
    
    // Comunicar con JavaFX si está disponible
    if (typeof window.javaConnector !== 'undefined' && window.javaConnector.refreshData) {
        window.javaConnector.refreshData();
    } else {
        // Recargar datos simulados
        loadDelivererData();
        loadShipments();
        alert('✅ Datos actualizados');
    }
}

function logout() {
    if (!confirm('¿Cerrar sesión?')) {
        return;
    }
    
    console.log('🚪 Cerrando sesión...');
    
    // Comunicar con JavaFX si está disponible
    if (typeof window.javaConnector !== 'undefined' && window.javaConnector.logout) {
        window.javaConnector.logout();
    } else {
        alert('Sesión cerrada');
        window.location.href = 'index.html';
    }
}

// ========================
// API PARA JAVAFX
// ========================

// Función para recibir datos desde JavaFX
function setDelivererData(data) {
    console.log('📥 Recibiendo datos desde JavaFX:', data);
    currentDeliverer = data;
    loadDelivererData();
    loadShipments();
    
    // Actualizar marcador del repartidor
    if (delivererMarker && map) {
        delivererMarker.setLatLng([currentDeliverer.currentLat, currentDeliverer.currentLng]);
        map.setView([currentDeliverer.currentLat, currentDeliverer.currentLng], 14);
    }
}

// Exponer funciones para JavaFX
window.setDelivererData = setDelivererData;
window.calculateRoute = calculateRoute;
window.completeShipment = completeShipment;
window.refreshData = refreshData;
window.logout = logout;

console.log('✅ Dashboard del Repartidor cargado completamente');
