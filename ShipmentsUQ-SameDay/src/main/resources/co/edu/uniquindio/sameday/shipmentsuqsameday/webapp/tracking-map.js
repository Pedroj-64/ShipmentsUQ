
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

// Cargar datos de tracking desde el backend
async function loadTrackingData() {
    console.log('🔄 Cargando datos de tracking desde API...');

    try {
        // Llamar al API REST para obtener envíos activos
        const response = await fetch('/api/tracking/active');
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (!data.success) {
            throw new Error(data.error || 'Error desconocido');
        }
        
        console.log(`✅ Datos recibidos: ${data.count} envíos activos`);
        
        if (data.count === 0) {
            console.warn('⚠️ No hay envíos activos en este momento');
            showNoShipmentsMessage();
            return;
        }
        
        // Usar el primer envío activo (o podrías filtrar por un ID específico)
        const shipment = data.shipments[0];
        
        console.log('📦 Envío:', shipment.shipmentId);
        console.log('🚴 Repartidor:', shipment.deliverer.name);
        console.log('📍 Total de envíos activos:', data.count);
        
        // Extraer ubicaciones
        const deliverer = {
            lat: shipment.deliverer.currentLocation.lat,
            lng: shipment.deliverer.currentLocation.lng,
            name: shipment.deliverer.name
        };
        
        console.log('🚴 Ubicación del repartidor:', deliverer.lat.toFixed(6), deliverer.lng.toFixed(6));
        
        const origin = shipment.origin ? {
            lat: shipment.origin.location.lat,
            lng: shipment.origin.location.lng,
            address: `${shipment.origin.street}, ${shipment.origin.city}`
        } : null;
        
        if (origin) {
            console.log('📦 Origen:', origin.address, '→', origin.lat.toFixed(6), origin.lng.toFixed(6));
        }
        
        const destination = {
            lat: shipment.destination.location.lat,
            lng: shipment.destination.location.lng,
            address: `${shipment.destination.street}, ${shipment.destination.city}`
        };
        
        console.log('🏠 Destino:', destination.address, '→', destination.lat.toFixed(6), destination.lng.toFixed(6));
        
        // Validar si las coordenadas son iguales (problema común)
        if (origin && Math.abs(origin.lat - destination.lat) < 0.0001 && Math.abs(origin.lng - destination.lng) < 0.0001) {
            console.error('⚠️ PROBLEMA: Origen y Destino tienen las MISMAS coordenadas!');
        }
        if (Math.abs(deliverer.lat - destination.lat) < 0.0001 && Math.abs(deliverer.lng - destination.lng) < 0.0001) {
            console.error('⚠️ PROBLEMA: Repartidor y Destino tienen las MISMAS coordenadas!');
        }
        
        // Actualizar marcadores
        updateDelivererMarker(deliverer.lat, deliverer.lng, deliverer.name);
        if (origin) {
            updateOriginMarker(origin.lat, origin.lng, origin.address);
        }
        updateDestinationMarker(destination.lat, destination.lng, destination.address);
        
        // Dibujar ruta
        if (origin) {
            drawRoute(origin, destination, deliverer);
        } else {
            drawRouteWithoutOrigin(destination, deliverer);
        }
        
        // Actualizar UI con datos reales
        updateTrackingInfo(deliverer, origin, destination, shipment);
        
        // Ajustar zoom para ver todo
        fitMapToBounds();

    } catch (error) {
        console.error('❌ Error al cargar datos de tracking:', error);
        showErrorMessage(error.message);
    }
}

// Mostrar mensaje cuando no hay envíos activos
function showNoShipmentsMessage() {
    document.getElementById('delivery-status').textContent = '📭 No hay envíos en tránsito';
    document.getElementById('deliverer-name').textContent = 'Sin asignar';
    document.getElementById('deliverer-coords').textContent = 'N/A';
    document.getElementById('eta-time').textContent = '--';
    document.getElementById('origin-address').textContent = 'Sin datos';
    document.getElementById('destination-address').textContent = 'Sin datos';
}

// Mostrar mensaje de error
function showErrorMessage(message) {
    document.getElementById('delivery-status').textContent = '❌ Error al cargar datos';
    document.getElementById('deliverer-coords').textContent = message;
}

// Dibujar ruta sin origen
function drawRouteWithoutOrigin(destination, deliverer) {
    if (routeLine) {
        map.removeLayer(routeLine);
    }

    // Ruta directa (repartidor → destino)
    routeLine = L.polyline([
        [deliverer.lat, deliverer.lng],
        [destination.lat, destination.lng]
    ], {
        color: '#3b82f6',
        weight: 4,
        opacity: 0.8,
        dashArray: '10, 10'
    }).addTo(map);

    // Círculo de progreso
    L.circle([deliverer.lat, deliverer.lng], {
        color: '#3b82f6',
        fillColor: '#dbeafe',
        fillOpacity: 0.2,
        radius: 200
    }).addTo(map);
}

// Actualizar marcador del repartidor
function updateDelivererMarker(lat, lng, name = 'Repartidor') {
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
                    ">${name}</div>
                </div>
            `,
            iconSize: [50, 50],
            iconAnchor: [25, 25]
        }),
        zIndexOffset: 1000
    }).addTo(map);

    delivererMarker.bindPopup(`
        <div style="text-align: center;">
            <strong style="color: #1e40af; font-size: 14px;">🚴 ${name}</strong><br>
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
function updateOriginMarker(lat, lng, address = '') {
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
        <span style="font-size: 12px;">${address || 'Punto de partida'}</span>
    `);

    originCoords = { lat, lng };
}

// Actualizar marcador de destino
function updateDestinationMarker(lat, lng, address = '') {
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
        <span style="font-size: 12px;">${address || 'Ubicación de entrega'}</span>
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
function updateTrackingInfo(deliverer, origin, destination, shipment) {
    // Actualizar nombre del repartidor
    document.getElementById('deliverer-name').textContent = deliverer.name || 'Repartidor';

    // Actualizar coordenadas del repartidor
    document.getElementById('deliverer-coords').textContent = 
        `📍 Lat: ${deliverer.lat.toFixed(6)}, Lng: ${deliverer.lng.toFixed(6)}`;

    // Actualizar direcciones
    if (origin) {
        document.getElementById('origin-address').textContent = origin.address || 'Sin especificar';
    } else {
        document.getElementById('origin-address').textContent = 'Sin origen definido';
    }
    
    document.getElementById('destination-address').textContent = destination.address || 'Sin especificar';

    // Calcular distancia restante
    const remainingDistance = calculateDistance(
        deliverer.lat, deliverer.lng,
        destination.lat, destination.lng
    );

    // Calcular ETA (30 km/h promedio para repartidores urbanos)
    let etaMinutes = Math.ceil((remainingDistance / 30) * 60);
    
    // Si hay simulación activa, usar su ETA
    if (shipment && shipment.simulation && shipment.simulation.active) {
        console.log('📊 Usando ETA de simulación');
        // Usar datos de simulación si están disponibles
        if (shipment.simulation.remainingDistance !== undefined) {
            const simDistance = shipment.simulation.remainingDistance;
            etaMinutes = Math.ceil((simDistance / 30) * 60);
        }
    }

    // Actualizar ETA
    if (etaMinutes <= 0 || remainingDistance < 0.1) {
        document.getElementById('eta-time').textContent = '¡Llegando!';
        document.getElementById('delivery-status').textContent = '🎉 ¡El repartidor ha llegado!';
    } else if (etaMinutes <= 2) {
        document.getElementById('eta-time').textContent = `${etaMinutes} min`;
        document.getElementById('delivery-status').textContent = '🎉 ¡El repartidor está muy cerca!';
    } else if (etaMinutes <= 5) {
        document.getElementById('eta-time').textContent = `${etaMinutes} min`;
        document.getElementById('delivery-status').textContent = '🚴 Llegando pronto';
    } else {
        document.getElementById('eta-time').textContent = `${etaMinutes} min`;
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
