import _ from 'lodash';
import queryString from 'query-string';
// Temporary 'hard-coded' checking for role to display an action in dropdown or not
export const hasMinimumRequiredRole = (role, highestUserRole) => {
  // TODO: Figure out better way to check roles
  const roles = ['Superuser', 'Admin', 'Manager', 'Assistant', 'Browser', 'Authenticated', 'Anonymous'];
  const userRoleIndex = roles.indexOf(role);
  const highestUserRoleIndex = roles.indexOf(highestUserRole);
  return (userRoleIndex !== -1 && highestUserRoleIndex !== -1)
    && highestUserRoleIndex <= userRoleIndex;
};

export const findActions = (actionList, row, props) => {
  const { supportedActivities = [], highestRole, customFilter } = props;
  // Filter out by status if any provided
  const filteredByStatus = actionList.filter((action) => {
    if (action.statuses) {
      return action.statuses.includes(row.original.status);
    }
    return true;
  });
  // Filter by activity code if any provided
  const filteredByActivityCode = filteredByStatus.filter((action) =>
    (action.activityCode
      ? action.activityCode.every((code) =>
        supportedActivities.some((activity) => activity === code))
      : true
    ));
  // Filter by required user's role if provided
  const filteredByMinimumRequiredRole = filteredByActivityCode.filter((action) => {
    if (action.minimumRequiredRole) {
      return hasMinimumRequiredRole(action.minimumRequiredRole, highestRole);
    }
    return true;
  });
  // Use custom filter callback
  if (customFilter && typeof customFilter === 'function') {
    return filteredByMinimumRequiredRole.filter((action) => customFilter(action, row));
  }
  return filteredByMinimumRequiredRole;
};

export const transformFilterParams = (filterValues, filterAccessors) => Object.keys(filterValues)
  .filter((key) => (filterAccessors[key] && !!filterValues[key]))
  .reduce((acc, key) => {
    const { name, accessor } = filterAccessors[key];

    if (!accessor) {
      return { ...acc, [key]: filterValues[name] };
    }
    if (Array.isArray(filterValues[name])) {
      return { ...acc, [key]: _.map(filterValues[name], accessor) };
    }
    return { ...acc, [key]: _.get(filterValues[name], accessor) };
  }, {});

// Transforms value into an Array
export const getParamList = (value) => [].concat(value);

export const getShipmentTypeTooltip = (translate, shipmentType) =>
  `${translate('react.stockMovement.shipmentType.label', 'Shipment type')}: ${shipmentType ?? 'Default'}`;

export const getCurrentEventComment = (event, currentStatus) => {
  if (event?.eventType?.eventCode && event?.eventType?.eventCode !== currentStatus) {
    return null;
  }
  return event?.comment;
};

// Clears query params keeping the ones that are included in fieldsToIgnore,
// and returns stringified query params
export const clearQueryParams = ({ fieldsToIgnore, queryParams }) => {
  const resultParams = Object.entries(queryParams).reduce((acc, [key, value]) => {
    if (fieldsToIgnore.includes(key)) {
      return {
        ...acc,
        [key]: value,
      };
    }
    return acc;
  }, {});

  return queryString.stringify(resultParams);
};

export const splitPreferenceTypes = (data) => data.reduce((acc, preferenceType) => {
  if (preferenceType?.destinationParty) {
    return { ...acc, preferenceTypes: [...acc.preferenceTypes, preferenceType] };
  }

  return { ...acc, defaultPreferenceType: preferenceType };
}, { preferenceTypes: [], defaultPreferenceType: {} });
