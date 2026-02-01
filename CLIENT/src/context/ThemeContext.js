import React, { createContext, useContext, useEffect, useMemo, useState } from "react";

const ThemeContext = createContext(null);

export function ThemeProvider(props) {
  // Initialize theme from system preference only
  const [theme, setTheme] = useState(() => {
    try {
      // Use system preference
      const mq = window.matchMedia("(prefers-color-scheme: dark)");
      return mq.matches ? "dark" : "light";
    } catch {
      return "light";
    }
  });

  // Apply theme to document
  useEffect(() => {
    const root = document.documentElement;
    if (theme === "dark") {
      root.classList.add("dark");
    } else {
      root.classList.remove("dark");
    }
  }, [theme]);

  // Listen for system theme changes only if no user preference is saved
  useEffect(() => {
    try {
      const savedTheme = localStorage.getItem("PaymentPortal-theme");
      if (savedTheme) return; // Don't override user preference
      
      const mq = window.matchMedia("(prefers-color-scheme: dark)");
      const onChange = (e) => setTheme(e.matches ? "dark" : "light");
      mq.addEventListener("change", onChange);
      return () => mq.removeEventListener("change", onChange);
    } catch {}
  }, []);

  const value = useMemo(() => ({ 
    theme, 
    setTheme: (newTheme) => {
      setTheme(newTheme);
    }, 
    toggle: () => {
      const newTheme = theme === "dark" ? "light" : "dark";
      setTheme(newTheme);
    }
  }), [theme]);

  return React.createElement(ThemeContext.Provider, { value }, props.children);
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("useTheme must be used within ThemeProvider");
  return ctx;
}
