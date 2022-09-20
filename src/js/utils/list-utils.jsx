// Temporary 'hard-coded' checking for role to display an action in dropdown or not
export const hasMinimumRequiredRole = (role, highestUserRole) => {
  // TODO: Figure out better way to check roles
  const roles = ['Superuser', 'Admin', 'Manager', 'Assistant', 'Browser', 'Authenticated', 'Anonymous'];
  return roles.indexOf(highestUserRole) <= roles.indexOf(role);
};


export const findActions = (actionList, row, supportedActivities, highestUserRole) => {
  // Firstly filter out by status if any provided
  const filteredByStatus = actionList.filter((action) => {
    if (action.statuses) {
      return action.statuses.includes(row.original.status);
    }
    return true;
  });
  // Secondly filter by activity code if any provided
  const filteredByActivityCode = filteredByStatus.filter(action =>
    (action.activityCode ?
      action.activityCode.every(code => supportedActivities.some(activity => activity === code))
      : true
    ));
  // Lastly filter by required user's role if provided
  return filteredByActivityCode.filter((action) => {
    if (action.minimumRequiredRole) {
      return hasMinimumRequiredRole(action.minimumRequiredRole, highestUserRole);
    }
    return true;
  });
};

