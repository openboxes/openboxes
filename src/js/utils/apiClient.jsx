/* eslint-disable no-console */
import _ from 'lodash';
import React from 'react';
import axios from 'axios';
import Alert from 'react-s-alert';
import { confirmAlert } from 'react-confirm-alert';

import LoginModal from '../components/LoginModal';

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

  return data === null || data === undefined ? '' : data;
}

const handleSuccess = response => response;

const handleError = (error) => {
  switch (error.response.status) {
    case 400:
      Alert.error(`Bad Request.</br> ${_.map(_.get(error, 'response.data.errorMessages', ''), errorMessage => `<div>${errorMessage}</div>`)}`);
      break;
    case 401:
      confirmAlert({
        customUI: props => (<LoginModal {...props} />),
      });
      break;
    case 403:
      Alert.error(`Access denied.</br>${_.get(error, 'response.data.errorMessage', '')}`);
      break;
    case 404:
      Alert.error(`Not found.</br>${_.get(error, 'response.data.errorMessage', '')}`);
      break;
    case 500:
      Alert.error(`Internal server error.</br>${_.get(error, 'response.data.errorMessage', '')}`);
      break;
    default: {
      Alert.error(`${error}</br>${_.get(error, 'response.data.errorMessage', '')}`);
    }
  }
  return Promise.reject(error);
};

apiClient.interceptors.response.use(handleSuccess, handleError);
apiClient.interceptors.request.use(config => config, justRejectRequestError);

export default apiClient;
