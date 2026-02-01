import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '@/context/AuthContext';
import { 
  getAllActiveServices,
  getServiceUIConfigByIntegrationId,
  getTabsForService,
  prepareServiceCardData,
  sortServices,
  filterServices
} from '@/controllers/ServiceUIConfigController';
import { 
  getOperations,
  searchOperations,
  getOperationExecutionConfig
} from '@/controllers/OperationController';
import { 
  getFormFieldsByOperationId 
} from '@/controllers/FormFieldController';
import { 
  createTransaction,
  executeTransaction 
} from '@/controllers/TransactionController';
import { 
  logAction,
  createStandardAuditLog 
} from '@/controllers/AuditController';
import { tokenAtom } from '@/recoil/tokenAtom';
import { useSetRecoilState, useRecoilValue } from 'recoil';

/**
 * Main hook for banking service integration
 */
export const useBankingServices = () => {
  const { user, getAuthHeader } = useAuth();
  const [services, setServices] = useState([]);
  const [selectedService, setSelectedService] = useState(null);
  const [operations, setOperations] = useState([]);
  const [selectedOperation, setSelectedOperation] = useState(null);
  const [tabs, setTabs] = useState([]);
  const token = useRecoilValue(tokenAtom);
  const [loading, setLoading] = useState({
    services: false,
    operations: false,
    tabs: false,
    transaction: false
  });
  const [error, setError] = useState(null);

  // Load all active services for teller dashboard
  const loadServices = useCallback(async () => {
    try {
      setLoading(prev => ({ ...prev, services: true }));
      setError(null);
      
      const authHeader = token;
      const response = await getAllActiveServices(authHeader);
      
      if (response && Array.isArray(response.data)) {
        const serviceCards = response.data.map(service => 
          prepareServiceCardData(service)
        );
        
        // Sort by display order and filter visible services
        const sortedServices = sortServices(serviceCards, 'order');
        const visibleServices = filterServices(sortedServices, { isVisible: true });
        
        setServices(visibleServices);
        return visibleServices;
      }
      return [];
    } catch (err) {
      setError(`Failed to load services: ${err.message}`);
      console.error('Error loading services:', err);
      return [];
    } finally {
      setLoading(prev => ({ ...prev, services: false }));
    }
  }, [getAuthHeader]);

  // Load service details and tabs
  const loadServiceDetails = useCallback(async (serviceId) => {
    try {
      setLoading(prev => ({ ...prev, tabs: true }));
      setError(null);
      
      const authHeader = token;
      
      // Get service configuration
      const serviceResponse = await getServiceUIConfigByIntegrationId(authHeader, serviceId);
      if (serviceResponse.data) {
        setSelectedService(serviceResponse.data);
        
        // Get tabs for this service
        const tabsResponse = await getTabsForService(authHeader, serviceId);
        if (tabsResponse.data) {
          const sortedTabs = tabsResponse.data
            .filter(tab => tab.isVisible)
            .sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0));
          
          setTabs(sortedTabs);
          
          // Load operations for each tab
          const operationPromises = sortedTabs.map(async (tab) => {
            try {
              const opResponse = await getOperations(authHeader);
              if (opResponse.data) {
                const tabOperation = opResponse.data.find(op => 
                  op.operationId === tab.operationId
                );
                return { ...tab, operation: tabOperation };
              }
              return tab;
            } catch (err) {
              console.warn(`Failed to load operation for tab ${tab.tabName}:`, err);
              return tab;
            }
          });
          
          const tabsWithOperations = await Promise.all(operationPromises);
          setTabs(tabsWithOperations);
          
          return tabsWithOperations;
        }
      }
      return [];
    } catch (err) {
      setError(`Failed to load service details: ${err.message}`);
      console.error('Error loading service details:', err);
      return [];
    } finally {
      setLoading(prev => ({ ...prev, tabs: false }));
    }
  }, [getAuthHeader]);

  // Load operations for a service
  const loadOperations = useCallback(async (integrationId) => {
    try {
      setLoading(prev => ({ ...prev, operations: true }));
      setError(null);
      
      const authHeader = token;
      const response = await searchOperations(authHeader, { 
        integrationId, 
        status: 'ACTIVE' 
      });
      
      if (response && Array.isArray(response.data)) {
        const activeOperations = response.data.filter(op => 
          op.status === 'ACTIVE'
        );
        setOperations(activeOperations);
        return activeOperations;
      }
      return [];
    } catch (err) {
      setError(`Failed to load operations: ${err.message}`);
      console.error('Error loading operations:', err);
      return [];
    } finally {
      setLoading(prev => ({ ...prev, operations: false }));
    }
  }, [getAuthHeader]);

  // Execute a transaction
  const executeServiceTransaction = useCallback(async (operationId, formData) => {
    try {
      setLoading(prev => ({ ...prev, transaction: true }));
      setError(null);
      
      const authHeader = token;
      
      // 1. Create transaction record
      const transactionData = {
        operationId,
        userId: user?.userId || 'SYSTEM',
        amount: formData.amount || 0,
        currency: 'USD',
        paymentMethod: formData.paymentMethod || 'BANK_TRANSFER',
        reference: `TRX-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
        description: formData.description || '',
        status: 'PENDING',
        metadata: formData
      };
      
      const transactionResponse = await createTransaction(authHeader, transactionData);
      
      if (transactionResponse.data) {
        const transactionId = transactionResponse.data.transactionId;
        
        // 2. Log audit action
        await logAction(authHeader, createStandardAuditLog(
          'CREATE',
          'TRANSACTION',
          transactionId,
          'TRANSACTION',
          {
            description: `Transaction created for operation ${operationId}`,
            newValue: JSON.stringify(transactionData)
          }
        ));
        
        // 3. Execute transaction
        const executionResponse = await executeTransaction(authHeader, transactionId);
        
        // 4. Log execution audit
        await logAction(authHeader, createStandardAuditLog(
          'EXECUTE',
          'TRANSACTION',
          transactionId,
          'TRANSACTION',
          {
            description: `Transaction executed`,
            newValue: JSON.stringify(executionResponse)
          }
        ));
        
        return {
          success: true,
          transactionId,
          data: executionResponse.data,
          receipt: executionResponse.receipt
        };
      }
      
      return { success: false, error: 'Failed to create transaction' };
    } catch (err) {
      setError(`Transaction failed: ${err.message}`);
      
      // Log error audit
      try {
        await logAction(token, createStandardAuditLog(
          'ERROR',
          'TRANSACTION',
          operationId,
          'OPERATION',
          {
            status: 'FAILED',
            description: `Transaction error: ${err.message}`,
            metadata: { formData }
          }
        ));
      } catch (auditError) {
        console.error('Failed to log audit:', auditError);
      }
      
      return { success: false, error: err.message };
    } finally {
      setLoading(prev => ({ ...prev, transaction: false }));
    }
  }, [user, getAuthHeader]);

  // Search services
  const searchServices = useCallback((searchTerm) => {
    if (!searchTerm.trim()) {
      loadServices();
      return;
    }
    
    const filtered = filterServices(services, { searchTerm });
    setServices(filtered);
  }, [services, loadServices]);

  // Select a service
  const selectService = useCallback(async (service) => {
    setSelectedService(service);
    
    // Load service details and operations
    await Promise.all([
      loadServiceDetails(service.id),
      loadOperations(service.id)
    ]);
  }, [loadServiceDetails, loadOperations]);

  // Select an operation (tab)
  const selectOperation = useCallback(async (operation) => {
    setSelectedOperation(operation);
    
    // Load form fields for this operation
    try {
      const authHeader = token;
      const formFieldsResponse = await getFormFieldsByOperationId(authHeader, operation.operationId);
      
      if (formFieldsResponse.data) {
        return formFieldsResponse.data;
      }
    } catch (err) {
      console.error('Error loading form fields:', err);
    }
    
    return [];
  }, [getAuthHeader]);

  return {
    // State
    services,
    selectedService,
    operations,
    selectedOperation,
    tabs,
    loading,
    error,
    
    // Actions
    loadServices,
    loadServiceDetails,
    loadOperations,
    selectService,
    selectOperation,
    executeServiceTransaction,
    searchServices,
    resetError: () => setError(null)
  };
};