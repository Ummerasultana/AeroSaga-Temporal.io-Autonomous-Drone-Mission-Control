export default function AlertsView({ alerts, onDismissAlert, onClearAll }) {
  const getAlertIcon = (type) => {
    switch (type) {
      case "error":
        return "🚨";
      case "warning":
        return "⚠️";
      case "success":
        return "✅";
      default:
        return "ℹ️";
    }
  };

  return (
    <div className="section-panel">
      <div className="section-header">
        <div>
          <h2>🚨 System Alerts & Event Log</h2>
          <p>Real-time notifications, telemetry warnings, and workflow audit events</p>
        </div>
        {alerts.length > 0 && (
          <button className="secondary-button" onClick={onClearAll}>
            Clear All Alerts
          </button>
        )}
      </div>

      {alerts.length === 0 ? (
        <div className="empty-panel">
          <div className="empty-icon">🛡️</div>
          <h3>All Systems Nominal</h3>
          <p>No active anomalies or warning flags reported across the drone fleet or orchestrator.</p>
        </div>
      ) : (
        <div className="alerts-list">
          {alerts.map((alert) => (
            <div key={alert.id} className={`alert-card alert-${alert.type}`}>
              <div className="alert-icon-col">
                <span>{getAlertIcon(alert.type)}</span>
              </div>
              <div className="alert-content-col">
                <div className="alert-title-row">
                  <strong>{alert.title}</strong>
                  <span className="alert-time">
                    {new Date(alert.timestamp).toLocaleTimeString()}
                  </span>
                </div>
                <p className="alert-msg">{alert.message}</p>
              </div>
              <button
                className="dismiss-alert-btn"
                onClick={() => onDismissAlert(alert.id)}
                title="Dismiss Alert"
              >
                ✕
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
