import _ from 'lodash';

import {
  CHANGE_CURRENT_LOCALE,
  CHANGE_CURRENT_LOCATION,
  FETCH_MENU_CONFIG,
  FETCH_SESSION_INFO,
  TOGGLE_USER_ACTION_MENU,
  TRANSLATIONS_FETCHED,
} from 'actions/types';

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
  isUserApprover: false,
  supportedActivities: [],
  menuConfig: [],
  menuSectionsUrlParts: {},
  activeLanguage: '',
  fetchedTranslations: {
    default: false,
    invoice: false,
    stockMovement: false,
    partialReceiving: false,
    putAway: false,
    stockListManagement: false,
    stockTransfer: false,
    replenishment: false,
    outboundReturns: false,
    inboundReturns: false,
    productsConfiguration: false,
    locationsConfiguration: false,
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
  userActionMenuOpen: false,
  highestRole: '',
  isOpen: false,
  pageSize: 50,
  logoUrl: '',
  supportedLocales: [],
  currencyCode: '',
  localizedHelpScoutKey: '',
  isHelpScoutEnabled: false,
  loading: false,
  localizationModeEnabled: false,
  localizationModeLocale: 'ach',
  displayDateFormat: 'MMM DD, yyyy',
  displayDateDefaultValue: '-',
  notificationAutohideDelay: 8000,
  browserConnectionTimeout: 0,
  isAutosaveEnabled: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_SESSION_INFO:
      return {
        ...state,
        currentLocation: _.get(action, 'payload.data.data.location'),
        isSuperuser: _.get(action, 'payload.data.data.isSuperuser'),
        isUserAdmin: _.get(action, 'payload.data.data.isUserAdmin'),
        isUserApprover: _.get(action, 'payload.data.data.isUserApprover', false),
        isUserManager: _.get(action, 'payload.data.data.isUserManager', false),
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
        localizedHelpScoutKey: _.get(action, 'payload.data.data.localizedHelpScoutKey'),
        isHelpScoutEnabled: _.get(action, 'payload.data.data.isHelpScoutEnabled'),
        loading: false,
        localizationModeEnabled: _.get(action, 'payload.data.data.localizationModeEnabled', false),
        localizationModeLocale: _.get(action, 'payload.data.data.localizationModeLocale', 'ach'),
        displayDateFormat: _.get(action, 'payload.data.data.displayDateFormat', 'MMM DD, yyyy'),
        displayDateDefaultValue: _.get(action, 'payload.data.data.displayDateDefaultValue', '-'),
        notificationAutohideDelay: _.get(action, 'payload.data.data.notificationAutohideDelay', 8000),
        browserConnectionTimeout: _.get(action, 'payload.data.data.browserConnectionTimeout', 0),
        isAutosaveEnabled: _.get(action, 'payload.data.data.isAutosaveEnabled', false),
      };
    case FETCH_MENU_CONFIG:
      return {
        ...state,
        menuConfig: _.get(action, 'payload.data.data.menuConfig'),
        menuSectionsUrlParts: _.get(action, 'payload.data.data.menuSectionsUrlParts'),
      };
    case CHANGE_CURRENT_LOCATION:
      return { ...state, currentLocation: action.payload, loading: true };
    case CHANGE_CURRENT_LOCALE:
      return { ...state, activeLanguage: action.payload };
    case TRANSLATIONS_FETCHED:
      return {
        ...state,
        fetchedTranslations: { ...state.fetchedTranslations, [action.payload]: true },
      };
    case TOGGLE_USER_ACTION_MENU:
      return {
        ...state,
        userActionMenuOpen: action.payload,
      };
    default:
      return state;
  }
}
