// ShipmentsUQ - Mapa Interactivo con comunicación Java
// Usando OpenStreetMap + Leaflet.js

// Configuración
const ARMENIA_CENTER = [4.533889, -75.681111];
const DEFAULT_ZOOM = 13;
const JAVA_SERVER = 'http://localhost:8080'; // Puerto del servidor Java

// Estado de la aplicación
let map;
let currentMode = 'origin'; // 'origin' o 'destination'
let originMarker = null;
let destinationMarker = null;
let routeLine = null;
let selectedOrigin = null;
let selectedDestination = null;

// Iconos personalizados
const originIcon = L.divIcon({
    className: 'custom-marker',
    html: `<div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                       width: 40px; height: 40px; border-radius: 50%; 
                       display: flex; align-items: center; justify-content: center;
                       box-shadow: 0 4px 15px rgba(102, 126, 234, 0.5);
                       border: 4px solid white;">
                <span style="color: white; font-size: 20px;">📍</span>
           </div>`,
    iconSize: [40, 40],
    iconAnchor: [20, 20]
});

const destinationIcon = L.divIcon({
    className: 'custom-marker',
    html: `<div style="background: linear-gradient(135deg, #10b981 0%, #059669 100%); 
                       width: 40px; height: 40px; border-radius: 50%; 
                       display: flex; align-items: center; justify-content: center;
                       box-shadow: 0 4px 15px rgba(16, 185, 129, 0.5);
                       border: 4px solid white;">
                <span style="color: white; font-size: 20px;">🎯</span>
           </div>`,
    iconSize: [40, 40],
    iconAnchor: [20, 20]
});

// Inicializar mapa
function initMap() {
    map = L.map('map').setView(ARMENIA_CENTER, DEFAULT_ZOOM);

    // Capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);

    // Evento de click en el mapa
    map.on('click', handleMapClick);

    console.log('Mapa inicializado en Armenia, Quindío');
}

// Manejar click en el mapa
function handleMapClick(e) {
    const { lat, lng } = e.latlng;

    if (currentMode === 'origin') {
        setOrigin(lat, lng);
    } else {
        setDestination(lat, lng);
    }

    // Calcular si ambos puntos están seleccionados
    if (selectedOrigin && selectedDestination) {
        calculateRoute();
        drawRoute();
    }
}

// Establecer origen
function setOrigin(lat, lng) {
    selectedOrigin = { lat, lng };

    // Remover marcador anterior
    if (originMarker) {
        map.removeLayer(originMarker);
    }

    // Crear nuevo marcador
    originMarker = L.marker([lat, lng], { icon: originIcon })
        .addTo(map)
        .bindPopup(`<b>Origen</b><br>Lat: ${lat.toFixed(6)}<br>Lng: ${lng.toFixed(6)}`)
        .openPopup();

    // Actualizar UI
    updateOriginDisplay(lat, lng);

    console.log('Origen seleccionado:', { lat, lng });
}

// Establecer destino
function setDestination(lat, lng) {
    selectedDestination = { lat, lng };

    // Remover marcador anterior
    if (destinationMarker) {
        map.removeLayer(destinationMarker);
    }

    // Crear nuevo marcador
    destinationMarker = L.marker([lat, lng], { icon: destinationIcon })
        .addTo(map)
        .bindPopup(`<b>Destino</b><br>Lat: ${lat.toFixed(6)}<br>Lng: ${lng.toFixed(6)}`)
        .openPopup();

    // Actualizar UI
    updateDestinationDisplay(lat, lng);

    console.log('Destino seleccionado:', { lat, lng });
}

// Dibujar línea de ruta
function drawRoute() {
    // Remover línea anterior
    if (routeLine) {
        map.removeLayer(routeLine);
    }

    // Crear nueva línea
    routeLine = L.polyline(
        [
            [selectedOrigin.lat, selectedOrigin.lng],
            [selectedDestination.lat, selectedDestination.lng]
        ],
        {
            color: '#667eea',
            weight: 4,
            opacity: 0.7,
            dashArray: '10, 10'
        }
    ).addTo(map);

    // Ajustar vista para mostrar ambos puntos
    const bounds = L.latLngBounds(
        [selectedOrigin.lat, selectedOrigin.lng],
        [selectedDestination.lat, selectedDestination.lng]
    );
    map.fitBounds(bounds, { padding: [50, 50] });
}

// Calcular ruta (Haversine)
function calculateRoute() {
    const R = 6371; // Radio de la Tierra en km

    const lat1 = selectedOrigin.lat * Math.PI / 180;
    const lat2 = selectedDestination.lat * Math.PI / 180;
    const deltaLat = (selectedDestination.lat - selectedOrigin.lat) * Math.PI / 180;
    const deltaLng = (selectedDestination.lng - selectedOrigin.lng) * Math.PI / 180;

    const a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
              Math.cos(lat1) * Math.cos(lat2) *
              Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;

    // Cálculos
    const time = (distance / 30) * 60 + 10; // 30 km/h + 10 min buffer
    const cost = 5000 + (distance * 2500);
    const sameDay = distance <= 30;

    // Actualizar UI
    updateCalculations(distance, time, cost, sameDay);

    console.log('Ruta calculada:', { distance, time, cost, sameDay });
}

// Actualizar displays
function updateOriginDisplay(lat, lng) {
    const el = document.getElementById('origin-coords');
    el.textContent = `Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)}`;
    el.classList.remove('empty');
}

function updateDestinationDisplay(lat, lng) {
    const el = document.getElementById('destination-coords');
    el.textContent = `Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)}`;
    el.classList.remove('empty');
}

function updateCalculations(distance, time, cost, sameDay) {
    document.getElementById('distance').textContent = `${distance.toFixed(2)} km`;
    
    const hours = Math.floor(time / 60);
    const minutes = Math.round(time % 60);
    document.getElementById('time').textContent = hours > 0 
        ? `${hours}h ${minutes}min` 
        : `${minutes} min`;
    
    document.getElementById('cost').textContent = `$${cost.toLocaleString('es-CO')} COP`;
    document.getElementById('same-day').textContent = sameDay ? '✓ Sí' : '✗ No';
}

// Limpiar mapa
function clearMap() {
    if (originMarker) map.removeLayer(originMarker);
    if (destinationMarker) map.removeLayer(destinationMarker);
    if (routeLine) map.removeLayer(routeLine);

    originMarker = null;
    destinationMarker = null;
    routeLine = null;
    selectedOrigin = null;
    selectedDestination = null;

    // Reset UI
    document.getElementById('origin-coords').textContent = 'No seleccionado - Haz clic en el mapa';
    document.getElementById('origin-coords').classList.add('empty');
    document.getElementById('destination-coords').textContent = 'No seleccionado - Haz clic en el mapa';
    document.getElementById('destination-coords').classList.add('empty');
    document.getElementById('distance').textContent = '-';
    document.getElementById('time').textContent = '-';
    document.getElementById('cost').textContent = '-';
    document.getElementById('same-day').textContent = '-';

    console.log('Mapa limpiado');
}

// Centrar en Armenia
function centerMap() {
    map.setView(ARMENIA_CENTER, DEFAULT_ZOOM);
    console.log('Mapa centrado en Armenia');
}

// Enviar datos a Java
async function sendToJava() {
    // Solo requiere origen (destino es opcional)
    if (!selectedOrigin) {
        alert('⚠️ Selecciona al menos el origen (haz clic en el mapa)');
        return;
    }

    const data = {
        origin: selectedOrigin,
        destination: selectedDestination || null, // Destino opcional
        timestamp: new Date().toISOString()
    };

    console.log('Enviando datos a Java:', data);

    try {
        const response = await fetch(`${JAVA_SERVER}/api/coordinates`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data),
            mode: 'cors'
        });

        console.log('Response status:', response.status);

        if (response.ok) {
            console.log('Datos enviados a Java exitosamente');
            alert('✓ Coordenadas enviadas al sistema Java');
            
            // Opcional: limpiar después de enviar
            // clearMap();
        } else {
            const errorText = await response.text();
            console.error('Error al enviar datos:', response.status, errorText);
            alert(`✗ Error del servidor: ${response.status} - ${errorText}`);
        }
    } catch (error) {
        console.error('Error de conexión:', error);
        alert(`✗ No se pudo conectar con Java.\n\nAsegúrate de que:\n1. El servidor esté corriendo en ${JAVA_SERVER}\n2. La aplicación JavaFX esté abierta\n3. No haya firewall bloqueando el puerto 8080\n\nError: ${error.message}`);
    }
}

// Event listeners
document.getElementById('btn-origin').addEventListener('click', () => {
    currentMode = 'origin';
    document.getElementById('btn-origin').classList.add('active');
    document.getElementById('btn-destination').classList.remove('active');
});

document.getElementById('btn-destination').addEventListener('click', () => {
    currentMode = 'destination';
    document.getElementById('btn-destination').classList.add('active');
    document.getElementById('btn-origin').classList.remove('active');
});

document.getElementById('btn-clear').addEventListener('click', clearMap);
document.getElementById('btn-center').addEventListener('click', centerMap);
document.getElementById('btn-send').addEventListener('click', sendToJava);

// Inicializar cuando cargue el DOM
document.addEventListener('DOMContentLoaded', () => {
    initMap();
    console.log('Aplicación de mapa inicializada');
    console.log('Esperando conexión con servidor Java en', JAVA_SERVER);
});
