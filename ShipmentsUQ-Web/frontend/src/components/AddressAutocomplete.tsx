import { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import { Icon } from 'leaflet';
import 'leaflet/dist/leaflet.css';

interface AddressSuggestion {
  display_name: string;
  lat: string;
  lon: string;
  address: {
    road?: string;
    house_number?: string;
    city?: string;
    state?: string;
    postcode?: string;
    country?: string;
  };
}

interface AddressAutocompleteProps {
  value: string;
  onChange: (address: string, lat?: number, lon?: number) => void;
  placeholder?: string;
  label?: string;
  required?: boolean;
  showMap?: boolean;
}

// Componente para manejar clicks en el mapa
function MapClickHandler({ onLocationSelect }: { onLocationSelect: (lat: number, lon: number) => void }) {
  useMapEvents({
    click: (e) => {
      onLocationSelect(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

export default function AddressAutocomplete({
  value,
  onChange,
  placeholder = 'Buscar dirección...',
  label,
  required = false,
  showMap = true
}: AddressAutocompleteProps) {
  const [suggestions, setSuggestions] = useState<AddressSuggestion[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [loading, setLoading] = useState(false);
  const [selectedPosition, setSelectedPosition] = useState<[number, number] | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const wrapperRef = useRef<HTMLDivElement>(null);

  // Icono personalizado para el marcador
  const customIcon = new Icon({
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
    iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  // Cerrar sugerencias al hacer click fuera
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setShowSuggestions(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Buscar direcciones usando Nominatim (OpenStreetMap)
  useEffect(() => {
    const searchAddress = async () => {
      if (value.length < 3) {
        setSuggestions([]);
        return;
      }

      setLoading(true);
      try {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(value)}&limit=5&addressdetails=1`
        );
        const data = await response.json();
        setSuggestions(data);
        setShowSuggestions(true);
      } catch (error) {
        console.error('Error buscando dirección:', error);
        setSuggestions([]);
      } finally {
        setLoading(false);
      }
    };

    const debounce = setTimeout(searchAddress, 300);
    return () => clearTimeout(debounce);
  }, [value]);

  // Obtener dirección desde coordenadas (reverse geocoding)
  const getAddressFromCoords = async (lat: number, lon: number) => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&addressdetails=1`
      );
      const data = await response.json();
      const address = data.display_name || `${lat.toFixed(6)}, ${lon.toFixed(6)}`;
      onChange(address, lat, lon);
      setSelectedPosition([lat, lon]);
    } catch (error) {
      console.error('Error obteniendo dirección:', error);
      onChange(`${lat.toFixed(6)}, ${lon.toFixed(6)}`, lat, lon);
      setSelectedPosition([lat, lon]);
    }
  };

  const handleSuggestionClick = (suggestion: AddressSuggestion) => {
    const lat = parseFloat(suggestion.lat);
    const lon = parseFloat(suggestion.lon);
    onChange(suggestion.display_name, lat, lon);
    setSelectedPosition([lat, lon]);
    setShowSuggestions(false);
    setSuggestions([]);
  };

  const handleMapClick = (lat: number, lon: number) => {
    getAddressFromCoords(lat, lon);
  };

  return (
    <div className="space-y-3">
      {label && (
        <label className="label">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      
      <div ref={wrapperRef} className="relative">
        <input
          ref={inputRef}
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          required={required}
          className="input"
        />
        
        {loading && (
          <div className="absolute right-3 top-1/2 -translate-y-1/2">
            <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-purple-600"></div>
          </div>
        )}

        {showSuggestions && suggestions.length > 0 && (
          <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-60 overflow-auto">
            {suggestions.map((suggestion, index) => (
              <button
                key={index}
                type="button"
                onClick={() => handleSuggestionClick(suggestion)}
                className="w-full px-4 py-3 text-left hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-b-0"
              >
                <div className="flex items-start gap-2">
                  <svg className="w-5 h-5 text-purple-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">
                      {suggestion.address.road && suggestion.address.house_number
                        ? `${suggestion.address.road} ${suggestion.address.house_number}`
                        : suggestion.address.road || suggestion.display_name.split(',')[0]}
                    </p>
                    <p className="text-xs text-gray-500 truncate">
                      {suggestion.display_name}
                    </p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>

      {showMap && (
        <div className="mt-4">
          <p className="text-sm text-gray-600 mb-2 flex items-center gap-2">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Haz click en el mapa para seleccionar una ubicación
          </p>
          <div className="rounded-lg overflow-hidden border border-gray-200 shadow-sm">
            <MapContainer
              center={selectedPosition || [4.5709, -74.2973]} // Bogotá por defecto
              zoom={selectedPosition ? 15 : 6}
              style={{ height: '300px', width: '100%' }}
              className="z-0"
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              />
              <MapClickHandler onLocationSelect={handleMapClick} />
              {selectedPosition && (
                <Marker position={selectedPosition} icon={customIcon} />
              )}
            </MapContainer>
          </div>
          {selectedPosition && (
            <p className="text-xs text-gray-500 mt-2 flex items-center gap-1">
              <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
              </svg>
              Coordenadas: {selectedPosition[0].toFixed(6)}, {selectedPosition[1].toFixed(6)}
            </p>
          )}
        </div>
      )}
    </div>
  );
}
