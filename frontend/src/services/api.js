const API_BASE = "http://localhost:8081/api";

export async function fetchHealth() {
  const res = await fetch(`${API_BASE}/health`);
  if (!res.ok) throw new Error(`Health check failed: ${res.statusText}`);
  return res.json();
}

export async function fetchDrones() {
  const res = await fetch(`${API_BASE}/missions/drones`);
  if (!res.ok) throw new Error(`Failed to fetch drones: ${res.statusText}`);
  return res.json();
}

export async function startMission(droneIds) {
  const res = await fetch(`${API_BASE}/missions/start`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(droneIds),
  });

  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || `Failed to start mission (${res.status})`);
  }
  return data;
}

export async function getMissionStatus(workflowId) {
  const res = await fetch(`${API_BASE}/missions/${encodeURIComponent(workflowId)}/status`);
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || `Failed to fetch mission status (${res.status})`);
  }
  return data;
}

export async function abortMission(workflowId) {
  const res = await fetch(`${API_BASE}/missions/${encodeURIComponent(workflowId)}/abort`, {
    method: "POST",
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || `Failed to abort mission (${res.status})`);
  }
  return data;
}

export async function getActiveMissions() {
  const res = await fetch(`${API_BASE}/missions/active`);
  if (!res.ok) throw new Error(`Failed to fetch active missions: ${res.statusText}`);
  return res.json();
}

export async function getMissionHistory() {
  const res = await fetch(`${API_BASE}/missions/history`);
  if (!res.ok) throw new Error(`Failed to fetch mission history: ${res.statusText}`);
  return res.json();
}
