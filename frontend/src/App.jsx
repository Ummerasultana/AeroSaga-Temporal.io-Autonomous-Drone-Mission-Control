
import { useEffect, useState } from "react";
import "./App.css";

const initialDrones = {
  "DRONE-101": {
    id: "DRONE-101",
    status: "CONNECTING",
    battery: 0,
    altitude: 0,
    latitude: 0,
    longitude: 0,
  },

  "DRONE-102": {
    id: "DRONE-102",
    status: "CONNECTING",
    battery: 0,
    altitude: 0,
    latitude: 0,
    longitude: 0,
  },

  "DRONE-103": {
    id: "DRONE-103",
    status: "CONNECTING",
    battery: 0,
    altitude: 0,
    latitude: 0,
    longitude: 0,
  },
};

function App() {
  const [drones, setDrones] = useState(initialDrones);
  const [connected, setConnected] = useState(false);

  // -----------------------------
  // TELEMETRY WEBSOCKET
  // -----------------------------
  useEffect(() => {
    const socket = new WebSocket(
      "ws://localhost:8081/telemetry/dashboard"
    );

    socket.onopen = () => {
      console.log(
        "Connected to AeroSaga Telemetry Gateway"
      );

      setConnected(true);
    };

    socket.onmessage = (event) => {
      try {
        const telemetry = JSON.parse(event.data);

        console.log(
          "Received telemetry:",
          telemetry
        );

        setDrones((previousDrones) => ({
          ...previousDrones,

          [telemetry.droneId]: {
            id: telemetry.droneId,
            status: "ACTIVE",
            battery: telemetry.batteryLevel,
            altitude: telemetry.altitude,
            latitude: telemetry.latitude,
            longitude: telemetry.longitude,
          },
        }));
      } catch (error) {
        console.error(
          "Failed to parse telemetry:",
          error
        );
      }
    };

    socket.onclose = () => {
      console.log(
        "Disconnected from AeroSaga Telemetry Gateway"
      );

      setConnected(false);
    };

    socket.onerror = (error) => {
      console.error(
        "WebSocket error:",
        error
      );

      setConnected(false);
    };

    return () => {
      socket.close();
    };
  }, []);

  // -----------------------------
  // START MISSION
  // -----------------------------
  const startMission = async () => {

    console.log(
      "START MISSION FUNCTION CALLED"
    );

    try {

      console.log(
        "Sending mission request to backend..."
      );

      const response = await fetch(
        "http://localhost:8081/api/missions/start",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify([
            "DRONE-101",
          ]),
        }
      );

      console.log(
        "Backend response status:",
        response.status
      );

      const result = await response.text();

      console.log(
        "Mission response:",
        result
      );

      alert(result);

    } catch (error) {

      console.error(
        "Failed to start mission:",
        error
      );

      alert(
        "Failed to start mission"
      );
    }
  };

  const droneList = Object.values(drones);

  return (
    <div className="dashboard">

      {/* SIDEBAR */}

      <aside className="sidebar">

        <div className="brand">

          <div className="brand-icon">
            ✈
          </div>

          <div>
            <h2>AeroSaga</h2>
            <span>Mission Control</span>
          </div>

        </div>

        <nav>

          <div className="nav-item active">
            📊 Dashboard
          </div>

          <div className="nav-item">
            🛩️ Drones
          </div>

          <div className="nav-item">
            🎯 Missions
          </div>

          <div className="nav-item">
            🚨 Alerts
          </div>

          <div className="nav-item">
            📜 Mission History
          </div>

        </nav>

        <div className="system-status">

          <span
            className="status-dot"
            style={{
              background: connected
                ? "#22c55e"
                : "#ef4444",
            }}
          ></span>

          {connected
            ? "System Online"
            : "System Offline"}

        </div>

      </aside>


      {/* MAIN CONTENT */}

      <main className="main-content">

        <header className="topbar">

          <div>

            <h1>
              Mission Dashboard
            </h1>

            <p>
              Monitor and control autonomous drone missions.
            </p>

          </div>

          <div className="connection">

            <span
              className="status-dot"
              style={{
                background: connected
                  ? "#22c55e"
                  : "#ef4444",
              }}
            ></span>

            {connected
              ? "Telemetry Connected"
              : "Telemetry Disconnected"}

          </div>

        </header>


        {/* STATISTICS */}

        <section className="stats">

          <div className="stat-card">

            <span>
              Total Drones
            </span>

            <strong>
              {droneList.length}
            </strong>

          </div>


          <div className="stat-card">

            <span>
              Active Drones
            </span>

            <strong>
              {
                droneList.filter(
                  (drone) =>
                    drone.status === "ACTIVE"
                ).length
              }
            </strong>

          </div>


          <div className="stat-card">

            <span>
              Active Missions
            </span>

            <strong>
              0
            </strong>

          </div>


          <div className="stat-card">

            <span>
              Active Alerts
            </span>

            <strong>
              0
            </strong>

          </div>

        </section>


        {/* DRONE TELEMETRY */}

        <section className="section">

          <div className="section-header">

            <h2>
              Live Drone Telemetry
            </h2>

            <span>
              {droneList.length} drones connected
            </span>

          </div>


          <div className="drone-grid">

            {droneList.map((drone) => (

              <div
                className="drone-card"
                key={drone.id}
              >

                <div className="drone-header">

                  <div>

                    <h3>
                      {drone.id}
                    </h3>

                    <span className="active-status">

                      <span
                        className="status-dot"
                        style={{
                          background:
                            drone.status === "ACTIVE"
                              ? "#22c55e"
                              : "#f59e0b",
                        }}
                      ></span>

                      {drone.status}

                    </span>

                  </div>


                  <div className="drone-icon">
                    🛩️
                  </div>

                </div>


                <div className="telemetry-grid">

                  <div className="telemetry-item">

                    <span>
                      Battery
                    </span>

                    <strong>
                      {drone.battery.toFixed(1)}%
                    </strong>

                  </div>


                  <div className="telemetry-item">

                    <span>
                      Altitude
                    </span>

                    <strong>
                      {drone.altitude.toFixed(2)} m
                    </strong>

                  </div>


                  <div className="telemetry-item">

                    <span>
                      Latitude
                    </span>

                    <strong>
                      {drone.latitude.toFixed(6)}
                    </strong>

                  </div>


                  <div className="telemetry-item">

                    <span>
                      Longitude
                    </span>

                    <strong>
                      {drone.longitude.toFixed(6)}
                    </strong>

                  </div>

                </div>


                <div className="mission-status">

                  <span>
                    Mission Status
                  </span>

                  <strong>
                    NO ACTIVE MISSION
                  </strong>

                </div>


                <button className="view-button">
                  View Drone
                </button>

              </div>

            ))}

          </div>

        </section>


        {/* BOTTOM PANELS */}

        <section className="bottom-grid">

          {/* MISSION CONTROL */}

          <div className="panel">

            <div className="section-header">

              <h2>
                Mission Control
              </h2>

            </div>


            <div className="mission-controls">

              <label>
                Select Drone
              </label>

              <select>
                <option>
                  DRONE-101
                </option>

                <option>
                  DRONE-102
                </option>

                <option>
                  DRONE-103
                </option>
              </select>


              <div className="mission-actions">

                <button
                  className="primary-button"
                  onClick={() => {

                    console.log(
                      "START MISSION BUTTON CLICKED"
                    );

                    startMission();

                  }}
                >
                  Start Mission
                </button>


                <button className="abort-button">
                  Abort Mission
                </button>

              </div>

            </div>


            <div className="current-mission">

              <span>
                Current Mission
              </span>

              <strong>
                No active mission
              </strong>

            </div>

          </div>


          {/* ALERTS */}

          <div className="panel">

            <div className="section-header">

              <h2>
                Recent Alerts
              </h2>

            </div>

            <p className="empty-message">
              No active alerts.
            </p>

          </div>

        </section>

      </main>

    </div>
  );
}

export default App;
