import { API_CONFIG } from "../config/APIConfig.js";

// Generic API call function
export const apiCall = async (endpoint, options = {}) => {
    try {
        console.log(`🌐 API Call: ${ API_CONFIG.BASE_URL }${ endpoint }`, {
            method: options.method,
            body: options.body,
            headers: API_CONFIG.HEADERS
        });

        const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
            headers: API_CONFIG.HEADERS,
            ...options
        });

        // console.log("apiResponse:::::" + JSON.stringify(response));

        // console.log(`📡 Response status: ${response.status} for ${endpoint}`);

        // if (!response.ok) {
        //     throw new Error(`HTTP error! status: ${response.status}`);
        // }

        const data = await response.json();
        // console.log(`✅ API Response for ${endpoint}:`, data);
        return data;
    } catch (error) {
        // console.error(`❌ API Call failed for ${endpoint}:`, error);
        throw error;
    }
};