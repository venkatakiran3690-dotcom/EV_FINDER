import React, { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Locate } from 'lucide-react';

export default function LeafletMap({ stations, selectedStation, onSelectStation, routePoints, onRouteSelect, userLocation, setUserLocation, theme }) {
  const mapContainerRef = useRef(null);
  const mapRef = useRef(null);
  const markersRef = useRef({});
  const routeLineRef = useRef(null);
  const userMarkerRef = useRef(null);
  const tileLayerRef = useRef(null);
  const [mapInstance, setMapInstance] = useState(null);

  // Initialize Map
  useEffect(() => {
    if (!mapContainerRef.current) return;

    // Centered around Nellore, Andhra Pradesh
    const map = L.map(mapContainerRef.current, {
      zoomControl: false,
      attributionControl: false
    }).setView([14.4426, 79.9865], 13);

    // Theme-compatible tiles
    const tileUrl = theme === 'dark'
      ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
      : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';

    const tiles = L.tileLayer(tileUrl, {
      maxZoom: 19
    }).addTo(map);

    tileLayerRef.current = tiles;

    // Position Zoom Controls in bottom-right
    L.control.zoom({
      position: 'bottomright'
    }).addTo(map);

    // User Locator Pin (Pulsing Blue GPS Dot)
    const initialUserLoc = userLocation || [14.4426, 79.9865];
    const userMarkerHtml = `
      <div class="user-locator-marker" style="width: 20px; height: 20px; border-radius: 50%; display: flex; align-items: center; justify-content: center; position: relative;">
        <div class="user-locator-pulse" style="position: absolute; width: 32px; height: 32px; border-radius: 50%; background-color: rgba(0, 180, 216, 0.4); animation: pulse-gps 1.8s infinite ease-out; pointer-events: none;"></div>
        <div style="width: 12px; height: 12px; border-radius: 50%; background-color: var(--color-primary); border: 2px solid #fff; box-shadow: 0 0 10px var(--color-primary-glow); z-index: 10;"></div>
      </div>
    `;

    const userLocIcon = L.divIcon({
      className: 'user-marker-icon',
      html: userMarkerHtml,
      iconSize: [32, 32],
      iconAnchor: [16, 16]
    });

    const userMarker = L.marker(initialUserLoc, { 
      icon: userLocIcon,
      draggable: true
    }).addTo(map);
    
    userMarker.bindPopup('<strong style="color:var(--color-primary)">You are here</strong><br/><span style="font-size:10px;color:var(--text-secondary)">Drag pin or click map to move</span>');
    
    userMarker.on('dragend', (e) => {
      const position = e.target.getLatLng();
      setUserLocation([position.lat, position.lng]);
    });

    userMarkerRef.current = userMarker;

    // Click map to set/move user location pin
    map.on('click', (e) => {
      if (e.originalEvent.target.closest('.leaflet-marker-icon') || e.originalEvent.target.closest('.leaflet-popup')) {
        return;
      }
      const { lat, lng } = e.latlng;
      setUserLocation([lat, lng]);
    });

    mapRef.current = map;
    setMapInstance(map);

    return () => {
      map.off('click');
      map.remove();
      mapRef.current = null;
      userMarkerRef.current = null;
      tileLayerRef.current = null;
      setMapInstance(null);
    };
  }, []);

  // Sync Map Tiles with Theme
  useEffect(() => {
    if (tileLayerRef.current) {
      const tileUrl = theme === 'dark'
        ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
        : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png';
      tileLayerRef.current.setUrl(tileUrl);
    }
  }, [theme]);

  // Sync User Marker with userLocation prop
  useEffect(() => {
    if (!mapInstance || !userLocation) return;

    if (userMarkerRef.current) {
      userMarkerRef.current.setLatLng(userLocation);
    }
  }, [mapInstance, userLocation]);

  // Sync Markers with Stations data
  useEffect(() => {
    if (!mapInstance) return;
    const map = mapInstance;

    // Remove obsolete markers
    Object.keys(markersRef.current).forEach(id => {
      if (!stations.some(s => s.id === id)) {
        markersRef.current[id].remove();
        delete markersRef.current[id];
      }
    });

    // Add or update markers
    stations.forEach(station => {
      try {
        const latVal = parseFloat(station.lat);
        const lngVal = parseFloat(station.lng);
        if (!station || isNaN(latVal) || isNaN(lngVal)) {
          console.warn(`Station ${station?.name || 'Unknown'} has invalid coordinates: lat=${station?.lat}, lng=${station?.lng}`);
          return;
        }

        const getMarkerColorClass = (status) => {
          if (status === 'Available') return 'marker-available';
          if (status === 'In Use') return 'marker-in-use';
          return 'marker-offline';
        };

        const markerHtml = `
          <div class="custom-marker-icon" style="width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 1.5px solid rgba(255,255,255,0.4); box-shadow: 0 0 10px rgba(0,0,0,0.6);">
            <div class="marker-inner ${getMarkerColorClass(station.status)}" style="width: 100%; height: 100%; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor" stroke="none"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
            </div>
          </div>
        `;

        const customIcon = L.divIcon({
          className: 'ev-marker-icon',
          html: markerHtml,
          iconSize: [28, 28],
          iconAnchor: [14, 14],
          popupAnchor: [0, -14]
        });

        const isOpen = (() => {
          const hoursStr = station.openingHours || '24/7';
          if (hoursStr === '24/7' || hoursStr === '24 Hours') return true;
          try {
            const parts = hoursStr.split('-');
            const [startHour, startMin] = parts[0].trim().split(':').map(Number);
            const [endHour, endMin] = parts[1].trim().split(':').map(Number);
            const now = new Date();
            const currentTotalMin = now.getHours() * 60 + now.getMinutes();
            const startTotal = startHour * 60 + startMin;
            const endTotal = endHour * 60 + endMin;
            return startTotal <= endTotal 
              ? (currentTotalMin >= startTotal && currentTotalMin <= endTotal)
              : (currentTotalMin >= startTotal || currentTotalMin <= endTotal);
          } catch (e) { return true; }
        })();

        const maxPower = (station.connectors && station.connectors.length > 0)
          ? Math.max(...station.connectors.map(c => c.power || 0))
          : 0;

        const popupContent = `
          <div style="font-family: 'Plus Jakarta Sans', sans-serif; color: var(--text-primary); width: 220px;">
            <h4 style="margin: 0 0 4px 0; font-size: 13px; font-weight: 700; color: var(--color-primary);">${station.name}</h4>
            <p style="margin: 0 0 6px 0; font-size: 11px; color: var(--text-secondary);">${station.address}</p>
            <div style="font-size: 10px; color: var(--text-secondary); margin-bottom: 4px;">
              Speed: <strong>${maxPower} kW</strong>
            </div>
            <div style="font-size: 10px; color: var(--text-secondary); margin-bottom: 8px; display: flex; align-items: center; gap: 4px;">
              Hours: <strong>${station.openingHours || '24/7'}</strong> 
              <span style="padding: 1px 4px; border-radius: 3px; font-weight: 800; font-size: 8px; color: #fff; background: ${isOpen ? '#00c853' : '#d50000'}">
                ${isOpen ? 'OPEN' : 'CLOSED'}
              </span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px; border-top: 1px dashed rgba(255, 179, 0, 0.15); padding-top: 8px;">
              <span style="font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px; background: rgba(0,0,0,0.05); color: ${
                station.status === 'Available' ? '#00c853' : station.status === 'In Use' ? '#ff6f00' : '#d50000'
              }">${station.status}</span>
              <div style="display: flex; gap: 4px;">
                <button id="btn-navigate-${station.id}" style="background: rgba(255, 179, 0, 0.08); border: 1px solid var(--color-primary); border-radius: 6px; color: var(--color-primary); font-size: 10px; font-weight: 700; padding: 5px 8px; cursor: pointer; display: flex; align-items: center; gap: 2px;">
                  <span style="transform: rotate(45deg); display: inline-block;">➔</span> Nav
                </button>
                <button id="btn-view-${station.id}" style="background: linear-gradient(135deg, var(--color-primary), var(--color-secondary)); border: none; border-radius: 6px; color: #fff; font-size: 10px; font-weight: 700; padding: 5px 10px; cursor: pointer;">Details</button>
              </div>
            </div>
          </div>
        `;

        if (markersRef.current[station.id]) {
          // Update existing marker
          const existingMarker = markersRef.current[station.id];
          existingMarker.setLatLng([latVal, lngVal]);
          existingMarker.setIcon(customIcon);
          existingMarker.setPopupContent(popupContent);
        } else {
          // Create new marker
          const marker = L.marker([latVal, lngVal], { icon: customIcon }).addTo(map);
          marker.bindPopup(popupContent);
          markersRef.current[station.id] = marker;
        }
      } catch (err) {
        console.error("Error drawing station marker:", err, station);
      }
    });
  }, [mapInstance, stations]);

  // Handle popup clicks dynamically (avoids stale closures for userLocation, onRouteSelect, onSelectStation)
  useEffect(() => {
    if (!mapInstance) return;
    const map = mapInstance;

    const handlePopupOpen = (e) => {
      stations.forEach(station => {
        const btnView = document.getElementById(`btn-view-${station.id}`);
        if (btnView) {
          btnView.onclick = (event) => {
            event.preventDefault();
            onSelectStation(station);
          };
        }

        const btnNav = document.getElementById(`btn-navigate-${station.id}`);
        if (btnNav) {
          btnNav.onclick = (event) => {
            event.preventDefault();
            if (userLocation && onRouteSelect) {
              const latVal = parseFloat(station.lat);
              const lngVal = parseFloat(station.lng);
              if (!isNaN(latVal) && !isNaN(lngVal)) {
                onRouteSelect([
                  [userLocation[0], userLocation[1]],
                  [latVal, lngVal]
                ]);
                onSelectStation(station);
              }
            }
          };
        }
      });
    };

    map.on('popupopen', handlePopupOpen);
    return () => {
      map.off('popupopen', handlePopupOpen);
    };
  }, [mapInstance, stations, userLocation, onRouteSelect, onSelectStation]);

  // Center on Selected Station
  useEffect(() => {
    if (!mapInstance || !selectedStation) return;
    const map = mapInstance;

    const latVal = parseFloat(selectedStation.lat);
    const lngVal = parseFloat(selectedStation.lng);
    if (isNaN(latVal) || isNaN(lngVal)) return;

    map.setView([latVal, lngVal], 15, {
      animate: true,
      duration: 1.0
    });

    // Automatically open popup
    const marker = markersRef.current[selectedStation.id];
    if (marker) {
      marker.openPopup();
    }
  }, [mapInstance, selectedStation]);

  // Render Trip Route Path overlay
  useEffect(() => {
    if (!mapInstance) return;
    const map = mapInstance;

    // Remove existing line
    if (routeLineRef.current) {
      routeLineRef.current.remove();
      routeLineRef.current = null;
    }

    if (routePoints && routePoints.length > 0) {
      // Draw new path line
      const pathLine = L.polyline(routePoints, {
        color: 'var(--color-primary)',
        weight: 5,
        opacity: 0.8,
        dashArray: '5, 10',
        lineCap: 'round',
        lineJoin: 'round'
      }).addTo(map);

      // Pulse style for routes using CSS
      pathLine.getElement().style.animation = 'dash 30s linear infinite';
      
      routeLineRef.current = pathLine;

      // Fit map bounds to show full route
      const bounds = L.latLngBounds(routePoints);
      map.fitBounds(bounds, { padding: [50, 50] });
    }
  }, [mapInstance, routePoints]);

  const handleLocateUser = () => {
    const map = mapRef.current;
    if (!map) return;

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          setUserLocation([latitude, longitude]);
          map.setView([latitude, longitude], 14, {
            animate: true,
            duration: 1.0
          });
          setTimeout(() => {
            if (userMarkerRef.current) {
              userMarkerRef.current.openPopup();
            }
          }, 1000);
        },
        (error) => {
          console.warn("Geolocation denied/failed, falling back to Nellore center:", error);
          const defaultLoc = [14.4426, 79.9865];
          setUserLocation(defaultLoc);
          map.setView(defaultLoc, 14, {
            animate: true,
            duration: 1.0
          });
          setTimeout(() => {
            if (userMarkerRef.current) {
              userMarkerRef.current.openPopup();
            }
          }, 1000);
        }
      );
    } else {
      const defaultLoc = [14.4426, 79.9865];
      setUserLocation(defaultLoc);
      map.setView(defaultLoc, 14, {
        animate: true,
        duration: 1.0
      });
      setTimeout(() => {
        if (userMarkerRef.current) {
          userMarkerRef.current.openPopup();
        }
      }, 1000);
    }
  };

  return (
    <div style={{ position: 'relative', width: '100%', height: '100%' }}>
      <div ref={mapContainerRef} style={{ width: '100%', height: '100%', outline: 'none' }} />
      
      {/* Recenter / Locate Me Button overlay */}
      <button 
        onClick={handleLocateUser}
        style={{
          position: 'absolute',
          bottom: '80px',
          right: '10px',
          width: '34px',
          height: '34px',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 500,
          background: 'var(--bg-card)',
          border: '1.5px solid var(--border-color)',
          color: 'var(--color-primary)',
          cursor: 'pointer',
          boxShadow: 'var(--glass-shadow)',
          transition: 'var(--transition-smooth)'
        }}
        className="hover-scale"
        title="Find My Location"
      >
        <Locate size={16} />
      </button>

      {/* Dynamic Watermark Indicator overlay */}
      <div style={{
        position: 'absolute',
        top: '20px',
        right: '20px',
        padding: '10px 16px',
        zIndex: 500,
        pointerEvents: 'none'
      }} className="glass-panel">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{
            display: 'inline-block',
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: '#00e676',
            boxShadow: '0 0 8px #00e676'
          }} />
          <span style={{ fontSize: '12px', fontWeight: '600', color: 'var(--text-secondary)' }}>Live Sim Connected</span>
        </div>
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        @keyframes dash {
          to {
            stroke-dashoffset: -1000;
          }
        }
        @keyframes pulse-gps {
          0% {
            transform: scale(0.6);
            opacity: 1;
          }
          100% {
            transform: scale(1.6);
            opacity: 0;
          }
        }
        .ev-marker-icon {
          background: transparent !important;
          border: none !important;
        }
        .user-marker-icon {
          background: transparent !important;
          border: none !important;
        }
      `}} />
    </div>
  );
}
