import { SHOW_SPINNER, HIDE_SPINNER, FETCH_LOCATIONS, FETCH_USERS, FETCH_PRODUCTS } from './types';
import apiClient from '../utils/apiClient';

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

export function fetchLocations() {
  const url = '/openboxes/api/locations';
  const request = apiClient.get(url);

  return {
    type: FETCH_LOCATIONS,
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
