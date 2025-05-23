import { formatDate } from 'utils/translation-utils';

export const getCycleCountRequestIds = (state) =>
  state.cycleCount.requests[state.session.currentLocation?.id] || [];

export const getCurrentLocation = (state) => state.session.currentLocation;

export const getCycleCountMaxSelectedProducts = (state) =>
  state.session.cycleCountMaxSelectedProducts;

export const getCurrentLocale = (state) => state.session.activeLanguage;

export const getUsers = (state) => state.users.data;

export const getCycleCountsIds = (state) =>
  state.cycleCount.cycleCounts[state.session.currentLocation?.id] || [];

export const getReasonCodes = (state) => state.cycleCount.reasonCodes;

export const getFormatLocalizedDate = (state) => formatDate(state.localize);

export const getBinLocations = (state) => state.cycleCount.binLocations;
