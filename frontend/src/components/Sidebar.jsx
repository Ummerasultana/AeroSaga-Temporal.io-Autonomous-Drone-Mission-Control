export default function Sidebar({ activeTab, onSelectTab, alertsCount, connected }) {
  const navItems = [
    { id: "dashboard", label: "Dashboard", icon: "📊" },
    { id: "drones", label: "Fleet Assets", icon: "🛩️" },
    { id: "missions", label: "Mission Control", icon: "🎯" },
    { id: "map", label: "Tactical Radar", icon: "🛰️" },
    { id: "history", label: "Mission History", icon: "📜" },
    { id: "alerts", label: "Alerts", icon: "🚨", badge: alertsCount },
  ];

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-icon">✈</div>
        <div>
          <h2>AeroSaga</h2>
          <span>Temporal Drone Control</span>
        </div>
      </div>

      <nav>
        {navItems.map((item) => (
          <div
            key={item.id}
            className={`nav-item ${activeTab === item.id ? "active" : ""}`}
            onClick={() => onSelectTab(item.id)}
          >
            <span className="nav-icon">{item.icon}</span>
            <span className="nav-label">{item.label}</span>
            {item.badge > 0 && <span className="nav-badge">{item.badge}</span>}
          </div>
        ))}
      </nav>

      <div className="system-status">
        <span
          className="status-dot"
          style={{ background: connected ? "#22c55e" : "#ef4444" }}
        ></span>
        <span>{connected ? "Gateway Online" : "Gateway Offline"}</span>
      </div>
    </aside>
  );
}
