// hooks/useAuthToken.js
import { useRecoilValue } from 'recoil';
import { tokenAtom } from '@/recoil/tokenAtom';

const useAuthToken = () => {
  const token = useRecoilValue(tokenAtom);

  // Debug: log token changes
  useEffect(() => {
    console.log('🔐 useAuthToken - Current token:', token ? `${token.substring(0, 20)}...` : 'null');
  }, [token]);
  
  const getAuthHeader = useCallback(() => {
    if (!token || token === "" || token === "null" || token === "undefined") {
      console.warn('❌ useAuthToken - No valid token available');
      return null;
    }
    return `Bearer ${token}`;
  }, [token]);

  const isAuthenticated = useMemo(() => {
    return !!(token && token !== "" && token !== "null" && token !== "undefined");
  }, [token]);

  return {
    token,
    getAuthHeader,
    isAuthenticated,
  };
};

export default useAuthToken;