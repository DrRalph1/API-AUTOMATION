// src/config/env.js

// Development environment variables (fallback for local development)
const developmentEnv = {
    REACT_APP_API_BASE_URL: "http://192.168.0.127:4789",
    REACT_APP_API_KEY: "sTr@wBerry$!",
    REACT_APP_API_SECRET: "iCeCre@M&!",
    REACT_APP_CLIENT_IP: "127.0.0.1",
    REACT_APP_API_TIMEOUT: "10000",
    REACT_APP_ENVIRONMENT: "development"
};

// Function to get environment variables
export const getEnv = () => {
    // Try to get from process.env (will work if webpack DefinePlugin is configured)
    // If not available, use development fallbacks
    return {
        REACT_APP_API_BASE_URL: process.env.REACT_APP_API_BASE_URL || developmentEnv.REACT_APP_API_BASE_URL,
        REACT_APP_API_KEY: process.env.REACT_APP_API_KEY || developmentEnv.REACT_APP_API_KEY,
        REACT_APP_API_SECRET: process.env.REACT_APP_API_SECRET || developmentEnv.REACT_APP_API_SECRET,
        REACT_APP_CLIENT_IP: process.env.REACT_APP_CLIENT_IP || developmentEnv.REACT_APP_CLIENT_IP,
        REACT_APP_API_TIMEOUT: process.env.REACT_APP_API_TIMEOUT || developmentEnv.REACT_APP_API_TIMEOUT,
        REACT_APP_ENVIRONMENT: process.env.REACT_APP_ENVIRONMENT || developmentEnv.REACT_APP_ENVIRONMENT,
    };
};

export const env = getEnv();