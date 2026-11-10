import { useEffect, useState, useRef, useCallback } from "react";
import "./App.css";
import Sidebar from "./components/Sidebar";
import Topbar from "./components/Topbar";
import DashboardView from "./components/DashboardView";
import DronesView from "./components/DronesView";
import MissionControlView from "./components/MissionControlView";
import TacticalMapView from "./components/TacticalMapView";
import MissionHistoryView from "./components/MissionHistoryView";
import AlertsView from "./components/AlertsView";
import {
  fetchHealth,
  startMission,
  getMissionStatus,
  abortMission,
  getActiveMissions,
  getMissionHistory,
} from "./services/api";

const initialDrones = {
  "DRONE-101": {
    id: "DRONE-101",
    status: "READY",
    battery: 100.0,
    altitude: 0.0,
    latitude: 15.1394,
    longitude: 76.9214,
    lastUpdate: 0,
    currentMission: null,
  },

  "DRONE-102": {
    id: "DRONE-102",
    status: "READY",
    battery: 100.0,
    altitude: 0.0,
    latitude: 15.14,
    longitude: 76.922,
    lastUpdate: 0,
    currentMission: null,
  },

  "DRONE-103": {
    id: "DRONE-103",
    status: "READY",
    battery: 100.0,
    altitude: 0.0,
    latitude: 15.1385,
    longitude: 76.9205,
    lastUpdate: 0,
    currentMission: null,
  },
};

const initialAlerts = [
  {
    id: "init-1",
    title: "System Initialized",
    message: "AeroSaga Autonomous Drone Mission Control is online.",
    type: "info",
    timestamp: 1710000000000,
  },
];

function App() {
  const [activeTab, setActiveTab] = useState("dashboard");
  const [drones, setDrones] = useState(initialDrones);
  const [connected, setConnected] = useState(false);
  const [temporalStatus, setTemporalStatus] = useState("CHECKING");

  const [activeMission, setActiveMission] = useState(null);
  const [isStarting, setIsStarting] = useState(false);
  const [isAborting, setIsAborting] = useState(false);

  const [history, setHistory] = useState([]);
  const [isHistoryLoading, setIsHistoryLoading] = useState(false);

  const [alerts, setAlerts] = useState(initialAlerts);

  const pollingRef = useRef(null);

  const addAlert = useCallback((title, message, type = "info") => {
    setAlerts((prev) => [
      {
        id: `alert-${Date.now()}-${Math.random()
          .toString(36)
          .substring(2, 6)}`,
        title,
        message,
        type,
        timestamp: Date.now(),
      },
      ...prev,
    ]);
  }, []);

  const loadHistory = useCallback(async () => {
    try {
      setIsHistoryLoading(true);
      const data = await getMissionHistory();
      setHistory(data);
    } catch {
      console.warn("Could not load mission history");
    } finally {
      setIsHistoryLoading(false);
    }
  }, []);

  // ----------------------------------------------------
  // 1. WEBSOCKET TELEMETRY
  // ----------------------------------------------------
  useEffect(() => {
    let socket = null;
    let reconnectTimeout = null;

    const connectWebSocket = () => {
      socket = new WebSocket(
        "ws://localhost:8081/telemetry/dashboard"
      );

      socket.onopen = () => {
        console.log("Connected to AeroSaga Telemetry Gateway");
        setConnected(true);
      };

      socket.onmessage = (event) => {
        try {
          const telemetry = JSON.parse(event.data);

          if (!telemetry || !telemetry.droneId) return;

          setDrones((prev) => {
            const current = prev[telemetry.droneId] || {
              id: telemetry.droneId,
            };

            return {
              ...prev,
              [telemetry.droneId]: {
                ...current,
                status: "ACTIVE",
                battery: telemetry.batteryLevel,
                altitude: telemetry.altitude,
                latitude: telemetry.latitude,
                longitude: telemetry.longitude,
                lastUpdate:
                  telemetry.timestamp || Date.now(),
              },
            };
          });

          // Warn if battery is critically low (<= 20%)
          if (
            telemetry.batteryLevel <= 20 &&
            telemetry.batteryLevel > 19
          ) {
            addAlert(
              `Low Battery Warning (${telemetry.droneId})`,
              `${telemetry.droneId} battery is critical at ${telemetry.batteryLevel.toFixed(
                1
              )}%. Return to base advised.`,
              "warning"
            );
          }
        } catch (err) {
          console.error("Telemetry parse error:", err);
        }
      };

      socket.onclose = () => {
        console.log(
          "Disconnected from Telemetry Gateway. Reconnecting in 3s..."
        );

        setConnected(false);

        reconnectTimeout = setTimeout(
          connectWebSocket,
          3000
        );
      };

      socket.onerror = (err) => {
        console.warn("WebSocket error:", err);
        setConnected(false);
      };
    };

    connectWebSocket();

    return () => {
      if (socket) socket.close();
      if (reconnectTimeout) clearTimeout(reconnectTimeout);
    };
  }, [addAlert]);

  // ----------------------------------------------------
  // 2. PERIODIC HEALTH & ACTIVE MISSIONS
  // ----------------------------------------------------
  useEffect(() => {
    let isMounted = true;

    const checkHealth = async () => {
      try {
        const health = await fetchHealth();

        if (isMounted) {
          setTemporalStatus(
            health.temporalStatus || "CONNECTED"
          );
        }
      } catch {
        if (isMounted) {
          setTemporalStatus("DISCONNECTED");
        }
      }

      try {
        const data = await getMissionHistory();

        if (isMounted) {
          setHistory(data);
        }
      } catch {
        // ignore on init
      }
    };

    checkHealth();

    const interval = setInterval(
      checkHealth,
      8000
    );

    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);

  // Check for active missions on mount
  useEffect(() => {
    getActiveMissions()
      .then((active) => {
        if (active && active.length > 0) {
          setActiveMission(active[0]);
        }
      })
      .catch((e) =>
        console.log(
          "No active mission on load:",
          e.message
        )
      );
  }, []);

  // ----------------------------------------------------
  // 3. REAL-TIME MISSION STATUS POLLING
  // ----------------------------------------------------
  useEffect(() => {
    if (
      !activeMission ||
      !activeMission.workflowId
    ) {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
      }

      return;
    }

    const { workflowId, status } = activeMission;

    const isTerminal = [
      "COMPLETED",
      "FAILED",
      "ABORTED",
    ].includes(status);

    if (isTerminal) {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
      }

      return;
    }

    pollingRef.current = setInterval(
      async () => {
        try {
          const updated =
            await getMissionStatus(workflowId);

          setActiveMission(updated);

          // Update assigned drones on drone cards
          setDrones((prev) => {
            const next = { ...prev };

            (updated.droneIds || []).forEach(
              (dId) => {
                if (next[dId]) {
                  next[dId] = {
                    ...next[dId],
                    currentMission: [
                      "COMPLETED",
                      "FAILED",
                      "ABORTED",
                    ].includes(updated.status)
                      ? null
                      : workflowId,
                  };
                }
              }
            );

            return next;
          });

          if (
            [
              "COMPLETED",
              "FAILED",
              "ABORTED",
            ].includes(updated.status)
          ) {
            clearInterval(pollingRef.current);

            loadHistory();

            if (
              updated.status === "COMPLETED"
            ) {
              addAlert(
                "Mission Completed Successfully",
                `Workflow ${workflowId} concluded successfully. All UAVs returned to base.`,
                "success"
              );
            } else if (
              updated.status === "ABORTED"
            ) {
              addAlert(
                "Mission Aborted",
                `Workflow ${workflowId} was safely aborted by operator command.`,
                "warning"
              );
            } else {
              addAlert(
                "Mission Delivery Failed",
                `Workflow ${workflowId} encountered delivery failure. Return compensation executed.`,
                "error"
              );
            }
          }
        } catch (err) {
          console.warn(
            "Polling error for workflow:",
            workflowId,
            err
          );
        }
      },
      1800
    );

    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current);
      }
    };
  }, [
    activeMission,
    addAlert,
    loadHistory,
  ]);

  // ----------------------------------------------------
  // 4. START MISSION HANDLER
  // ----------------------------------------------------
  const handleStartMission = async (
    selectedDroneIds
  ) => {
    if (
      !selectedDroneIds ||
      selectedDroneIds.length === 0
    ) {
      alert("Please select at least one drone.");
      return;
    }

    try {
      setIsStarting(true);

      const res =
        await startMission(selectedDroneIds);

      setActiveMission({
        workflowId: res.workflowId,
        droneIds: res.droneIds,
        status: res.status || "PENDING",
        droneStatuses: {},
        progressPercent: 5,
        startTime: res.startTime,
      });

      // Mark drones as busy
      setDrones((prev) => {
        const next = { ...prev };

        selectedDroneIds.forEach((id) => {
          if (next[id]) {
            next[id] = {
              ...next[id],
              currentMission: res.workflowId,
            };
          }
        });

        return next;
      });

      addAlert(
        "Mission Launched",
        `Temporal workflow ${res.workflowId} initiated for ${selectedDroneIds.join(
          ", "
        )}.`,
        "info"
      );
    } catch (err) {
      console.error(
        "Start mission failure:",
        err
      );

      addAlert(
        "Mission Launch Failed",
        err.message,
        "error"
      );

      alert(
        err.message ||
          "Failed to start mission"
      );
    } finally {
      setIsStarting(false);
    }
  };

  // ----------------------------------------------------
  // 5. ABORT MISSION HANDLER
  // ----------------------------------------------------
  const handleAbortMission = async (
    workflowId
  ) => {
    if (!workflowId) {
      alert("No active mission to abort.");
      return;
    }

    try {
      setIsAborting(true);

      console.log(
        "Sending abort request for workflow:",
        workflowId
      );

      const result =
        await abortMission(workflowId);

      console.log(
        "Abort response:",
        result
      );

      setActiveMission((prev) =>
        prev &&
        prev.workflowId === workflowId
          ? {
              ...prev,
              status: "ABORTING",
            }
          : prev
      );

      addAlert(
        "Emergency Abort Signaled",
        `Abort signal dispatched to Temporal workflow ${workflowId}. UAVs initiating return to base.`,
        "warning"
      );
    } catch (err) {
      console.error(
        "Abort mission error:",
        err
      );

      addAlert(
        "Abort Failed",
        err.message ||
          "Failed to abort mission",
        "error"
      );

      alert(
        err.message ||
          "Failed to abort mission"
      );
    } finally {
      setIsAborting(false);
    }
  };

  const handleSelectDroneForMission = (
    _droneId
  ) => {
    setActiveTab("missions");
  };

  return (
    <div className="dashboard">
      <Sidebar
        activeTab={activeTab}
        onSelectTab={setActiveTab}
        alertsCount={alerts.length}
        connected={connected}
      />

      <main className="main-content">
        <Topbar
          connected={connected}
          temporalStatus={temporalStatus}
          activeTab={activeTab}
        />

        {activeTab === "dashboard" && (
          <DashboardView
            drones={drones}
            activeMission={activeMission}
            onAbortMission={
              handleAbortMission
            }
            isAborting={isAborting}
            history={history}
            alerts={alerts}
            onNavigateTab={setActiveTab}
          />
        )}

        {activeTab === "drones" && (
          <DronesView
            drones={drones}
            onSelectDroneForMission={
              handleSelectDroneForMission
            }
          />
        )}

        {activeTab === "missions" && (
          <MissionControlView
            drones={drones}
            activeMission={activeMission}
            onStartMission={
              handleStartMission
            }
            onAbortMission={
              handleAbortMission
            }
            isStarting={isStarting}
            isAborting={isAborting}
          />
        )}

        {activeTab === "map" && (
          <div className="section-panel">
            <TacticalMapView
              drones={drones}
              activeMission={activeMission}
            />
          </div>
        )}

        {activeTab === "history" && (
          <MissionHistoryView
            history={history}
            onRefresh={loadHistory}
            isLoading={isHistoryLoading}
          />
        )}

        {activeTab === "alerts" && (
          <AlertsView
            alerts={alerts}
            onDismissAlert={(id) =>
              setAlerts((prev) =>
                prev.filter(
                  (a) => a.id !== id
                )
              )
            }
            onClearAll={() =>
              setAlerts([])
            }
          />
        )}
      </main>
    </div>
  );
}

export default App;