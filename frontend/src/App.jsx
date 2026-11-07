import { useCallback, useEffect, useState } from "react";
import axios from "axios";
import {
  Activity,
  AlertCircle,
  ArrowLeft,
  CheckCircle2,
  Clock,
  Crosshair,
  LayoutDashboard,
  List,
  MapPin,
  Navigation,
  Plus,
  RefreshCw,
  Search,
  Send,
  ShieldAlert,
  XCircle,
  Plane,
  Target,
} from "lucide-react";

const API = "/api/missions";

const WORKFLOW_STAGES = [
  "VALIDATING_ROUTE",
  "PREPARING_DRONE",
  "EXECUTING_MISSION",
  "COMPLETED",
];

const terminalStatuses = ["COMPLETED", "ABORTED"];

function Status({ value }) {
  const status = (value || "STARTED").toUpperCase();

  const className =
    status === "COMPLETED"
      ? "completed"
      : status === "ABORTED"
      ? "aborted"
      : status === "IN_PROGRESS"
      ? "active"
      : "started";

  return (
    <span className={`status ${className}`}>
      {status.replaceAll("_", " ")}
    </span>
  );
}

function StatCard({ title, value, icon: Icon, color }) {
  return (
    <div className="stat-card">
      <div className="stat-info">
        <p className="muted">{title}</p>
        <h2>{value}</h2>
      </div>

      <div className={`stat-icon ${color}`}>
        <Icon size={23} />
      </div>
    </div>
  );
}

function App() {
  const [page, setPage] = useState("dashboard");
  const [missions, setMissions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [progress, setProgress] = useState(null);

  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("ALL");

  const [form, setForm] = useState({
    droneId: "",
    startLatitude: "",
    startLongitude: "",
    targetLatitude: "",
    targetLongitude: "",
  });

  const getErrorMessage = (err) => {
    return (
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      "Something went wrong. Please try again."
    );
  };

  // FETCH ALL MISSIONS
  const fetchMissions = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const response = await axios.get(API);

      const data = Array.isArray(response.data)
        ? response.data
        : response.data?.missions || [];

      setMissions(data);

      if (selected) {
        const updated = data.find(
          (mission) => mission.missionId === selected.missionId
        );

        if (updated) {
          setSelected(updated);
        }
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [selected]);

  useEffect(() => {
    fetchMissions();
  }, []);

  // FETCH SELECTED MISSION PROGRESS
  const fetchProgress = useCallback(async (missionId) => {
    if (!missionId) return;

    try {
      const response = await axios.get(
        `${API}/${missionId}/progress`
      );

      setProgress(response.data);
    } catch (err) {
      console.error("Progress fetching failed:", err);
    }
  }, []);

  // AUTOMATIC LIVE TRACKING
  useEffect(() => {
    if (
      page !== "details" ||
      !selected?.missionId ||
      terminalStatuses.includes(selected.status)
    ) {
      return;
    }

    fetchProgress(selected.missionId);

    const interval = setInterval(() => {
      fetchProgress(selected.missionId);
      fetchMissions();
    }, 3000);

    return () => clearInterval(interval);
  }, [
    page,
    selected?.missionId,
    selected?.status,
    fetchProgress,
    fetchMissions,
  ]);

  // DASHBOARD STATISTICS
  const totalMissions = missions.length;

  const activeMissions = missions.filter((mission) =>
    ["STARTED", "IN_PROGRESS"].includes(mission.status)
  ).length;

  const completedMissions = missions.filter(
    (mission) => mission.status === "COMPLETED"
  ).length;

  const abortedMissions = missions.filter(
    (mission) => mission.status === "ABORTED"
  ).length;

  // SEARCH AND FILTER
  const filteredMissions = missions.filter((mission) => {
    const matchesSearch =
      (mission.droneId || "")
        .toLowerCase()
        .includes(searchTerm.toLowerCase()) ||
      (mission.missionId || "")
        .toLowerCase()
        .includes(searchTerm.toLowerCase());

    const matchesStatus =
      filterStatus === "ALL" ||
      mission.status === filterStatus;

    return matchesSearch && matchesStatus;
  });

  // CREATE MISSION
  const handleCreateMission = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    const coordinates = [
      form.startLatitude,
      form.startLongitude,
      form.targetLatitude,
      form.targetLongitude,
    ];

    if (
      !form.droneId.trim() ||
      coordinates.some((value) => value === "")
    ) {
      setError("Please fill in all mission details.");
      return;
    }

    const latitudeValues = [
      Number(form.startLatitude),
      Number(form.targetLatitude),
    ];

    const longitudeValues = [
      Number(form.startLongitude),
      Number(form.targetLongitude),
    ];

    if (
      latitudeValues.some(
        (value) => !Number.isFinite(value) || value < -90 || value > 90
      ) ||
      longitudeValues.some(
        (value) => !Number.isFinite(value) || value < -180 || value > 180
      )
    ) {
      setError("Please enter valid GPS coordinates.");
      return;
    }

    const payload = {
      droneId: form.droneId.trim(),
      startLatitude: Number(form.startLatitude),
      startLongitude: Number(form.startLongitude),
      targetLatitude: Number(form.targetLatitude),
      targetLongitude: Number(form.targetLongitude),
    };

    try {
      setCreating(true);

      const response = await axios.post(API, payload);

      setSuccess("Mission created successfully!");

      setForm({
        droneId: "",
        startLatitude: "",
        startLongitude: "",
        targetLatitude: "",
        targetLongitude: "",
      });

      await fetchMissions();

      if (response.data?.missionId) {
        setSelected(response.data);
        setProgress(null);
        setPage("details");
        fetchProgress(response.data.missionId);
      } else {
        setPage("dashboard");
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setCreating(false);
    }
  };

  // OPEN MISSION DETAILS
  const openMission = async (mission) => {
    setSelected(mission);
    setProgress(null);
    setError("");
    setPage("details");

    await fetchProgress(mission.missionId);
  };

  // UPDATE MISSION STATUS
  const handleStatusUpdate = async (newStatus) => {
    if (!selected?.missionId) return;

    try {
      setError("");
      setSuccess("");

      await axios.put(
        `${API}/${selected.missionId}/status`,
        { status: newStatus }
      );

      setSuccess("Mission status updated successfully.");

      await fetchMissions();

      const response = await axios.get(
        `${API}/${selected.missionId}`
      );

      setSelected(response.data);
      await fetchProgress(selected.missionId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  // CANCEL MISSION
  const handleCancelMission = async () => {
    if (!selected?.missionId) return;

    const confirmed = window.confirm(
      "Are you sure you want to cancel this mission?"
    );

    if (!confirmed) return;

    try {
      setError("");
      setSuccess("");

      await axios.put(
        `${API}/${selected.missionId}/cancel`
      );

      setSuccess("Mission cancelled successfully.");

      const response = await axios.get(
        `${API}/${selected.missionId}`
      );

      setSelected(response.data);

      await fetchMissions();
      await fetchProgress(selected.missionId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const changePage = (newPage) => {
    setError("");
    setSuccess("");
    setPage(newPage);
  };

  // WORKFLOW PROGRESS
  const currentStage =
    progress?.currentStage ||
    selected?.currentStage ||
    selected?.status ||
    "VALIDATING_ROUTE";

  const currentStageIndex = WORKFLOW_STAGES.indexOf(currentStage);

  const isCompleted =
    progress?.completed ||
    selected?.status === "COMPLETED";

  const isAborted =
    selected?.status === "ABORTED" ||
    currentStage === "ABORTED";

  const progressPercentage = isAborted
    ? Math.max(0, ((Math.max(currentStageIndex, 0) + 1) / 4) * 100)
    : isCompleted
    ? 100
    : currentStageIndex >= 0
    ? ((currentStageIndex + 1) / 4) * 100
    : 0;return (
    <div className="app-shell">

      {/* SIDEBAR */}

      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            <Plane size={25} />
          </div>

          <div>
            <h2>AeroSaga</h2>
            <span>MISSION CONTROL</span>
          </div>
        </div>

        <div className="sidebar-label">WORKSPACE</div>

        <nav className="sidebar-nav">
          <button
            className={page === "dashboard" ? "nav-item active" : "nav-item"}
            onClick={() => changePage("dashboard")}
          >
            <LayoutDashboard size={19} />
            Dashboard
          </button>

          <button
            className={page === "missions" ? "nav-item active" : "nav-item"}
            onClick={() => changePage("missions")}
          >
            <List size={19} />
            All Missions
          </button>

          <button
            className={page === "create" ? "nav-item active" : "nav-item"}
            onClick={() => changePage("create")}
          >
            <Plus size={19} />
            Create Mission
          </button>
        </nav>

        <div className="sidebar-bottom">
          <div className="system-status">
            <span className="online-dot"></span>
            <div>
              <strong>System Online</strong>
              <small>Mission services ready</small>
            </div>
          </div>

          <p>AeroSaga Mission Control</p>
          <small>Temporal-powered drone management</small>
        </div>
      </aside>

      {/* MAIN CONTENT */}

      <main className="main-content">

        {/* TOPBAR */}

        <header className="topbar">
          <div>
            <p className="eyebrow">AUTONOMOUS DRONE PLATFORM</p>

            <h1>
              {page === "dashboard"
                ? "Dashboard"
                : page === "missions"
                ? "All Missions"
                : page === "create"
                ? "Create Mission"
                : "Mission Details"}
            </h1>
          </div>

          <div className="topbar-actions">
            <button
              className="icon-button"
              onClick={fetchMissions}
              title="Refresh missions"
            >
              <RefreshCw size={18} />
            </button>

            <button
              className="primary-button"
              onClick={() => changePage("create")}
            >
              <Plus size={17} />
              New Mission
            </button>
          </div>
        </header>

        {/* ALERTS */}

        {error && (
          <div className="alert error-alert">
            <AlertCircle size={18} />
            <span>{error}</span>

            <button onClick={() => setError("")}>
              <XCircle size={17} />
            </button>
          </div>
        )}

        {success && (
          <div className="alert success-alert">
            <CheckCircle2 size={18} />
            <span>{success}</span>

            <button onClick={() => setSuccess("")}>
              <XCircle size={17} />
            </button>
          </div>
        )}

        {/* DASHBOARD */}

        {page === "dashboard" && (
          <>
            <section className="welcome-section">
              <div>
                <p className="eyebrow">MISSION OVERVIEW</p>
                <h2>Welcome to Mission Control</h2>
                <p className="muted">
                  Monitor, manage and track your autonomous drone missions.
                </p>
              </div>

              <div className="welcome-icon">
                <Crosshair size={38} />
              </div>
            </section>

            <section className="stats-grid">
              <StatCard
                title="Total Missions"
                value={totalMissions}
                icon={Navigation}
                color="blue"
              />

              <StatCard
                title="Active Missions"
                value={activeMissions}
                icon={Activity}
                color="purple"
              />

              <StatCard
                title="Completed Missions"
                value={completedMissions}
                icon={CheckCircle2}
                color="green"
              />

              <StatCard
                title="Aborted Missions"
                value={abortedMissions}
                icon={ShieldAlert}
                color="red"
              />
            </section>

            <section className="panel">
              <div className="panel-heading">
                <div>
                  <h3>Recent Missions</h3>
                  <p className="muted">
                    Latest drone mission activity
                  </p>
                </div>

                <button
                  className="text-button"
                  onClick={() => changePage("missions")}
                >
                  View All <ArrowLeft size={15} />
                </button>
              </div>

              {loading ? (
                <p className="empty-state">Loading missions...</p>
              ) : missions.length === 0 ? (
                <div className="empty-state">
                  <Plane size={35} />
                  <h3>No missions yet</h3>
                  <p>Create your first drone mission to get started.</p>

                  <button
                    className="primary-button"
                    onClick={() => changePage("create")}
                  >
                    <Plus size={17} />
                    Create Mission
                  </button>
                </div>
              ) : (
                <div className="mission-list">
                  {missions.slice(0, 5).map((mission) => (
                    <div
                      className="mission-row"
                      key={mission.missionId}
                      onClick={() => openMission(mission)}
                    >
                      <div className="mission-avatar">
                        <Plane size={19} />
                      </div>

                      <div className="mission-row-info">
                        <strong>{mission.droneId}</strong>
                        <small>{mission.missionId}</small>
                      </div>

                      <Status value={mission.status} />

                      <button className="view-button">
                        View <ArrowLeft size={15} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </>
        )}

        {/* ALL MISSIONS */}

        {page === "missions" && (
          <section className="panel">
            <div className="panel-heading">
              <div>
                <h3>All Missions</h3>
                <p className="muted">
                  Manage and monitor all drone missions
                </p>
              </div>

              <button
                className="primary-button"
                onClick={() => changePage("create")}
              >
                <Plus size={17} />
                Create Mission
              </button>
            </div>

            <div className="filter-bar">
              <div className="search-box">
                <Search size={17} />

                <input
                  type="text"
                  placeholder="Search drone ID or mission ID..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>

              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
              >
                <option value="ALL">All Status</option>
                <option value="STARTED">Started</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="COMPLETED">Completed</option>
                <option value="ABORTED">Aborted</option>
              </select>
            </div>

            {loading ? (
              <p className="empty-state">Loading missions...</p>
            ) : filteredMissions.length === 0 ? (
              <div className="empty-state">
                <Search size={32} />
                <h3>No matching missions</h3>
                <p>Try changing your search or filter.</p>
              </div>
            ) : (
              <div className="mission-list">
                {filteredMissions.map((mission) => (
                  <div
                    className="mission-row"
                    key={mission.missionId}
                    onClick={() => openMission(mission)}
                  >
                    <div className="mission-avatar">
                      <Plane size={19} />
                    </div>

                    <div className="mission-row-info">
                      <strong>{mission.droneId}</strong>
                      <small>{mission.missionId}</small>
                    </div>

                    <Status value={mission.status} />

                    <button className="view-button">
                      Details <ArrowLeft size={15} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {/* CREATE MISSION */}

        {page === "create" && (
          <section className="panel form-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">MISSION CONFIGURATION</p>
                <h3>Create New Mission</h3>
                <p className="muted">
                  Enter drone details and GPS coordinates.
                </p>
              </div>

              <div className="form-icon">
                <Navigation size={27} />
              </div>
            </div>

            <form onSubmit={handleCreateMission}>
              <div className="form-group">
                <label>Drone ID</label>

                <input
                  type="text"
                  placeholder="Example: DRONE-001"
                  value={form.droneId}
                  onChange={(e) =>
                    setForm({ ...form, droneId: e.target.value })
                  }
                  required
                />
              </div>

              <div className="coordinate-section">
                <h4>
                  <MapPin size={18} />
                  Starting Coordinates
                </h4>

                <div className="form-grid">
                  <div className="form-group">
                    <label>Start Latitude</label>

                    <input
                      type="number"
                      step="any"
                      min="-90"
                      max="90"
                      placeholder="28.6139"
                      value={form.startLatitude}
                      onChange={(e) =>
                        setForm({
                          ...form,
                          startLatitude: e.target.value,
                        })
                      }
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Start Longitude</label>

                    <input
                      type="number"
                      step="any"
                      min="-180"
                      max="180"
                      placeholder="77.2090"
                      value={form.startLongitude}
                      onChange={(e) =>
                        setForm({
                          ...form,
                          startLongitude: e.target.value,
                        })
                      }
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="coordinate-section">
                <h4>
                  <Target size={18} />
                  Target Coordinates
                </h4>

                <div className="form-grid">
                  <div className="form-group">
                    <label>Target Latitude</label>

                    <input
                      type="number"
                      step="any"
                      min="-90"
                      max="90"
                      placeholder="28.5355"
                      value={form.targetLatitude}
                      onChange={(e) =>
                        setForm({
                          ...form,
                          targetLatitude: e.target.value,
                        })
                      }
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Target Longitude</label>

                    <input
                      type="number"
                      step="any"
                      min="-180"
                      max="180"
                      placeholder="77.3910"
                      value={form.targetLongitude}
                      onChange={(e) =>
                        setForm({
                          ...form,
                          targetLongitude: e.target.value,
                        })
                      }
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => changePage("dashboard")}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="primary-button"
                  disabled={creating}
                >
                  {creating ? (
                    <>
                      <RefreshCw size={17} />
                      Creating...
                    </>
                  ) : (
                    <>
                      <Send size={17} />
                      Start Mission
                    </>
                  )}
                </button>
              </div>
            </form>
          </section>
        )}

        {/* MISSION DETAILS AND LIVE TRACKING */}

        {page === "details" && selected && (
          <section className="panel detail-panel">

            <div className="panel-heading">
              <div>
                <p className="eyebrow">
                  MISSION IDENTIFICATION
                </p>

                <h3>{selected.droneId}</h3>

                <p className="muted mission-id">
                  {selected.missionId}
                </p>
              </div>

              <Status value={selected.status} />
            </div>

            <div className="detail-grid">
              <div className="detail-card">
                <MapPin size={19} />

                <p className="muted">Start Coordinates</p>

                <strong>
                  {selected.startLatitude}, {selected.startLongitude}
                </strong>
              </div>

              <div className="detail-card">
                <Navigation size={19} />

                <p className="muted">Target Coordinates</p>

                <strong>
                  {selected.targetLatitude}, {selected.targetLongitude}
                </strong>
              </div>
            </div>

            {/* LIVE WORKFLOW TRACKER */}

            <div className="progress-section">
              <div className="panel-heading">
                <div>
                  <h3>
                    <Activity size={19} />
                    Live Mission Progress
                  </h3>

                  <p className="muted">
                    Temporal workflow execution
                  </p>
                </div>

                {!terminalStatuses.includes(selected.status) && (
                  <span className="live-indicator">
                    LIVE
                  </span>
                )}
              </div>

              <div className="progress-stage">
                {isAborted
                  ? "MISSION ABORTED"
                  : isCompleted
                  ? "MISSION COMPLETED"
                  : currentStage.replaceAll("_", " ")}
              </div>

              <div className="workflow-progress">
                <div
                  className="workflow-progress-fill"
                  style={{ width: `${progressPercentage}%` }}
                />
              </div>

              <div className="workflow-stages">
                {WORKFLOW_STAGES.map((stage, index) => {
                  const isStageComplete =
                    isCompleted ||
                    (!isAborted && index < currentStageIndex);

                  const isCurrent =
                    !isCompleted &&
                    !isAborted &&
                    index === currentStageIndex;

                  return (
                    <div
                      className={`workflow-stage ${
                        isStageComplete ? "stage-complete" : ""
                      } ${isCurrent ? "stage-current" : ""}`}
                      key={stage}
                    >
                      <div className="stage-icon">
                        {isStageComplete ? (
                          <CheckCircle2 size={19} />
                        ) : (
                          index + 1
                        )}
                      </div>

                      <small>{stage.replaceAll("_", " ")}</small>
                    </div>
                  );
                })}
              </div>

              <div className="progress-meta">
                <div>
                  <Clock size={17} />
                  <span>
                    Status: <strong>{selected.status}</strong>
                  </span>
                </div>

                <div>
                  {isAborted ? (
                    <XCircle size={17} />
                  ) : (
                    <CheckCircle2 size={17} />
                  )}

                  <span>
                    {isAborted
                      ? "Mission stopped"
                      : isCompleted
                      ? "All stages completed"
                      : progress?.completed
                      ? "Workflow completed"
                      : "Workflow in progress"}
                  </span>
                </div>
              </div>

              <p className="refresh-note muted">
                <RefreshCw size={13} />
                Progress refreshes automatically every 3 seconds.
              </p>
            </div>

            {/* MISSION ACTIONS */}

            <div className="mission-actions">
              <button
                className="secondary-button"
                onClick={() => {
                  changePage("missions");
                  fetchMissions();
                }}
              >
                <ArrowLeft size={16} />
                Back to Missions
              </button>

              {!terminalStatuses.includes(selected.status) && (
                <button
                  className="danger-button"
                  onClick={handleCancelMission}
                >
                  <XCircle size={17} />
                  Cancel Mission
                </button>
              )}
            </div>
          </section>
        )}

        {/* NO MISSION SELECTED */}

        {page === "details" && !selected && (
          <section className="panel empty-state">
            <AlertCircle size={35} />
            <h3>No mission selected</h3>
            <p>Please select a mission from the mission list.</p>

            <button
              className="primary-button"
              onClick={() => changePage("missions")}
              >
              View Missions
            </button>
          </section>
        )}

        <footer className="app-footer">
          <span>AeroSaga © 2026</span>
          <span>Autonomous Drone Mission Control</span>
        </footer>
      </main>
    </div>
  );
}

export default App;