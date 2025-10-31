import ActivityCode from 'consts/activityCode';
import StockMovementStatus from 'consts/stockMovementStatus';
import { supports } from 'utils/supportedActivitiesUtils';

const canEditRequest = (currentUser, request, location) => {
  const isUserRequestor = currentUser?.id === request?.requestedBy?.id;
  const isLocationOrigin = location.id === request?.origin?.id;
  const isLocationDestination = location.id === request?.destination?.id;
  const isApprovalRequired = supports(
    location?.supportedActivities,
    ActivityCode.APPROVE_REQUEST,
  );

  if (isApprovalRequired) {
    // If the location supports request approval, only the requestor is able to edit
    // If the request is rejected, then it cannot be edited
    const statusesWithAbilityToEdit = [
      StockMovementStatus.APPROVED,
      StockMovementStatus.PACKED,
      StockMovementStatus.PICKED,
      StockMovementStatus.PICKING,
    ];
    if (!statusesWithAbilityToEdit.includes(request?.statusCode)) {
      return isUserRequestor
        && (isLocationDestination || isLocationOrigin)
        && request.statusCode !== StockMovementStatus.REJECTED;
    }
    // If the request is approved, everyone from the fulfilling location can edit
    return isLocationOrigin;
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

export const hasRole = (user, role) => user?.roles?.includes(role);

export default canEditRequest;
