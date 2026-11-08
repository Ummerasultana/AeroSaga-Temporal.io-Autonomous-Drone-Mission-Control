import { useCallback, useEffect, useState } from "react";
import axios from "axios";

import {
  Activity,
  AlertCircle,
  ArrowLeft,
  BarChart3,
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
  PieChart as PieChartIcon,
} from "lucide-react";

import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";

const API = "/api/missions";

const WORKFLOW_STAGES = [
  "VALIDATING_ROUTE",
  "PREPARING_DRONE",
  "EXECUTING_MISSION",
  "COMPLETED",
];

const terminalStatuses = ["COMPLETED", "ABORTED"];

function formatStatus(status) {
  if (!status) return "UNKNOWN";

  return status.replaceAll("_", " ");
}

function StatusBadge({ status }) {
  const normalized = (status || "UNKNOWN").toLowerCase();

  return (
    <span className={`status status-${normalized}`}>
      <span className="status-dot"></span>
      {formatStatus(status)}
    </span>
  );
}

function StatCard({ title, value, icon: Icon, color }) {
  return (
    <div className={`stat-card stat-${color}`}>
      <div className="stat-card-top">
        <span>{title}</span>
        <div className="stat-icon">
          <Icon size={21} />
        </div>
      </div>

      <h2>{value}</h2>
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
  const [mobileMenu, setMobileMenu] = useState(false);

  const [form, setForm] = useState({
    droneId: "DRONE001",
    startLatitude: "28.6139",
    startLongitude: "77.2090",
    targetLatitude: "28.5355",
    targetLongitude: "77.3910",
  });

  const fetchMissions = useCallback(async () => {
    try {
      setLoading(true);

      const response = await axios.get(API);
      const data = Array.isArray(response.data) ? response.data : [];

      setMissions(data);

      setSelected((previous) => {
        if (!previous) return previous;

        return (
          data.find(
            (mission) => mission.missionId === previous.missionId
          ) || previous
        );
      });

      setError("");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to load missions. Check whether the backend is running."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchProgress = useCallback(async (missionId) => {
    if (!missionId) return;

    try {
      const response = await axios.get(
        `${API}/${missionId}/progress`
      );

      setProgress(response.data);
    } catch (err) {
      console.error("Progress request failed:", err);
    }
  }, []);

  useEffect(() => {
    fetchMissions();
  }, [fetchMissions]);

  useEffect(() => {
    if (page !== "details" || !selected?.missionId) return;

    const missionId = selected.missionId;

    fetchProgress(missionId);

    const interval = setInterval(() => {
      fetchProgress(missionId);
      fetchMissions();
    }, 3000);

    return () => clearInterval(interval);
  }, [page, selected?.missionId, fetchProgress, fetchMissions]);

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

  const filteredMissions = missions.filter((mission) => {
    const search = searchTerm.toLowerCase();

    const matchesSearch =
      (mission.droneId || "").toLowerCase().includes(search) ||
      (mission.missionId || "").toLowerCase().includes(search);

    const matchesStatus =
      filterStatus === "ALL" || mission.status === filterStatus;

    return matchesSearch && matchesStatus;
  });

  const changePage = (nextPage) => {
    setPage(nextPage);
    setMobileMenu(false);
    setError("");
    setSuccess("");
  };

  const updateForm = (event) => {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const createMission = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    const coordinates = [
      Number(form.startLatitude),
      Number(form.startLongitude),
      Number(form.targetLatitude),
      Number(form.targetLongitude),
    ];

    if (!form.droneId.trim()) {
      setError("Please enter a drone ID.");
      return;
    }

    if (
      coordinates.some(
        (value) =>
          !Number.isFinite(value) ||
          value < -180 ||
          value > 180
      ) ||
      Math.abs(coordinates[0]) > 90 ||
      Math.abs(coordinates[2]) > 90
    ) {
      setError("Please enter valid GPS coordinates.");
      return;
    }

    const payload = {
      droneId: form.droneId.trim(),
      startLatitude: coordinates[0],
      startLongitude: coordinates[1],
      targetLatitude: coordinates[2],
      targetLongitude: coordinates[3],
    };

    try {
      setCreating(true);

      const response = await axios.post(API, payload);

      setSuccess("Mission created successfully!");

      await fetchMissions();

      const createdMission = response.data;

      if (createdMission?.missionId) {
        setSelected(createdMission);
        setProgress(null);
        setPage("details");
      } else {
        setPage("missions");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to create mission. Please check the backend."
      );
    } finally {
      setCreating(false);
    }
  };

  const openMission = async (mission) => {
    setSelected(mission);
    setProgress(null);
    setPage("details");
    setError("");
    setSuccess("");

    try {
      const response = await axios.get(
        `${API}/${mission.missionId}`
      );

      setSelected(response.data);
      await fetchProgress(mission.missionId);
    } catch (err) {
      console.error("Unable to load mission details:", err);
    }
  };

  const cancelMission = async () => {
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

      const response = await axios.get(
        `${API}/${selected.missionId}`
      );

      setSelected(response.data);
      await fetchProgress(selected.missionId);
      await fetchMissions();

      setSuccess("Mission cancelled successfully.");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to cancel this mission."
      );
    }
  };

  const currentStage =
    progress?.currentStage || selected?.currentStage || "";

  const currentStageIndex = WORKFLOW_STAGES.indexOf(currentStage);

  const isCompleted =
    selected?.status === "COMPLETED" ||
    currentStage === "COMPLETED";

  const isAborted = selected?.status === "ABORTED";

  const progressPercentage = isAborted
    ? Math.max(0, ((Math.max(currentStageIndex, 0) + 1) / 4) * 100)
    : isCompleted
    ? 100
    : currentStageIndex >= 0
    ? ((currentStageIndex + 1) / 4) * 100
    : 0;
    return (
    <div className="app-shell">

      {/* SIDEBAR */}

      <aside className={`sidebar ${mobileMenu ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-icon">
            <Plane size={25} />
          </div>

          <div>
            <h2>AeroSaga</h2>
            <span>MISSION CONTROL</span>
          </div>
        </div>

        <div className="sidebar-label">MAIN MENU</div>

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

          <button
            className={page === "analytics" ? "nav-item active" : "nav-item"}
            onClick={() => changePage("analytics")}
          >
            <BarChart3 size={19} />
            Mission Analytics
          </button>
        </nav>

        <div className="sidebar-bottom">
          <div className="connection-indicator">
            <span className="connection-dot"></span>
            <div>
              <strong>Mission System</strong>
              <small>Dashboard available</small>
            </div>
          </div>
        </div>
      </aside>

      {/* MAIN AREA */}

      <main className="main-content">

        {/* TOPBAR */}

        <header className="topbar">
          <div className="topbar-left">
            <button
              className="mobile-menu-button"
              onClick={() => setMobileMenu(!mobileMenu)}
            >
              {mobileMenu ? <XCircle size={22} /> : <List size={22} />}
            </button>

            <div>
              <p className="eyebrow">AEROSAGA / MISSION CONTROL</p>

              <h1>
                {page === "dashboard"
                  ? "Dashboard"
                  : page === "missions"
                  ? "All Missions"
                  : page === "create"
                  ? "Create Mission"
                  : page === "analytics"
                  ? "Mission Analytics"
                  : "Mission Details"}
              </h1>
            </div>
          </div>

          <div className="topbar-actions">
            <button
              className="icon-button"
              onClick={fetchMissions}
              title="Refresh missions"
            >
              <RefreshCw size={19} />
            </button>

            <button
              className="primary-button"
              onClick={() => changePage("create")}
            >
              <Plus size={18} />
              New Mission
            </button>
          </div>
        </header>

        {/* ALERT MESSAGES */}

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={19} />
            <span>{error}</span>

            <button onClick={() => setError("")}>
              <XCircle size={17} />
            </button>
          </div>
        )}

        {success && (
          <div className="alert alert-success">
            <CheckCircle2 size={19} />
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
                <p className="eyebrow">WELCOME TO AEROSAGA</p>

                <h2>Autonomous Drone Mission Control</h2>

                <p className="muted">
                  Monitor, manage and track your drone missions from one place.
                </p>

                <button
                  className="primary-button welcome-button"
                  onClick={() => changePage("create")}
                >
                  <Plus size={18} />
                  Create New Mission
                </button>
              </div>

              <div className="welcome-icon">
                <Navigation size={42} />
              </div>
            </section>

            <section className="stats-grid">
              <StatCard
                title="Total Missions"
                value={totalMissions}
                icon={Plane}
                color="blue"
              />

              <StatCard
                title="Active Missions"
                value={activeMissions}
                icon={Activity}
                color="purple"
              />

              <StatCard
                title="Completed"
                value={completedMissions}
                icon={CheckCircle2}
                color="green"
              />

              <StatCard
                title="Aborted"
                value={abortedMissions}
                icon={XCircle}
                color="red"
              />
            </section>

            <section className="panel">
              <div className="panel-heading">
                <div>
                  <h3>Recent Missions</h3>
                  <p className="muted">
                    Latest missions from your mission database
                  </p>
                </div>

                <button
                  className="text-button"
                  onClick={() => changePage("missions")}
                >
                  View All <ArrowLeft size={15} />
                </button>
              </div>

              {loading && missions.length === 0 ? (
                <div className="empty-state">
                  <RefreshCw size={25} />
                  <p>Loading missions...</p>
                </div>
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
                    <button
                      className="mission-row"
                      key={mission.missionId}
                      onClick={() => openMission(mission)}
                    >
                      <div className="mission-row-icon">
                        <Plane size={20} />
                      </div>

                      <div className="mission-row-info">
                        <strong>{mission.droneId}</strong>
                        <small>{mission.missionId}</small>
                      </div>

                      <StatusBadge status={mission.status} />

                      <ArrowLeft className="mission-arrow" size={18} />
                    </button>
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
                  Search, filter and manage your drone missions.
                </p>
              </div>

              <button
                className="primary-button"
                onClick={() => changePage("create")}
              >
                <Plus size={17} />
                New Mission
              </button>
            </div>

            <div className="filter-toolbar">
              <div className="search-box">
                <Search size={18} />

                <input
                  type="text"
                  placeholder="Search drone ID or mission ID..."
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.target.value)}
                />
              </div>

              <select
                value={filterStatus}
                onChange={(event) => setFilterStatus(event.target.value)}
              >
                <option value="ALL">All Statuses</option>
                <option value="STARTED">Started</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="COMPLETED">Completed</option>
                <option value="ABORTED">Aborted</option>
              </select>
            </div>

            {filteredMissions.length === 0 ? (
              <div className="empty-state">
                <Search size={30} />
                <h3>No matching missions</h3>
                <p>Try changing your search or status filter.</p>
              </div>
            ) : (
              <div className="mission-list">
                {filteredMissions.map((mission) => (
                  <button
                    className="mission-row"
                    key={mission.missionId}
                    onClick={() => openMission(mission)}
                  >
                    <div className="mission-row-icon">
                      <Plane size={20} />
                    </div>

                    <div className="mission-row-info">
                      <strong>{mission.droneId}</strong>
                      <small>{mission.missionId}</small>
                    </div>

                    <StatusBadge status={mission.status} />

                    <ArrowLeft className="mission-arrow" size={18} />
                  </button>
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

              <div className="welcome-icon">
                <Crosshair size={30} />
              </div>
            </div>

            <form onSubmit={createMission}>
              <div className="form-group">
                <label>Drone ID</label>

                <input
                  type="text"
                  name="droneId"
                  value={form.droneId}
                  onChange={updateForm}
                  placeholder="Enter drone ID"
                  required
                />
              </div>

              <div className="form-section-heading">
                <MapPin size={19} />
                <h4>Starting Coordinates</h4>
              </div>

              <div className="form-grid">
                <div className="form-group">
                  <label>Start Latitude</label>

                  <input
                    type="number"
                    name="startLatitude"
                    value={form.startLatitude}
                    onChange={updateForm}
                    step="any"
                    min="-90"
                    max="90"
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Start Longitude</label>

                  <input
                    type="number"
                    name="startLongitude"
                    value={form.startLongitude}
                    onChange={updateForm}
                    step="any"
                    min="-180"
                    max="180"
                    required
                  />
                </div>
              </div>

              <div className="form-section-heading">
                <Target size={19} />
                <h4>Destination Coordinates</h4>
              </div>

              <div className="form-grid">
                <div className="form-group">
                  <label>Target Latitude</label>

                  <input
                    type="number"
                    name="targetLatitude"
                    value={form.targetLatitude}
                    onChange={updateForm}
                    step="any"
                    min="-90"
                    max="90"
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Target Longitude</label>

                  <input
                    type="number"
                    name="targetLongitude"
                    value={form.targetLongitude}
                    onChange={updateForm}
                    step="any"
                    min="-180"
                    max="180"
                    required
                  />
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
                  <Send size={17} />
                  {creating ? "Creating..." : "Launch Mission"}
                </button>
              </div>
            </form>
          </section>
        )}
        {/* MISSION ANALYTICS */}

        {page === "analytics" && (
          <>
            <section className="welcome-section">
              <div>
                <p className="eyebrow">PERFORMANCE OVERVIEW</p>
                <h2>Mission Analytics</h2>
                <p className="muted">
                  Analyze drone mission performance and workflow statistics.
                </p>
              </div>

              <div className="welcome-icon">
                <BarChart3 size={38} />
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
                title="Completed"
                value={completedMissions}
                icon={CheckCircle2}
                color="green"
              />

              <StatCard
                title="Aborted"
                value={abortedMissions}
                icon={ShieldAlert}
                color="red"
              />
            </section>

            <section className="analytics-grid">

              {/* PIE CHART */}

              <div className="panel analytics-panel">
                <div className="panel-heading">
                  <div>
                    <h3>Mission Distribution</h3>
                    <p className="muted">
                      Breakdown by mission status
                    </p>
                  </div>

                  <PieChartIcon size={22} />
                </div>

                <div className="chart-container">
                  <ResponsiveContainer width="100%" height={280}>
                    <PieChart>
                      <Pie
                        data={[
                          { name: "Active", value: activeMissions },
                          { name: "Completed", value: completedMissions },
                          { name: "Aborted", value: abortedMissions },
                        ]}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        outerRadius={90}
                        innerRadius={55}
                        paddingAngle={4}
                      >
                        <Cell fill="#818cf8" />
                        <Cell fill="#34d399" />
                        <Cell fill="#fb7185" />
                      </Pie>

                      <Tooltip
                        contentStyle={{
                          background: "#151b2b",
                          border: "1px solid #343d52",
                          borderRadius: "10px",
                          color: "#ffffff",
                        }}
                      />

                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>

              {/* BAR CHART */}

              <div className="panel analytics-panel">
                <div className="panel-heading">
                  <div>
                    <h3>Mission Performance</h3>
                    <p className="muted">
                      Number of missions by status
                    </p>
                  </div>

                  <BarChart3 size={22} />
                </div>

                <div className="chart-container">
                  <ResponsiveContainer width="100%" height={280}>
                    <BarChart
                      data={[
                        { name: "Active", missions: activeMissions },
                        { name: "Completed", missions: completedMissions },
                        { name: "Aborted", missions: abortedMissions },
                      ]}
                    >
                      <CartesianGrid
                        strokeDasharray="3 3"
                        stroke="#30394b"
                      />

                      <XAxis
                        dataKey="name"
                        stroke="#a0a9bd"
                      />

                      <YAxis
                        stroke="#a0a9bd"
                        allowDecimals={false}
                      />

                      <Tooltip
                        contentStyle={{
                          background: "#151b2b",
                          border: "1px solid #343d52",
                          borderRadius: "10px",
                          color: "#ffffff",
                        }}
                      />

                      <Bar
                        dataKey="missions"
                        fill="#818cf8"
                        radius={[7, 7, 0, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </section>

            {/* COMPLETION RATE */}

            <section className="panel completion-panel">
              <div className="panel-heading">
                <div>
                  <h3>Mission Completion Rate</h3>
                  <p className="muted">
                    Percentage of missions completed successfully
                  </p>
                </div>

                <CheckCircle2 size={24} />
              </div>

              <div className="completion-content">
                <div className="completion-number">
                  {totalMissions === 0
                    ? "0%"
                    : `${Math.round(
                        (completedMissions / totalMissions) * 100
                      )}%`}
                </div>

                <div className="completion-bar">
                  <div
                    className="completion-fill"
                    style={{
                      width:
                        totalMissions === 0
                          ? "0%"
                          : `${(completedMissions / totalMissions) * 100}%`,
                    }}
                  />
                </div>

                <p className="muted">
                  {completedMissions} successful missions out of{" "}
                  {totalMissions} total missions.
                </p>
              </div>
            </section>

            {/* PERFORMANCE SUMMARY */}

            <section className="panel">
              <div className="panel-heading">
                <div>
                  <h3>Performance Summary</h3>
                  <p className="muted">
                    Current mission statistics
                  </p>
                </div>
              </div>

              <div className="analytics-summary">
                <div>
                  <span className="summary-dot active-dot"></span>
                  <span>Active Missions</span>
                  <strong>{activeMissions}</strong>
                </div>

                <div>
                  <span className="summary-dot completed-dot"></span>
                  <span>Completed Missions</span>
                  <strong>{completedMissions}</strong>
                </div>

                <div>
                  <span className="summary-dot aborted-dot"></span>
                  <span>Aborted Missions</span>
                  <strong>{abortedMissions}</strong>
                </div>
              </div>
            </section>
          </>
        )}

        {/* MISSION DETAILS AND LIVE TRACKING */}

        {page === "details" && selected && (
          <section className="panel detail-panel">

            <div className="panel-heading">
              <div>
                <p className="eyebrow">MISSION IDENTIFICATION</p>
                <h3>{selected.droneId}</h3>
                <p className="muted">{selected.missionId}</p>
              </div>

              <StatusBadge status={selected.status} />
            </div>

            <div className="detail-actions">
              <button
                className="secondary-button"
                onClick={() => changePage("missions")}
              >
                <ArrowLeft size={17} />
                Back to Missions
              </button>

              {!terminalStatuses.includes(selected.status) && (
                <button
                  className="danger-button"
                  onClick={cancelMission}
                >
                  <XCircle size={17} />
                  Cancel Mission
                </button>
              )}
            </div>

            <div className="detail-grid">

              <div className="detail-card">
                <MapPin size={21} />
                <span>Starting Latitude</span>
                <strong>{selected.startLatitude ?? "N/A"}</strong>
              </div>

              <div className="detail-card">
                <MapPin size={21} />
                <span>Starting Longitude</span>
                <strong>{selected.startLongitude ?? "N/A"}</strong>
              </div>

              <div className="detail-card">
                <Target size={21} />
                <span>Target Latitude</span>
                <strong>{selected.targetLatitude ?? "N/A"}</strong>
              </div>

              <div className="detail-card">
                <Target size={21} />
                <span>Target Longitude</span>
                <strong>{selected.targetLongitude ?? "N/A"}</strong>
              </div>
            </div>

            {/* LIVE WORKFLOW TRACKER */}

            <div className="workflow-section">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow">TEMPORAL WORKFLOW</p>
                  <h3>Live Workflow Tracker</h3>
                  <p className="muted">
                    Track the execution stages of your drone mission.
                  </p>
                </div>

                <Activity size={25} />
              </div>

              <div className="workflow-progress">
                <div className="workflow-progress-header">
                  <span>Mission Progress</span>
                  <strong>
                    {isAborted
                      ? "Aborted"
                      : `${Math.round(progressPercentage)}%`}
                  </strong>
                </div>

                <div className="completion-bar">
                  <div
                    className="completion-fill"
                    style={{
                      width: `${progressPercentage}%`,
                    }}
                  />
                </div>
              </div>

              <div className="workflow-stages">
                {WORKFLOW_STAGES.map((stage, index) => {
                  const isStageComplete =
                    isCompleted || index < currentStageIndex;

                  const isCurrent =
                    !isCompleted &&
                    !isAborted &&
                    index === currentStageIndex;

                  return (
                    <div
                      className={`workflow-stage ${
                        isStageComplete
                          ? "stage-completed"
                          : isCurrent
                          ? "stage-current"
                          : ""
                      }`}
                      key={stage}
                    >
                      <div className="workflow-stage-icon">
                        {isStageComplete ? (
                          <CheckCircle2 size={22} />
                        ) : isCurrent ? (
                          <Activity size={22} />
                        ) : (
                          <Clock size={22} />
                        )}
                      </div>

                      <div>
                        <strong>
                          {stage.replaceAll("_", " ")}
                        </strong>

                        <small>
                          {isStageComplete
                            ? "Completed"
                            : isCurrent
                            ? "Currently Running"
                            : "Waiting"}
                        </small>
                      </div>
                    </div>
                  );
                })}
              </div>

              {isAborted && (
                <div className="workflow-message aborted-message">
                  <ShieldAlert size={19} />
                  This mission has been aborted.
                </div>
              )}

              {isCompleted && (
                <div className="workflow-message completed-message">
                  <CheckCircle2 size={19} />
                  Mission completed successfully!
                </div>
              )}

              {!isAborted && !isCompleted && (
                <div className="workflow-message">
                  <Activity size={19} />
                  Current Stage:{" "}
                  {formatStatus(currentStage || "NOT STARTED")}
                </div>
              )}
            </div>

            <div className="detail-footer">
              <span>
                <RadioIcon />
                Workflow progress updates automatically.
              </span>

              <button
                className="icon-button"
                onClick={() => {
                  fetchProgress(selected.missionId);
                  fetchMissions();
                }}
                title="Refresh progress"
              >
                <RefreshCw size={18} />
              </button>
            </div>
          </section>
        )}

        {/* NO MISSION SELECTED */}

        {page === "details" && !selected && (
          <section className="panel empty-state">
            <AlertCircle size={35} />
            <h3>No mission selected</h3>
            <p>Please select a mission to view its details.</p>

            <button
              className="primary-button"
              onClick={() => changePage("missions")}
            >
              <List size={17} />
              View Missions
            </button>
          </section>
        )}

        {/* FOOTER */}

        <footer className="app-footer">
          <span>AeroSaga Mission Control</span>
          <span>Autonomous Drone Management System</span>
        </footer>

      </main>
    </div>
  );
}

function RadioIcon() {
  return <Activity size={16} />;
}

export default App;