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

export const debounceLocationsFetch = (waitTime, minSearchLength, activityCodes) =>
  _.debounce((searchTerm, callback) => {
    if (searchTerm && searchTerm.length >= minSearchLength) {
      const activityCodesParams = activityCodes ? activityCodes.map(activityCode => `&activityCodes=${activityCode}`).join('') : '';
      const { direction } = queryString.parse(window.location.search);
      apiClient.get(`/openboxes/api/locations?name=${searchTerm}${direction ? `&direction=${direction}` : ''}${activityCodesParams}`)
        .then(result => callback(
          null,
          {
            complete: true,
            options: _.map(result.data.data, obj => (
              {
                value: {
                  id: obj.id,
                  type: obj.locationType.locationTypeCode,
                  name: obj.name,
                  label: `${obj.name} [${obj.locationType.description}]`,
                },
                label: `${obj.name} [${obj.locationType.description}]`,
              }
            )),
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
