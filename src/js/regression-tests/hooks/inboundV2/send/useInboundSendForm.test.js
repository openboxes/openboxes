import { renderHook } from '@testing-library/react-hooks';

import { updateWorkflowHeader } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import RequisitionStatus from 'consts/requisitionStatus';
import useInboundSendForm from 'hooks/inboundV2/send/useInboundSendForm';
import useFileActions from 'hooks/useFileActions';
import confirmationModal from 'utils/confirmationModalUtils';

const mockState = {
  currentLocation: {
    id: 'loc-1', name: 'Main', locationType: { description: 'Depot' },
  },
  locale: 'en',
  shipmentTypes: [{ id: 'st-0', name: 'Default' }, { id: 'st-1', name: 'Air' }],
};
jest.mock('selectors', () => ({
  getCurrentLocation: () => mockState.currentLocation,
  getCurrentLocale: () => mockState.locale,
  getShipmentTypes: () => mockState.shipmentTypes,
  getDebounceTime: () => 500,
  getMinSearchLength: () => 3,
}));
const mockDispatch = jest.fn();
jest.mock('react-redux', () => ({
  useSelector: (selector) => selector(),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
const mockHistory = { push: jest.fn() };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
  useParams: () => ({ stockMovementId: 'sm-1' }),
}));
jest.mock('actions', () => ({
  ...jest.requireActual('actions'),
  fetchShipmentTypes: jest.fn(() => ({ type: 'FETCH_SHIPMENT_TYPES' })),
}));
jest.mock('api/services/StockMovementApi', () => ({
  getStockMovementById: jest.fn(),
  updateShipment: jest.fn(() => Promise.resolve({})),
  updateStatus: jest.fn(() => Promise.resolve({})),
  uploadDocuments: jest.fn(),
}));
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('hooks/useUserHasPermissions', () => jest.fn(() => true));
const mockFileActions = {
  files: [],
  handleDownloadFiles: jest.fn(),
  handleRemoveFile: jest.fn(),
  sendFiles: jest.fn(() => Promise.resolve()),
  handleExportFile: jest.fn(),
};
jest.mock('hooks/useFileActions', () => jest.fn(() => mockFileActions));
jest.mock('utils/option-utils', () => ({
  debounceLocationsFetch: jest.fn(() => 'debounced-fetch'),
}));
jest.mock('utils/confirmationModalUtils', () => jest.fn());
const mockNotify = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockNotify));
jest.mock('utils/createInboundWorkflowHeader', () => jest.fn(() => ['header']));

const flushPromises = async () => {
  for (let i = 0; i < 30; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const stockMovement = (overrides = {}) => ({
  origin: { id: 'o-1', name: 'Supplier A', locationType: { description: 'Supplier' } },
  destination: { id: 'loc-1', name: 'Main', locationType: { description: 'Depot' } },
  dateShipped: '2026-10-01T10:00:00',
  expectedDeliveryDate: '2026-10-05T00:00:00',
  shipmentType: { id: 'st-1', name: 'Air', displayName: 'By air' },
  trackingNumber: 'TRK',
  driverName: null,
  comments: 'note',
  statusCode: RequisitionStatus.CHECKING,
  hasManageInventory: false,
  shipped: false,
  displayStatus: { name: 'Checking' },
  associations: { documents: [{ id: 'doc-5', stepNumber: 5 }, { id: 'doc-1', stepNumber: 1 }] },
  ...overrides,
});

describe('useInboundSendForm', () => {
  let previous;

  const renderSendForm = async (data = stockMovement()) => {
    stockMovementApi.getStockMovementById.mockResolvedValue({ data: { data } });
    const hook = renderHook(() => useInboundSendForm({ previous }));
    // reset() and the resolver run asynchronously; rerender to read the settled form state.
    for (let i = 0; i < 4; i += 1) {
      // eslint-disable-next-line no-await-in-loop
      await flushPromises();
      hook.rerender();
    }
    return hook;
  };

  const lastShipmentPayload = () => stockMovementApi.updateShipment.mock.calls.slice(-1)[0];

  const originalLocation = window.location;

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
    jest.setSystemTime(new Date(2026, 9, 2, 12, 0, 0));
    mockFileActions.files = [];
    mockState.currentLocation = { id: 'loc-1', name: 'Main', locationType: { description: 'Depot' } };
    previous = jest.fn();
    delete window.location;
    window.location = {};
  });

  afterEach(() => {
    window.location = originalLocation;
    jest.useRealTimers();
  });

  it('loads the stock movement into the form and the workflow header', async () => {
    const { result } = await renderSendForm();

    expect(stockMovementApi.getStockMovementById).toHaveBeenCalledWith('sm-1', { stepNumber: 6 });
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_SHIPMENT_TYPES' });
    expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader(['header'], 'Checking'));
    expect(result.current.data.statusCode).toBe(RequisitionStatus.CHECKING);
    expect(result.current.data.shipped).toBe(false);
    expect(result.current.data.documents).toEqual([{ id: 'doc-5', stepNumber: 5 }]);
    expect(result.current.data.shipmentTypesWithoutDefaultValue).toEqual([{ id: 'st-1', name: 'Air' }]);
    expect(result.current.data.debouncedDestinationLocationsFetch).toBe('debounced-fetch');
    expect(result.current.permissions.hasRoleAdmin).toBe(true);
    expect(result.current.form.isValid).toBe(true);
    expect(useFileActions).toHaveBeenCalledWith(expect.objectContaining({
      entityId: 'sm-1',
      uploadDocuments: stockMovementApi.uploadDocuments,
    }));
  });

  it('builds the shipment payload from the form values when saving', async () => {
    const { result } = await renderSendForm();
    await result.current.actions.onSave({});

    const [id, payload] = lastShipmentPayload();
    expect(id).toBe('sm-1');
    expect(payload).toEqual({
      destination: { id: 'loc-1' },
      dateShipped: expect.stringMatching(/^10\/01\/2026 10:00 (Z|[+-]\d{2}:\d{2})$/),
      shipmentType: 'st-1',
      trackingNumber: 'TRK',
      driverName: '',
      comments: 'note',
      expectedDeliveryDate: '10/05/2026',
    });
    expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
    expect(mockNotify).toHaveBeenCalledWith({ message: 'Changes saved successfully' });
    expect(stockMovementApi.getStockMovementById).toHaveBeenCalledTimes(1);
  });

  it('refetches a dispatched shipment after saving and can skip the notification', async () => {
    const { result } = await renderSendForm(
      stockMovement({ statusCode: RequisitionStatus.DISPATCHED }),
    );
    await result.current.actions.onSave({ showSuccessNotification: false });
    expect(stockMovementApi.getStockMovementById).toHaveBeenCalledTimes(2);
    expect(mockNotify).not.toHaveBeenCalled();
  });

  it('does not save an invalid form', async () => {
    const { result } = await renderSendForm(stockMovement({ shipmentType: { id: 'st-0', name: 'Default' } }));
    expect(result.current.form.isValid).toBe(false);
    await result.current.actions.onSave({});
    expect(stockMovementApi.updateShipment).not.toHaveBeenCalled();
  });

  it('redirects to the create inbound flow when loading fails', async () => {
    stockMovementApi.getStockMovementById.mockRejectedValue(new Error('gone'));
    renderHook(() => useInboundSendForm({ previous }));
    await flushPromises();
    expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader([], null));
    expect(mockHistory.push).toHaveBeenCalledWith({
      pathname: '/openboxes/stockMovement/createInbound',
      search: 'direction=INBOUND',
    });
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  describe('sendShipment', () => {
    it('uploads pending files, saves, dispatches and opens the shipment', async () => {
      mockFileActions.files = [{ name: 'packing.pdf' }];
      const { result } = await renderSendForm();
      await result.current.actions.sendShipment({ origin: { type: 'SUPPLIER' }, hasManageInventory: true });

      expect(mockFileActions.sendFiles).toHaveBeenCalled();
      expect(stockMovementApi.updateShipment).toHaveBeenCalledWith('sm-1', expect.any(Object));
      expect(stockMovementApi.updateStatus).toHaveBeenCalledWith('sm-1', { status: RequisitionStatus.DISPATCHED });
      expect(window.location).toBe('/openboxes/stockMovement/show/sm-1');
    });

    it('skips the upload when there are no files', async () => {
      const { result } = await renderSendForm();
      await result.current.actions.sendShipment({ origin: { type: 'DEPOT' }, hasManageInventory: false });
      expect(mockFileActions.sendFiles).not.toHaveBeenCalled();
      expect(stockMovementApi.updateStatus).toHaveBeenCalled();
    });

    it('refuses to send from a managed-inventory location other than a supplier origin', async () => {
      const { result } = await renderSendForm();
      await result.current.actions.sendShipment({ origin: { type: 'DEPOT' }, hasManageInventory: true });
      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_FILLED);
      expect(mockNotify).toHaveBeenCalledWith(expect.objectContaining({ message: 'Error' }));
      expect(stockMovementApi.updateShipment).not.toHaveBeenCalled();
      expect(stockMovementApi.updateStatus).not.toHaveBeenCalled();
      expect(mockSpinner.hide).toHaveBeenCalled();
    });
  });

  describe('rollbackStockMovement', () => {
    it('rolls back when the current location is the destination of a non-managed shipment', async () => {
      const { result } = await renderSendForm();
      await result.current.actions.rollbackStockMovement();
      expect(stockMovementApi.updateStatus).toHaveBeenCalledWith('sm-1', { rollback: true });
      expect(stockMovementApi.getStockMovementById).toHaveBeenCalledTimes(2);
    });

    it('rolls back from the origin of a managed-inventory shipment', async () => {
      mockState.currentLocation = { id: 'o-1', name: 'Supplier A' };
      const { result } = await renderSendForm(stockMovement({ hasManageInventory: true }));
      await result.current.actions.rollbackStockMovement();
      expect(stockMovementApi.updateStatus).toHaveBeenCalledWith('sm-1', { rollback: true });
    });

    it('refuses to roll back from an unrelated location', async () => {
      const { result } = await renderSendForm(stockMovement({ hasManageInventory: true }));
      await result.current.actions.rollbackStockMovement();
      expect(stockMovementApi.updateStatus).not.toHaveBeenCalled();
      expect(mockNotify).toHaveBeenCalledWith(expect.objectContaining({
        details: 'You are not able to rollback shipment from your location.',
      }));
    });
  });

  describe('navigation', () => {
    it('saves and goes back when the form is valid', async () => {
      const { result } = await renderSendForm();
      await result.current.actions.previousPage();
      expect(stockMovementApi.updateShipment).toHaveBeenCalled();
      expect(previous).toHaveBeenCalled();
    });

    it('asks to correct errors or continue when going back with an invalid form', async () => {
      const { result } = await renderSendForm(stockMovement({ shipmentType: null }));
      await result.current.actions.previousPage();
      expect(previous).not.toHaveBeenCalled();
      const { buttons, title } = confirmationModal.mock.calls[0][0];
      expect(title).toEqual({ label: 'react.stockMovement.confirmPreviousPage.label', default: 'Validation error' });
      const onClose = jest.fn();
      const [correct, cont] = buttons(onClose);
      correct.onClick();
      expect(previous).not.toHaveBeenCalled();
      await cont.onClick();
      expect(previous).toHaveBeenCalled();
      expect(onClose).toHaveBeenCalledTimes(2);
      expect(stockMovementApi.updateShipment).not.toHaveBeenCalled();
    });

    it('saves and exits to the shipment page', async () => {
      const { result } = await renderSendForm();
      await result.current.actions.saveAndExit();
      expect(stockMovementApi.updateShipment).toHaveBeenCalled();
      expect(window.location).toBe('/openboxes/stockMovement/show/sm-1');
    });

    it('confirms before exiting with validation errors', async () => {
      const { result } = await renderSendForm(stockMovement({ shipmentType: null }));
      await result.current.actions.saveAndExit();
      expect(window.location).toEqual({});
      const { buttons, content } = confirmationModal.mock.calls[0][0];
      expect(content.label).toBe('react.stockMovement.confirmExit.message');
      const onClose = jest.fn();
      const [no, yes] = buttons(onClose);
      no.onClick();
      expect(window.location).toEqual({});
      yes.onClick();
      expect(window.location).toBe('/openboxes/stockMovement/show/sm-1');
      expect(stockMovementApi.updateShipment).not.toHaveBeenCalled();
    });
  });
});
