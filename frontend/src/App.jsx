import { useEffect, useState } from "react";
import axios from "axios";
import {
  Activity, Plane, LayoutDashboard, PlusCircle,
  RefreshCw, Search, MapPin, Clock, CheckCircle2,
  XCircle, AlertCircle, Menu, X, Navigation,
  Radio, ArrowUpRight, Trash2
} from "lucide-react";

const API = "/api/missions";

function App() {
  const [missions, setMissions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [progress, setProgress] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");
  const [page, setPage] = useState("dashboard");
  const [mobileMenu, setMobileMenu] = useState(false);

  const [form, setForm] = useState({
    droneId: "DRONE001",
    startLatitude: "28.6139",
    startLongitude: "77.2090",
    targetLatitude: "28.5355",
    targetLongitude: "77.3910"
  });

  const [newStatus, setNewStatus] = useState("IN_PROGRESS");

  const notify = (text, isError = false) => {
    setMessage(isError ? "" : text);
    setError(isError ? text : "");
    setTimeout(() => {
      setMessage("");
      setError("");
    }, 4500);
  };

  const loadMissions = async () => {
    setLoading(true);
    try {
      const response = await axios.get(API);
      setMissions(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      notify("Unable to load missions. Check your backend.", true);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMissions();
  }, []);

  const createMission = async (e) => {
    e.preventDefault();

    const body = {
      droneId: form.droneId.trim(),
      startLatitude: Number(form.startLatitude),
      startLongitude: Number(form.startLongitude),
      targetLatitude: Number(form.targetLatitude),
      targetLongitude: Number(form.targetLongitude)
    };

    if (!body.droneId) {
      notify("Please enter a drone ID.", true);
      return;
    }

    if (
      Math.abs(body.startLatitude) > 90 ||
      Math.abs(body.targetLatitude) > 90 ||
      Math.abs(body.startLongitude) > 180 ||
      Math.abs(body.targetLongitude) > 180
    ) {
      notify("Enter valid GPS coordinates.", true);
      return;
    }

    if (
      body.startLatitude === body.targetLatitude &&
      body.startLongitude === body.targetLongitude
    ) {
      notify("Start and target coordinates must be different.", true);
      return;
    }

    try {
      await axios.post(API, body);
      notify("Mission created successfully!");
      setPage("missions");
      await loadMissions();
    } catch (err) {
      notify(err.response?.data?.message || "Mission creation failed.", true);
    }
  };

  const openMission = async (mission) => {
    setSelected(mission);
    setPage("details");
    setProgress(null);

    try {
      const response = await axios.get(
        `${API}/${mission.missionId}/progress`
      );
      setProgress(response.data);
    } catch {
      notify("Unable to load mission progress.", true);
    }
  };

  const updateStatus = async () => {
    if (!selected) return;

    try {
      await axios.put(`${API}/${selected.missionId}/status`, {
        status: newStatus
      });
      notify("Mission status updated.");
      await loadMissions();
      const updated = await axios.get(`${API}/${selected.missionId}`);
      setSelected(updated.data);
      const progressResponse = await axios.get(
        `${API}/${selected.missionId}/progress`
      );
      setProgress(progressResponse.data);
    } catch (err) {
      notify(err.response?.data?.message || "Status update failed.", true);
    }
  };

  const cancelMission = async () => {
    if (!selected) return;

    if (!window.confirm("Are you sure you want to cancel this mission?")) {
      return;
    }

    try {
      await axios.put(`${API}/${selected.missionId}/cancel`);
      notify("Mission cancelled successfully.");
      await loadMissions();

      const updated = await axios.get(`${API}/${selected.missionId}`);
      setSelected(updated.data);

      const progressResponse = await axios.get(
        `${API}/${selected.missionId}/progress`
      );
      setProgress(progressResponse.data);
    } catch (err) {
      notify(err.response?.data?.message || "Cancellation failed.", true);
    }
  };

  const filteredMissions = missions.filter((mission) => {
    const matchesSearch =
      (mission.droneId || "").toLowerCase().includes(search.toLowerCase()) ||
      (mission.missionId || "").toLowerCase().includes(search.toLowerCase());

    const matchesStatus =
      filter === "ALL" || mission.status === filter;

    return matchesSearch && matchesStatus;
  });

  const counts = {
    total: missions.length,
    active: missions.filter(
      m => ["STARTED", "IN_PROGRESS"].includes(m.status)
    ).length,
    completed: missions.filter(m => m.status === "COMPLETED").length,
    aborted: missions.filter(m => m.status === "ABORTED").length
  };

  const navItems = [
    { name: "Dashboard", icon: LayoutDashboard, page: "dashboard" },
    { name: "All Missions", icon: Plane, page: "missions" },
    { name: "Create Mission", icon: PlusCircle, page: "create" }
  ];

  const statusStyle = (status) => {
    if (status === "COMPLETED") return "green";
    if (status === "ABORTED") return "red";
    if (status === "IN_PROGRESS") return "blue";
    return "yellow";
  };

  const Status = ({ value }) => (
    <span className={`status ${statusStyle(value)}`}>
      <span className="status-dot"></span>
      {value || "UNKNOWN"}
    </span>
  );

  const StatCard = ({ title, value, icon: Icon, color }) => (
    <div className="stat-card">
      <div className={`stat-icon ${color}`}>
        <Icon size={21} />
      </div>
      <div>
        <p className="muted">{title}</p>
        <h2>{value}</h2>
      </div>
    </div>
  );

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileMenu ? "mobile-open" : ""}`}>
        <div className="brand">
          <div className="brand-icon"><Navigation size={23} /></div>
          <div>
            <h2>AEROSAGA</h2>
            <span>MISSION CONTROL</span>
          </div>
          <button className="mobile-close" onClick={() => setMobileMenu(false)}>
            <X size={20} />
          </button>
        </div>

        <p className="nav-label">WORKSPACE</p>

        {navItems.map((item) => (
          <button
            key={item.page}
            className={`nav-item ${page === item.page ? "active" : ""}`}
            onClick={() => {
              setPage(item.page);
              setMobileMenu(false);
            }}
          >
            <item.icon size={19} />
            {item.name}
          </button>
        ))}

        <div className="sidebar-bottom">
          <div className="connection">
            <span className="online-dot"></span>
            <div>
              <strong>Backend Connection</strong>
              <small>Local API · Port 8080</small>
            </div>
          </div>
          <p> AEROSAGA · DRONE CONTROL SYSTEM</p>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <button className="mobile-menu" onClick={() => setMobileMenu(true)}>
            <Menu />
          </button>
          <div>
            <p className="muted">AEROSAGA / WORKSPACE</p>
            <h2>
              {page === "dashboard" ? "Mission Dashboard" :
               page === "missions" ? "Mission Management" :
               page === "create" ? "Create New Mission" :
               "Mission Details"}
            </h2>
          </div>
          <div className="top-actions">
            <span className="live-indicator">
              <span className="online-dot"></span> SYSTEM ONLINE
            </span>
            <button className="icon-button" onClick={loadMissions}>
              <RefreshCw size={18} />
            </button>
          </div>
        </header>

        {(message || error) && (
          <div className={`notification ${error ? "error" : "success"}`}>
            {error || message}
          </div>
        )}

        {page === "dashboard" && (
          <>
            <section className="welcome">
              <div>
                <p className="eyebrow">AUTONOMOUS FLEET MANAGEMENT</p>
                <h1>Mission Overview</h1>
                <p>Monitor, manage and control your drone missions.</p>
              </div>
              <button className="primary-button" onClick={() => setPage("create")}>
                <PlusCircle size={18} /> Create Mission
              </button>
            </section>

            <section className="stats-grid">
              <StatCard title="Total Missions" value={counts.total} icon={Plane} color="purple" />
              <StatCard title="Active Missions" value={counts.active} icon={Activity} color="blue" />
              <StatCard title="Completed" value={counts.completed} icon={CheckCircle2} color="green" />
              <StatCard title="Aborted" value={counts.aborted} icon={XCircle} color="red" />
            </section>

            <section className="panel">
              <div className="panel-heading">
                <div>
                  <h3>Recent Missions</h3>
                  <p className="muted">Latest mission activity</p>
                </div>
                <button className="text-button" onClick={() => setPage("missions")}>
                  View all <ArrowUpRight size={16} />
                </button>
              </div>

              <MissionTable
                data={missions.slice(0, 5)}
                onSelect={openMission}
                loading={loading}
                Status={Status}
              />
            </section>
          </>
        )}

        {page === "missions" && (
          <section className="panel">
            <div className="panel-heading">
              <div>
                <h3>All Missions</h3>
                <p className="muted">Search and manage drone missions</p>
              </div>
              <button className="primary-button" onClick={() => setPage("create")}>
                <PlusCircle size={18} /> New Mission
              </button>
            </div>

            <div className="toolbar">
              <div className="search-box">
                <Search size={18} />
                <input
                  placeholder="Search drone ID or mission ID..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                />
              </div>
              <select value={filter} onChange={e => setFilter(e.target.value)}>
                <option value="ALL">All Statuses</option>
                <option value="STARTED">Started</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="COMPLETED">Completed</option>
                <option value="ABORTED">Aborted</option>
              </select>
            </div>

            <MissionTable
              data={filteredMissions}
              onSelect={openMission}
              loading={loading}
              Status={Status}
            />
          </section>
        )}

        {page === "create" && (
          <section className="form-panel">
            <div className="panel-heading">
              <div>
                <h3>Launch a New Mission</h3>
                <p className="muted">Enter drone and GPS coordinates.</p>
              </div>
              <div className="form-icon"><Plane size={25} /></div>
            </div>

            <form onSubmit={createMission}>
              <label>Drone ID</label>
              <input
                value={form.droneId}
                onChange={e => setForm({...form, droneId: e.target.value})}
                placeholder="e.g. DRONE001"
                required
              />

              <div className="coordinate-heading">
                <MapPin size={18} /> Starting Coordinates
              </div>
              <div className="form-grid">
                <div>
                  <label>Start Latitude</label>
                  <input type="number" step="any" required
                    value={form.startLatitude}
                    onChange={e => setForm({...form, startLatitude: e.target.value})}/>
                </div>
                <div>
                  <label>Start Longitude</label>
                  <input type="number" step="any" required
                    value={form.startLongitude}
                    onChange={e => setForm({...form, startLongitude: e.target.value})}/>
                </div>
              </div>

              <div className="coordinate-heading">
                <Navigation size={18} /> Target Coordinates
              </div>
              <div className="form-grid">
                <div>
                  <label>Target Latitude</label>
                  <input type="number" step="any" required
                    value={form.targetLatitude}
                    onChange={e => setForm({...form, targetLatitude: e.target.value})}/>
                </div>
                <div>
                  <label>Target Longitude</label>
                  <input type="number" step="any" required
                    value={form.targetLongitude}
                    onChange={e => setForm({...form, targetLongitude: e.target.value})}/>
                </div>
              </div>

              <div className="form-note">
                <AlertCircle size={17}/>
                Coordinates must be valid GPS values and the start and target must differ.
              </div>

              <button className="primary-button submit-button" type="submit">
                <Plane size={18}/> Launch Mission
              </button>
            </form>
          </section>
        )}

        {page === "details" && selected && (
          <section className="panel detail-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">MISSION IDENTIFICATION</p>
                <h3>{selected.droneId}</h3>
                <p className="muted mission-id">{selected.missionId}</p>
              </div>
              <Status value={selected.status}/>
            </div>

            <div className="detail-grid">
              <div className="detail-card">
                <MapPin size={19}/>
                <p className="muted">Start Coordinates</p>
                <strong>{selected.startLatitude}, {selected.startLongitude}</strong>
              </div>
              <div className="detail-card">
                <Navigation size={19}/>
                <p className="muted">Target Coordinates</p>
                <strong>{selected.targetLatitude}, {selected.targetLongitude}</strong>
              </div>
            </div>

            <div className="progress-section">
              <h3><Activity size={19}/> Mission Progress</h3>
              {progress ? (
                <>
                  <div className="progress-stage">{progress.currentStage || "Not available"}</div>
                  <p className="muted">Current workflow stage</p>
                  <Status value={progress.status}/>
                  <p className="muted">
                    Completed: {String(progress.completed)}
                  </p>
                </>
              ) : <p className="muted">Loading progress...</p>}
            </div>

            <div className="action-section">
              <div>
                <label>Update Mission Status</label>
                <div className="status-actions">
                  <select value={newStatus} onChange={e => setNewStatus(e.target.value)}>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="COMPLETED">COMPLETED</option>
                  </select>
                  <button className="secondary-button" onClick={updateStatus}>
                    Update Status
                  </button>
                </div>
              </div>
              {["STARTED", "IN_PROGRESS"].includes(selected.status) && (
                <button className="danger-button" onClick={cancelMission}>
                  <XCircle size={18}/> Cancel Mission
                </button>
              )}
            </div>

            <button className="secondary-button back-button" onClick={() => setPage("missions")}>
              Back to Missions
            </button>
          </section>
        )}
      </main>
    </div>
  );
}

function MissionTable({ data, onSelect, loading, Status }) {
  if (loading) return <p className="empty-state">Loading missions...</p>;

  if (!data.length) {
    return <div className="empty-state">
      <Plane size={32}/>
      <p>No missions found.</p>
    </div>;
  }

  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>MISSION ID</th>
            <th>DRONE</th>
            <th>STATUS</th>
            <th>START LATITUDE</th>
            <th>ACTION</th>
          </tr>
        </thead>
        <tbody>
          {data.map((mission) => (
            <tr key={mission.missionId}>
              <td className="id-cell">
                {mission.missionId?.slice(0, 12)}...
              </td>
              <td>{mission.droneId}</td>
              <td><Status value={mission.status}/></td>
              <td>{mission.startLatitude}</td>
              <td>
                <button className="view-button" onClick={() => onSelect(mission)}>
                  View Details <ArrowUpRight size={14}/>
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default App;