// src/config/APIConfig.js
import { env } from './ENVConfig.js';

// console.log("🔧 Loaded environment variables:", env);

export const API_CONFIG = {
    BASE_URL: env.REACT_APP_API_BASE_URL + "/plx/api",
    HEADERS: {
        "Content-Type": "application/json",
        "x-api-key": env.REACT_APP_API_KEY,
        "x-api-secret": env.REACT_APP_API_SECRET,
        "x-forwarded-for": env.REACT_APP_CLIENT_IP,
    },
    TIMEOUT: parseInt(env.REACT_APP_API_TIMEOUT) || 10000,
};