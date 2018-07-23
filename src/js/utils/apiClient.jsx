/* eslint-disable no-console */
import _ from 'lodash';
import axios from 'axios';

const justRejectRequestError = error => Promise.reject(error);

const apiClient = axios.create({});

export function parseResponse(data) {
  if (_.isArray(data)) {
    return _.map(data, value => (parseResponse(value)));
  }

  if (_.isPlainObject(data)) {
    const obj = {};
    _.forEach(data, (value, key) => { _.set(obj, key, parseResponse(value)); });
    return obj;
  }

  return data;
}

export function flattenRequest(data) {
  if (_.isArray(data)) {
    return _.map(data, value => flattenRequest(value));
  }

  if (_.isPlainObject(data)) {
    const obj = {};

    _.forEach(data, (value, key) => {
      const flattenedVal = flattenRequest(value);

      if (_.isPlainObject(flattenedVal)) {
        _.forEach(flattenedVal, (childVal, childKey) => { obj[`${key}.${childKey}`] = childVal; });
      } else {
        obj[key] = flattenedVal;
      }
    });

    return obj;
  }

  return data === null ? '' : data;
}

const handleSuccess = response => response;

const handleError = (error) => {
  switch (error.response.status) {
    case 400:
      console.error('Bad request.');
      break;
    case 401:
      console.error('Unauthorized.');
      break;
    case 403:
      console.error('Access denied.');
      break;
    case 404:
      console.error('Not found.');
      break;
    case 500:
      console.error('Internal server error.');
      break;
    default: {
      console.error(error);
    }
  }
  return Promise.reject(error);
};

apiClient.interceptors.response.use(handleSuccess, handleError);
apiClient.interceptors.request.use(config => config, justRejectRequestError);

export default apiClient;
