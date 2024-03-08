import { useMemo } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';

import RoleType from 'consts/roleType';
import { hasRole } from 'utils/permissionUtils';

const useUserHasPermissions = ({ minRequiredRole, roles }) => {
  const {
    currentUser,
    isSuperuser,
    isAdmin,
    isApprover,
    isRequestApprover,
  } = useSelector((state) => ({
    currentUser: state.session.user,
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

  const hasRequiredRoles = useMemo(() =>
    _.every(roles, (role) => hasRole(currentUser, role)),
  [currentUser]);

  return hasMinimumRequiredRole && hasRequiredRoles;
};

export default useUserHasPermissions;
