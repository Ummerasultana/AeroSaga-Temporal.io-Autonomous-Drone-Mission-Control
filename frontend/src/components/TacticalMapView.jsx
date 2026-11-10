import { useEffect, useRef } from "react";

export default function TacticalMapView({ drones, activeMission }) {
  const canvasRef = useRef(null);
  const trailsRef = useRef({});

  // Base station coordinates (Bellary hub)
  const BASE_LAT = 15.1394;
  const BASE_LON = 76.9214;
  const SCALE = 35000; // coordinate to pixel scaling

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    let animationFrameId;

    // Update trails
    Object.values(drones).forEach((d) => {
      if (d.latitude && d.longitude) {
        if (!trailsRef.current[d.id]) {
          trailsRef.current[d.id] = [];
        }
        const trail = trailsRef.current[d.id];
        const last = trail[trail.length - 1];
        if (!last || last.lat !== d.latitude || last.lon !== d.longitude) {
          trail.push({ lat: d.latitude, lon: d.longitude });
          if (trail.length > 50) trail.shift();
        }
      }
    });

    let angle = 0;

    const render = () => {
      const width = canvas.width;
      const height = canvas.height;
      const centerX = width / 2;
      const centerY = height / 2;

      // Clear with dark tactical background
      ctx.fillStyle = "#090d16";
      ctx.fillRect(0, 0, width, height);

      // Draw grid
      ctx.strokeStyle = "rgba(30, 41, 59, 0.6)";
      ctx.lineWidth = 1;
      const gridSize = 40;
      for (let x = 0; x < width; x += gridSize) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, height);
        ctx.stroke();
      }
      for (let y = 0; y < height; y += gridSize) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
        ctx.stroke();
      }

      // Draw range rings
      const rings = [70, 140, 210, 280];
      rings.forEach((r, idx) => {
        ctx.beginPath();
        ctx.arc(centerX, centerY, r, 0, Math.PI * 2);
        ctx.strokeStyle = "rgba(56, 189, 248, 0.15)";
        ctx.setLineDash([4, 4]);
        ctx.stroke();
        ctx.setLineDash([]);

        // Ring label
        ctx.fillStyle = "#475569";
        ctx.font = "10px monospace";
        ctx.fillText(`${(idx + 1) * 500}m`, centerX + r + 5, centerY - 5);
      });

      // Rotating radar sweep
      angle += 0.02;
      const sweepGradient = ctx.createConicGradient(angle, centerX, centerY);
      sweepGradient.addColorStop(0, "rgba(56, 189, 248, 0.25)");
      sweepGradient.addColorStop(0.1, "rgba(56, 189, 248, 0.05)");
      sweepGradient.addColorStop(0.2, "rgba(56, 189, 248, 0.0)");
      sweepGradient.addColorStop(1, "rgba(56, 189, 248, 0.0)");

      ctx.fillStyle = sweepGradient;
      ctx.beginPath();
      ctx.arc(centerX, centerY, 280, 0, Math.PI * 2);
      ctx.fill();

      // Crosshairs
      ctx.strokeStyle = "rgba(56, 189, 248, 0.3)";
      ctx.beginPath();
      ctx.moveTo(centerX - 290, centerY);
      ctx.lineTo(centerX + 290, centerY);
      ctx.moveTo(centerX, centerY - 290);
      ctx.lineTo(centerX, centerY + 290);
      ctx.stroke();

      // Draw Base Station Hub
      ctx.fillStyle = "#38bdf8";
      ctx.beginPath();
      ctx.arc(centerX, centerY, 6, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = "#38bdf8";
      ctx.beginPath();
      ctx.arc(centerX, centerY, 12, 0, Math.PI * 2);
      ctx.stroke();

      ctx.fillStyle = "#94a3b8";
      ctx.font = "11px sans-serif";
      ctx.fillText("HQ BASE", centerX + 15, centerY + 4);

      // Draw Delivery Drop Targets
      const dropTargets = [
        { name: "DROP-ZONE A", dx: 130, dy: -90 },
        { name: "DROP-ZONE B", dx: -150, dy: 100 },
        { name: "DROP-ZONE C", dx: 110, dy: 140 },
      ];

      dropTargets.forEach((t) => {
        const tx = centerX + t.dx;
        const ty = centerY + t.dy;

        ctx.strokeStyle = "rgba(234, 179, 8, 0.6)";
        ctx.strokeRect(tx - 10, ty - 10, 20, 20);

        ctx.fillStyle = "rgba(234, 179, 8, 0.15)";
        ctx.fillRect(tx - 10, ty - 10, 20, 20);

        ctx.fillStyle = "#eab308";
        ctx.font = "10px monospace";
        ctx.fillText(t.name, tx + 14, ty + 4);
      });

      // Colors for drones
      const droneColors = {
        "DRONE-101": "#38bdf8",
        "DRONE-102": "#a855f7",
        "DRONE-103": "#ec4899",
      };

      // Draw Drones & Flight Trails
      Object.values(drones).forEach((d) => {
        const color = droneColors[d.id] || "#22c55e";
        const dx = (d.longitude - BASE_LON) * SCALE;
        const dy = -(d.latitude - BASE_LAT) * SCALE; // Invert latitude for canvas Y

        const x = centerX + dx;
        const y = centerY + dy;

        // Draw Trail
        const trail = trailsRef.current[d.id] || [];
        if (trail.length > 1) {
          ctx.beginPath();
          ctx.strokeStyle = color;
          ctx.lineWidth = 1.5;
          ctx.globalAlpha = 0.4;
          for (let i = 0; i < trail.length; i++) {
            const ptX = centerX + (trail[i].lon - BASE_LON) * SCALE;
            const ptY = centerY - (trail[i].lat - BASE_LAT) * SCALE;
            if (i === 0) ctx.moveTo(ptX, ptY);
            else ctx.lineTo(ptX, ptY);
          }
          ctx.stroke();
          ctx.globalAlpha = 1.0;
        }

        // Pulse ring around drone
        ctx.strokeStyle = color;
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.arc(x, y, 14 + Math.sin(angle * 3) * 3, 0, Math.PI * 2);
        ctx.stroke();

        // Drone core blip
        ctx.fillStyle = color;
        ctx.beginPath();
        ctx.arc(x, y, 5, 0, Math.PI * 2);
        ctx.fill();

        // Drone Info Tag
        ctx.fillStyle = "#ffffff";
        ctx.font = "bold 11px sans-serif";
        ctx.fillText(d.id, x + 16, y - 8);

        ctx.fillStyle = "#94a3b8";
        ctx.font = "10px monospace";
        ctx.fillText(`ALT: ${d.altitude?.toFixed(0) || 0}m | BAT: ${d.battery?.toFixed(0) || 0}%`, x + 16, y + 6);
        ctx.fillText(`GPS: ${d.latitude?.toFixed(4)}, ${d.longitude?.toFixed(4)}`, x + 16, y + 18);
      });

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, [drones, activeMission]);

  return (
    <div className="tactical-map-container">
      <div className="tactical-map-header">
        <div>
          <h3>🛰️ Tactical Fleet Radar (Live GPS Coordinates)</h3>
          <p>Real-time spatial telemetry tracking centered at AeroSaga HQ (Bellary)</p>
        </div>
        <div className="map-legend">
          <span className="legend-tag" style={{ color: "#38bdf8" }}>● DRONE-101</span>
          <span className="legend-tag" style={{ color: "#a855f7" }}>● DRONE-102</span>
          <span className="legend-tag" style={{ color: "#ec4899" }}>● DRONE-103</span>
          <span className="legend-tag" style={{ color: "#eab308" }}>■ Drop Targets</span>
        </div>
      </div>
      <div className="canvas-wrapper">
        <canvas ref={canvasRef} width={800} height={460} className="radar-canvas" />
      </div>
    </div>
  );
}
