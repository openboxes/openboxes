/* eslint-disable no-console */
import React from 'react';

import axios from 'axios';
import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';

import notification from 'components/Layout/notifications/notification';
import LoginModal from 'components/LoginModal';
import NotificationType from 'consts/notificationTypes';

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

export const handleSuccess = response => response;

export const handleError = (error) => {
  const errorMessage = _.get(error, 'response.data.errorMessage', '');
  const errorMessages = _.get(error, 'response.data.errorMessages', []).join(', ');
  switch (error.response.status) {
    case 400: {
      notification(NotificationType.ERROR_OUTLINED)({
        message: 'Bad request',
        details: errorMessages || errorMessage,
      });
      break;
    }

    case 401:
      confirmAlert({
        customUI: props => (<LoginModal {...props} />),
      });
      break;
    case 403:
      notification(NotificationType.WARNING)({
        message: 'Access denied',
        details: errorMessage || errorMessages,
      });
      break;
    case 404:
      notification(NotificationType.ERROR_OUTLINED)({
        message: 'Not found',
        details: errorMessage || errorMessages,
      });
      break;
    case 500:
      notification(NotificationType.ERROR_FILLED)({
        message: 'Internal server error',
        details: errorMessage || errorMessages,
      });
      break;
    default:
      notification(NotificationType.ERROR_FILLED)({
        message: error,
        details: errorMessage || errorMessages,
      });
  }
  return Promise.reject(error);
};

apiClient.interceptors.response.use(handleSuccess, handleError);
apiClient.interceptors.request.use(config => config, justRejectRequestError);

export default apiClient;
