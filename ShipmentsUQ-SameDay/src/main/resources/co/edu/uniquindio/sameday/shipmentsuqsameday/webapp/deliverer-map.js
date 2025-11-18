
// Armenia, Quindío como centro
const ARMENIA_CENTER = [4.533889, -75.681111];
const SERVICE_RADIUS_KM = 20;

// Estado
let map;
let delivererMarker = null;
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

    // Dibujar círculo de área de cobertura
    serviceCircle = L.circle(ARMENIA_CENTER, {
        color: '#f59e0b',
        fillColor: '#fef3c7',
        fillOpacity: 0.15,
        radius: SERVICE_RADIUS_KM * 1000,
        weight: 3,
        dashArray: '10, 10'
    }).addTo(map);

    // Marcador del centro (Armenia)
    L.marker(ARMENIA_CENTER, {
        icon: L.divIcon({
            className: 'center-marker',
            html: '<div style="background: #f59e0b; color: white; padding: 6px 12px; border-radius: 20px; font-weight: bold; font-size: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">🏢 Centro Armenia</div>',
            iconSize: [120, 30],
            iconAnchor: [60, 15]
        })
    }).addTo(map).bindPopup('Centro de Operaciones - Armenia, Quindío');

    // Evento de clic en el mapa
    map.on('click', handleMapClick);

    console.log('✅ Mapa inicializado - Modo: Asignación de Repartidor');
}

// Manejar clic en el mapa
function handleMapClick(e) {
    const lat = e.latlng.lat;
    const lng = e.latlng.lng;

    console.log(`🚴 Ubicación seleccionada: Lat ${lat.toFixed(6)}, Lng ${lng.toFixed(6)}`);

    // Verificar si está dentro del área de cobertura
    const distance = calculateDistance(ARMENIA_CENTER[0], ARMENIA_CENTER[1], lat, lng);
    const inCoverageArea = distance <= SERVICE_RADIUS_KM;

    // Actualizar coordenadas seleccionadas
    selectedCoords = { lat, lng };

    // Remover marcador anterior si existe
    if (delivererMarker) {
        map.removeLayer(delivererMarker);
    }

    // Crear icono del repartidor
    const markerHtml = inCoverageArea 
        ? `<div style="background: #10b981; color: white; width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.3); border: 4px solid white;">🚴</div>`
        : `<div style="background: #ef4444; color: white; width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.3); border: 4px solid white;">⚠️</div>`;
    
    delivererMarker = L.marker([lat, lng], {
        icon: L.divIcon({
            className: 'deliverer-marker',
            html: markerHtml,
            iconSize: [50, 50],
            iconAnchor: [25, 50]
        }),
        draggable: true
    }).addTo(map);

    // Permitir arrastrar el marcador
    delivererMarker.on('dragend', function(event) {
        const marker = event.target;
        const position = marker.getLatLng();
        handleMapClick({ latlng: position });
    });

    // Popup con información
    delivererMarker.bindPopup(
        inCoverageArea 
            ? `<strong>✅ Ubicación válida para repartidor</strong><br>
               📍 Lat: ${lat.toFixed(6)}<br>
               📍 Lng: ${lng.toFixed(6)}<br>
               📏 Distancia al centro: ${distance.toFixed(2)} km<br>
               <small>💡 Arrastra el marcador para ajustar</small>`
            : `<strong>⚠️ Fuera del área de cobertura</strong><br>
               📍 Lat: ${lat.toFixed(6)}<br>
               📍 Lng: ${lng.toFixed(6)}<br>
               📏 Distancia: ${distance.toFixed(2)} km<br>
               ❌ Máximo permitido: ${SERVICE_RADIUS_KM} km`
    ).openPopup();

    // Actualizar UI
    updateCoordinatesDisplay(lat, lng, inCoverageArea, distance);

    // Habilitar/deshabilitar botón de guardar
    document.getElementById('btn-save').disabled = !inCoverageArea;
}

// Actualizar display de coordenadas
function updateCoordinatesDisplay(lat, lng, inCoverageArea, distance) {
    const coordContainer = document.getElementById('coord-container');
    const coordsDisplay = document.getElementById('coords-display');
    const serviceStatus = document.getElementById('service-status');

    // Actualizar coordenadas
    coordsDisplay.textContent = `Lat: ${lat.toFixed(6)}, Lng: ${lng.toFixed(6)}`;
    coordsDisplay.classList.remove('empty');
    coordContainer.classList.remove('empty');

    if (inCoverageArea) {
        coordContainer.style.borderLeftColor = '#10b981';
        coordContainer.style.background = '#f0fdf4';
        
        const coordLabel = coordContainer.querySelector('.coord-label');
        coordLabel.style.color = '#166534';
        
        coordsDisplay.style.color = '#166534';
        
        serviceStatus.innerHTML = `✅ Repartidor dentro del área de cobertura<br>📏 Distancia al centro: ${distance.toFixed(2)} km`;
        serviceStatus.style.color = '#166534';
        serviceStatus.style.background = '#f0fdf4';
        serviceStatus.style.border = '1px solid #86efac';

    } else {
        coordContainer.style.borderLeftColor = '#ef4444';
        coordContainer.style.background = '#fee2e2';
        
        const coordLabel = coordContainer.querySelector('.coord-label');
        coordLabel.style.color = '#991b1b';
        
        coordsDisplay.style.color = '#991b1b';
        
        serviceStatus.innerHTML = `⚠️ Repartidor fuera del área permitida<br>📏 Distancia: ${distance.toFixed(2)} km (máx: ${SERVICE_RADIUS_KM} km)`;
        serviceStatus.style.color = '#991b1b';
        serviceStatus.style.background = '#fee2e2';
        serviceStatus.style.border = '1px solid #fca5a5';
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
    if (delivererMarker) {
        map.removeLayer(delivererMarker);
        delivererMarker = null;
        selectedCoords = null;

        // Resetear UI
        const coordsDisplay = document.getElementById('coords-display');
        const coordContainer = document.getElementById('coord-container');
        
        coordsDisplay.textContent = 'Haz clic en el mapa para asignar ubicación';
        coordsDisplay.classList.add('empty');
        coordContainer.classList.add('empty');
        coordContainer.style.background = '#f8fafc';
        coordContainer.style.borderLeftColor = '#cbd5e1';
        
        const coordLabel = coordContainer.querySelector('.coord-label');
        coordLabel.style.color = '#64748b';
        
        document.getElementById('service-status').innerHTML = 'ℹ️ Selecciona un punto en el mapa';
        document.getElementById('service-status').style.color = '#64748b';
        document.getElementById('service-status').style.background = '#f8fafc';
        document.getElementById('service-status').style.border = 'none';
        document.getElementById('btn-save').disabled = true;

        console.log('🗑️ Ubicación del repartidor limpiada');
    }
});

// Botón: Centrar en Armenia
document.getElementById('btn-center').addEventListener('click', () => {
    map.setView(ARMENIA_CENTER, 13);
    console.log('🎯 Mapa centrado en Armenia');
});

// Botón: Guardar ubicación del repartidor (enviar a Java)
document.getElementById('btn-save').addEventListener('click', async () => {
    if (!selectedCoords) {
        alert('⚠️ Primero selecciona la ubicación del repartidor en el mapa');
        return;
    }

    console.log('💾 Asignando ubicación al repartidor:', selectedCoords);

    const payload = {
        origin: {
            lat: selectedCoords.lat,
            lng: selectedCoords.lng
        },
        destination: null // Solo enviamos ubicación del repartidor
    };

    try {
        const response = await fetch('/api/coordinates', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const result = await response.json();
            console.log('Ubicación asignada correctamente:', result);
            
            // Feedback visual
            const btn = document.getElementById('btn-save');
            const originalText = btn.textContent;
            btn.textContent = 'Ubicación Asignada!';
            btn.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
            
            // Animación del marcador
            if (delivererMarker) {
                const marker = delivererMarker.getElement();
                marker.style.animation = 'bounce 0.5s ease';
            }
            
            // Mensaje de cierre
            console.log('Cerrando pestaña en 1.5 segundos...');
            document.getElementById('info-panel').innerHTML = 
                '<div style="background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 20px; border-radius: 10px; color: white; text-align: center; margin-top: 20px;">' +
                '<h3 style="margin: 0 0 10px 0;">Ubicación asignada exitosamente</h3>' +
                '<p style="margin: 0;">La pestaña se cerrará automáticamente...</p>' +
                '</div>';
            
            // Cerrar pestaña automáticamente después de 1.5 segundos
            setTimeout(() => {
                console.log('Intentando cerrar pestaña del navegador...');
                window.close();
                
                // Si window.close() no funciona
                setTimeout(() => {
                    document.body.innerHTML = 
                        '<div style="display: flex; justify-content: center; align-items: center; height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-family: Arial, sans-serif;">' +
                        '<div style="text-align: center;">' +
                        '<h1 style="font-size: 3em; margin-bottom: 20px;">Ubicación Asignada</h1>' +
                        '<p style="font-size: 1.2em;">Puedes cerrar esta pestaña manualmente</p>' +
                        '<p style="margin-top: 30px; opacity: 0.8;">Esta ventana intentó cerrarse automáticamente</p>' +
                        '</div>' +
                        '</div>';
                }, 100);
            }, 1500);
        } else {
            const errorText = await response.text().catch(() => 'Sin detalles');
            console.error('Servidor respondió con error:', response.status, errorText);
            alert(`Error del servidor (${response.status}). Por favor intenta nuevamente.`);
        }
    } catch (error) {
        console.error('Error al comunicarse con el servidor:', error);
        // Solo mostrar alerta si realmente no se puede conectar después de un reintento
        setTimeout(async () => {
            try {
                await fetch('/api/coordinates', { method: 'HEAD' });
                console.log('Servidor está activo, reintentando operación...');
                alert('Hubo un error temporal. Por favor intenta guardar nuevamente.');
            } catch {
                alert('No se puede conectar al servidor. Por favor verifica que la aplicación esté en ejecución.');
            }
        }, 500);
    }
});

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initMap);

console.log('🗺️ deliverer-map.js cargado - Modo: Asignación de Ubicación de Repartidor');
