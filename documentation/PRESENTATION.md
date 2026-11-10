# 🚁 AeroSaga: Temporal.io Autonomous Drone Mission Control
## Internship Project Final Defense Presentation (Slide Deck)

---

### Slide 1: Title Slide
- **Title:** AeroSaga: Autonomous Drone Mission Control
- **Subtitle:** Resilient UAV Fleet Orchestration using Temporal.io, Spring Boot & React
- **Domain:** Java Development / Distributed Systems Engineering
- **Presenter:** Ummera Sultana (Internship Candidate)
- **Key Concepts:** Workflow Orchestration, Durable Execution, Multi-UAV Coordination, Real-Time Telemetry

---

### Slide 2: The Challenge of Autonomous Drone Logistics
- **Problem Statement:**
  - Modern drone logistics require strict guarantees: payloads must be accounted for, flights must be monitored, and unexpected failures must be mitigated immediately.
- **Traditional Limitations:**
  - Ad-hoc thread pools lose state if a server process crashes or restarts.
  - Relational database polling creates lag and race conditions.
  - Hard to handle compensations (e.g. package delivery fails halfway).
- **The AeroSaga Solution:**
  - Decoupled, event-driven architecture using **Temporal.io** for durable execution guarantees, **Spring Boot** for telemetry broadcasting, and **React** for ground-control operations.

---

### Slide 3: AeroSaga Architecture Overview
- **Ground Control Tier:** React 19 + Vite dashboard with live telemetry HUD, multi-drone selector, mission stepper, and spatial radar map.
- **Telemetry Gateway Tier:** Spring Boot application running on port `8081` managing REST endpoints and WebSocket relays.
- **Workflow Engine:** Temporal Development Server on gRPC port `7233` and Web UI on port `8233`.
- **Worker & Activities:** `DroneWorker` registering workflow implementations and mission activities (`takeoff`, `navigate`, `dropPackage`, `returnToBase`).
- **Telemetry Simulator:** Simulates 3 UAVs (`DRONE-101`, `DRONE-102`, `DRONE-103`) emitting live GPS coordinates and battery discharge over WebSockets.

---

### Slide 4: Nominal Mission State Machine
```text
PENDING ──► TAKING_OFF ──► NAVIGATING ──► DELIVERING ──► RETURNING ──► COMPLETED
```
- **Takeoff:** Pre-flight diagnostics and initial ascent.
- **Navigation:** Transit across delivery corridors with simulated progress intervals.
- **Delivery:** Controlled descent and cargo release.
- **Return to Base:** Ingress navigation back to home base coordinates followed by safe touchdown.
- **Deterministic Guarantees:** State transitions occur within workflow code, while non-deterministic actions run as Temporal activities.

---

### Slide 5: Fault Tolerance & Failure Compensation (Saga Pattern)
- **Delivery Failure Compensation:**
  - If `dropPackage` encounters an anomaly (weather, mechanical latch fault, customer obstruction):
  - Catch block intercepts the failure, preserves the cargo, transitions status to `RETURNING`, and invokes `activities.returnToBase(droneId)`.
  - Workflow terminates with `FAILED` and raises `ApplicationFailure` after compensation is safely confirmed.
- **Process Recovery:**
  - If the worker process restarts mid-flight, Temporal's event history replays preceding activities from memory and resumes transit seamlessly.

---

### Slide 6: Real-Time Emergency Abort Signaling
- **Operational Need:** Ground control must be capable of recalling a drone mid-transit for immediate airspace deconfliction.
- **Implementation:**
  - REST endpoint: `POST /api/missions/{workflowId}/abort`.
  - Temporal Signal: `WorkflowStub.signal("requestAbort")`.
  - Workflow intercepts the signal, shifts state from `RUNNING` to `ABORTING`, executes `returnToBase`, and completes as `ABORTED`.
  - Zero fake status: The dashboard displays actual workflow signal acknowledgement.

---

### Slide 7: Concurrent Multi-Drone Sorties
- **Concurrency Support:**
  - Launch individual drones or coordinate multiple drones simultaneously (`DRONE-101`, `DRONE-102`, `DRONE-103`).
  - Implemented via `MultiDroneMissionWorkflow` utilizing Temporal `Async.function` and `Promise.allOf`.
  - Per-drone status tracking exposed through Temporal queries (`getDroneStatuses`).
- **Duplicate Dispatch Prevention:**
  - Automatic validation preventing double-clicks or assigning a drone that is already engaged in an active mission.

---

### Slide 8: Real-Time Telemetry & Tactical Radar Map
- **Dual-Path WebSockets:**
  - `/telemetry/drone`: Ingests simulated telemetry (latitude, longitude, altitude, battery %, timestamp).
  - `/telemetry/dashboard`: Non-blocking push to all active browser dashboards.
- **Interactive Canvas Radar:**
  - Real-time GPS coordinate mapping centered at base station.
  - Rotating radar sweep, 500m–2000m range rings, flight path trails, and drop-target zones.
  - Zero paid API keys or external dependencies required.

---

### Slide 9: Verification & Test Results
- **23 Automated Backend Tests Passed:**
  - `TelemetryServiceTest` (3 tests): Validation, spatial cache, active/stale/offline detection.
  - `DroneMissionActivitiesTest` (3 tests): Nominal activities, simulated package drop failures.
  - `MissionServiceTest` (9 tests): Single & multi-drone starts, duplicate prevention, aborts, history.
  - `MissionControllerTest` (5 tests): REST contracts, status codes, payload parsing.
  - `TelemetryWebSocketHandlerTest` (3 tests): Broker synchronization, session cleanup, malformed JSON resilience.
- **Frontend Code Quality:**
  - Oxlint passed with **0 warnings, 0 errors**.
  - Production Vite build generated in **221ms**.

---

### Slide 10: Final Demonstration Script
1. **System Startup:** Start Temporal Docker container → Start Spring Boot (`TelemetryApplication`) → Start `DroneWorker` → Start `TelemetrySimulator` → Start React (`npm run dev`).
2. **Dashboard Verification:** View 3 drones connected with live battery and GPS updates.
3. **Single Drone Sortie:** Dispatch `DRONE-101`, observe progress through `TAKING_OFF` → `NAVIGATING` → `DELIVERING` → `RETURNING` → `COMPLETED`.
4. **Emergency Abort Demo:** Dispatch `DRONE-102`, click **Abort Mission** during navigation, observe transition to `ABORTING` → `RETURNING` → `ABORTED`.
5. **Multi-Drone Concurrent Sortie:** Select `DRONE-101` and `DRONE-103`, launch concurrent mission, observe parallel execution on Temporal UI (`localhost:8233`).
6. **Mission History Inspection:** Open Mission History view and verify recorded durable workflow executions.
