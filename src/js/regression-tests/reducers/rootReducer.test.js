import { SHOW_SPINNER } from 'actions/types';
import rootReducer from 'reducers';

// The real client pulls in the store (through the login modal), and the store imports the root
// reducer: loading it from here would close that import cycle half way. No request is made.
jest.mock('utils/apiClient', () => ({ __esModule: true, default: {} }));

// Adapted for OBAF (OBAF lacks the code): no partialReceiving slice.

describe('rootReducer', () => {
  it('combines every slice of the store', () => {
    const state = rootReducer(undefined, { type: 'UNKNOWN' });

    expect(Object.keys(state).sort()).toEqual([
      'approvers', 'connection', 'countWorkflow', 'cycleCount', 'errors', 'filterForm', 'inbound',
      'indicators', 'infoBar', 'infoBarVisibility', 'invoices', 'localize', 'location', 'lotNumbers',
      'organizations', 'outboundImport', 'productSupplier', 'purchaseOrder',
      'reasonCodes', 'requisitionStatuses', 'session', 'shipmentStatuses', 'spinner', 'stockMovementCommon',
      'stockMovementDraft', 'stockTransfer', 'unitOfMeasure', 'users',
    ]);
  });

  it('routes an action to its slice and keeps the other slices by reference', () => {
    const previous = rootReducer(undefined, { type: 'UNKNOWN' });

    const next = rootReducer(previous, { type: SHOW_SPINNER });

    expect(next.spinner).toEqual({ show: true });
    expect(next.session).toBe(previous.session);
    expect(next.indicators).toBe(previous.indicators);
  });
});
