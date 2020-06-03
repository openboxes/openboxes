/* eslint no-param-reassign: ["error", { "props": false }] */
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
  FETCH_GRAPHS,
  FETCH_NUMBERS,
  RESET_INDICATORS,
  ADD_TO_INDICATORS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
  FETCH_CONFIG,
  SET_ACTIVE_CONFIG,
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

// New Dashboard

function fetchGraphIndicator(
  dispatch,
  indicatorConfig,
  params = '',
) {
  const id = indicatorConfig.order;

  const url = `${indicatorConfig.endpoint}?${params}`;

  dispatch({
    type: FETCH_GRAPHS,
    payload: {
      id,
      title: 'Loading...',
      type: 'loading',
      data: [],
      archived: indicatorConfig.archived,
    },
  });

  apiClient.get(url).then((res) => {
    const indicatorData = res.data;
    dispatch({
      type: FETCH_GRAPHS,
      payload: {
        id,
        title: indicatorData.title,
        type: indicatorData.type,
        data: indicatorData.data,
        archived: indicatorConfig.archived,
        link: indicatorData.link,
        config: {
          stacked: indicatorConfig.stacked,
          datalabel: indicatorConfig.datalabel,
          colors: indicatorConfig.colors,
        },
      },
    });
  }, () => {
    dispatch({
      type: FETCH_GRAPHS,
      payload: {
        id,
        title: 'Indicator could not be loaded',
        type: 'error',
        data: [],
        archived: indicatorConfig.archived,
      },
    });
  });
}

function fetchNumberIndicator(
  dispatch,
  indicatorConfig,
) {
  const id = indicatorConfig.order;

  const url = indicatorConfig.endpoint;

  apiClient.get(url).then((res) => {
    const indicatorData = res.data;
    dispatch({
      type: FETCH_NUMBERS,
      payload: {
        ...indicatorData,
        id,
        archived: indicatorConfig.archived,
      },
    });
  });
}

export function reloadIndicator(indicatorConfig, params) {
  return (dispatch) => {
    // new reference so that the original config is not modified
    const indicatorConfigData = JSON.parse(JSON.stringify(indicatorConfig));
    indicatorConfigData.archived = false;
    fetchGraphIndicator(dispatch, indicatorConfigData, params);
  };
}

function getData(dispatch, configData, config = 'personal') {
  // new reference so that the original config is not modified
  const dataEndpoints = JSON.parse(JSON.stringify(configData.endpoints));
  if (configData.enabled) {
    Object.values(dataEndpoints.graph).forEach((indicatorConfig) => {
      indicatorConfig.archived = indicatorConfig.archived.includes(config);
      fetchGraphIndicator(dispatch, indicatorConfig);
    });
    Object.values(dataEndpoints.number).forEach((indicatorConfig) => {
      indicatorConfig.archived = indicatorConfig.archived.includes(config);
      fetchNumberIndicator(dispatch, indicatorConfig);
    });
  } else {
    Object.values(dataEndpoints.graph).forEach((indicatorConfig) => {
      indicatorConfig.archived = false;
      indicatorConfig.colors = undefined;
      fetchGraphIndicator(dispatch, indicatorConfig);
    });
    Object.values(dataEndpoints.number).forEach((indicatorConfig) => {
      indicatorConfig.archived = false;
      fetchNumberIndicator(dispatch, indicatorConfig);
    });
  }
}

export function fetchIndicators(configData, config) {
  return (dispatch) => {
    dispatch({
      type: SET_ACTIVE_CONFIG,
      payload: {
        data: config,
      },
    });

    getData(dispatch, configData, config);
  };
}

export function resetIndicators() {
  return {
    type: RESET_INDICATORS,
  };
}

export function addToIndicators(index, type) {
  return {
    type: ADD_TO_INDICATORS,
    payload: { index, type },
  };
}

export function reorderIndicators({ oldIndex, newIndex }, e, type) {
  if (e.target.id === 'archive') {
    return {
      type: REMOVE_FROM_INDICATORS,
      payload: { index: oldIndex, type },
    };
  }
  return {
    type: REORDER_INDICATORS,
    payload: { oldIndex, newIndex, type },
  };
}

export function fetchConfigAndData() {
  return (dispatch) => {
    apiClient.get('/openboxes/tablero/config').then((res) => {
      dispatch({
        type: FETCH_CONFIG,
        payload: {
          data: res.data.data,
        },
      });
      getData(dispatch, res.data.data);
    });
  };
}
