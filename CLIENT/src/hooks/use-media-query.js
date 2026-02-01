import { useEffect, useState } from "react";

/**
 * useMediaQuery (JavaScript version)
 * Listens to a CSS media query and returns whether it matches.
 *
 * @param {string} query - A valid CSS media query, e.g. "(min-width: 768px)"
 * @returns {boolean}
 */
export function useMediaQuery(query) {
  const getMatches = (q) => {
    if (typeof window !== "undefined") {
      return window.matchMedia(q).matches;
    }
    return false;
  };

  const [matches, setMatches] = useState(getMatches(query));

  useEffect(() => {
    if (typeof window === "undefined") return;

    const mediaQueryList = window.matchMedia(query);

    const listener = (event) => {
      setMatches(event.matches);
    };

    // Set initial value
    setMatches(mediaQueryList.matches);

    if (mediaQueryList.addEventListener) {
      mediaQueryList.addEventListener("change", listener);
    } else {
      // Safari < 14
      mediaQueryList.addListener(listener);
    }

    return () => {
      if (mediaQueryList.removeEventListener) {
        mediaQueryList.removeEventListener("change", listener);
      } else {
        mediaQueryList.removeListener(listener);
      }
    };
  }, [query]);

  return matches;
}
