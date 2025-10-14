import { getTranslate } from 'react-localize-redux';
import { createSelector } from 'reselect';

import { translateWithDefaultMessage } from 'utils/Translate';
import { formatDate } from 'utils/translation-utils';

/**
 * SESSION SELECTORS
 */
export const getSession = (state) => state.session;

export const getNotificationAutohideDelay = createSelector(
  [getSession],
  (session) => session.notificationAutohideDelay,
);

// Cache for locations
const locationCache = new Map();

export const getCurrentLocation = createSelector(
  [getSession],
  (session) => {
    const location = session.currentLocation;
    if (!location?.id) {
      return location;
    }

    if (!locationCache.has(location.id)) {
      locationCache.set(location.id, location);
    }

    return locationCache.get(location.id);
  },
);

export const getCurrentLocationId = createSelector(
  [getCurrentLocation],
  (currentLocation) => currentLocation?.id,
);

export const getCurrentUser = createSelector(
  [getSession],
  (session) => session.user,
);

export const getCurrentUserId = createSelector(
  [getCurrentUser],
  (user) => user?.id,
);

export const getSearchConfig = createSelector(
  [getSession],
  (session) => session.searchConfig,
);

export const getDebounceTime = createSelector(
  [getSearchConfig],
  (config) => config.debounceTime,
);

export const getMinSearchLength = createSelector(
  [getSearchConfig],
  (config) => config.minSearchLength,
);

export const getBrowserConnectionTimeout = createSelector(
  [getSession],
  (session) => session.browserConnectionTimeout,
);

export const getCurrentLocale = createSelector(
  [getSession],
  (session) => session.activeLanguage,
);

export const getCurrentLocationSupportedActivities = createSelector(
  [getCurrentLocation],
  (location) => location?.supportedActivities,
);

export const getCycleCountMaxSelectedProducts = createSelector(
  [getSession],
  (session) => session.cycleCountMaxSelectedProducts,
);

/**
 * LOCALIZE & TRANSLATION
 */
export const getLocalize = (state) => state.localize;

export const getLocaleCode = createSelector(
  [getLocalize],
  (localize) => localize.languages.find((lang) => lang.active)?.code,
);

export const getActiveLanguage = createSelector(
  [getSession],
  (session) => session.activeLanguage,
);

export const getSupportedLocales = createSelector(
  [getSession],
  (session) => session.supportedLocales,
);

// Cache for formatDate functions
const formatDateCache = new Map();

export const getFormatLocalizedDate = createSelector(
  [getLocalize, getLocaleCode],
  (localize, localeCode) => {
    if (!formatDateCache.has(localeCode)) {
      formatDateCache.set(localeCode, formatDate(localize));
    }
    return formatDateCache.get(localeCode);
  },
);

// Cache translate function
const translateCache = new Map();

export const getAppTranslate = createSelector(
  [getLocalize, getLocaleCode],
  (localize, localeCode) => {
    if (!translateCache.has(localeCode)) {
      translateCache.set(localeCode, translateWithDefaultMessage(getTranslate(localize)));
    }
    return translateCache.get(localeCode);
  },
);

/**
 * USERS
 */
export const getUsers = (state) => state.users.data;

/**
 * CYCLE COUNT
 */
export const getCycleCount = (state) => state.cycleCount;

export const getRequests = createSelector(
  [getCycleCount],
  (cycleCount) => cycleCount.requests,
);

export const getCycleCountRequestIds = createSelector(
  [getRequests, getCurrentLocationId],
  (requests, locationId) => requests?.[locationId] || [],
);

export const getCycleCountIds = createSelector(
  [getCycleCount, getCurrentLocationId],
  (cycleCount, locationId) => cycleCount.cycleCounts?.[locationId] || [],
);

export const getReasonCodes = createSelector(
  [getCycleCount],
  (cycleCount) => cycleCount.reasonCodes,
);

export const getBinLocations = createSelector(
  [getCycleCount],
  (cycleCount) => cycleCount.binLocations,
);

export const getCycleCountTranslations = createSelector(
  [getSession],
  (session) => session.fetchedTranslations.cycleCount,
);

/**
 * LOT NUMBERS
 */
export const getLotNumbersWithExpiration = (state) =>
  state.lotNumbers?.lotNumbersWithExpiration || [];

export const makeGetLotNumbersByProductId = () =>
  createSelector(
    [getLotNumbersWithExpiration],
    (_, productId) => productId,
    (lotNumbers, productId) => lotNumbers?.[productId] || [],
  );

/**
 * COUNT WORKFLOW
 */
export const getCountWorkflow = (state) => state.countWorkflow;

export const getCountWorkflowEntities = createSelector(
  [getCountWorkflow],
  (wf) => wf?.entities,
);

export const makeGetCycleCountItems = () =>
  createSelector(
    getCountWorkflowEntities,
    (_, cycleCountId) => cycleCountId,
    (entities, id) => entities?.[id]?.cycleCountItems || [],
  );

export const makeGetCycleCountDateCounted = () =>
  createSelector(
    (state) => state.countWorkflow?.dateCounted,
    (_, cycleCountId) => cycleCountId,
    (dates, id) => dates?.[id],
  );

export const makeGetCycleCountCountedBy = () =>
  createSelector(
    (state) => state.countWorkflow?.countedBy,
    (_, cycleCountId) => cycleCountId,
    (countedBy, id) => countedBy?.[id],
  );

export const getAllCycleCountProducts = createSelector(
  [getCountWorkflowEntities],
  (entities) => {
    if (!entities) return [];
    return Object.values(entities)
      .flatMap((cc) => cc?.cycleCountItems || [])
      .map((item) => item.product?.id)
      .filter(Boolean);
  },
);

export const makeGetCycleCountProduct = () =>
  createSelector(
    makeGetCycleCountItems(),
    (items) => items?.[0]?.product,
  );

/**
 * CONNECTION
 */
export const getOnline = (state) => state.connection.online;

/**
 * SPINNER
 */
export const getSpinner = (state) => state.spinner.show;

/**
 *
 * CURRENCY
 */
export const getCurrencyCode = (state) => state.session.currencyCode;
