import { useCallback, useMemo } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';

import RoleType from 'consts/roleType';

/**
 * Returns true or false if current logged-in user has all the roles specified in the parameters.
 * The function checks if user has either global or location based roles.
 * @param minRequiredRole {RoleType} - minimum required role
 * @param supplementalRoles {RoleType[]} [d=[]] - list of supplemental required roles
 * @returns {boolean}
 */
const useUserHasPermissions = ({ minRequiredRole, supplementalRoles = [] }) => {
  const {
    currentUser,
    currentLocation,
    currentLocationRoles,
    isSuperuser,
    isAdmin,
    isApprover,
    isRequestApprover,
  } = useSelector((state) => ({
    currentUser: state.session.user,
    currentLocationRoles: state.session.currentLocationRoles,
    isSuperuser: state.session.isSuperuser,
    isAdmin: state.session.isUserAdmin,
    isApprover: state.session.isUserApprover,
    isRequestApprover: state.session.isUserRequestApprover,
    currentLocation: state.session.currentLocation,
  }));

  /**
   * Returns true if user has all of the specified roles as global or location roles.
   * @type {function(RoleType[]): boolean}
   */
  const hasEveryRole = useCallback((roles) => {
    const userRoles = Array.isArray(currentUser?.roles) ? currentUser?.roles : [];
    const userLocationRoles = Array.isArray(currentLocationRoles) ? currentLocationRoles : [];

    const allUserRoles = new Set([...userRoles, ...userLocationRoles]);

    return _.every(roles, (role) => allUserRoles.has(role));
  }, [currentUser?.id, currentLocation?.id]);

  /**
   * Returns true if user has minimum required role.
   * if minRequired role is specified as ROLE_ADMIN and user has ROLE_SUPERUSER, it will return true
   * because SUPERUSER is a higher role than admin
   * @returns {boolean}
   */
  const hasMinimumRequiredRole = useMemo(() => {
    switch (minRequiredRole) {
      case RoleType.ROLE_SUPERUSER:
        return isSuperuser;
      case RoleType.ROLE_ADMIN:
        return isAdmin;
      case RoleType.ROLE_PURCHASE_APPROVER:
        return isApprover;
      case RoleType.ROLE_REQUISITION_APPROVER:
        return isRequestApprover;
      default:
        return hasEveryRole([minRequiredRole]);
    }
  }, [currentUser?.id, currentLocation?.id]);

  /**
   * Returns true if user has all the supplemental roles
   * @returns {boolean}
   */
  const hasRequiredRoles = useMemo(() => hasEveryRole(supplementalRoles),
    [currentUser?.id, currentLocation?.id]);

  return hasMinimumRequiredRole && hasRequiredRoles;
};

export default useUserHasPermissions;
