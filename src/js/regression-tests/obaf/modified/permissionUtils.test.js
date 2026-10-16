// OBAF-modified: canEditRequest blocks editing a request whose origin requires mobile picking
// while it is being picked, checked or staged (returns are exempt).
import canEditRequest, { hasRole } from 'utils/permissionUtils';

const USER = { id: 'u1' };
const ORIGIN = { id: 'origin', supportedActivities: ['REQUIRE_MOBILE_PICKING'] };
const DEST = { id: 'dest' };

const request = (overrides = {}) => ({
  origin: ORIGIN,
  destination: DEST,
  requestedBy: { id: 'u1' },
  statusCode: 'PICKING',
  ...overrides,
});

describe('canEditRequest (OBAF)', () => {
  describe('origin requires mobile picking', () => {
    it.each(['PICKING', 'PICKED', 'CHECKING', 'STAGED'])('blocks editing in %s, even at the origin', (statusCode) => {
      expect(canEditRequest(USER, request({ statusCode }), { id: 'origin' })).toBe(false);
    });

    it.each(['REQUESTED', 'VALIDATED', 'PACKED'])('still lets the origin edit in %s', (statusCode) => {
      expect(canEditRequest(USER, request({ statusCode }), { id: 'origin' })).toBe(true);
    });

    it('does not block a return', () => {
      expect(canEditRequest(USER, request({ isReturn: true, statusCode: 'PICKED' }), { id: 'origin' })).toBe(true);
    });

    it('blocks a PICKED request even when approval would otherwise allow the origin', () => {
      const location = { id: 'origin', supportedActivities: ['APPROVE_REQUEST'] };
      expect(canEditRequest(USER, request({ statusCode: 'PICKED' }), location)).toBe(false);
    });
  });

  describe('origin without mobile picking (upstream rules)', () => {
    const plain = (overrides) => request({ origin: { id: 'origin', supportedActivities: [] }, ...overrides });

    it('lets the origin edit while picking', () => {
      expect(canEditRequest(USER, plain({ statusCode: 'PICKING' }), { id: 'origin' })).toBe(true);
    });

    it('lets only the requestor edit at the destination', () => {
      expect(canEditRequest(USER, plain(), { id: 'dest' })).toBe(true);
      expect(canEditRequest({ id: 'other' }, plain(), { id: 'dest' })).toBe(false);
      expect(canEditRequest(USER, plain(), { id: 'elsewhere' })).toBe(false);
    });

    it('with approval, the requestor edits before approval but not after a rejection', () => {
      const location = { id: 'dest', supportedActivities: ['APPROVE_REQUEST'] };
      expect(canEditRequest(USER, plain({ statusCode: 'PENDING_APPROVAL' }), location)).toBe(true);
      expect(canEditRequest(USER, plain({ statusCode: 'REJECTED' }), location)).toBe(false);
      expect(canEditRequest(USER, plain({ statusCode: 'APPROVED' }), location)).toBe(false);
      expect(canEditRequest(USER, plain({ statusCode: 'APPROVED' }), { ...location, id: 'origin' })).toBe(true);
    });
  });

  it('hasRole reads the user roles', () => {
    expect(hasRole({ roles: ['ROLE_ADMIN'] }, 'ROLE_ADMIN')).toBe(true);
    expect(hasRole({ roles: [] }, 'ROLE_ADMIN')).toBe(false);
    expect(hasRole(undefined, 'ROLE_ADMIN')).toBeUndefined();
  });
});
