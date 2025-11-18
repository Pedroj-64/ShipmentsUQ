
// Armenia, Quindío como centro
const ARMENIA_CENTER = [4.533889, -75.681111];

// Estado
let map;
let delivererMarker = null;
let originMarker = null;
let destinationMarker = null;
let routeLine = null;
let delivererCoords = null;
let originCoords = null;
let destinationCoords = null;

// Auto-refresh
let refreshInterval = null;

// Inicializar mapa
function initMap() {
    // Crear mapa centrado en Armenia (modo solo lectura - sin clic)
    map = L.map('map', {
        center: ARMENIA_CENTER,
        zoom: 13,
        zoomControl: true,
        scrollWheelZoom: true,
        doubleClickZoom: false,  // Deshabilitar zoom con doble clic
        dragging: true
    });

    // Añadir tiles de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);

    // Cargar datos iniciales (simulados - en producción vienen del backend)
    loadTrackingData();

    // Auto-refresh cada 30 segundos
    startAutoRefresh();

    console.log('✅ Mapa inicializado - Modo: Tracking (Solo Lectura)');
}

// Cargar datos de tracking
async function loadTrackingData() {
    console.log('🔄 Cargando datos de tracking...');

    try {
        // En producción, esto vendría de una API
        // Por ahora, usamos datos simulados
        
        // Simular ubicación del repartidor (en movimiento)
        const baseTime = Date.now();
        const offset = (baseTime % 60000) / 60000; // Ciclo de 1 minuto
        
        // Ruta simulada: repartidor moviéndose de origen a destino
        const origin = { lat: 4.540000, lng: -75.670000 }; // Origen del pedido
        const destination = { lat: 4.520000, lng: -75.690000 }; // Destino (cliente)
        
        // Posición actual del repartidor (interpolación)
        const deliverer = {
            lat: origin.lat + (destination.lat - origin.lat) * offset,
            lng: origin.lng + (destination.lng - destination.lng) * offset
        };

        // Actualizar marcadores
        updateDelivererMarker(deliverer.lat, deliverer.lng);
        updateOriginMarker(origin.lat, origin.lng);
        updateDestinationMarker(destination.lat, destination.lng);
        
        // Dibujar ruta
        drawRoute(origin, destination, deliverer);
        
        // Actualizar UI
        updateTrackingInfo(deliverer, origin, destination);
        
        // Ajustar zoom para ver todo
        fitMapToBounds();

    } catch (error) {
        console.error('❌ Error al cargar datos de tracking:', error);
    }
}

// Actualizar marcador del repartidor
function updateDelivererMarker(lat, lng) {
    if (delivererMarker) {
        map.removeLayer(delivererMarker);
    }

    delivererMarker = L.marker([lat, lng], {
        icon: L.divIcon({
            className: 'deliverer-marker',
            html: `
                <div style="position: relative;">
                    <div style="
                        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
                        color: white;
                        width: 50px;
                        height: 50px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 24px;
                        box-shadow: 0 4px 20px rgba(59, 130, 246, 0.5);
                        border: 4px solid white;
                        animation: pulse-marker 2s infinite;
                    ">🚴</div>
                    <div style="
                        position: absolute;
                        bottom: -25px;
                        left: 50%;
                        transform: translateX(-50%);
                        background: #1e40af;
                        color: white;
                        padding: 4px 8px;
                        border-radius: 12px;
                        font-size: 10px;
                        font-weight: bold;
                        white-space: nowrap;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                    ">Tu Repartidor</div>
                </div>
            `,
            iconSize: [50, 50],
            iconAnchor: [25, 25]
        }),
        zIndexOffset: 1000 // Asegurar que esté al frente
    }).addTo(map);

    delivererMarker.bindPopup(`
        <div style="text-align: center;">
            <strong style="color: #1e40af; font-size: 14px;">🚴 Tu Repartidor</strong><br>
            <span style="color: #10b981; font-size: 12px;">● En servicio</span><br>
            <hr style="margin: 8px 0; border: none; border-top: 1px solid #e2e8f0;">
            <span style="font-size: 11px; color: #64748b; font-family: monospace;">
                📍 Lat: ${lat.toFixed(6)}<br>
                📍 Lng: ${lng.toFixed(6)}
            </span>
        </div>
    `);

    delivererCoords = { lat, lng };
}

// Actualizar marcador de origen
function updateOriginMarker(lat, lng) {
    if (originMarker) {
        map.removeLayer(originMarker);
    }

    originMarker = L.marker([lat, lng], {
        icon: L.divIcon({
            className: 'origin-marker',
            html: `
                <div style="
                    background: #10b981;
                    color: white;
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 20px;
                    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
                    border: 3px solid white;
                ">📦</div>
            `,
            iconSize: [40, 40],
            iconAnchor: [20, 40]
        })
    }).addTo(map);

    originMarker.bindPopup(`
        <strong style="color: #10b981;">📦 Origen (Recogida)</strong><br>
        <span style="font-size: 12px;">Punto de partida del pedido</span>
    `);

    originCoords = { lat, lng };
}

// Actualizar marcador de destino
function updateDestinationMarker(lat, lng) {
    if (destinationMarker) {
        map.removeLayer(destinationMarker);
    }

    destinationMarker = L.marker([lat, lng], {
        icon: L.divIcon({
            className: 'destination-marker',
            html: `
                <div style="
                    background: #ef4444;
                    color: white;
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 20px;
                    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
                    border: 3px solid white;
                ">🏠</div>
            `,
            iconSize: [40, 40],
            iconAnchor: [20, 40]
        })
    }).addTo(map);

    destinationMarker.bindPopup(`
        <strong style="color: #ef4444;">🏠 Destino (Tu Dirección)</strong><br>
        <span style="font-size: 12px;">Ubicación de entrega</span>
    `);

    destinationCoords = { lat, lng };
}

// Dibujar ruta
function drawRoute(origin, destination, deliverer) {
    if (routeLine) {
        map.removeLayer(routeLine);
    }

    // Ruta completa (origen → destino)
    routeLine = L.polyline([
        [origin.lat, origin.lng],
        [destination.lat, destination.lng]
    ], {
        color: '#cbd5e1',
        weight: 4,
        opacity: 0.5,
        dashArray: '10, 10'
    }).addTo(map);

    // Ruta completada (origen → repartidor actual)
    const completedRoute = L.polyline([
        [origin.lat, origin.lng],
        [deliverer.lat, deliverer.lng]
    ], {
        color: '#3b82f6',
        weight: 4,
        opacity: 0.8
    }).addTo(map);

    // Círculo de progreso alrededor del repartidor
    L.circle([deliverer.lat, deliverer.lng], {
        color: '#3b82f6',
        fillColor: '#dbeafe',
        fillOpacity: 0.2,
        radius: 200 // 200 metros
    }).addTo(map);
}

// Actualizar información de tracking
function updateTrackingInfo(deliverer, origin, destination) {
    // Calcular distancia restante
    const remainingDistance = calculateDistance(
        deliverer.lat, deliverer.lng,
        destination.lat, destination.lng
    );

    // Calcular ETA (30 km/h promedio)
    const etaMinutes = Math.ceil((remainingDistance / 30) * 60);

    // Actualizar coordenadas del repartidor
    document.getElementById('deliverer-coords').textContent = 
        `📍 Lat: ${deliverer.lat.toFixed(6)}, Lng: ${deliverer.lng.toFixed(6)}`;

    // Actualizar ETA
    document.getElementById('eta-time').textContent = 
        etaMinutes > 0 ? `${etaMinutes} min` : '¡Llegando!';

    // Actualizar estado
    if (etaMinutes <= 2) {
        document.getElementById('delivery-status').textContent = '🎉 ¡El repartidor está muy cerca!';
    } else if (etaMinutes <= 5) {
        document.getElementById('delivery-status').textContent = '🚴 Llegando pronto';
    } else {
        document.getElementById('delivery-status').textContent = '🚴 En camino a tu ubicación';
    }

    console.log(`📊 Distancia restante: ${remainingDistance.toFixed(2)} km, ETA: ${etaMinutes} min`);
}

// Ajustar zoom para ver todos los marcadores
function fitMapToBounds() {
    if (delivererCoords && originCoords && destinationCoords) {
        const bounds = L.latLngBounds([
            [originCoords.lat, originCoords.lng],
            [destinationCoords.lat, destinationCoords.lng],
            [delivererCoords.lat, delivererCoords.lng]
        ]);
        
        map.fitBounds(bounds, { padding: [50, 50] });
    }
}

// Calcular distancia Haversine
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function toRad(degrees) {
    return degrees * (Math.PI / 180);
}

// Auto-refresh
function startAutoRefresh() {
    refreshInterval = setInterval(() => {
        console.log('🔄 Auto-refresh: actualizando ubicación...');
        loadTrackingData();
    }, 30000); // Cada 30 segundos
}

function stopAutoRefresh() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
        refreshInterval = null;
        console.log('⏸️ Auto-refresh detenido');
    }
}

// Botón: Actualizar manualmente
document.getElementById('btn-refresh').addEventListener('click', () => {
    console.log('🔄 Actualización manual solicitada');
    loadTrackingData();
    
    // Feedback visual
    const btn = document.getElementById('btn-refresh');
    const originalText = btn.textContent;
    btn.textContent = '✅ Actualizado';
    setTimeout(() => {
        btn.textContent = originalText;
    }, 1500);
});

// Botón: Centrar en repartidor
document.getElementById('btn-center').addEventListener('click', () => {
    if (delivererCoords) {
        map.setView([delivererCoords.lat, delivererCoords.lng], 15);
        
        // Abrir popup del repartidor
        if (delivererMarker) {
            delivererMarker.openPopup();
        }
        
        console.log('🎯 Mapa centrado en repartidor');
    }
});

// Limpiar al salir
window.addEventListener('beforeunload', () => {
    stopAutoRefresh();
});

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initMap);

// Agregar CSS de animación
const style = document.createElement('style');
style.textContent = `
    @keyframes pulse-marker {
        0%, 100% {
            box-shadow: 0 4px 20px rgba(59, 130, 246, 0.5);
        }
        50% {
            box-shadow: 0 4px 30px rgba(59, 130, 246, 0.8), 0 0 0 15px rgba(59, 130, 246, 0.1);
        }
    }
`;
document.head.appendChild(style);

console.log('🗺️ tracking-map.js cargado - Modo: Visualización en Tiempo Real (Solo Lectura)');
