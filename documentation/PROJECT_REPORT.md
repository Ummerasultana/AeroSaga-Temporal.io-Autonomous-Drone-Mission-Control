# 📄 AeroSaga: Autonomous Drone Mission Control
## Official Internship Project Technical Report

**Domain:** Java Development / Distributed Workflow Orchestration  
**Project Title:** AeroSaga — Temporal.io Autonomous Drone Mission Control  
**Architectural Framework:** Temporal.io Java SDK, Spring Boot 4.0, WebSockets, React 19, Vite  
**Developer:** Senior Engineering / Internship Project Submission  

---

## 1. Executive Summary

Autonomous Unmanned Aerial Vehicle (UAV) fleet operations demand zero-loss execution guarantees, resilient state tracking, real-time spatial awareness, and deterministic compensation in the event of hardware or environmental disruptions. Traditional mission dispatch architectures rely on ad-hoc thread pools, database-backed state machines, or polling-based messaging queues. Such designs suffer from distributed state drift, unrecoverable crashes during mid-flight worker failures, and difficult-to-audit compensation procedures.

**AeroSaga** resolves these critical engineering bottlenecks by leveraging **Temporal.io** as the distributed durable execution engine coupled with a **Spring Boot** telemetry gateway and a **React-based tactical mission control interface**. AeroSaga coordinates single and multi-drone delivery missions with end-to-end fault tolerance: mid-air emergency aborts safely reroute drones back to base, delivery failures automatically trigger compensation (Saga pattern), and live GPS telemetry streams to ground control operators via non-blocking WebSockets.

---

## 2. Problem Statement & Mission Objectives

In commercial drone delivery networks (medical supply transport, parcel logistics, tactical reconnaissance), a mission consists of discrete sequential and concurrent phases:
1. Pre-flight check & takeoff
2. Waypoint navigation across designated flight corridors
3. Package delivery at drop coordinates
4. Safe return to base station and landing

### Key Engineering Challenges
- **Process Failure Resilience:** If an orchestrator node or drone worker process terminates mid-flight, the mission state must survive and resume exactly where it was interrupted without re-executing completed operations.
- **Concurrent Fleet Coordination:** Missions involving multiple drones (`DRONE-101`, `DRONE-102`, `DRONE-103`) must execute in parallel without race conditions or shared-state corruption.
- **Asynchronous Operator Intervention:** Ground operators must possess the capability to signal an immediate Emergency Abort at any moment during transit, forcing the UAV into an immediate return-to-base sequence.
- **Failure Compensation (Saga Pattern):** If payload release fails at the delivery site, the drone must not be abandoned; it must execute compensation logic by securing the cargo, aborting the mission, and navigating back to home base.
- **Continuous Telemetry Tele-monitoring:** GPS coordinates, altitude, battery percentage, and packet timestamps must stream in real time to command dashboards with sub-second latency.

---

## 3. System Architecture & Component Design

The AeroSaga system follows a decoupled, resilient architecture partitioned into three coordinated tiers:

```text
+------------------------------------------------------------------------------------+
|                               GROUND CONTROL TIER                                  |
|   React 19 + Vite Dashboard: Telemetry HUD, Mission Control, Tactical Radar Map    |
+------------------------+------------------------------------+----------------------+
                         | REST API                           | WebSockets
                         v                                    v
+------------------------------------------------------------------------------------+
|                                TELEMETRY GATEWAY                                   |
|   Spring Boot 4 Application (Port 8081)                                            |
|   - MissionController & HealthController (REST Endpoints)                          |
|   - TelemetryWebSocketHandler (Broadcast broker on /telemetry/dashboard)           |
|   - MissionService (Registry, Duplicate Prevention, Temporal Client)               |
|   - TelemetryService (In-memory spatial cache, Drone status evaluation)            |
+------------------------+------------------------------------+----------------------+
                         |                                    ^
                         | Temporal gRPC (127.0.0.1:7233)     | WebSocket (/telemetry/drone)
                         v                                    |
+------------------------------------+       +---------------------------------------+
|          TEMPORAL SERVER           |       |           SIMULATION TIER             |
| Durable Execution History & Queues |       | Drone Telemetry Simulator             |
| Task Queue: DRONE_TASK_QUEUE       |       | (Simulates DRONE-101, 102, 103 GPS)   |
+------------------------+-----------+       +---------------------------------------+
                         |
                         v Task Queue Polling
+------------------------------------------------------------------------------------+
|                           TEMPORAL WORKER & ACTIVITIES                             |
|   DroneWorker.java                                                                 |
|   - Workflows: DroneMissionWorkflowImpl, MultiDroneMissionWorkflowImpl            |
|   - Activities: Takeoff, Navigate, DropPackage, ReturnToBase                       |
+------------------------------------------------------------------------------------+
```

### 3.1 Orchestration Tier: Temporal.io
Unlike traditional cron jobs or stateless REST services, Temporal workflows are **durable and deterministic**:
- **Workflow State Machines:** Both single-drone (`DroneMissionWorkflow`) and multi-drone (`MultiDroneMissionWorkflow`) orchestrators track workflow state transitions:
  - `PENDING` → `TAKING_OFF` → `NAVIGATING` → `DELIVERING` → `RETURNING` → `COMPLETED`
- **Emergency Abort:** Implemented as a Temporal `@SignalMethod void requestAbort()`. Workflow execution checks this signal between and during activities to trigger safe compensation.
- **Real-Time Queries:** Implemented as Temporal `@QueryMethod String getMissionStatus()`, enabling real-time status inspection directly from durable workflow memory without querying external databases.

### 3.2 Telemetry Gateway Tier: Spring Boot
The telemetry gateway acts as an event-driven message router:
- Exposes REST endpoints for dispatching missions, querying Temporal statuses, aborting missions, and checking service health.
- Maintains in-memory active mission registries and prevents duplicate mission dispatch for already engaged drones.
- Handles dual-ended WebSockets: `/telemetry/drone` ingests drone GPS packets while `/telemetry/dashboard` distributes them to browser clients.

### 3.3 Tactical Visualization Tier: React Dashboard
The frontend delivers a command-center interface:
- **Live Fleet Telemetry HUD:** Displays real-time battery bars, altitude gauges, coordinates, and online/stale status.
- **Multi-Drone Selector & Mission Dispatcher:** Select individual or multiple UAVs with duplicate-submission locking.
- **Real-Time Mission Stepper:** Visualizes exact workflow phases (`Takeoff`, `Navigation`, `Delivery`, `Return to Base`) with live polling and emergency abort controls.
- **Tactical Radar & Fleet Map:** Canvas-based spatial radar tracking drone coordinates, flight trails, and drop zones.
- **Mission History & System Alerts:** Filterable log of past executions and anomaly notifications.

---

## 4. Workflow Lifecycle & State Machine Modeling

### 4.1 Nominal Delivery Workflow
```text
[PENDING]
    │
    ▼
[TAKING_OFF] ──────► Activity: activities.takeoff(droneId)
    │
    ▼
[NAVIGATING] ──────► Activity: activities.navigate(droneId)
    │
    ▼
[DELIVERING] ──────► Activity: activities.dropPackage(droneId)
    │
    ▼
[RETURNING]  ──────► Activity: activities.returnToBase(droneId)
    │
    ▼
[COMPLETED]
```

### 4.2 Emergency Abort State Machine
```text
[ANY RUNNING STATE] ──(Operator Abort Signal)──► [ABORTING]
                                                     │
                                                     ▼
                                        Activity: activities.returnToBase(droneId)
                                                     │
                                                     ▼
                                                 [ABORTED]
```

### 4.3 Package Delivery Failure & Compensation (Saga Pattern)
```text
[DELIVERING]
    │
    ├── (Delivery Exception Thrown)
    ▼
[RETURNING] ──────► Compensation Activity: activities.returnToBase(droneId)
    │
    ▼
[FAILED]    ──────► Workflow completes in FAILED state with ApplicationFailure
```

---

## 5. Implementation Verification & Test Results

The backend testing suite was executed with JUnit 6.0 and Java 25, comprising 23 automated tests across unit and integration levels:

| Test Class | Focus Area | Scenarios Evaluated | Result |
|---|---|---|---|
| `TelemetryServiceTest` | Spatial Cache & Health | Known drone validation, telemetry updates, active/stale/offline thresholds | **PASSED (3/3)** |
| `DroneMissionActivitiesTest` | Temporal Activities | Takeoff, navigation, drop package, simulated failure mode, return to base | **PASSED (3/3)** |
| `MissionServiceTest` | Business Logic & Workflow Client | Single drone launch, multi-drone concurrent launch, duplicate mission prevention, invalid drone rejection, query status, abort signal dispatch | **PASSED (9/9)** |
| `MissionControllerTest` | REST API Layer | JSON array & object payloads, 201 Created, 200 OK status, 404 Not Found, active & history retrieval | **PASSED (5/5)** |
| `TelemetryWebSocketHandlerTest` | WebSocket Broker | Connection lifecycle, telemetry broadcast synchronization, malformed JSON resilience | **PASSED (3/3)** |

**Overall Test Suite Result:** `23 Tests Run, 0 Failures, 0 Errors, 0 Skipped` (Total build time: ~7.9s).  
**Frontend Linter & Build Result:** Oxlint passed with `0 warnings, 0 errors`; Vite production bundle built in `221ms`.

---

## 6. Architectural Justification: No External Relational Database

The assignment guidelines note that databases should only be integrated when technically justified. AeroSaga intentionally operates without an external SQL database for the following architectural reasons:
1. **Temporal Workflow Durability:** In Temporal, the workflow history is durable, append-only, and fault-tolerant by design. Workflow state, execution timestamps, activity inputs, signals, and results are persisted in Temporal's underlying storage. Adding an external relational database to duplicate workflow states would introduce data synchronization lag, two-phase commit overhead, and dual-write divergence.
2. **Real-Time Gateway State:** The Spring Boot gateway retains an active mission registry and in-memory spatial cache for sub-second REST/WebSocket response times.
3. **Operational Simplicity:** Developers and evaluators can spin up the entire system with Docker and Maven in under two minutes without running SQL migration scripts or provisioning relational database containers.

---

## 7. Conclusion & Future Roadmap

AeroSaga successfully fulfills all requirements of the internship project assignment:
- Robust Temporal workflow orchestration for autonomous drone logistics.
- Concurrent multi-drone coordination and real-time execution tracking.
- Seamless emergency abort handling and delivery failure compensation.
- High-throughput WebSocket telemetry pipeline with real-time HUD and tactical canvas radar.
- Zero fake APIs, zero placeholder buttons, and comprehensive automated test coverage.

### Future Enhancements:
1. Integration of simulated geofencing boundaries and dynamic no-fly zones.
2. Integration with physical PX4/MAVLink drone autopilots over UDP.
3. Automated battery return-to-base triggers when charge drops below 15%.
