import ActivityCode from 'consts/activityCode';

const isRequestFromWard = (currentLocationId, destinationId, supportedActivities) =>
  currentLocationId === destinationId &&
  !supportedActivities.includes(ActivityCode.MANAGE_INVENTORY) &&
  supportedActivities.includes(ActivityCode.SUBMIT_REQUEST);

export default isRequestFromWard;
