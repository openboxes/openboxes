import {
  CLEAR_ERRORS_DATA,
  FETCH_APPROVERS,
  FETCH_ATTRIBUTES,
  FETCH_BUYERS,
  FETCH_INVOICE_STATUSES,
  FETCH_INVOICE_TYPE_CODES,
  FETCH_LOCATION_TYPES,
  FETCH_LOT_NUMBERS_BY_PRODUCT_IDS,
  FETCH_ORGANIZATIONS,
  FETCH_PAYMENT_TERMS,
  FETCH_PREFERENCE_TYPES,
  FETCH_PURCHASE_ORDER_STATUSES,
  FETCH_RATING_TYPE_OPTIONS,
  FETCH_REASONCODES,
  FETCH_REQUISITION_STATUS_CODES,
  FETCH_SHIPMENT_STATUS_CODES,
  FETCH_SHIPMENT_TYPES,
  FETCH_STOCK_TRANSFER_STATUSES,
  FETCH_SUPPLIERS,
  FETCH_UNIT_OF_MEASURE_CURRENCY,
  FETCH_UNIT_OF_MEASURE_QUANTITY,
  FETCH_USERS,
  FILTER_FORM_PARAMS_BUILT,
  HIDE_SPINNER,
  REBUILD_FILTER_FORM_PARAMS,
  SET_ERRORS,
  SET_ERRORS_BY_ID,
  SET_OFFLINE,
  SET_ONLINE,
  SET_SCROLL_TO_BOTTOM,
  SHOW_SPINNER,
  SUBMIT_FORM,
  UPDATE_WORKFLOW_HEADER,
} from 'actions/types';
import approversReducer from 'reducers/approversReducer';
import connectionReducer from 'reducers/connectionReducer';
import errorsReducer from 'reducers/errorsReducer';
import filterFormReducer from 'reducers/filterFormReducer';
import inboundReducer from 'reducers/inboundReducer';
import invoiceReducer from 'reducers/invoiceReducer';
import locationReducer from 'reducers/locationReducer';
import lotNumbersReducer from 'reducers/lotNumbersReducer';
import organizationsReducer from 'reducers/organizationsReducer';
import outboundImportReducer from 'reducers/outboundImportReducer';
import productSupplierReducer from 'reducers/productSupplierReducer';
import purchaseOrderReducer from 'reducers/purchaseOrderReducer';
import reasonCodesReducer from 'reducers/reasonCodesReducer';
import requisitionStatusCodes from 'reducers/requisitionStatusCodes';
import shipmentStatusCodes from 'reducers/shipmentStatusCodes';
import spinnerReducer from 'reducers/spinnerReducer';
import stockMovementCommonReducer from 'reducers/stockMovementCommonReducer';
import stockTransferReducer from 'reducers/stockTransferReducer';
import unitOfMeasureReducer from 'reducers/unitOfMeasureReducer';
import usersReducer from 'reducers/usersReducer';

import deepFreeze from './deepFreeze';

const UNKNOWN = { type: 'SOME_UNRELATED_ACTION' };
const init = (reducer) => reducer(undefined, UNKNOWN);

// Reducers whose actions copy the payload into a single key of the state.
const payloadSetters = [
  ['invoiceReducer', FETCH_INVOICE_STATUSES, 'statuses', invoiceReducer],
  ['invoiceReducer', FETCH_INVOICE_TYPE_CODES, 'typeCodes', invoiceReducer],
  ['locationReducer', FETCH_LOCATION_TYPES, 'locationTypes', locationReducer],
  ['purchaseOrderReducer', FETCH_PURCHASE_ORDER_STATUSES, 'statuses', purchaseOrderReducer],
  ['purchaseOrderReducer', FETCH_PAYMENT_TERMS, 'paymentTerms', purchaseOrderReducer],
  ['productSupplierReducer', FETCH_PREFERENCE_TYPES, 'preferenceTypes', productSupplierReducer],
  ['productSupplierReducer', FETCH_RATING_TYPE_OPTIONS, 'ratingTypeCodes', productSupplierReducer],
  ['productSupplierReducer', FETCH_ATTRIBUTES, 'attributes', productSupplierReducer],
  ['stockTransferReducer', FETCH_STOCK_TRANSFER_STATUSES, 'statuses', stockTransferReducer],
  ['stockMovementCommonReducer', FETCH_SHIPMENT_TYPES, 'shipmentTypes', stockMovementCommonReducer],
  ['organizationsReducer', FETCH_SUPPLIERS, 'suppliers', organizationsReducer],
  ['organizationsReducer', FETCH_BUYERS, 'buyers', organizationsReducer],
];

describe('reducers that store the payload under one key', () => {
  it.each(payloadSetters)('%s stores %s under "%s" and keeps the other keys', (name, type, key, reducer) => {
    const previous = deepFreeze({ ...init(reducer), untouched: 'kept' });
    const payload = [{ id: 'a', label: 'A' }];

    const next = reducer(previous, { type, payload });

    expect(next[key]).toBe(payload);
    expect(next.untouched).toBe('kept');
    expect(next).not.toBe(previous);
  });

  it.each([
    ['invoiceReducer', invoiceReducer, { statuses: [], typeCodes: [] }],
    ['locationReducer', locationReducer, { locationTypes: [] }],
    ['purchaseOrderReducer', purchaseOrderReducer, { statuses: [], paymentTerms: [] }],
    ['productSupplierReducer', productSupplierReducer, { preferenceTypes: [], ratingTypeCodes: [], attributes: [] }],
    ['stockTransferReducer', stockTransferReducer, { statuses: [] }],
    ['stockMovementCommonReducer', stockMovementCommonReducer, { shipmentTypes: [] }],
    ['organizationsReducer', organizationsReducer, {
      data: [], fetched: false, suppliers: [], buyers: null,
    }],
    ['outboundImportReducer', outboundImportReducer, { scrollToBottom: false }],
    ['reasonCodesReducer', reasonCodesReducer, { data: [], fetched: false }],
    ['spinnerReducer', spinnerReducer, { show: false }],
    ['filterFormReducer', filterFormReducer, { shouldRebuildParams: false }],
    ['lotNumbersReducer', lotNumbersReducer, { lotNumbersWithExpiration: {} }],
    ['inboundReducer', inboundReducer, { headerInfo: [], headerStatus: null }],
    ['requisitionStatusCodes', requisitionStatusCodes, { data: [] }],
    ['shipmentStatusCodes', shipmentStatusCodes, { data: [] }],
    ['unitOfMeasureReducer', unitOfMeasureReducer, { currency: [], quantity: [] }],
    ['usersReducer', usersReducer, { data: [], fetched: false }],
    ['approversReducer', approversReducer, { data: [], fetched: false }],
    ['errorsReducer', errorsReducer, { isFormSubmitted: false, errors: {} }],
  ])('%s starts from its initial state', (name, reducer, expected) => {
    expect(init(reducer)).toEqual(expected);
  });

  it.each([
    invoiceReducer, locationReducer, purchaseOrderReducer, productSupplierReducer,
    stockTransferReducer, stockMovementCommonReducer, organizationsReducer, outboundImportReducer,
    reasonCodesReducer, spinnerReducer, connectionReducer, filterFormReducer, lotNumbersReducer,
    inboundReducer, requisitionStatusCodes, shipmentStatusCodes, unitOfMeasureReducer, usersReducer,
    approversReducer, errorsReducer,
  ])('returns the same state reference for an unknown action (%#)', (reducer) => {
    const previous = deepFreeze({ some: 'state' });

    expect(reducer(previous, UNKNOWN)).toBe(previous);
  });
});

describe('spinnerReducer', () => {
  it('shows and hides the spinner', () => {
    const shown = spinnerReducer(deepFreeze({ show: false }), { type: SHOW_SPINNER });
    expect(shown).toEqual({ show: true });

    const hidden = spinnerReducer(deepFreeze(shown), { type: HIDE_SPINNER });
    expect(hidden).toEqual({ show: false });
  });
});

describe('connectionReducer', () => {
  it('takes the initial online flag from the browser', () => {
    expect(init(connectionReducer)).toEqual({ online: navigator.onLine });
  });

  it('switches between online and offline', () => {
    const offline = connectionReducer(deepFreeze({ online: true }), { type: SET_OFFLINE });
    expect(offline).toEqual({ online: false });

    const online = connectionReducer(deepFreeze(offline), { type: SET_ONLINE });
    expect(online).toEqual({ online: true });
  });
});

describe('filterFormReducer', () => {
  it('raises the rebuild flag and lowers it once the params are built', () => {
    const raised = filterFormReducer(deepFreeze({ shouldRebuildParams: false }), {
      type: REBUILD_FILTER_FORM_PARAMS,
    });
    expect(raised.shouldRebuildParams).toBe(true);

    const lowered = filterFormReducer(deepFreeze(raised), { type: FILTER_FORM_PARAMS_BUILT });
    expect(lowered.shouldRebuildParams).toBe(false);
  });
});

describe('outboundImportReducer', () => {
  it('stores the scroll flag from the payload', () => {
    const next = outboundImportReducer(deepFreeze({ scrollToBottom: false }), {
      type: SET_SCROLL_TO_BOTTOM,
      payload: true,
    });

    expect(next).toEqual({ scrollToBottom: true });
  });
});

describe('reasonCodesReducer', () => {
  it('stores the reason codes and marks them fetched', () => {
    const reasonCodes = [{ id: 'DAMAGED', value: 'DAMAGED', label: 'Damaged' }];

    const next = reasonCodesReducer(deepFreeze(init(reasonCodesReducer)), {
      type: FETCH_REASONCODES,
      payload: reasonCodes,
    });

    expect(next).toEqual({ data: reasonCodes, fetched: true });
  });
});

describe('lotNumbersReducer', () => {
  it('merges new products into the lots already loaded', () => {
    const previous = deepFreeze({
      lotNumbersWithExpiration: {
        p1: [{ lotNumber: 'L1' }],
        p2: [{ lotNumber: 'L2' }],
      },
    });

    const next = lotNumbersReducer(previous, {
      type: FETCH_LOT_NUMBERS_BY_PRODUCT_IDS,
      payload: { p2: [{ lotNumber: 'L2-NEW' }], p3: [] },
    });

    expect(next.lotNumbersWithExpiration).toEqual({
      p1: [{ lotNumber: 'L1' }],
      p2: [{ lotNumber: 'L2-NEW' }],
      p3: [],
    });
  });
});

describe('inboundReducer', () => {
  it('stores the header info and status', () => {
    const next = inboundReducer(deepFreeze(init(inboundReducer)), {
      type: UPDATE_WORKFLOW_HEADER,
      payload: { headerInfo: [{ text: 'Shipment 1' }], headerStatus: 'PENDING' },
    });

    expect(next).toEqual({ headerInfo: [{ text: 'Shipment 1' }], headerStatus: 'PENDING' });
  });

  it('ignores an update without a payload', () => {
    const previous = deepFreeze({ headerInfo: [{ text: 'kept' }], headerStatus: 'SHIPPED' });

    expect(inboundReducer(previous, { type: UPDATE_WORKFLOW_HEADER })).toBe(previous);
  });
});

describe.each([
  ['requisitionStatusCodes', requisitionStatusCodes, FETCH_REQUISITION_STATUS_CODES],
  ['shipmentStatusCodes', shipmentStatusCodes, FETCH_SHIPMENT_STATUS_CODES],
])('%s', (name, reducer, type) => {
  it('replaces the data with the payload', () => {
    const codes = [{ id: 'PENDING', label: 'Pending' }];

    expect(reducer(deepFreeze({ data: [{ id: 'old' }] }), { type, payload: codes }))
      .toEqual({ data: codes });
  });

  it('keeps the previous data when the payload is empty', () => {
    const previous = deepFreeze({ data: [{ id: 'old' }] });

    expect(reducer(previous, { type, payload: undefined })).toBe(previous);
  });
});

describe('unitOfMeasureReducer', () => {
  it.each([
    [FETCH_UNIT_OF_MEASURE_CURRENCY, 'currency', 'quantity'],
    [FETCH_UNIT_OF_MEASURE_QUANTITY, 'quantity', 'currency'],
  ])('%s stores payload.data under "%s" and leaves "%s" alone', (type, key, otherKey) => {
    const previous = deepFreeze({ currency: [{ id: 'USD' }], quantity: [{ id: 'EA' }] });
    const units = [{ id: 'new' }];

    const next = unitOfMeasureReducer(previous, { type, payload: { data: units } });

    expect(next[key]).toBe(units);
    expect(next[otherKey]).toBe(previous[otherKey]);
  });

  it.each([FETCH_UNIT_OF_MEASURE_CURRENCY, FETCH_UNIT_OF_MEASURE_QUANTITY])(
    '%s without a payload keeps the state',
    (type) => {
      const previous = deepFreeze({ currency: [], quantity: [] });

      expect(unitOfMeasureReducer(previous, { type })).toBe(previous);
    },
  );
});

describe('usersReducer', () => {
  it('maps the users to select options and marks them fetched', () => {
    const next = usersReducer(deepFreeze(init(usersReducer)), {
      type: FETCH_USERS,
      payload: { data: [{ id: 'u1', name: 'Ann Smith', email: 'ann@example.com' }] },
    });

    expect(next).toEqual({
      data: [{
        value: 'u1', id: 'u1', label: 'Ann Smith', name: 'Ann Smith',
      }],
      fetched: true,
    });
  });

  it('keeps the state when the payload is missing', () => {
    const previous = deepFreeze(init(usersReducer));

    expect(usersReducer(previous, { type: FETCH_USERS })).toBe(previous);
  });
});

describe('approversReducer', () => {
  it('maps the approvers (a plain list) to select options', () => {
    const next = approversReducer(deepFreeze(init(approversReducer)), {
      type: FETCH_APPROVERS,
      payload: [{ id: 'a1', name: 'Bo Lee' }, { id: 'a2', name: '\u00c5sa Berg' }],
    });

    expect(next).toEqual({
      data: [
        {
          value: 'a1', id: 'a1', label: 'Bo Lee', name: 'Bo Lee',
        },
        {
          value: 'a2', id: 'a2', label: '\u00c5sa Berg', name: '\u00c5sa Berg',
        },
      ],
      fetched: true,
    });
  });

  it('stores an empty list when the response had no approvers', () => {
    const next = approversReducer(deepFreeze(init(approversReducer)), {
      type: FETCH_APPROVERS,
      payload: undefined,
    });

    expect(next).toEqual({ data: [], fetched: true });
  });
});

describe('organizationsReducer', () => {
  it('maps the organizations of the response to options', () => {
    const next = organizationsReducer(deepFreeze(init(organizationsReducer)), {
      type: FETCH_ORGANIZATIONS,
      payload: { data: { data: [{ id: 'o1', name: 'Supplier One', code: 'S1' }] } },
    });

    expect(next.data).toEqual([{ value: 'o1', id: 'o1', label: 'Supplier One' }]);
    expect(next.fetched).toBe(true);
    expect(next.suppliers).toEqual([]);
  });

  it('keeps the state when the response has no data', () => {
    const previous = deepFreeze(init(organizationsReducer));

    expect(organizationsReducer(previous, { type: FETCH_ORGANIZATIONS, payload: {} }))
      .toBe(previous);
  });
});

describe('errorsReducer', () => {
  it('replaces all errors', () => {
    const previous = deepFreeze({ isFormSubmitted: true, errors: { a: ['old'] } });

    const next = errorsReducer(previous, { type: SET_ERRORS, payload: { b: ['new'] } });

    expect(next).toEqual({ isFormSubmitted: true, errors: { b: ['new'] } });
  });

  it('sets the errors of one id and keeps the others', () => {
    const previous = deepFreeze({ isFormSubmitted: false, errors: { a: ['kept'], b: ['old'] } });

    const next = errorsReducer(previous, {
      type: SET_ERRORS_BY_ID,
      payload: { id: 'b', errors: ['new'] },
    });

    expect(next.errors).toEqual({ a: ['kept'], b: ['new'] });
  });

  it('marks the form submitted', () => {
    const next = errorsReducer(deepFreeze(init(errorsReducer)), { type: SUBMIT_FORM });

    expect(next).toEqual({ isFormSubmitted: true, errors: {} });
  });

  it('clears back to the initial state', () => {
    const previous = deepFreeze({ isFormSubmitted: true, errors: { a: ['x'] } });

    expect(errorsReducer(previous, { type: CLEAR_ERRORS_DATA }))
      .toEqual({ isFormSubmitted: false, errors: {} });
  });
});
