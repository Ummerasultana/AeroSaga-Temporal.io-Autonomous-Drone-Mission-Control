import { useState } from "react";
import MissionTracker from "./MissionTracker";

export default function MissionControlView({
  drones,
  activeMission,
  onStartMission,
  onAbortMission,
  isStarting,
  isAborting,
}) {
  const [selectedDrones, setSelectedDrones] = useState(["DRONE-101"]);
  const droneKeys = Object.keys(drones);

  const toggleDrone = (id) => {
    setSelectedDrones((prev) =>
      prev.includes(id) ? prev.filter((d) => d !== id) : [...prev, id]
    );
  };

  const selectAll = () => {
    setSelectedDrones([...droneKeys]);
  };

  const clearSelection = () => {
    setSelectedDrones([]);
  };

  const handleStart = () => {
    if (selectedDrones.length === 0) return;
    onStartMission(selectedDrones);
  };

  return (
    <div className="section-panel">
      <div className="section-header">
        <div>
          <h2>🎯 Drone Mission Control Center</h2>
          <p>Configure autonomous delivery missions, dispatch multi-drone sorties, or signal emergency aborts</p>
        </div>
      </div>

      <div className="control-workspace-grid">
        {/* Left: Dispatcher Card */}
        <div className="dispatcher-card">
          <div className="dispatcher-header">
            <h3>1. Select UAV Assets</h3>
            <div className="selection-quick-actions">
              <button type="button" onClick={selectAll} className="quick-btn">Select All</button>
              <button type="button" onClick={clearSelection} className="quick-btn">Clear</button>
            </div>
          </div>

          <div className="drone-selector-list">
            {droneKeys.map((id) => {
              const d = drones[id] || {};
              const isSelected = selectedDrones.includes(id);
              const isBusy = !!d.currentMission;

              return (
                <div
                  key={id}
                  className={`drone-select-item ${isSelected ? "selected" : ""} ${isBusy ? "busy" : ""}`}
                  onClick={() => !isBusy && toggleDrone(id)}
                >
                  <input
                    type="checkbox"
                    checked={isSelected}
                    disabled={isBusy}
                    onChange={() => !isBusy && toggleDrone(id)}
                  />
                  <div className="drone-select-info">
                    <strong>{id}</strong>
                    <span>Battery: {(d.battery || 0).toFixed(0)}% | Alt: {(d.altitude || 0).toFixed(0)}m</span>
                  </div>
                  {isBusy && <span className="busy-badge">ON MISSION</span>}
                </div>
              );
            })}
          </div>

          <div className="mission-launch-footer">
            <button
              className="launch-mission-btn"
              onClick={handleStart}
              disabled={isStarting || selectedDrones.length === 0}
            >
              {isStarting ? (
                <span>⏳ Initializing Workflow...</span>
              ) : (
                <span>🚀 Dispatch Mission ({selectedDrones.length} Drone{selectedDrones.length !== 1 ? "s" : ""})</span>
              )}
            </button>
            <p className="launch-tip">
              Orchestrates <code>DroneMissionWorkflow</code> (single drone) or <code>MultiDroneMissionWorkflow</code> (concurrent).
            </p>
          </div>
        </div>

        {/* Right: Live Mission Monitor */}
        <div className="monitor-column">
          <h3 className="subhead">2. Live Workflow Execution Status</h3>
          <MissionTracker
            activeMission={activeMission}
            onAbort={onAbortMission}
            isAborting={isAborting}
          />
        </div>
      </div>
    </div>
  );
}
