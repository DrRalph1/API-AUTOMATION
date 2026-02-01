// controllers/ServiceController.js
// This is a wrapper/alias file that maps the old service function names
// to the actual ServiceUIConfigController functions

import {
  // Main functions
  getServiceUIConfigs as getServices,
  getServiceUIConfigById as getServiceById,
  getServiceUIConfigByIntegrationId as getServiceByIntegrationId,
  createServiceUIConfig as createService,
  updateServiceUIConfig as updateService,
  deleteServiceUIConfig as deleteService,
  getAllServiceUIConfigs as getAllServices,
  searchServiceUIConfigs as searchServices,
  
  // Additional functions
  getAllActiveServices,
  getFeaturedServices,
  getServicesByCategory,
  getPopularServices,
  recordServiceUsage,
  getServiceStatistics,
  
  // Response handlers
  handleServiceUIConfigResponse as handleServiceResponse,
  extractServiceUIConfigPaginationInfo as extractServicePaginationInfo,
  
  // Validation and DTO builders
  validateServiceUIConfigData as validateServiceData,
  buildServiceUIConfigDTO as buildServiceDTO,
  buildBulkServiceUIConfigsRequest as buildBulkServicesRequest,
  
  // UI helpers
  prepareServiceCardData,
  getServiceCategoryBadge,
  getServiceStatusBadge,
  getDefaultServiceIcon,
  sortServices,
  filterServices,
  extractServiceStatistics
} from './ServiceUIConfigController.js';

// Alias for toggleServiceStatus (not directly available, so we create it)
/**
 * Toggle service status between ACTIVE and INACTIVE
 * @param {string} authorizationHeader - Bearer token
 * @param {string} serviceId - Service ID
 * @param {string} newStatus - New status (ACTIVE/INACTIVE)
 * @returns {Promise} API response
 */
export const toggleServiceStatus = async (authorizationHeader, serviceId, newStatus) => {
  try {
    // First get the current service
    const currentService = await getServiceById(authorizationHeader, serviceId);
    
    if (!currentService || !currentService.data) {
      throw new Error('Service not found');
    }
    
    const serviceData = currentService.data;
    
    // Update the service with new status
    const updateData = {
      ...serviceData,
      status: newStatus.toUpperCase()
    };
    
    // Remove internal fields that shouldn't be sent
    delete updateData.configId;
    delete updateData.createdAt;
    delete updateData.updatedAt;
    delete updateData.createdBy;
    delete updateData.updatedBy;
    
    return await updateService(authorizationHeader, serviceId, updateData);
  } catch (error) {
    throw new Error(`Failed to toggle service status: ${error.message}`);
  }
};

// Alias for reorderServices (not directly available, so we create it)
/**
 * Reorder services based on provided order
 * @param {string} authorizationHeader - Bearer token
 * @param {Array} servicesOrder - Array of service IDs in new order
 * @returns {Promise} API response
 */
export const reorderServices = async (authorizationHeader, servicesOrder) => {
  try {
    // Get all services
    const servicesResponse = await getAllServices(authorizationHeader, { size: 1000 });
    
    if (!servicesResponse || !servicesResponse.data) {
      throw new Error('Failed to fetch services');
    }
    
    const services = servicesResponse.data;
    const updatePromises = [];
    
    // Update displayOrder for each service based on new order
    servicesOrder.forEach((serviceId, index) => {
      const service = services.find(s => s.configId === serviceId);
      if (service) {
        const updateData = {
          ...service,
          displayOrder: index
        };
        
        // Remove internal fields
        delete updateData.configId;
        delete updateData.createdAt;
        delete updateData.updatedAt;
        delete updateData.createdBy;
        delete updateData.updatedBy;
        
        updatePromises.push(
          updateService(authorizationHeader, serviceId, updateData)
        );
      }
    });
    
    const results = await Promise.all(updatePromises);
    return {
      success: true,
      message: 'Services reordered successfully',
      data: results.map(r => r.data)
    };
  } catch (error) {
    throw new Error(`Failed to reorder services: ${error.message}`);
  }
};