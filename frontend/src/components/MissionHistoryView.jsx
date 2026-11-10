import { useState } from "react";

export default function MissionHistoryView({ history, onRefresh, isLoading }) {
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");

  const filteredHistory = history.filter((item) => {
    if (filter !== "ALL" && item.status !== filter) return false;
    if (search.trim()) {
      const q = search.toLowerCase();
      const matchWf = item.workflowId.toLowerCase().includes(q);
      const matchDrone = item.droneIds && item.droneIds.some((d) => d.toLowerCase().includes(q));
      return matchWf || matchDrone;
    }
    return true;
  });

  const getStatusClass = (status) => {
    switch (status) {
      case "COMPLETED":
        return "badge-completed";
      case "ABORTED":
      case "ABORTING":
        return "badge-aborted";
      case "FAILED":
        return "badge-failed";
      default:
        return "badge-running";
    }
  };

  return (
    <div className="section-panel">
      <div className="section-header">
        <div>
          <h2>📜 Mission Execution History</h2>
          <p>Durable workflow execution logs retrieved from Temporal.io</p>
        </div>
        <button className="secondary-button" onClick={onRefresh} disabled={isLoading}>
          {isLoading ? "Refreshing..." : "🔄 Refresh History"}
        </button>
      </div>

      <div className="history-toolbar">
        <div className="filter-group">
          {["ALL", "COMPLETED", "ABORTED", "FAILED"].map((cat) => (
            <button
              key={cat}
              className={`filter-btn ${filter === cat ? "active" : ""}`}
              onClick={() => setFilter(cat)}
            >
              {cat}
            </button>
          ))}
        </div>
        <div className="search-box">
          <input
            type="text"
            placeholder="Search by workflow or drone ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {filteredHistory.length === 0 ? (
        <div className="empty-panel">
          <p>No missions match the selected filter.</p>
        </div>
      ) : (
        <div className="table-responsive">
          <table className="mission-table">
            <thead>
              <tr>
                <th>Workflow ID</th>
                <th>Drones</th>
                <th>Status</th>
                <th>Start Time</th>
                <th>Duration</th>
                <th>Result / Notes</th>
              </tr>
            </thead>
            <tbody>
              {filteredHistory.map((m) => {
                const durationSec = m.durationMs ? (m.durationMs / 1000).toFixed(1) + "s" : "--";
                return (
                  <tr key={m.workflowId}>
                    <td>
                      <code className="table-workflow-id">{m.workflowId}</code>
                    </td>
                    <td>
                      <div className="table-drone-pills">
                        {m.droneIds && m.droneIds.map((d) => (
                          <span key={d} className="table-pill">{d}</span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <span className={`status-badge ${getStatusClass(m.status)}`}>
                        {m.status}
                      </span>
                    </td>
                    <td>{new Date(m.startTime).toLocaleString()}</td>
                    <td>{durationSec}</td>
                    <td>
                      <span className="result-text">{m.result || (m.status === "ABORTED" ? "Aborted by Operator" : m.status)}</span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
