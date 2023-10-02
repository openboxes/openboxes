import ActivityCode from 'consts/activityCode';
import RequisitionStatus from 'consts/requisitionStatus';
import { supports } from 'utils/supportedActivitiesUtils';

const canEditRequest = (currentUser, request, location) => {
  const isRequestApprovalSupported = supports(
    location?.supportedActivities,
    ActivityCode.APPROVE_REQUEST,
  );

  // If request approval is not supported by the location we are able to edit every request
  if (!isRequestApprovalSupported) {
    return true;
  }

  const isUserRequestor = currentUser?.id === request?.requestedBy?.id;
  // If the location supports request approval, only the requestor is able to edit
  // and the request can't be approved / rejected when editing
  return isUserRequestor &&
    request.statusCode !== RequisitionStatus.APPROVED &&
    request.statusCode !== RequisitionStatus.REJECTED;
};

export default canEditRequest;
