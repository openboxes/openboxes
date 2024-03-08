import { useMemo } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';

import RoleType from 'consts/roleType';

const useUserHasPermissions = ({ minRequiredRole, roles }) => {
  const {
    currentUser,
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
  }));

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
        return true;
    }
  }, [currentUser]);

  const hasRequiredRoles = useMemo(() => {
    const userRoles = Array.isArray(currentUser?.roles) ? currentUser?.roles : [];
    const userLocationRoles = Array.isArray(currentLocationRoles) ? currentLocationRoles : [];

    const allUserRoles = new Set([...userRoles, ...userLocationRoles]);

    return _.every(roles, (role) => allUserRoles.has(role));
  }, [currentUser]);

  return hasMinimumRequiredRole && hasRequiredRoles;
};

export default useUserHasPermissions;
