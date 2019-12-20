import _ from 'lodash';

import { FETCH_SESSION_INFO, CHANGE_CURRENT_LOCATION, TRANSLATIONS_FETCHED, CHANGE_CURRENT_LOCALE } from '../actions/types';

const initialState = {
  currentLocation: {
    id: '',
    name: '',
    hasBinLocationSupport: true,
    hasPackingSupport: true,
    locationType: { description: '', locationTypeCode: '' },
  },
  isSuperuser: false,
  isUserAdmin: false,
  supportedActivities: [],
  menuConfig: {},
  activeLanguage: '',
  fetchedTranslations: {
    default: false,
    stockMovement: false,
    partialReceiving: false,
    putAway: false,
    stockListManagement: false,
  },
  searchConfig: {
    debounceTime: 500,
    minSearchLength: 3,
  },
  user: {
    id: '',
    username: '',
  },
  isImpersonated: false,
  grailsVersion: '',
  appVersion: '',
  branchName: '',
  buildNumber: '',
  environment: '',
  buildDate: '',
  ipAddress: '',
  hostname: '',
  timezone: '',
  minimumExpirationDate: '',
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_SESSION_INFO:
      return {
        ...state,
        currentLocation: _.get(action, 'payload.data.data.location'),
        isSuperuser: _.get(action, 'payload.data.data.isSuperuser'),
        isUserAdmin: _.get(action, 'payload.data.data.isUserAdmin'),
        supportedActivities: _.get(action, 'payload.data.data.supportedActivities'),
        menuConfig: _.get(action, 'payload.data.data.menuConfig'),
        activeLanguage: _.get(action, 'payload.data.data.activeLanguage'),
        user: _.get(action, 'payload.data.data.user'),
        isImpersonated: _.get(action, 'payload.data.data.isImpersonated'),
        grailsVersion: _.get(action, 'payload.data.data.grailsVersion'),
        appVersion: _.get(action, 'payload.data.data.appVersion'),
        branchName: _.get(action, 'payload.data.data.branchName'),
        buildNumber: _.get(action, 'payload.data.data.buildNumber'),
        environment: _.get(action, 'payload.data.data.environment'),
        buildDate: _.get(action, 'payload.data.data.buildDate'),
        ipAddress: _.get(action, 'payload.data.data.ipAddress'),
        hostname: _.get(action, 'payload.data.data.hostname'),
        timezone: _.get(action, 'payload.data.data.timezone'),
        minimumExpirationDate: _.get(action, 'payload.data.data.minimumExpirationDate'),
      };
    case CHANGE_CURRENT_LOCATION:
      return { ...state, currentLocation: action.payload };
    case CHANGE_CURRENT_LOCALE:
      return { ...state, activeLanguage: action.payload };
    case TRANSLATIONS_FETCHED:
      return {
        ...state,
        fetchedTranslations: { ...state.fetchedTranslations, [action.payload]: true },
      };
    default:
      return state;
  }
}
