import ActivityCode from 'consts/activityCode';
import RequisitionStatus from 'consts/requisitionStatus';
import { supports } from 'utils/supportedActivitiesUtils';

const canEditRequestWithoutRequestApproval = (location, origin, destination, isUserRequestor) => {
  const isLocationOrigin = location.id === origin?.id;
  // If we are in verifying/fulfilling location (origin), allow to add items for any person
  // verifying the request
  if (isLocationOrigin) {
    return true;
  }
  const isLocationDestination = location.id === destination?.id;
  // If we are in requesting location (destination), allow to add items only for a person
  // who created a stock request
  return isLocationDestination && isUserRequestor;
};

const canEditRequest = (currentUser, request, location) => {
  const isRequestApprovalSupported = supports(
    location?.supportedActivities,
    ActivityCode.APPROVE_REQUEST,
  );
  const isUserRequestor = currentUser?.id === request?.requestedBy?.id;
  // If request approval is not supported by the location we have to check if the
  // location is destination or origin
  if (!isRequestApprovalSupported) {
    return canEditRequestWithoutRequestApproval(
      location,
      request?.origin,
      request?.destination,
      isUserRequestor,
    );
  }

  const isLocationOrigin = location.id === request?.origin?.id;
  const isLocationDestination = location.id === request?.destination?.id;
  // If the location supports request approval, only the requestor is able to edit
  // and the request can't be approved / rejected when editing
  return isUserRequestor &&
    (isLocationDestination || isLocationOrigin) &&
    request.statusCode !== RequisitionStatus.APPROVED &&
    request.statusCode !== RequisitionStatus.REJECTED;
};

export default canEditRequest;
