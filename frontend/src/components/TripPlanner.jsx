import React, { useState } from 'react';
import { Route, Navigation, BatteryCharging, AlertCircle, Compass } from 'lucide-react';

const MOCK_ROUTES = {
  'bypass-railway': {
    name: 'Nellore Bypass to Railway Station',
    distance: '4.3 km',
    estBattery: '6%',
    points: [
      [14.4605, 79.9592], // Nellore Bypass
      [14.4560, 79.9680], // Ramesh Hospital
      [14.4510, 79.9750], // Grand Trunk Road
      [14.4468, 79.9811]  // Nellore Railway Station
    ],
    recommendations: ['station-1', 'station-2']
  },
  'dargamitta-vrc': {
    name: 'Dargamitta to VRC Centre',
    distance: '2.5 km',
    estBattery: '4%',
    points: [
      [14.4324, 79.9721], // Dargamitta
      [14.4360, 79.9760], // Magunta Layout
      [14.4400, 79.9810], // Children's Park
      [14.4452, 79.9868]  // VRC Centre
    ],
    recommendations: ['station-3', 'station-4']
  },
  'podalakur-haranathapuram': {
    name: 'Podalakur Road to Haranathapuram',
    distance: '4.5 km',
    estBattery: '7%',
    points: [
      [14.4285, 79.9605], // Podalakur Road
      [14.4350, 79.9650], // Kasturiba Kalasala
      [14.4450, 79.9700], // Dargamitta Junction
      [14.4552, 79.9754]  // Haranathapuram
    ],
    recommendations: ['station-5', 'station-7']
  }
};

export default function TripPlanner({ onRouteSelect, onHighlightStations, clearRoute, selectedVehicle }) {
  const [selectedRouteId, setSelectedRouteId] = useState('');
  const [currentBattery, setCurrentBattery] = useState(45);

  const handleRouteClick = (routeId) => {
    if (selectedRouteId === routeId) {
      // Toggle off
      setSelectedRouteId('');
      clearRoute();
      onHighlightStations(null);
      return;
    }

    setSelectedRouteId(routeId);
    const route = MOCK_ROUTES[routeId];
    onRouteSelect(route.points);
    onHighlightStations(route.recommendations);
  };

  const selectedRoute = MOCK_ROUTES[selectedRouteId];
  const estBatteryUsed = selectedRoute && selectedVehicle
    ? Math.round((parseFloat(selectedRoute.distance) / selectedVehicle.range) * 100)
    : 0;
  const postTripBattery = selectedRoute 
    ? currentBattery - estBatteryUsed
    : currentBattery;

  return (
    <div style={{ padding: '4px 0' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
        <Route size={18} style={{ color: 'var(--color-secondary)' }} />
        <h3 style={{ fontSize: '15px', fontWeight: '800', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          EV Route Planner
        </h3>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
          <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Current Vehicle Charge (SOC)</span>
          <span style={{ fontSize: '12px', fontWeight: '700', color: currentBattery < 20 ? 'var(--color-danger)' : 'var(--color-primary)' }}>
            {currentBattery}%
          </span>
        </div>
        <input 
          type="range" 
          min="5" 
          max="100" 
          value={currentBattery}
          onChange={(e) => setCurrentBattery(parseInt(e.target.value))}
          style={{
            width: '100%',
            height: '4px',
            borderRadius: '2px',
            outline: 'none',
            WebkitAppearance: 'none',
            background: 'rgba(255,255,255,0.1)',
            cursor: 'pointer'
          }}
        />
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {Object.entries(MOCK_ROUTES).map(([id, r]) => {
          const isActive = selectedRouteId === id;
          return (
            <div 
              key={id}
              onClick={() => handleRouteClick(id)}
              className="glass-panel"
              style={{
                padding: '14px',
                cursor: 'pointer',
                borderColor: isActive ? 'var(--color-secondary)' : 'var(--border-color)',
                backgroundColor: isActive ? 'rgba(0, 176, 255, 0.04)' : 'var(--bg-card)',
                transition: 'var(--transition-smooth)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <h4 style={{ fontSize: '13px', fontWeight: '700', maxWidth: '80%', color: isActive ? '#fff' : 'var(--text-primary)' }}>
                  {r.name}
                </h4>
                <Navigation 
                  size={14} 
                  style={{ 
                    color: isActive ? 'var(--color-secondary)' : 'var(--text-muted)',
                    transform: isActive ? 'rotate(45deg)' : 'none',
                    transition: 'transform 0.3s'
                  }} 
                />
              </div>

              <div style={{ display: 'flex', gap: '14px', fontSize: '11px', color: 'var(--text-secondary)' }}>
                <span>Distance: <strong>{r.distance}</strong></span>
                <span>Est. Use: <strong>{selectedVehicle ? Math.round((parseFloat(r.distance) / selectedVehicle.range) * 100) : 10}%</strong></span>
              </div>

              {isActive && (
                <div style={{
                  marginTop: '12px',
                  paddingTop: '10px',
                  borderTop: '1px solid rgba(255,255,255,0.05)',
                  animation: 'fadeIn 0.2s'
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                    <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>SOC on Arrival:</span>
                    <span style={{ 
                      fontSize: '11px', 
                      fontWeight: '700', 
                      color: postTripBattery < 0 ? 'var(--color-danger)' : postTripBattery < 15 ? 'var(--color-warning)' : 'var(--color-primary)' 
                    }}>
                      {postTripBattery}%
                    </span>
                  </div>

                  {postTripBattery < 0 ? (
                    <div style={{ 
                      display: 'flex', 
                      gap: '6px', 
                      backgroundColor: 'rgba(255, 23, 68, 0.08)',
                      padding: '8px',
                      borderRadius: '6px',
                      color: 'var(--color-danger)',
                      fontSize: '10px',
                      lineHeight: 1.3,
                      border: '1px solid rgba(255, 23, 68, 0.2)'
                    }}>
                      <AlertCircle size={14} style={{ flexShrink: 0 }} />
                      <span>Critical Warning: Insufficient charge to reach destination! Plan immediate stop.</span>
                    </div>
                  ) : postTripBattery < 15 ? (
                    <div style={{ 
                      display: 'flex', 
                      gap: '6px', 
                      backgroundColor: 'rgba(255, 145, 0, 0.08)',
                      padding: '8px',
                      borderRadius: '6px',
                      color: 'var(--color-warning)',
                      fontSize: '10px',
                      lineHeight: 1.3,
                      border: '1px solid rgba(255, 145, 0, 0.2)'
                    }}>
                      <AlertCircle size={14} style={{ flexShrink: 0 }} />
                      <span>Warning: Low Battery on arrival! Recharge stops recommended along this route.</span>
                    </div>
                  ) : (
                    <div style={{ 
                      display: 'flex', 
                      gap: '6px', 
                      backgroundColor: 'rgba(0, 230, 118, 0.08)',
                      padding: '8px',
                      borderRadius: '6px',
                      color: 'var(--color-primary)',
                      fontSize: '10px',
                      lineHeight: 1.3,
                      border: '1px solid rgba(0, 230, 118, 0.2)'
                    }}>
                      <BatteryCharging size={14} style={{ flexShrink: 0 }} />
                      <span>Safe Range. Stations nearby highlighted on map. Click details to reserve.</span>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      <style dangerouslySetInnerHTML={{__html: `
        input[type=range]::-webkit-slider-thumb {
          -webkit-appearance: none;
          height: 14px;
          width: 14px;
          border-radius: 50%;
          background: var(--color-primary);
          box-shadow: 0 0 8px var(--color-primary-glow);
          cursor: pointer;
        }
      `}} />
    </div>
  );
}
