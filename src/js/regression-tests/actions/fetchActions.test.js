import { addTranslationForLanguage } from 'react-localize-redux';
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  changeCurrentLocale,
  changeCurrentLocation,
  fetchAttributes,
  fetchAvailableApprovers,
  fetchBinLocations,
  fetchBuyers,
  fetchCurrencies,
  fetchInvoiceStatuses,
  fetchInvoiceTypeCodes,
  fetchLocationTypes,
  fetchLotNumbersByProductIds,
  fetchMenuConfig,
  fetchPaymentTerms,
  fetchPreferenceTypes,
  fetchPurchaseOrderStatuses,
  fetchQuantityUnitOfMeasure,
  fetchRatingTypeCodes,
  fetchReasonCodes,
  fetchRequisitionStatusCodes,
  fetchSessionInfo,
  fetchShipmentStatusCodes,
  fetchShipmentTypes,
  fetchStockTransferStatuses,
  fetchSuppliers,
  fetchTranslations,
  fetchUsers,
} from 'actions';
import {
  CHANGE_CURRENT_LOCALE,
  CHANGE_CURRENT_LOCATION,
  FETCH_APPROVERS,
  FETCH_ATTRIBUTES,
  FETCH_BIN_LOCATIONS,
  FETCH_BUYERS,
  FETCH_CYCLE_COUNT_REASON_CODES,
  FETCH_INVOICE_STATUSES,
  FETCH_INVOICE_TYPE_CODES,
  FETCH_LOCATION_TYPES,
  FETCH_LOT_NUMBERS_BY_PRODUCT_IDS,
  FETCH_MENU_CONFIG,
  FETCH_PAYMENT_TERMS,
  FETCH_PREFERENCE_TYPES,
  FETCH_PURCHASE_ORDER_STATUSES,
  FETCH_RATING_TYPE_OPTIONS,
  FETCH_REASONCODES,
  FETCH_REQUISITION_STATUS_CODES,
  FETCH_SESSION_INFO,
  FETCH_SHIPMENT_STATUS_CODES,
  FETCH_SHIPMENT_TYPES,
  FETCH_STOCK_TRANSFER_STATUSES,
  FETCH_SUPPLIERS,
  FETCH_UNIT_OF_MEASURE_CURRENCY,
  FETCH_UNIT_OF_MEASURE_QUANTITY,
  FETCH_USERS,
  START_FETCHING_TRANSLATIONS,
  TRANSLATIONS_FETCHED,
} from 'actions/types';
import {
  ATTRIBUTES,
  CURRENCIES_OPTIONS,
  INTERNAL_LOCATIONS,
  LOCATION_TYPES,
  LOT_NUMBERS_WITH_EXPIRATION_DATE,
  PAYMENT_TERMS_OPTION,
  PREFERENCE_TYPE_OPTIONS,
  RATING_TYPE_OPTIONS,
  SHIPMENT_TYPES,
  UNIT_OF_MEASURE_OPTIONS,
  USERS_OPTIONS,
} from 'api/urls';
import apiClient, { parseResponse } from 'utils/apiClient';

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
  parseResponse: jest.fn(),
}));

const createStore = () => configureStore([thunk])({});

// Several thunks start the request without returning its promise; let it settle.
const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const respond = (data) => Promise.resolve({ data: { data } });

describe('fetch actions', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('fetchReasonCodes', () => {
    it('maps the reason codes to options', async () => {
      apiClient.get.mockReturnValue(respond([{ id: 'DAMAGED', name: 'Damaged', other: 1 }]));
      const store = createStore();

      store.dispatch(fetchReasonCodes());
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith('/api/reasonCodes');
      expect(store.getActions()).toEqual([{
        type: FETCH_REASONCODES,
        payload: [{ id: 'DAMAGED', value: 'DAMAGED', label: 'Damaged' }],
      }]);
    });

    it('filters by activity code and can dispatch under another type', async () => {
      apiClient.get.mockReturnValue(respond([]));
      const store = createStore();

      store.dispatch(fetchReasonCodes('ADJUST_INVENTORY', FETCH_CYCLE_COUNT_REASON_CODES));
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith('/api/reasonCodes?activityCode=ADJUST_INVENTORY');
      expect(store.getActions()).toEqual([{ type: FETCH_CYCLE_COUNT_REASON_CODES, payload: [] }]);
    });
  });

  it('fetchCurrencies passes the response body on', async () => {
    const body = { data: [{ id: 'USD' }] };
    apiClient.get.mockResolvedValue({ data: body });
    const store = createStore();

    store.dispatch(fetchCurrencies());
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith(CURRENCIES_OPTIONS);
    expect(store.getActions()).toEqual([{ type: FETCH_UNIT_OF_MEASURE_CURRENCY, payload: body }]);
  });

  describe('fetchQuantityUnitOfMeasure', () => {
    it('adds the quantity type to the given params without changing the given config', async () => {
      const body = { data: [{ id: 'EA' }] };
      apiClient.get.mockResolvedValue({ data: body });
      const config = { params: { active: true }, signal: 'abort-signal' };
      const store = createStore();

      store.dispatch(fetchQuantityUnitOfMeasure(config));
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith(UNIT_OF_MEASURE_OPTIONS, {
        params: { active: true, type: 'QUANTITY' },
        signal: 'abort-signal',
      });
      expect(config).toEqual({ params: { active: true }, signal: 'abort-signal' });
      expect(store.getActions()).toEqual([{ type: FETCH_UNIT_OF_MEASURE_QUANTITY, payload: body }]);
    });

    it('works without a config', async () => {
      apiClient.get.mockResolvedValue(undefined);
      const store = createStore();

      store.dispatch(fetchQuantityUnitOfMeasure());
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith(UNIT_OF_MEASURE_OPTIONS, { params: { type: 'QUANTITY' } });
      expect(store.getActions())
        .toEqual([{ type: FETCH_UNIT_OF_MEASURE_QUANTITY, payload: undefined }]);
    });
  });

  it('fetchUsers asks for active persons only', async () => {
    const body = { data: [{ id: 'u1' }] };
    apiClient.get.mockResolvedValue({ data: body });
    const store = createStore();

    store.dispatch(fetchUsers());
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith('/api/persons', { params: { status: true } });
    expect(store.getActions()).toEqual([{ type: FETCH_USERS, payload: body }]);
  });

  describe('fetchAvailableApprovers', () => {
    it('asks for active requisition approvers', async () => {
      apiClient.get.mockReturnValue(respond([{ id: 'u1', name: 'Ann' }]));
      const store = createStore();

      await store.dispatch(fetchAvailableApprovers());

      expect(apiClient.get).toHaveBeenCalledWith(USERS_OPTIONS, {
        params: { roleTypes: 'ROLE_REQUISITION_APPROVER', active: true },
      });
      expect(store.getActions()).toEqual([{ type: FETCH_APPROVERS, payload: [{ id: 'u1', name: 'Ann' }] }]);
    });

    it('dispatches nothing when the request fails', async () => {
      apiClient.get.mockRejectedValue(new Error('500'));
      const store = createStore();

      await expect(store.dispatch(fetchAvailableApprovers())).rejects.toThrow('500');
      expect(store.getActions()).toEqual([]);
    });
  });

  describe('fetchSessionInfo', () => {
    it('loads the app context first and returns a thunk that stores it', async () => {
      const response = { data: { data: { user: { id: 'u1' } } } };
      apiClient.get.mockResolvedValue(response);
      const dispatch = jest.fn();

      const thunkAction = await fetchSessionInfo();
      expect(apiClient.get).toHaveBeenCalledWith('/api/getAppContext');
      expect(dispatch).not.toHaveBeenCalled();

      thunkAction(dispatch);
      expect(dispatch).toHaveBeenCalledWith({ type: FETCH_SESSION_INFO, payload: response });
    });

    it('rejects when the app context cannot be loaded', async () => {
      apiClient.get.mockRejectedValue(new Error('401'));

      await expect(fetchSessionInfo()).rejects.toThrow('401');
    });
  });

  it('fetchMenuConfig stores the whole response', async () => {
    const response = { data: { data: { menuConfig: [] } } };
    apiClient.get.mockResolvedValue(response);
    const store = createStore();

    store.dispatch(fetchMenuConfig());
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith('/api/getMenuConfig');
    expect(store.getActions()).toEqual([{ type: FETCH_MENU_CONFIG, payload: response }]);
  });

  describe('changeCurrentLocation', () => {
    it('switches the location on the server, then in the store', async () => {
      apiClient.put.mockResolvedValue({});
      const location = { id: 'loc2', name: 'Depot' };
      const store = createStore();

      await store.dispatch(changeCurrentLocation(location));

      expect(apiClient.put).toHaveBeenCalledWith('/api/chooseLocation/loc2');
      expect(store.getActions()).toEqual([{ type: CHANGE_CURRENT_LOCATION, payload: location }]);
    });

    it('keeps the store unchanged when the server refuses the switch', async () => {
      apiClient.put.mockRejectedValue(new Error('403'));
      const store = createStore();

      await expect(store.dispatch(changeCurrentLocation({ id: 'loc2' }))).rejects.toThrow('403');
      expect(store.getActions()).toEqual([]);
    });
  });

  describe('fetchTranslations', () => {
    it('flags the prefix as loading, then adds the parsed messages and flags it fetched', async () => {
      const raw = { messages: { 'react.default.ok': 'OK' }, currentLocale: 'fr' };
      apiClient.get.mockResolvedValue({ data: raw });
      parseResponse.mockReturnValue({ messages: { react: { default: { ok: 'OK' } } }, currentLocale: 'fr' });
      const store = createStore();

      store.dispatch(fetchTranslations('fr', 'default'));
      expect(store.getActions()).toEqual([{ type: START_FETCHING_TRANSLATIONS, payload: 'default' }]);
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith('/api/localizations?languageCode=fr&prefix=react.default');
      expect(parseResponse).toHaveBeenCalledWith(raw);
      expect(store.getActions()).toEqual([
        { type: START_FETCHING_TRANSLATIONS, payload: 'default' },
        addTranslationForLanguage({ react: { default: { ok: 'OK' } } }, 'fr'),
        { type: TRANSLATIONS_FETCHED, payload: 'default' },
      ]);
    });

    it('sends empty values when no language or prefix is given', async () => {
      apiClient.get.mockResolvedValue({ data: {} });
      parseResponse.mockReturnValue({ messages: {}, currentLocale: 'en' });
      const store = createStore();

      store.dispatch(fetchTranslations(undefined, undefined));
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith('/api/localizations?languageCode=&prefix=react.');
    });
  });

  it('changeCurrentLocale switches the locale on the server and stores the response', async () => {
    const response = { data: { data: { activeLanguage: 'es' } } };
    apiClient.put.mockResolvedValue(response);
    const store = createStore();

    store.dispatch(changeCurrentLocale('es'));
    await flushPromises();

    expect(apiClient.put).toHaveBeenCalledWith('/api/chooseLocale/es');
    expect(store.getActions()).toEqual([{ type: CHANGE_CURRENT_LOCALE, payload: response }]);
  });

  it.each([
    ['fetchPurchaseOrderStatuses', '/api/orderSummaryStatus', fetchPurchaseOrderStatuses, FETCH_PURCHASE_ORDER_STATUSES],
    ['fetchInvoiceStatuses', '/api/invoiceStatuses', fetchInvoiceStatuses, FETCH_INVOICE_STATUSES],
    ['fetchInvoiceTypeCodes', '/api/invoiceTypeCodes', fetchInvoiceTypeCodes, FETCH_INVOICE_TYPE_CODES],
    ['fetchStockTransferStatuses', '/api/stockTransfers/statusOptions', fetchStockTransferStatuses, FETCH_STOCK_TRANSFER_STATUSES],
  ])('%s loads %s and stores the list', async (name, url, creator, type) => {
    apiClient.get.mockReturnValue(respond([{ id: 'S1' }]));
    const store = createStore();

    store.dispatch(creator());
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith(url);
    expect(store.getActions()).toEqual([{ type, payload: [{ id: 'S1' }] }]);
  });

  it.each([
    ['fetchPaymentTerms', () => fetchPaymentTerms(), PAYMENT_TERMS_OPTION, undefined, FETCH_PAYMENT_TERMS],
    ['fetchPreferenceTypes', (c) => fetchPreferenceTypes(c), PREFERENCE_TYPE_OPTIONS, { params: { a: 1 } }, FETCH_PREFERENCE_TYPES],
    ['fetchRatingTypeCodes', (c) => fetchRatingTypeCodes(c), RATING_TYPE_OPTIONS, { params: { b: 2 } }, FETCH_RATING_TYPE_OPTIONS],
    ['fetchAttributes', (c) => fetchAttributes(c), ATTRIBUTES, { params: { c: 3 } }, FETCH_ATTRIBUTES],
    ['fetchLocationTypes', (c) => fetchLocationTypes(c), LOCATION_TYPES, { params: { d: 4 } }, FETCH_LOCATION_TYPES],
  ])('%s loads its options and resolves with the dispatched action', async (name, creator, url, config, type) => {
    apiClient.get.mockReturnValue(respond([{ id: 'o1' }]));
    const store = createStore();

    const result = await store.dispatch(creator(config));

    expect(apiClient.get).toHaveBeenCalledWith(url, config);
    expect(result).toEqual({ type, payload: [{ id: 'o1' }] });
    expect(store.getActions()).toEqual([result]);
  });

  it('fetchPaymentTerms stores undefined when the response has no body', async () => {
    apiClient.get.mockResolvedValue(undefined);
    const store = createStore();

    await store.dispatch(fetchPaymentTerms());

    expect(store.getActions()).toEqual([{ type: FETCH_PAYMENT_TERMS, payload: undefined }]);
  });

  describe.each([
    ['fetchSuppliers', FETCH_SUPPLIERS, 'ROLE_SUPPLIER'],
    ['fetchBuyers', FETCH_BUYERS, 'ROLE_BUYER'],
  ])('%s', (name, type, roleType) => {
    const fetch = (args) => (name === 'fetchSuppliers'
      ? fetchSuppliers(args || {})
      : fetchBuyers(args && args.active));

    it('maps the organizations to options labelled by display name', async () => {
      apiClient.get.mockReturnValue(respond([{
        id: 'o1', name: 'Acme', displayName: 'Acme (ACM)', code: 'ACM',
      }]));
      const store = createStore();

      store.dispatch(fetch({ active: true }));
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith(`/api/organizations?roleType=${roleType}&active=true`);
      expect(store.getActions()).toEqual([{
        type,
        payload: [{
          id: 'o1', value: 'o1', name: 'Acme', label: 'Acme (ACM)',
        }],
      }]);
    });

    it('asks for inactive ones too by default and stores an empty list when there is no data', async () => {
      apiClient.get.mockResolvedValue({ data: {} });
      const store = createStore();

      store.dispatch(fetch());
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith(`/api/organizations?roleType=${roleType}&active=false`);
      expect(store.getActions()).toEqual([{ type, payload: [] }]);
    });
  });

  it('fetchSuppliers adds the sorting only when both sort and order are given', async () => {
    apiClient.get.mockReturnValue(respond([]));
    const store = createStore();

    store.dispatch(fetchSuppliers({ sort: 'name', order: 'desc' }));
    store.dispatch(fetchSuppliers({ sort: 'name' }));
    await flushPromises();

    expect(apiClient.get.mock.calls).toEqual([
      ['/api/organizations?roleType=ROLE_SUPPLIER&active=false&sort=name&order=desc'],
      ['/api/organizations?roleType=ROLE_SUPPLIER&active=false'],
    ]);
  });

  it('fetchBinLocations asks for the bins and internal locations of the facility', async () => {
    apiClient.get.mockReturnValue(respond([{ id: 'bin1' }]));
    const store = createStore();

    await store.dispatch(fetchBinLocations('loc1', ['PICKING'], 'name'));

    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe(INTERNAL_LOCATIONS);
    expect(config.params).toEqual({
      'location.id': 'loc1',
      locationTypeCode: ['BIN_LOCATION', 'INTERNAL'],
      ignoreActivityCodes: ['PICKING'],
      sort: 'name',
    });
    expect(config.paramsSerializer(config.params)).toBe(
      'ignoreActivityCodes=PICKING&location.id=loc1&locationTypeCode=BIN_LOCATION&locationTypeCode=INTERNAL&sort=name',
    );
    expect(store.getActions()).toEqual([{ type: FETCH_BIN_LOCATIONS, payload: [{ id: 'bin1' }] }]);
  });

  describe('fetchShipmentStatusCodes', () => {
    it('sends the excluded statuses as repeated params', async () => {
      apiClient.get.mockReturnValue(respond([{ id: 'PENDING' }]));
      const store = createStore();

      store.dispatch(fetchShipmentStatusCodes({ excludedStatuses: ['RECEIVED', 'PARTIALLY_RECEIVED'] }));
      await flushPromises();

      const [url, config] = apiClient.get.mock.calls[0];
      expect(url).toBe('/api/stockMovements/shipmentStatusCodes');
      expect(config.paramsSerializer(config.params))
        .toBe('excludedStatuses=RECEIVED&excludedStatuses=PARTIALLY_RECEIVED');
      expect(store.getActions()).toEqual([{ type: FETCH_SHIPMENT_STATUS_CODES, payload: [{ id: 'PENDING' }] }]);
    });

    it('excludes nothing by default', async () => {
      apiClient.get.mockReturnValue(respond([]));

      createStore().dispatch(fetchShipmentStatusCodes());
      await flushPromises();

      expect(apiClient.get.mock.calls[0][1].params).toEqual({ excludedStatuses: [] });
    });
  });

  it('fetchRequisitionStatusCodes filters by source type, null by default', async () => {
    apiClient.get.mockReturnValue(respond([{ id: 'PENDING' }]));
    const store = createStore();

    store.dispatch(fetchRequisitionStatusCodes('ELECTRONIC'));
    store.dispatch(fetchRequisitionStatusCodes());
    await flushPromises();

    expect(apiClient.get.mock.calls).toEqual([
      ['/api/stockMovements/requisitionsStatusCodes', { params: { sourceType: 'ELECTRONIC' } }],
      ['/api/stockMovements/requisitionsStatusCodes', { params: { sourceType: null } }],
    ]);
    expect(store.getActions()[0]).toEqual({ type: FETCH_REQUISITION_STATUS_CODES, payload: [{ id: 'PENDING' }] });
  });

  it('fetchShipmentTypes maps the types to options with an enum key', async () => {
    apiClient.get.mockReturnValue(respond([{ id: '1', name: 'Air' }, { id: '2', name: 'Sea' }]));
    const store = createStore();

    await store.dispatch(fetchShipmentTypes());

    expect(apiClient.get).toHaveBeenCalledWith(SHIPMENT_TYPES);
    expect(store.getActions()).toEqual([{
      type: FETCH_SHIPMENT_TYPES,
      payload: [
        {
          id: '1', name: 'Air', value: '1', enumKey: 'AIR',
        },
        {
          id: '2', name: 'Sea', value: '2', enumKey: 'SEA',
        },
      ],
    }]);
  });

  it('fetchLotNumbersByProductIds loads the lots of the given products', async () => {
    const lots = { p1: [{ lotNumber: 'L1' }] };
    apiClient.get.mockReturnValue(respond(lots));
    const store = createStore();

    await store.dispatch(fetchLotNumbersByProductIds(['p1', 'p2']));

    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe(LOT_NUMBERS_WITH_EXPIRATION_DATE);
    expect(config.paramsSerializer(config.params)).toBe('productIds=p1&productIds=p2');
    expect(store.getActions()).toEqual([{ type: FETCH_LOT_NUMBERS_BY_PRODUCT_IDS, payload: lots }]);
  });
});
