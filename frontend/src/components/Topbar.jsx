export default function Topbar({ connected, temporalStatus, activeTab }) {
  const getTabTitle = () => {
    switch (activeTab) {
      case "dashboard":
        return { title: "Mission Control Dashboard", desc: "Real-time UAV fleet command and Temporal.io workflow orchestration" };
      case "drones":
        return { title: "Drone Fleet Inventory", desc: "Live GPS telemetry, battery levels, and telemetry connection states" };
      case "missions":
        return { title: "Mission Operations", desc: "Dispatch single or multi-drone sorties and monitor execution progress" };
      case "map":
        return { title: "Tactical Fleet Radar", desc: "Spatial radar visualization of live drone GPS coordinates" };
      case "history":
        return { title: "Mission History", desc: "Audit records of past autonomous drone delivery missions" };
      case "alerts":
        return { title: "System Alerts & Warnings", desc: "Fleet anomalies, telemetry disruptions, and mission event logs" };
      default:
        return { title: "AeroSaga Mission Control", desc: "Autonomous Drone Mission Control" };
    }
  };

  const { title, desc } = getTabTitle();

  return (
    <header className="topbar">
      <div>
        <h1>{title}</h1>
        <p>{desc}</p>
      </div>

      <div className="topbar-status-group">
        <div className="connection-badge">
          <span
            className="status-dot"
            style={{ background: connected ? "#22c55e" : "#ef4444" }}
          ></span>
          <span>{connected ? "Telemetry: Online" : "Telemetry: Offline"}</span>
        </div>

        <div className="connection-badge">
          <span
            className="status-dot"
            style={{ background: temporalStatus === "CONNECTED" ? "#38bdf8" : "#f59e0b" }}
          ></span>
          <span>Temporal: {temporalStatus}</span>
        </div>
      </div>
    </header>
  );
}
