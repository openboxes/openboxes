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
  const url = '/api/reasonCodes';
  const request = apiClient.get(url);

  return {
    type: FETCH_REASONCODES,
    payload: request,
  };
}

export function fetchUsers() {
  const url = '/api/generic/person';
  const request = apiClient.get(url);

  return {
    type: FETCH_USERS,
    payload: request,
  };
}

export function fetchSessionInfo() {
  const url = '/api/getUserSession';
  const request = apiClient.get(url);

  return {
    type: FETCH_SESSION_INFO,
    payload: request,
  };
}

export function changeCurrentLocation(location) {
  return (dispatch) => {
    const url = `/api/chooseLocation/${location.id}`;

    apiClient.put(url)
      .then(() => {
        dispatch({
          type: CHANGE_CURRENT_LOCATION,
          payload: location,
        });
      });
  };
}

export function fetchTranslations(lang, prefix) {
  return (dispatch) => {
    const url = `/api/localizations?lang=${lang || ''}&prefix=react.${prefix || ''}`;

    apiClient.get(url)
      .then((response) => {
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
    const url = `/api/chooseLocale/${locale}`;

    apiClient.put(url)
      .then(() => {
        dispatch({
          type: CHANGE_CURRENT_LOCALE,
          payload: locale,
        });
      });
  };
}
