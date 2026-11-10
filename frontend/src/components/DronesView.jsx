export default function DronesView({ drones, onSelectDroneForMission }) {
  const droneList = Object.values(drones);

  const getStatusColor = (status) => {
    switch (status) {
      case "ACTIVE":
        return "#22c55e";
      case "STALE":
        return "#f59e0b";
      case "READY":
        return "#38bdf8";
      default:
        return "#ef4444";
    }
  };

  const getBatteryColor = (level) => {
    if (level > 60) return "#22c55e";
    if (level > 25) return "#f59e0b";
    return "#ef4444";
  };

  return (
    <div className="section-panel">
      <div className="section-header">
        <div>
          <h2>🛩️ Autonomous Drone Fleet</h2>
          <p>Real-time telemetry and operational readiness of active UAV assets</p>
        </div>
        <span className="drone-count-pill">{droneList.length} Drones Configured</span>
      </div>

      <div className="drone-cards-grid">
        {droneList.map((d) => (
          <div key={d.id} className="fleet-drone-card">
            <div className="fleet-card-top">
              <div className="drone-id-block">
                <span className="uav-icon">🛸</span>
                <div>
                  <h3>{d.id}</h3>
                  <span className="drone-model-text">AeroSaga Quad-Payload UAV</span>
                </div>
              </div>
              <span className="status-pill-badge" style={{ borderColor: getStatusColor(d.status), color: getStatusColor(d.status) }}>
                <span className="status-dot" style={{ background: getStatusColor(d.status) }}></span>
                {d.status}
              </span>
            </div>

            <div className="battery-gauge-section">
              <div className="gauge-header">
                <span>Power Supply (LiPo)</span>
                <strong>{(d.battery || 0).toFixed(1)}%</strong>
              </div>
              <div className="battery-track">
                <div
                  className="battery-fill"
                  style={{
                    width: `${Math.min(100, Math.max(0, d.battery || 0))}%`,
                    background: getBatteryColor(d.battery || 0),
                  }}
                ></div>
              </div>
            </div>

            <div className="fleet-telemetry-specs">
              <div className="spec-item">
                <span className="spec-label">Altitude</span>
                <span className="spec-val">{(d.altitude || 0).toFixed(1)} m</span>
              </div>
              <div className="spec-item">
                <span className="spec-label">Latitude</span>
                <span className="spec-val">{(d.latitude || 0).toFixed(5)}° N</span>
              </div>
              <div className="spec-item">
                <span className="spec-label">Longitude</span>
                <span className="spec-val">{(d.longitude || 0).toFixed(5)}° E</span>
              </div>
              <div className="spec-item">
                <span className="spec-label">Mission Task</span>
                <span className="spec-val" style={{ color: d.currentMission ? "#38bdf8" : "#94a3b8" }}>
                  {d.currentMission ? "ACTIVE" : "IDLE"}
                </span>
              </div>
            </div>

            <div className="fleet-card-actions">
              <button
                className="dispatch-drone-btn"
                onClick={() => onSelectDroneForMission(d.id)}
              >
                🎯 Dispatch on Mission
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
