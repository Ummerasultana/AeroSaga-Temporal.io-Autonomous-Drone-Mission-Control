import { useState } from "react";

export default function MissionTracker({ activeMission, onAbort, isAborting }) {
  const [copied, setCopied] = useState(false);

  if (!activeMission) {
    return (
      <div className="mission-tracker-empty">
        <div className="empty-icon">🎯</div>
        <h3>No Active Mission Running</h3>
        <p>Select one or more drones below and click <strong>Start Mission</strong> to launch an autonomous Temporal workflow.</p>
      </div>
    );
  }

  const {
    workflowId,
    droneIds = [],
    status = "PENDING",
    droneStatuses = {},
    progressPercent = 10,
    startTime,
    endTime,
  } = activeMission;

  const isTerminal = ["COMPLETED", "FAILED", "ABORTED"].includes(status);

  const copyWorkflowId = () => {
    navigator.clipboard.writeText(workflowId);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const stages = [
    { key: "TAKING_OFF", label: "Takeoff", icon: "🛫" },
    { key: "NAVIGATING", label: "Navigation", icon: "🛰️" },
    { key: "DELIVERING", label: "Delivery", icon: "📦" },
    { key: "RETURNING", label: "Return to Base", icon: "🏁" },
  ];

  const getStageState = (stageKey) => {
    const order = ["PENDING", "TAKING_OFF", "NAVIGATING", "DELIVERING", "RETURNING", "COMPLETED"];
    const currentIndex = order.indexOf(status);
    const stageIndex = order.indexOf(stageKey);

    if (status === "ABORTING" || status === "ABORTED") {
      if (stageKey === "RETURNING") return "active";
      if (stageIndex < order.indexOf("RETURNING")) return "aborted";
      return "upcoming";
    }

    if (status === "FAILED") {
      if (stageKey === "DELIVERING") return "failed";
      if (stageKey === "RETURNING") return "completed";
      return "failed";
    }

    if (currentIndex > stageIndex) return "completed";
    if (currentIndex === stageIndex) return "active";
    return "upcoming";
  };

  const getStatusBadgeClass = () => {
    switch (status) {
      case "COMPLETED":
        return "badge-completed";
      case "FAILED":
        return "badge-failed";
      case "ABORTING":
      case "ABORTED":
        return "badge-aborted";
      default:
        return "badge-running";
    }
  };

  return (
    <div className="mission-tracker-card">
      <div className="tracker-header">
        <div>
          <div className="tracker-title-row">
            <span className="workflow-tag">TEMPORAL WORKFLOW</span>
            <span className={`status-badge ${getStatusBadgeClass()}`}>
              <span className="pulse-indicator"></span>
              {status}
            </span>
          </div>
          <div className="workflow-id-row">
            <code className="workflow-id-code">{workflowId}</code>
            <button className="copy-btn" onClick={copyWorkflowId} title="Copy Workflow ID">
              {copied ? "✓ Copied" : "📋 Copy ID"}
            </button>
          </div>
        </div>

        {!isTerminal && (
          <button
            className="abort-mission-btn"
            onClick={() => onAbort(workflowId)}
            disabled={isAborting || status === "ABORTING"}
          >
            {isAborting || status === "ABORTING" ? "⏳ Aborting..." : "🛑 Abort Mission"}
          </button>
        )}
      </div>

      <div className="tracker-drones-assigned">
        <span>Assigned Drone(s):</span>
        <div className="drone-pills">
          {droneIds.map((id) => (
            <span key={id} className="drone-pill">
              🛩️ {id} {droneStatuses[id] ? `(${droneStatuses[id]})` : ""}
            </span>
          ))}
        </div>
      </div>

      {/* Progress Bar */}
      <div className="progress-bar-container">
        <div className="progress-bar-track">
          <div
            className={`progress-bar-fill ${status === "ABORTED" || status === "FAILED" ? "fill-warn" : ""}`}
            style={{ width: `${progressPercent}%` }}
          ></div>
        </div>
        <span className="progress-percent-label">{progressPercent}%</span>
      </div>

      {/* Step by Step Tracker */}
      <div className="stepper-grid">
        {stages.map((stg) => {
          const state = getStageState(stg.key);
          return (
            <div key={stg.key} className={`stepper-step step-${state}`}>
              <div className="step-icon-wrapper">
                <span className="step-icon">{stg.icon}</span>
                <span className="step-status-marker">
                  {state === "completed" && "✓"}
                  {state === "active" && "●"}
                  {state === "upcoming" && "○"}
                  {state === "aborted" && "⚠"}
                  {state === "failed" && "✗"}
                </span>
              </div>
              <span className="step-label">{stg.label}</span>
              <span className="step-sub">{state.toUpperCase()}</span>
            </div>
          );
        })}
      </div>

      <div className="tracker-footer">
        <span>
          Started: {startTime ? new Date(startTime).toLocaleTimeString() : "--"}
        </span>
        {endTime && (
          <span>
            Ended: {new Date(endTime).toLocaleTimeString()} (Duration: {((endTime - startTime) / 1000).toFixed(1)}s)
          </span>
        )}
        <span className="engine-label">Orchestrator: Temporal.io Task Queue <code>DRONE_TASK_QUEUE</code></span>
      </div>
    </div>
  );
}
