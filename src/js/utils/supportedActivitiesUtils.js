import ActivityCode from 'consts/activityCode';

/**
 * Request from Ward = request from any location (not only ward),
 * that does not support MANAGE INVENTORY activity but, it supports SUBMIT REQUEST
 * */
export const isRequestFromWard = (currentLocationId, destinationId, supportedActivities) =>
  currentLocationId === destinationId
  && !supportedActivities.includes(ActivityCode.MANAGE_INVENTORY)
  && supportedActivities.includes(ActivityCode.SUBMIT_REQUEST);

export const supports = (locationSupportedActivities, activity) =>
  locationSupportedActivities?.includes(activity);

export const checkBinLocationSupport = (supportedActivities) =>
  supports(supportedActivities, ActivityCode.PUTAWAY_STOCK)
  && supports(supportedActivities, ActivityCode.PICK_STOCK);
