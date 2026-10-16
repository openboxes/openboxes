import { renderHook } from '@testing-library/react-hooks';
import moment from 'moment/moment';

import { FULFILLMENT_API, FULFILLMENT_VALIDATION, IMPORT_PACKING_LIST } from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import { DateFormat } from 'consts/timeFormat';
import useOutboundImportForm from 'hooks/outboundImport/useOutboundImportForm';
import useQueryParams from 'hooks/useQueryParams';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('hooks/useQueryParams', () => jest.fn(() => ({})));
const mockShowNotification = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockShowNotification));

const STORAGE_KEY = 'outbound-import';

const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const location = {
  id: 'loc-1',
  name: 'Main Depot',
  locationType: { description: 'Depot' },
};

const formValues = {
  description: 'Shipment to clinic',
  origin: { id: 'loc-1', label: 'Main Depot [Depot]', name: 'Main Depot' },
  destination: { id: 'loc-2', label: 'Clinic', name: 'Clinic' },
  requestedBy: { id: 'user-1', label: 'Jane', name: 'Jane' },
  dateRequested: '2026-03-01T00:00:00',
  dateShipped: '2026-03-02T10:30:00',
  shipmentType: { id: 'st-1' },
  trackingNumber: 'TRK-1',
  expectedDeliveryDate: '2026-03-05T00:00:00',
  packingList: 'file-object',
};

const spinnerTypes = () => mockDispatch.mock.calls.map(([action]) => action.type);

describe('useOutboundImportForm', () => {
  let next;
  const originalLocation = window.location;

  beforeEach(() => {
    jest.clearAllMocks();
    sessionStorage.clear();
    useQueryParams.mockReturnValue({});
    mockState = { session: { currentLocation: location } };
    next = jest.fn();
  });

  afterEach(() => {
    window.location = originalLocation;
  });

  const render = () => renderHook(() => useOutboundImportForm({ next }));

  it('defaults origin to the current location and sets it as the form value', () => {
    const { result } = render();

    expect(result.current.getValues('origin')).toEqual({
      id: 'loc-1',
      name: 'Main Depot',
      label: 'Main Depot [Depot]',
    });
    expect(result.current.getValues('description')).toBeUndefined();
    expect(result.current.lineItems).toEqual([]);
    expect(result.current.lineItemErrors).toEqual({});
    expect(result.current.validateStatus).toBe(200);
  });

  it('clears cached data when the page is not on the confirm step', () => {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ fulfillmentDetails: { description: 'old' } }));

    const { result } = render();

    expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull();
    expect(result.current.getValues('description')).toBeUndefined();
    expect(apiClientCustomResponseHandler.post).not.toHaveBeenCalled();
  });

  it('restores cached values and revalidates them with the new origin on the confirm step', async () => {
    useQueryParams.mockReturnValue({ step: 'CONFIRM' });
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
      fulfillmentDetails: {
        description: 'cached description',
        origin: 'old-origin',
        destination: { id: 'loc-2' },
        requestedBy: { id: 'user-1' },
        dateRequested: '03/01/2026',
      },
      sendingOptions: {
        expectedShippingDate: '03/02/2026 10:30 +00:00',
        shipmentType: { id: 'st-1' },
        trackingNumber: 'TRK-9',
        comments: 'fragile',
        expectedDeliveryDate: '03/05/2026',
      },
      packingList: [{ rowId: '1', origin: 'old-origin', product: 'P1' }],
    }));
    apiClientCustomResponseHandler.post.mockResolvedValue({
      status: 200,
      data: { data: [{ rowId: '1', product: { id: 'p1' } }], errors: {} },
    });

    const { result } = render();
    await flushPromises();

    expect(result.current.getValues('description')).toBe('cached description');
    expect(result.current.getValues('trackingNumber')).toBe('TRK-9');
    expect(result.current.getValues('comments')).toBe('fragile');
    expect(result.current.getValues('dateShipped')).toBe('03/02/2026 10:30 +00:00');
    expect(apiClientCustomResponseHandler.post).toHaveBeenCalledWith(
      FULFILLMENT_VALIDATION,
      expect.objectContaining({
        fulfillmentDetails: expect.objectContaining({ origin: 'loc-1' }),
        packingList: [{ rowId: '1', origin: 'loc-1', product: 'P1' }],
      }),
    );
    expect(result.current.lineItems.itemsInOrder).toEqual([
      { rowId: '1', product: { id: 'p1' }, fileRowNumber: 2 },
    ]);
    expect(next).toHaveBeenCalledTimes(1);
    expect(spinnerTypes()).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('imports the packing list, caches the payload and groups validated rows', async () => {
    apiClient.post.mockResolvedValue({
      data: { data: [{ productCode: 'P1', quantityPicked: 2 }, { productCode: 'P2', quantityPicked: 1 }] },
    });
    apiClientCustomResponseHandler.post.mockImplementation((url, payload) => Promise.resolve({
      status: 200,
      data: { data: payload.packingList, errors: {} },
    }));
    const { result } = render();

    await result.current.onSubmitStockMovementDetails(formValues);

    expect(apiClient.post).toHaveBeenCalledWith(
      IMPORT_PACKING_LIST,
      expect.any(FormData),
      { headers: { 'content-type': 'multipart/form-data' } },
    );
    expect(apiClient.post.mock.calls[0][1].get('importFile')).toBe('file-object');

    const [url, payload] = apiClientCustomResponseHandler.post.mock.calls[0];
    expect(url).toBe(FULFILLMENT_VALIDATION);
    expect(payload.fulfillmentDetails).toEqual({
      description: 'Shipment to clinic',
      origin: formValues.origin,
      destination: formValues.destination,
      requestedBy: formValues.requestedBy,
      dateRequested: '03/01/2026',
    });
    expect(payload.sendingOptions).toEqual({
      shipmentType: { id: 'st-1' },
      trackingNumber: 'TRK-1',
      expectedShippingDate: moment(formValues.dateShipped).format(DateFormat.MM_DD_YYYY_HH_MM_Z),
      expectedDeliveryDate: '03/05/2026',
    });
    expect(payload.packingList).toHaveLength(2);
    expect(payload.packingList[0]).toEqual(expect.objectContaining({
      origin: 'loc-1',
      productCode: 'P1',
      product: 'P1',
      quantityPicked: 2,
      rowId: expect.any(String),
    }));
    expect(payload.packingList[0].rowId).not.toBe(payload.packingList[1].rowId);
    expect(JSON.parse(sessionStorage.getItem(STORAGE_KEY))).toEqual(payload);

    const rowNumbers = result.current.lineItems.itemsWithoutErrors.map((it) => it.fileRowNumber);
    expect(rowNumbers).toEqual([2, 3]);
    expect(result.current.lineItems.itemsWithErrors).toEqual([]);
    expect(next).toHaveBeenCalledTimes(1);
    expect(spinnerTypes()).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('refuses an empty packing list without validating', async () => {
    apiClient.post.mockResolvedValue({ data: { data: [] } });
    const { result } = render();

    await result.current.onSubmitStockMovementDetails(formValues);

    expect(apiClientCustomResponseHandler.post).not.toHaveBeenCalled();
    expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_OUTLINED);
    expect(mockShowNotification).toHaveBeenCalledWith({ message: 'Packing list cannot be empty' });
    expect(next).not.toHaveBeenCalled();
    expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull();
    expect(spinnerTypes()).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('puts rows with validation errors first and reports how many are invalid on a 400', async () => {
    apiClient.post.mockResolvedValue({ data: { data: [{ productCode: 'P1' }] } });
    const rows = [{ rowId: 'a' }, { rowId: 'b' }, { rowId: 'c' }];
    apiClientCustomResponseHandler.post.mockRejectedValue({
      response: {
        status: 400,
        data: { data: rows, errors: { packingList: { b: { product: 'Unknown' } } } },
      },
    });
    const { result } = render();

    await result.current.onSubmitStockMovementDetails(formValues);

    expect(result.current.lineItems.itemsWithErrors).toEqual([{ rowId: 'b', fileRowNumber: 3 }]);
    expect(result.current.lineItems.itemsWithoutErrors).toEqual([
      { rowId: 'a', fileRowNumber: 2 },
      { rowId: 'c', fileRowNumber: 4 },
    ]);
    expect(result.current.lineItems.itemsInOrder.map((it) => it.rowId)).toEqual(['a', 'b', 'c']);
    expect(result.current.lineItemErrors).toEqual({ packingList: { b: { product: 'Unknown' } } });
    expect(result.current.validateStatus).toBe(400);
    expect(mockShowNotification).toHaveBeenCalledWith({
      message: 'Validation exception',
      details: '1 rows in the import are invalid. Review the table below to correct errors in the import.',
    });
    expect(next).toHaveBeenCalledTimes(1);
  });

  it('shows a server error and stays on the step for non-400 validation failures', async () => {
    apiClient.post.mockResolvedValue({ data: { data: [{ productCode: 'P1' }] } });
    apiClientCustomResponseHandler.post.mockRejectedValue({
      response: { status: 500, data: { errorMessage: 'boom' } },
    });
    const { result } = render();

    await result.current.onSubmitStockMovementDetails(formValues);

    expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_FILLED);
    expect(mockShowNotification).toHaveBeenCalledWith({ message: 'Internal server error', details: 'boom' });
    expect(next).not.toHaveBeenCalled();
    expect(result.current.lineItems).toEqual([]);
    expect(spinnerTypes()).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  describe('onConfirmImport', () => {
    const submitPackingList = async (result) => {
      apiClient.post.mockResolvedValue({ data: { data: [{ productCode: 'P1' }] } });
      apiClientCustomResponseHandler.post
        .mockResolvedValueOnce({ status: 200, data: { data: [] } });
      await result.current.onSubmitStockMovementDetails(formValues);
      return apiClientCustomResponseHandler.post.mock.calls[0][1].packingList;
    };

    it('creates the outbound, clears the cache and redirects to the stock movement', async () => {
      delete window.location;
      window.location = {};
      const { result } = render();
      const packingList = await submitPackingList(result);
      apiClientCustomResponseHandler.post.mockResolvedValueOnce({ data: { data: { id: 'sm-9' } } });

      await result.current.onConfirmImport(formValues);

      expect(apiClientCustomResponseHandler.post).toHaveBeenLastCalledWith(
        FULFILLMENT_API,
        expect.objectContaining({
          packingList,
          fulfillmentDetails: expect.objectContaining({ dateRequested: '03/01/2026' }),
        }),
      );
      expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
      expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull();
      expect(window.location).toMatch(/\/stockMovement\/show\/sm-9$/);
    });

    it('populates the table errors when the save is rejected with row errors', async () => {
      const { result } = render();
      await submitPackingList(result);
      apiClientCustomResponseHandler.post.mockRejectedValueOnce({
        response: {
          status: 400,
          data: { data: [{ rowId: 'x' }], errors: { packingList: { x: { quantityPicked: 'Too many' } } } },
        },
      });

      await result.current.onConfirmImport(formValues);

      expect(result.current.lineItems.itemsWithErrors).toEqual([{ rowId: 'x', fileRowNumber: 2 }]);
      expect(result.current.validateStatus).toBe(400);
      expect(mockShowNotification).toHaveBeenLastCalledWith({
        message: 'Validation exception',
        details: 'Check out the table for the validation exceptions',
      });
      expect(spinnerTypes().slice(-1)).toEqual(['HIDE_SPINNER']);
    });

    it('reports the joined error messages when the save fails without row errors', async () => {
      const { result } = render();
      apiClientCustomResponseHandler.post.mockRejectedValueOnce({
        response: { status: 400, data: { errorMessages: ['First', 'Second'] } },
      });

      await result.current.onConfirmImport(formValues);

      expect(mockShowNotification).toHaveBeenCalledWith({ message: 'Bad request', details: 'First, Second' });
      expect(result.current.lineItemErrors).toEqual({});
    });
  });
});
