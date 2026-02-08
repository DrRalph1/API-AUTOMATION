// hooks/useApi.js
import { useSetRecoilState } from 'recoil';
import { tokenAtom } from '@/recoil/tokenAtom';
import useAuthToken from './useAuthToken';
import { refreshToken } from '@/controllers/UserManagementController';
import { tokenAtom } from '@/recoil/tokenAtom';
import { useSetRecoilState, useRecoilValue } from 'recoil';

export const useApi = () => {
  const { getAuthHeader } = useAuthToken();
  const token = useRecoilValue(tokenAtom);
  const setRecoilToken = useSetRecoilState(tokenAtom);

  const callWithAuth = async (apiFunction, ...args) => {
    const authHeader = token;
    
    if (!authHeader) {
      throw new Error('No authentication token available');
    }

    try {
      // First attempt with current token
      return await apiFunction(authHeader, ...args);
    } catch (error) {
      const isUnauthorized =
        error?.responseCode === 401 ||
        error?.response?.status === 401 ||
        error?.message?.toLowerCase().includes("unauthorized");

      if (!isUnauthorized) {
        throw error;
      }

      console.warn('🔄 API call returned 401, attempting token refresh...');
      
      try {
        const refreshResponse = await refreshToken(token);
        
        if (refreshResponse.data?.token) {
          const newToken = refreshResponse.data.token;
          
          // Update Recoil atom with new token
          console.log('✅ Token refreshed, updating Recoil atom...');
          setRecoilToken(newToken);
          
          // Retry with new token
          const newAuthHeader = `Bearer ${newToken}`;
          return await apiFunction(newAuthHeader, ...args);
        } else {
          throw new Error('Token refresh failed');
        }
      } catch (refreshError) {
        console.error('❌ Token refresh failed:', refreshError);
        
        // Clear token on refresh failure
        setRecoilToken(null);
        
        throw new Error('Session expired. Please login again.');
      }
    }
  };

  return {
    callWithAuth,
    token,
    getAuthHeader
  };
};