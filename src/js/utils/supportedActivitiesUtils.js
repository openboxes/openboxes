import ActivityCode from 'consts/activityCode';

/**
 * Request from Ward = request from any location (not only ward),
 * that does not support MANAGE INVENTORY activity but, it supports SUBMIT REQUEST
 * */
const isRequestFromWard = (currentLocationId, destinationId, supportedActivities) =>
  currentLocationId === destinationId &&
  !supportedActivities.includes(ActivityCode.MANAGE_INVENTORY) &&
  supportedActivities.includes(ActivityCode.SUBMIT_REQUEST);

export default isRequestFromWard;
