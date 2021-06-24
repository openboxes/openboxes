import _ from 'lodash';

import {
  FETCH_SESSION_INFO,
  CHANGE_CURRENT_LOCATION,
  TRANSLATIONS_FETCHED,
  CHANGE_CURRENT_LOCALE,
  FETCH_MENU_CONFIG,
  TOGGLE_LOCATION_CHOOSER,
  TOGGLE_USER_ACTION_MENU,
  UPDATE_BREADCRUMBS_PARAMS,
  FETCH_BREADCRUMBS_CONFIG,
} from '../actions/types';

const initialState = {
  currentLocation: {
    id: '',
    name: '',
    hasBinLocationSupport: true,
    hasPackingSupport: true,
    hasPartialReceivingSupport: true,
    hasCentralPurchasingEnabled: true,
    locationType: { description: '', locationTypeCode: '' },
  },
  isSuperuser: false,
  isUserAdmin: false,
  supportedActivities: [],
  menuConfig: {},
  activeLanguage: '',
  fetchedTranslations: {
    default: false,
    invoice: false,
    stockMovement: false,
    partialReceiving: false,
    putAway: false,
    stockListManagement: false,
    stockTransfer: false,
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
  isPaginated: false,
  logoLabel: '',
  menuItems: [],
  locationChooser: false,
  userActionMenuOpen: false,
  highestRole: '',
  isOpen: false,
  pageSize: 50,
  logoUrl: '',
  supportedLocales: [],
  breadcrumbsParams: [],
  breadcrumbsConfig: [],
  currencyCode: '',
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
        isPaginated: _.get(action, 'payload.data.data.isPaginated'),
        logoLabel: _.get(action, 'payload.data.data.logoLabel'),
        menuItems: _.get(action, 'payload.data.data.menuItems'),
        highestRole: _.get(action, 'payload.data.data.highestRole'),
        pageSize: _.get(action, 'payload.data.data.pageSize'),
        logoUrl: _.get(action, 'payload.data.data.logoUrl'),
        supportedLocales: _.get(action, 'payload.data.data.supportedLocales'),
        currencyCode: _.get(action, 'payload.data.data.currencyCode'),
      };
    case FETCH_MENU_CONFIG:
      return {
        ...state,
        menuConfig: _.get(action, 'payload.data.data.menuConfig'),
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
    case TOGGLE_LOCATION_CHOOSER:
      return {
        ...state,
        locationChooser: action.payload,
      };
    case TOGGLE_USER_ACTION_MENU:
      return {
        ...state,
        userActionMenuOpen: action.payload,
      };
    case UPDATE_BREADCRUMBS_PARAMS:
      return {
        ...state,
        breadcrumbsParams: action.payload,
      };
    case FETCH_BREADCRUMBS_CONFIG:
      return {
        ...state,
        breadcrumbsConfig: action.payload,
      };
    default:
      return state;
  }
}
