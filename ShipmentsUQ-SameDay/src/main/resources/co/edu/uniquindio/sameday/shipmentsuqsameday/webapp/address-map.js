// 📍 Mapa para Seleccionar Dirección (Solo 1 Punto)
// Armenia, Quindío como centro
const ARMENIA_CENTER = [4.533889, -75.681111];
const SERVICE_RADIUS_KM = 20;

// Estado
let map;
let selectedMarker = null;
let serviceCircle = null;
let selectedCoords = null;

// Inicializar mapa
function initMap() {
    // Crear mapa centrado en Armenia
    map = L.map('map').setView(ARMENIA_CENTER, 13);

    // Añadir tiles de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);

    // Dibujar círculo de área de servicio
    serviceCircle = L.circle(ARMENIA_CENTER, {
        color: '#667eea',
        fillColor: '#667eea',
        fillOpacity: 0.1,
        radius: SERVICE_RADIUS_KM * 1000, // Convertir km a metros
        weight: 2,
        dashArray: '10, 10'
    }).addTo(map);

    // Marcador del centro (Armenia)
    L.marker(ARMENIA_CENTER, {
        icon: L.divIcon({
            className: 'center-marker',
            html: '<div style="background: #667eea; color: white; padding: 6px 12px; border-radius: 20px; font-weight: bold; font-size: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">🏢 Armenia</div>',
            iconSize: [100, 30],
            iconAnchor: [50, 15]
        })
    }).addTo(map).bindPopup('Centro de Servicio - Armenia, Quindío');

    // Evento de clic en el mapa
    map.on('click', handleMapClick);

    console.log('✅ Mapa inicializado - Modo: Selección de Dirección');
}

// Manejar clic en el mapa
function handleMapClick(e) {
    const lat = e.latlng.lat;
    const lng = e.latlng.lng;

    console.log(`📍 Clic en mapa: Lat ${lat.toFixed(6)}, Lng ${lng.toFixed(6)}`);

    // Verificar si está dentro del área de servicio
    const distance = calculateDistance(ARMENIA_CENTER[0], ARMENIA_CENTER[1], lat, lng);
    const inServiceArea = distance <= SERVICE_RADIUS_KM;

    // Actualizar coordenadas seleccionadas
    selectedCoords = { lat, lng };

    // Remover marcador anterior si existe
    if (selectedMarker) {
        map.removeLayer(selectedMarker);
    }

    // Crear nuevo marcador
    const markerColor = inServiceArea ? '#10b981' : '#ef4444';
    const markerIcon = inServiceArea ? '📍' : '⚠️';
    
    selectedMarker = L.marker([lat, lng], {
        icon: L.divIcon({
            className: 'location-marker',
            html: `<div style="background: ${markerColor}; color: white; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.3); border: 3px solid white;">${markerIcon}</div>`,
            iconSize: [40, 40],
            iconAnchor: [20, 40]
        }),
        draggable: true
    }).addTo(map);

    // Permitir arrastrar el marcador
    selectedMarker.on('dragend', function(event) {
        const marker = event.target;
        const position = marker.getLatLng();
        handleMapClick({ latlng: position });
    });

    // Popup con información
    selectedMarker.bindPopup(
        inServiceArea 
            ? `<strong>✅ Ubicación válida</strong><br>Lat: ${lat.toFixed(6)}<br>Lng: ${lng.toFixed(6)}<br>Distancia: ${distance.toFixed(2)} km`
            : `<strong>⚠️ Fuera del área de servicio</strong><br>Lat: ${lat.toFixed(6)}<br>Lng: ${lng.toFixed(6)}<br>Distancia: ${distance.toFixed(2)} km (máx: ${SERVICE_RADIUS_KM} km)`
    ).openPopup();

    // Actualizar UI
    updateCoordinatesDisplay(lat, lng, inServiceArea, distance);

    // Habilitar/deshabilitar botón de guardar
    document.getElementById('btn-save').disabled = !inServiceArea;
}

// Actualizar display de coordenadas
function updateCoordinatesDisplay(lat, lng, inServiceArea, distance) {
    const coordContainer = document.getElementById('coord-container');
    const coordsDisplay = document.getElementById('coords-display');
    const serviceStatus = document.getElementById('service-status');
    const addressPreview = document.getElementById('address-preview');

    // Actualizar coordenadas
    coordsDisplay.textContent = `Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)}`;
    coordsDisplay.classList.remove('empty');
    coordContainer.classList.remove('empty');

    if (inServiceArea) {
        coordContainer.style.borderLeftColor = '#10b981';
        serviceStatus.innerHTML = `✅ Dentro del área de servicio (${distance.toFixed(2)} km del centro)`;
        serviceStatus.style.color = '#166534';
        serviceStatus.style.background = '#f0fdf4';

        // Intentar obtener dirección (simulado - en producción usar geocoding)
        addressPreview.style.display = 'block';
        document.getElementById('address-text').textContent = 'Obteniendo dirección desde GPS...';
        
        // Simular llamada a geocoding
        setTimeout(() => {
            document.getElementById('address-text').textContent = 
                `📍 Ubicación en Armenia, Quindío\n(Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)})`;
        }, 500);

    } else {
        coordContainer.style.borderLeftColor = '#ef4444';
        serviceStatus.innerHTML = `⚠️ Fuera del área de servicio (${distance.toFixed(2)} km - máx: ${SERVICE_RADIUS_KM} km)`;
        serviceStatus.style.color = '#991b1b';
        serviceStatus.style.background = '#fee2e2';
        addressPreview.style.display = 'none';
    }
}

// Calcular distancia Haversine
function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // Radio de la Tierra en km
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

// Botón: Limpiar selección
document.getElementById('btn-clear').addEventListener('click', () => {
    if (selectedMarker) {
        map.removeLayer(selectedMarker);
        selectedMarker = null;
        selectedCoords = null;

        // Resetear UI
        const coordsDisplay = document.getElementById('coords-display');
        coordsDisplay.textContent = 'Haz clic en el mapa para seleccionar';
        coordsDisplay.classList.add('empty');
        document.getElementById('coord-container').classList.add('empty');
        document.getElementById('service-status').innerHTML = 'ℹ️ Selecciona un punto para verificar si está en área de servicio';
        document.getElementById('service-status').style.color = '#64748b';
        document.getElementById('service-status').style.background = '#f8fafc';
        document.getElementById('address-preview').style.display = 'none';
        document.getElementById('btn-save').disabled = true;

        console.log('🗑️ Selección limpiada');
    }
});

// Botón: Centrar en Armenia
document.getElementById('btn-center').addEventListener('click', () => {
    map.setView(ARMENIA_CENTER, 13);
    console.log('🎯 Mapa centrado en Armenia');
});

// Botón: Guardar ubicación (enviar a Java)
document.getElementById('btn-save').addEventListener('click', async () => {
    if (!selectedCoords) {
        alert('⚠️ Primero selecciona una ubicación en el mapa');
        return;
    }

    console.log('💾 Guardando ubicación:', selectedCoords);

    const payload = {
        origin: {
            lat: selectedCoords.lat,
            lng: selectedCoords.lng
        },
        destination: null // Solo enviamos origen para direcciones
    };

    try {
        const response = await fetch('/api/coordinates', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const result = await response.json();
            console.log('✅ Respuesta del servidor:', result);
            
            // Feedback visual
            const btn = document.getElementById('btn-save');
            btn.textContent = '✅ ¡Ubicación Guardada!';
            btn.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
            
            setTimeout(() => {
                btn.textContent = '💾 Guardar Ubicación';
                btn.style.background = '';
            }, 2000);
        } else {
            console.error('❌ Error al enviar coordenadas:', response.status);
            alert('❌ Error al guardar ubicación. Verifica la conexión con Java.');
        }
    } catch (error) {
        console.error('❌ Error de red:', error);
        alert('❌ Error de conexión. ¿El servidor Java está activo?');
    }
});

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initMap);

console.log('🗺️ address-map.js cargado - Modo: Selección de Dirección (1 punto)');
