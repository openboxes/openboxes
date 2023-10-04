import ActivityCode from 'consts/activityCode';
import RequisitionStatus from 'consts/requisitionStatus';
import { supports } from 'utils/supportedActivitiesUtils';

const canEditRequest = (currentUser, request, location) => {
  const isUserRequestor = currentUser?.id === request?.requestedBy?.id;
  const isLocationOrigin = location.id === request?.origin?.id;
  const isLocationDestination = location.id === request?.destination?.id;
  const isApprovalRequired = supports(
    location?.supportedActivities,
    ActivityCode.APPROVE_REQUEST,
  );

  // If the location supports request approval, only the requestor is able to edit
  // and the request can't be approved / rejected when editing
  if (isApprovalRequired) {
    return isUserRequestor &&
      (isLocationDestination || isLocationOrigin) &&
      request.statusCode !== RequisitionStatus.APPROVED &&
      request.statusCode !== RequisitionStatus.REJECTED;
  }

  // If request approval is not supported by the location we have to check if the
  // location is destination or origin
  // If we are in verifying/fulfilling location (origin), allow to add items for any person
  // verifying the request
  if (isLocationOrigin) {
    return true;
  }
  // If we are in requesting location (destination), allow to add items only for a person
  // who created a stock request
  return isLocationDestination && isUserRequestor;
};

export default canEditRequest;
