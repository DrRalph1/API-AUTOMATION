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

        // Check if response has content
        const responseText = await response.text();
        
        let data = {};
        if (responseText) {
            try {
                data = JSON.parse(responseText);
            } catch (e) {
                data = { message: responseText };
            }
        }

        // Handle failed responses with user-friendly messages
        if (!response.ok) {
            let errorMessage = "";
            
            // User-friendly messages based on status code
            switch (response.status) {
                case 401:
                    errorMessage = data.message || "Invalid username or password. Please try again.";
                    break;
                case 403:
                    errorMessage = "You don't have permission to access this account. Please contact your administrator.";
                    break;
                case 404:
                    errorMessage = "Service not found. Please try again later.";
                    break;
                case 500:
                    errorMessage = "Server error. Please try again in a few moments.";
                    break;
                default:
                    errorMessage = data.message || `Login failed. Please check your credentials and try again.`;
            }
            
            const error = new Error(errorMessage);
            error.status = response.status;
            throw error;
        }

        console.log(`✅ API Response for ${endpoint}:`, data);
        return data;
        
    } catch (error) {
        // Make sure error always has a user-friendly message
        if (!error.message || error.message.includes("JSON") || error.message.includes("fetch")) {
            error.message = "Unable to connect to server. Please check your internet connection and try again.";
        }
        console.error(`❌ API Call failed for ${endpoint}:`, error);
        throw error;
    }
};