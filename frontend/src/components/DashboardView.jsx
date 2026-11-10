import MissionTracker from "./MissionTracker";
import TacticalMapView from "./TacticalMapView";

export default function DashboardView({
  drones,
  activeMission,
  onAbortMission,
  isAborting,
  history,
  alerts,
  onNavigateTab,
}) {
  const droneList = Object.values(drones);
  const activeCount = droneList.filter((d) => d.status === "ACTIVE").length;
  const activeMissionsCount = activeMission && !["COMPLETED", "FAILED", "ABORTED"].includes(activeMission.status) ? 1 : 0;

  return (
    <div className="dashboard-view-content">
      {/* 4 Stat Cards */}
      <section className="stats-row">
        <div className="stat-card">
          <div className="stat-icon-wrapper blue">🛩️</div>
          <div>
            <span className="stat-label">Total Fleet UAVs</span>
            <strong className="stat-value">{droneList.length}</strong>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper green">📡</div>
          <div>
            <span className="stat-label">Active Transmitting</span>
            <strong className="stat-value">{activeCount}</strong>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper purple">🎯</div>
          <div>
            <span className="stat-label">Active Missions</span>
            <strong className="stat-value">{activeMissionsCount}</strong>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper amber">📜</div>
          <div>
            <span className="stat-label">Mission History</span>
            <strong className="stat-value">{history.length}</strong>
          </div>
        </div>
      </section>

      {/* Active Mission Banner / Tracker */}
      <section className="section-block">
        <div className="section-header">
          <h2>🎯 Active Mission Orchestration</h2>
          <button className="link-action-btn" onClick={() => onNavigateTab("missions")}>
            Mission Control Center →
          </button>
        </div>
        <MissionTracker
          activeMission={activeMission}
          onAbort={onAbortMission}
          isAborting={isAborting}
        />
      </section>

      {/* Split: Tactical Radar Map + Fleet Overview */}
      <div className="dashboard-grid-two">
        <section className="section-block">
          <TacticalMapView drones={drones} activeMission={activeMission} />
        </section>

        <section className="section-block">
          <div className="section-header">
            <h2>🛸 UAV Fleet Status</h2>
            <button className="link-action-btn" onClick={() => onNavigateTab("drones")}>
              View All Assets →
            </button>
          </div>

          <div className="mini-drone-list">
            {droneList.map((d) => (
              <div key={d.id} className="mini-drone-card">
                <div className="mini-drone-top">
                  <strong>{d.id}</strong>
                  <span className={`status-pill ${d.status === "ACTIVE" ? "pill-active" : "pill-idle"}`}>
                    {d.status}
                  </span>
                </div>
                <div className="mini-telemetry-row">
                  <span>🔋 {(d.battery || 0).toFixed(1)}%</span>
                  <span>⛰️ {(d.altitude || 0).toFixed(0)}m</span>
                  <span>GPS: {(d.latitude || 0).toFixed(4)}, {(d.longitude || 0).toFixed(4)}</span>
                </div>
              </div>
            ))}
          </div>

          {/* Quick Alert Preview */}
          <div className="dashboard-alerts-preview">
            <div className="preview-header">
              <span>Recent Alerts ({alerts.length})</span>
              <button className="link-action-btn" onClick={() => onNavigateTab("alerts")}>
                View All →
              </button>
            </div>
            {alerts.length === 0 ? (
              <p className="no-alert-msg">No alerts logged. All systems normal.</p>
            ) : (
              <ul className="preview-alerts-list">
                {alerts.slice(0, 2).map((a) => (
                  <li key={a.id} className={`preview-alert alert-${a.type}`}>
                    <span>{a.title}: {a.message}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
