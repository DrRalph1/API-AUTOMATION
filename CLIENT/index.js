// src/index.js
import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App.js";
import "./global.css"; // Tailwind/global styles

// Find root container
const container = document.getElementById("root");
if (!container) {
  throw new Error("❌ Root element #root not found in index.html");
}

// Mount React
const root = createRoot(container);
root.render(<App />);
