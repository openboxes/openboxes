import _ from 'lodash';
import queryString from 'query-string';

import glAccountApi from 'api/services/GlAccountApi';
import locationApi from 'api/services/LocationApi';
import organizationApi from 'api/services/OrganizationApi';
import productApi from 'api/services/ProductApi';
import productClassificationApi from 'api/services/ProductClassificationApi';
import productGroupApi from 'api/services/ProductGroupApi';
import selectOptionsApi from 'api/services/SelectOptionsApi';
import userApi from 'api/services/UserApi';
import { INTERNAL_LOCATIONS } from 'api/urls';
import ActivityCode from 'consts/activityCode';
import locationType from 'consts/locationType';
import apiClient from 'utils/apiClient';
import splitTranslation from 'utils/translation-utils';

export const debouncePeopleFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get('/api/persons', { params: { name: searchTerm, status: true } })
        .then((result) => callback(_.map(result.data.data, (obj) => (
          {
            ...obj,
            value: obj.id,
            label: obj.name,
          }
        ))))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const debounceUsersFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm?.length >= minSearchLength) {
      const config = {
        params: {
          searchTerm,
          active: true,
        },
      };
      userApi.getUsersOptions(config).then((users) => {
        callback(users?.data?.data?.map?.((user) => ({
          ...user,
          value: user.id,
          label: user.name,
        })));
      });
      return;
    }
    callback([]);
  }, waitTime);

export const debounceLocationsFetch = (
  waitTime,
  minSearchLength,
  activityCodes,
  fetchAll = false,
  withOrgCode = false,
  withTypeDescription = true,
  isReturnOrder = false,
  direction,
) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      const queryParams = queryString.parse(window.location.search);

      locationApi.getLocations({
        paramsSerializer: (parameters) => queryString.stringify(parameters),
        params: {
          name: searchTerm,
          direction: fetchAll ? undefined : (direction || queryParams?.direction),
          isReturnOrder: isReturnOrder || undefined,
          activityCodes,
        },
      }).then((result) => callback(_.map(result.data.data, (obj) => {
        const locationTypeData = withTypeDescription ? ` [${obj.locationType.description}]` : '';
        const label = `${obj.name}${locationTypeData}`;
        return {
          id: obj.id,
          type: obj.locationType.locationTypeCode,
          name: obj.name,
          supportedActivities: obj.supportedActivities,
          label: withOrgCode ? `${obj.organizationCode ? `${obj.organizationCode} - ` : ''}${label}` : label,
        };
      })))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const debounceGlobalSearch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(encodeURI(`/json/globalSearch?term=${searchTerm}`))
        .then((result) => callback(_.map(result.data, (obj) => (
          {
            value: obj.url,
            url: obj.url,
            label: obj.label,
            color: obj.color,
            displayName: obj.displayName,
            originalName: obj.value,
          }
        ))))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const debounceProductsFetch = (waitTime, minSearchLength, locationId, params = {}) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(encodeURI(`/api/products/search?name=${searchTerm}&productCode=${searchTerm}&location.id=${locationId}`))
        .then((result) => callback(_.map(result.data.data, (obj) => (
          {
            value: obj.id,
            id: obj.id,
            name: obj.name,
            productCode: obj.productCode,
            handlingIcons: obj.handlingIcons,
            lotAndExpiryControl: obj.lotAndExpiryControl,
            displayName: obj.displayName,
            color: obj.color,
            exactMatch: obj.exactMatch,
            active: obj.active,
            label: `${obj.productCode} - ${obj.displayName ?? obj.name} ${(params.includeUom && obj.unitOfMeasure) ? `[${obj.unitOfMeasure}]` : ''}`,
          }
        ))))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const debounceAvailableItemsFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(encodeURI(`/api/products/search?name=${searchTerm}&productCode=${searchTerm}&availableItems=true`))
        .then((result) => callback(_.map(result.data.data, (obj) => (
          {
            id: obj.id,
            value: obj.id,
            name: obj.name,
            productCode: obj.productCode,
            quantityAvailable: obj.quantityAvailable,
            minExpirationDate: obj.minExpirationDate,
            handlingIcons: obj.product.handlingIcons,
            displayNames: obj?.product?.displayNames,
            color: obj.color,
          }
        ))))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const debounceProductsInOrders = (waitTime, minSearchLength, vendor, destination) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(encodeURI(`/api/combinedShipmentItems/getProductsInOrders?name=${searchTerm}&vendor=${vendor}&destination=${destination}`))
        .then((result) => callback(_.map(result.data.data, (obj) => (
          {
            value: obj.id,
            id: obj.id,
            name: obj.name,
            productCode: obj.productCode,
            handlingIcons: obj.handlingIcons,
            displayNames: obj?.displayNames,
            color: obj.color,
          }
        ))))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const debounceOrganizationsFetch = (waitTime, minSearchLength, roleTypes = ['ROLE_SUPPLIER'], active = false) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/api/organizations?q=${searchTerm}${roleTypes ? roleTypes.map((roleType) => `&roleType=${roleType}`).join('') : ''}&active=${active}`)
        .then((result) => callback(_.map(result.data.data, (obj) => (
          {
            value: obj.id,
            id: obj.id,
            name: obj.name,
            label: `${obj.code} ${obj.name}`,
          }
        ))))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const debounceLocationGroupsFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/api/locationGroups?q=${searchTerm}`)
        .then((result) => callback(_.map(result.data.data, (obj) => (
          {
            id: obj.id,
            value: obj.id,
            name: obj.name,
            label: `${obj.name}`,
          }
        ))))
        .catch(() => callback([]));
    } else {
      callback([]);
    }
  }, waitTime);

export const organizationsFetch = (roleTypes = ['ROLE_SUPPLIER'], active = false) =>
  apiClient.get(`/api/organizations?${roleTypes ? roleTypes.map((roleType) => `&roleType=${roleType}`).join('') : ''}&active=${active}`)
    .then((res) => {
      if (res.data.data) {
        return res.data.data.map((obj) => (
          {
            id: obj.id,
            value: obj.id,
            name: obj.name,
            label: `${obj.name}`,
          }
        ));
      }
      return null;
    });

export const fetchUserById = async (id) => {
  const response = await apiClient(`/api/generic/person/${id}`);
  return response.data?.data;
};

export const fetchLocationById = async (id) => {
  const response = await apiClient(`/api/locations/${id}`);
  return response.data?.data;
};

export const fetchBins = async (locationId) => {
  const response = await apiClient.get(INTERNAL_LOCATIONS, {
    paramsSerializer: (parameters) => queryString.stringify(parameters),
    params: {
      'location.id': locationId,
      locationTypeCode: [locationType.BIN_LOCATION, locationType.INTERNAL],
      ignoreActivityCodes: [ActivityCode.RECEIVE_STOCK],
    },
  });

  return response.data.data;
};

export const fetchProductsCategories = async () => {
  const response = await apiClient.get('/api/categoryOptions');
  return response.data.data;
};

export const fetchProductsCatalogs = async (params = {}) => {
  const response = await apiClient.get('/api/catalogOptions', { params });
  return response.data.data;
};

export const fetchProductsTags = async (params = {}) => {
  const response = await apiClient.get('/api/tagOptions', { params });
  return response.data.data;
};

export const fetchProductsGlAccounts = async (params) => {
  const { data } = await glAccountApi.getGlAccountOptions({ params });
  return data.data;
};

export const fetchProductGroups = async () => {
  const { data } = await productGroupApi.getProductGroupsOptions();
  return data.data;
};

export const fetchProductClassifications = async (facilityId) => {
  const { data } = await productClassificationApi.getProductClassifications(facilityId);
  if (data.data) {
    return data.data.map((obj) => (
      {
        id: obj.name,
        value: obj.name,
        label: obj.name,
      }
    ));
  }
  return null;
};

export const fetchHandlingRequirements = async () => {
  const { data } = await selectOptionsApi.getHandlingRequirementsOptions();
  return data.data;
};

export const fetchProduct = async (id) => {
  const { data } = await productApi.getProduct(id);
  return data.data;
};

export const fetchOrganization = async (id) => {
  const { data } = await organizationApi.getOrganization(id);
  return data.data;
};

const mapShipmentType = (shipmentType) => {
  // Enum keys can be e.g. AIR, LAND, SEA etc.
  const enumKey = splitTranslation(shipmentType?.name, null)?.toUpperCase();
  return {
    ...shipmentType,
    value: shipmentType?.id,
    enumKey,
  };
};

// Shipment types arg can be an array or a single element (passed from table cell)
export const mapShipmentTypes = (shipmentTypes) => {
  if (_.isArray(shipmentTypes)) {
    return shipmentTypes.map((shipmentType) => mapShipmentType(shipmentType));
  }
  return mapShipmentType(shipmentTypes);
};

export const selectNullOption = { id: 'null', value: 'null' };
