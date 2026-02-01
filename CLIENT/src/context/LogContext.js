import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

const LogContext = createContext(null);
const STORAGE_KEY = "logs:entries";

export function LogProvider(props) {
  const [logs, setLogs] = useState([]);



  const log = useCallback((entry) => {
    const now = new Date().toISOString();
    const payload = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      time: now,
      level: entry.level ?? "info",
      category: entry.category ?? "system",
      action: entry.action ?? "event",
      message: entry.message,
      user: entry.user ?? null,
      route: entry.route,
      meta: entry.meta ?? null,
    };
    setLogs((prev) => [payload, ...prev].slice(0, 2000));
  }, []);

  const clear = useCallback(() => setLogs([]), []);
  const exportJSON = useCallback(() => JSON.stringify(logs, null, 2), [logs]);

  const value = useMemo(() => ({ logs, log, clear, exportJSON }), [logs, log, clear, exportJSON]);
  return React.createElement(LogContext.Provider, { value }, props.children);
}

export function useLog() {
  const ctx = useContext(LogContext);
  if (!ctx) throw new Error("useLog must be used within LogProvider");
  return ctx;
}
