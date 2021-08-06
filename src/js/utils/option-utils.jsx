import _ from 'lodash';
import queryString from 'query-string';
import apiClient from './apiClient';

export const debounceUsersFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/openboxes/api/persons?name=${searchTerm}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  ...obj,
                },
                name: obj.name,
                label: obj.name,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceLocationsFetch = (
  waitTime,
  minSearchLength,
  activityCodes,
  fetchAll = false,
  withOrgCode = false,
  withTypeDescription = true,
) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      const activityCodesParams = activityCodes ? activityCodes.map(activityCode => `&activityCodes=${activityCode}`).join('') : '';
      const { direction } = queryString.parse(window.location.search);
      const directionParam = fetchAll ? null : direction;
      apiClient.get(`/openboxes/api/locations?name=${searchTerm}${directionParam ? `&direction=${directionParam}` : ''}${activityCodesParams}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, (obj) => {
              const locationType = withTypeDescription ? ` [${obj.locationType.description}]` : '';
              const label = `${obj.name}${locationType}`;
              return {
                value: {
                  id: obj.id,
                  type: obj.locationType.locationTypeCode,
                  name: obj.name,
                  label: withOrgCode ? `${obj.organizationCode ? `${obj.organizationCode} - ` : ''}${label}` : label,
                },
                label: withOrgCode ? `${obj.organizationCode ? `${obj.organizationCode} - ` : ''}${label}` : label,
              };
            }),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceGlobalSearch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/openboxes/json/globalSearch?term=${searchTerm}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data, obj => (
              {
                value: {
                  url: obj.url,
                },
                label: obj.label,
                color: obj.color,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceProductsFetch = (waitTime, minSearchLength, locationId) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/openboxes/api/products?name=${searchTerm}&productCode=${searchTerm}&location.id=${locationId}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  name: obj.name,
                  productCode: obj.productCode,
                  label: `${obj.productCode} - ${obj.name}`,
                  handlingIcons: obj.handlingIcons,
                  lotAndExpiryControl: obj.lotAndExpiryControl,
                },
                label: `${obj.productCode} - ${obj.name}`,
                color: obj.color,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceAvailableItemsFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/openboxes/api/products?name=${searchTerm}&productCode=${searchTerm}&availableItems=true`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  name: obj.name,
                  productCode: obj.productCode,
                  label: `${obj.productCode} - ${obj.name}`,
                  quantityAvailable: obj.quantityAvailable,
                  minExpirationDate: obj.minExpirationDate,
                  handlingIcons: obj.product.handlingIcons,
                },
                label: `${obj.productCode} - ${obj.name}`,
                color: obj.color,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceProductsInOrders = (waitTime, minSearchLength, vendor, destination) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/openboxes/api/combinedShipmentItems/getProductsInOrders?name=${searchTerm}&vendor=${vendor}&destination=${destination}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  name: obj.name,
                  productCode: obj.productCode,
                  label: `${obj.productCode} - ${obj.name}`,
                  handlingIcons: obj.handlingIcons,
                },
                label: `${obj.productCode} - ${obj.name}`,
                color: obj.color,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);

export const debounceOrganizationsFetch = (waitTime, minSearchLength) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      apiClient.get(`/openboxes/api/organizations?q=${searchTerm}&roleType=ROLE_SUPPLIER`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  name: obj.name,
                  label: `${obj.code} ${obj.name}`,
                },
                label: `${obj.code} ${obj.name}`,
              }
            )),
          },
        ))
        .catch(error => callback(error, { options: [] }));
    } else {
      callback(null, { options: [] });
    }
  }, waitTime);
