import { addTranslationForLanguage } from 'react-localize-redux';

import {
  SHOW_SPINNER,
  HIDE_SPINNER,
  FETCH_USERS,
  FETCH_REASONCODES,
  FETCH_SESSION_INFO,
  CHANGE_CURRENT_LOCATION,
  TRANSLATIONS_FETCHED,
  CHANGE_CURRENT_LOCALE,
  FETCH_INDICATORS,
  ADD_TO_INDICATORS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
} from './types';
import apiClient, { parseResponse } from '../utils/apiClient';

export function showSpinner() {
  return {
    type: SHOW_SPINNER,
    payload: true,
  };
}

export function hideSpinner() {
  return {
    type: HIDE_SPINNER,
    payload: false,
  };
}

export function fetchReasonCodes() {
  const url = '/openboxes/api/reasonCodes';
  const request = apiClient.get(url);

  return {
    type: FETCH_REASONCODES,
    payload: request,
  };
}

export function fetchUsers() {
  const url = '/openboxes/api/generic/person';
  const request = apiClient.get(url);

  return {
    type: FETCH_USERS,
    payload: request,
  };
}

export function fetchSessionInfo() {
  const url = '/openboxes/api/getAppContext';
  const request = apiClient.get(url);

  return {
    type: FETCH_SESSION_INFO,
    payload: request,
  };
}

export function changeCurrentLocation(location) {
  return (dispatch) => {
    const url = `/openboxes/api/chooseLocation/${location.id}`;

    apiClient.put(url).then(() => {
      dispatch({
        type: CHANGE_CURRENT_LOCATION,
        payload: location,
      });
    });
  };
}

export function fetchTranslations(lang, prefix) {
  return (dispatch) => {
    const url = `/openboxes/api/localizations?lang=${lang ||
      ''}&prefix=react.${prefix || ''}`;

    apiClient.get(url).then((response) => {
      const { messages, currentLocale } = parseResponse(response.data);

      dispatch(addTranslationForLanguage(messages, currentLocale));

      dispatch({
        type: TRANSLATIONS_FETCHED,
        payload: prefix,
      });
    });
  };
}

export function changeCurrentLocale(locale) {
  return (dispatch) => {
    const url = `/openboxes/api/chooseLocale/${locale}`;

    apiClient.put(url).then(() => {
      dispatch({
        type: CHANGE_CURRENT_LOCALE,
        payload: locale,
      });
    });
  };
}

//New Dashboard

function getDataWithMethod(method) {
  const url = "/openboxes/apitablero/" + method;
  return apiClient.get(url).then(res => {
    return res.data;
  });
}

function fetchIndicator(indicatorName, dispatch) {
  let data = [];
  let title = null;
  let type = null;
  let options = [];
  let archived = 0;
  const id = Math.random();

  switch (indicatorName) {
    case 'expirationSummary':
      data = getDataWithMethod('getExpirationSummary');
      title = 'Expiration Summary';
      type = 'line';
      break;
    case 'inventorySummary':
      data = getDataWithMethod('getInventorySummary');
      title = 'Inventory Summary';
      type = 'horizontalBar';
      break;
    case 'fillRate':
      data = getDataWithMethod('getFillRate');
      title = 'Fill Rate';
      type = 'line';
      break;
    case 'sentStock':
      data = getDataWithMethod('getSentStockMovements');
      title = 'Sent Stock Movements';
      type = 'bar';
      break;
    case 'stockReceived':
      data = getDataWithMethod('getReceivedStockMovements');
      title = 'Stock Movements Received';
      type = 'doughnut';
      archived = 1;
      break;
    case 'outgoingStock':
      data = getDataWithMethod('getOutgoingStock');
      title = 'Outgoing Stock Movements';
      type = 'numbers';
      break;
    default:
      title = 'Error';
      type = 'error';
  }

  dispatch({
    type: FETCH_INDICATORS,
    payload: {
      id,
      title,
      type: 'loading',
      data,
      archived,
    },
  });

  new Promise((resolve, reject) => {
    setTimeout(() => {
      if (data) {
        resolve(data);
      } else {
        reject();
      }
    }, Math.floor((Math.random() * 5) + 2) * 1000); // between 2 and 6 seconds
  }).then((res) => {
    dispatch({
      type: FETCH_INDICATORS,
      payload: {
        id,
        title,
        type,
        data: res,
        archived,
      },
    });
  }, () => {
    dispatch({
      type: FETCH_INDICATORS,
      payload: {
        id,
        title,
        type: 'error',
        data: [],
        archived,
      },
    });
  });
}

export function fetchIndicators() {
  return (dispatch) => {
    fetchIndicator('expirationSummary', dispatch);
    fetchIndicator('inventorySummary', dispatch);
    fetchIndicator('fillRate', dispatch);
    fetchIndicator('sentStock', dispatch);
    fetchIndicator('stockReceived', dispatch);
    fetchIndicator('outgoingStock', dispatch);
  };
}

export function addToIndicators(index) {
  return {
    type: ADD_TO_INDICATORS,
    payload: { index },
  };
}

export function reorderIndicators({ oldIndex, newIndex }, e) {
  if (e.target.id === 'archive') {
    return {
      type: REMOVE_FROM_INDICATORS,
      payload: { index: oldIndex },
    };
  }
  return {
    type: REORDER_INDICATORS,
    payload: { oldIndex, newIndex },
  };
}
