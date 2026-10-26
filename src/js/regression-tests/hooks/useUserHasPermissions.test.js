import { renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import RoleType from 'consts/roleType';
import useUserHasPermissions from 'hooks/useUserHasPermissions';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));

describe('useUserHasPermissions', () => {
  let session;

  const hasPermissions = (params) => renderHook(() => useUserHasPermissions(params)).result.current;

  beforeEach(() => {
    session = {
      user: { id: 'u1', roles: [RoleType.ROLE_MANAGER] },
      currentLocation: { id: 'loc-1' },
      currentLocationRoles: [RoleType.ROLE_INVOICE],
      isSuperuser: false,
      isUserAdmin: false,
      isUserApprover: false,
      isUserRequestApprover: false,
    };
    useSelector.mockImplementation((selector) => selector({ session }));
  });

  it('accepts a role the user has globally or at the current location', () => {
    expect(hasPermissions({ minRequiredRole: RoleType.ROLE_MANAGER })).toBe(true);
    expect(hasPermissions({ minRequiredRole: RoleType.ROLE_INVOICE })).toBe(true);
  });

  it('refuses a role the user does not have', () => {
    expect(hasPermissions({ minRequiredRole: RoleType.ROLE_FINANCE })).toBe(false);
  });

  it.each([
    [RoleType.ROLE_SUPERUSER, 'isSuperuser'],
    [RoleType.ROLE_ADMIN, 'isUserAdmin'],
    [RoleType.ROLE_PURCHASE_APPROVER, 'isUserApprover'],
    [RoleType.ROLE_REQUISITION_APPROVER, 'isUserRequestApprover'],
  ])('decides %s by the %s flag of the session, which covers the higher roles', (role, flag) => {
    // The flag is computed by the server and also holds for higher roles, which the role list
    // does not show
    session.user.roles = [role];
    expect(hasPermissions({ minRequiredRole: role })).toBe(false);

    session = { ...session, user: { id: 'u1', roles: [] }, [flag]: true };
    expect(hasPermissions({ minRequiredRole: role })).toBe(true);
  });

  it('requires every supplemental role as well', () => {
    expect(hasPermissions({
      minRequiredRole: RoleType.ROLE_MANAGER,
      supplementalRoles: [RoleType.ROLE_INVOICE],
    })).toBe(true);
    expect(hasPermissions({
      minRequiredRole: RoleType.ROLE_MANAGER,
      supplementalRoles: [RoleType.ROLE_INVOICE, RoleType.ROLE_FINANCE],
    })).toBe(false);
  });

  it('refuses everything while no user or roles are loaded', () => {
    session = { ...session, user: undefined, currentLocationRoles: undefined };

    expect(hasPermissions({ minRequiredRole: RoleType.ROLE_MANAGER })).toBe(false);
  });

  it('checks again when the location changes', () => {
    const { result, rerender } = renderHook(
      () => useUserHasPermissions({ minRequiredRole: RoleType.ROLE_INVOICE }),
    );
    expect(result.current).toBe(true);

    session = { ...session, currentLocation: { id: 'loc-2' }, currentLocationRoles: [] };
    rerender();

    expect(result.current).toBe(false);
  });
});
