/* eslint no-param-reassign: ["error", { "props": false }] */
import { addTranslationForLanguage } from 'react-localize-redux';
import {
  SHOW_SPINNER,
  HIDE_SPINNER,
  TOGGLE_LOCATION_CHOOSER,
  TOGGLE_USER_ACTION_MENU,
  FETCH_USERS,
  FETCH_REASONCODES,
  FETCH_SESSION_INFO,
  FETCH_MENU_CONFIG,
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
  UPDATE_BREADCRUMBS_PARAMS,
  FETCH_BREADCRUMBS_CONFIG,
  FETCH_CURRENCIES,
  FETCH_ORGANIZATIONS,
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

export function showLocationChooser() {
  return {
    type: TOGGLE_LOCATION_CHOOSER,
    payload: true,
  };
}

export function showUserActions() {
  return {
    type: TOGGLE_USER_ACTION_MENU,
    payload: true,
  };
}

export function hideLocationChooser() {
  return {
    type: TOGGLE_LOCATION_CHOOSER,
    payload: false,
  };
}

export function hideUserActions() {
  return {
    type: TOGGLE_USER_ACTION_MENU,
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

export function fetchCurrencies() {
  const url = '/openboxes/api/unitOfMeasure/currencies';
  const request = apiClient.get(url);

  return {
    type: FETCH_CURRENCIES,
    payload: request,
  };
}

export function fetchOrganizations() {
  const url = '/openboxes/api/organizations';
  const request = apiClient.get(url);

  return {
    type: FETCH_ORGANIZATIONS,
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

export function fetchMenuConfig() {
  const url = '/openboxes/api/getMenuConfig';
  const request = apiClient.get(url);

  return {
    type: FETCH_MENU_CONFIG,
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

function getParameterList(params = '', locationId = '', userId = '') {
  const listFiltersSelected = [];
  const listValues = [];

  const dashboardKey = sessionStorage.getItem('dashboardKey');

  const pageConfig = JSON.parse(sessionStorage.getItem('pageConfig')) || {};

  if (!pageConfig[dashboardKey]) { pageConfig[dashboardKey] = {}; }

  let listParams = params === '' ? `locationId=${locationId}` : `${params}&locationId=${locationId}`;
  listParams += userId ? `&userId=${userId}` : '';

  // List of filter and category
  // filter[0] is the category
  // filter[1] represent the values
  Object.entries(pageConfig[dashboardKey]).forEach((filter) => {
    listFiltersSelected.push(filter[0]);
    filter[1].forEach(value => listValues.push(value));
  });
  // Add condition to check if currentPage has any filter
  listFiltersSelected.forEach((filter) => {
    listParams = `${listParams}&listFiltersSelected=${filter}`;
  });
  listValues.forEach((value) => {
    listParams = `${listParams}&value=${value.id}`;
  });

  return listParams;
}

function fetchGraphIndicator(
  dispatch,
  indicatorConfig,
  locationId = '',
  params = '',
) {
  const id = indicatorConfig.order;

  const listParams = getParameterList(params, locationId);
  const url = `${indicatorConfig.endpoint}?${listParams}`;

  if (!indicatorConfig.enabled) {
    dispatch({
      type: FETCH_GRAPHS,
      payload: {
        id,
        archived: indicatorConfig.archived,
        enabled: indicatorConfig.enabled,
      },
    });
  } else {
    dispatch({
      type: FETCH_GRAPHS,
      payload: {
        id,
        title: 'Loading...',
        info: 'Loading...',
        type: 'loading',
        data: [],
        archived: indicatorConfig.archived,
        enabled: indicatorConfig.enabled,
      },
    });

    apiClient.get(url).then((res) => {
      const indicatorData = res.data;
      dispatch({
        type: FETCH_GRAPHS,
        payload: {
          id,
          title: indicatorData.title,
          info: indicatorData.info,
          type: indicatorData.type,
          data: indicatorData.data,
          archived: indicatorConfig.archived,
          timeFilter: indicatorConfig.timeFilter,
          locationFilter: indicatorConfig.locationFilter,
          timeLimit: indicatorConfig.timeLimit,
          link: indicatorData.link,
          legend: indicatorConfig.legend,
          doubleAxeY: indicatorConfig.doubleAxeY,
          config: {
            stacked: indicatorConfig.stacked,
            datalabel: indicatorConfig.datalabel,
            colors: indicatorConfig.colors,
          },
          enabled: indicatorConfig.enabled,
          size: indicatorConfig.size,
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
          enabled: indicatorConfig.enabled,
        },
      });
    });
  }
}

function fetchNumberIndicator(
  dispatch,
  indicatorConfig,
  locationId,
  userId
) {
  const id = indicatorConfig.order;

  const listParams = getParameterList('', locationId, userId);

  const url = `${indicatorConfig.endpoint}?${listParams}`;
  if (!indicatorConfig.enabled) {
    dispatch({
      type: FETCH_NUMBERS,
      payload: {
        id,
        enabled: indicatorConfig.enabled,
      },
    });
  } else {
    apiClient.get(url).then((res) => {
      const indicatorData = res.data;
      dispatch({
        type: FETCH_NUMBERS,
        payload: {
          ...indicatorData,
          id,
          archived: indicatorConfig.archived,
          enabled: indicatorConfig.enabled,
        },
      });
    });
  }
}

export function reloadIndicator(indicatorConfig, params, locationId) {
  return (dispatch) => {
    // new reference so that the original config is not modified
    const indicatorConfigData = JSON.parse(JSON.stringify(indicatorConfig));
    indicatorConfigData.archived = false;
    fetchGraphIndicator(dispatch, indicatorConfigData, locationId, params);
  };
}

function getData(dispatch, configData, locationId, config = 'personal', userId = '') {
  // new reference so that the original config is not modified

  const dataEndpoints = JSON.parse(JSON.stringify(configData.endpoints));
  if (configData.enabled) {
    Object.values(dataEndpoints.graph).forEach((indicatorConfig) => {
      indicatorConfig.archived = indicatorConfig.archived.includes(config);

      fetchGraphIndicator(dispatch, indicatorConfig, locationId, '');
    });
    Object.values(dataEndpoints.number).forEach((indicatorConfig) => {
      indicatorConfig.archived = indicatorConfig.archived.includes(config);
      fetchNumberIndicator(dispatch, indicatorConfig, locationId, userId);
    });
  } else {
    Object.values(dataEndpoints.graph).forEach((indicatorConfig) => {
      indicatorConfig.archived = false;
      indicatorConfig.colors = undefined;

      fetchGraphIndicator(dispatch, indicatorConfig, locationId, '');
    });
    Object.values(dataEndpoints.number).forEach((indicatorConfig) => {
      indicatorConfig.archived = false;
      fetchNumberIndicator(dispatch, indicatorConfig, locationId, userId);
    });
  }
}

export function fetchIndicators(
  configData,
  config,
  locationId,
  userId
) {
  return (dispatch) => {
    dispatch({
      type: SET_ACTIVE_CONFIG,
      payload: {
        data: config,
      },
    });

    getData(dispatch, configData, locationId, config, userId);
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

export function fetchConfigAndData(locationId, config = 'personal', userId, filterSelected) {
  return (dispatch) => {
    apiClient.get('/openboxes/apitablero/config').then((res) => {
      dispatch({
        type: FETCH_CONFIG,
        payload: {
          data: res.data,
        },
      });
      getData(dispatch, res.data, locationId, config, userId, filterSelected);
    });
  };
}

export function fetchConfig() {
  return (dispatch) => {
    apiClient.get('/openboxes/apitablero/config').then((res) => {
      dispatch({
        type: FETCH_CONFIG,
        payload: {
          data: res.data,
        },
      });
    });
  };
}

function dispachBreadcrumbsParams(newData, dispatch) {
  dispatch({
    type: UPDATE_BREADCRUMBS_PARAMS,
    payload: newData,
  });
}

export function updateBreadcrumbs(listBreadcrumbsStep = [
  {
    label: null, defaultLabel: null, url: null, id: null,
  },
]) {
  return (dispatch) => {
    const breadcrumbsParams = [];
    listBreadcrumbsStep.forEach((step) => {
      breadcrumbsParams.push({
        label: step.label || '',
        defaultLabel: step.defaultLabel,
        url: step.id ? `${step.url}${step.id}` : step.url || '',
      });
    });
    dispachBreadcrumbsParams(breadcrumbsParams, dispatch);
  };
}

export function fetchBreadcrumbsConfig() {
  return (dispatch) => {
    apiClient.get('/openboxes/apitablero/breadcrumbsConfig').then((res) => {
      dispatch({
        type: FETCH_BREADCRUMBS_CONFIG,
        payload: res.data,
      });
    });
  };
}
