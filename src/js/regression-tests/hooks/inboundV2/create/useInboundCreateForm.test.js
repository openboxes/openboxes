import { renderHook } from '@testing-library/react-hooks';
import { useController } from 'react-hook-form';

import { updateWorkflowHeader } from 'actions';
import stockListApi from 'api/services/StockListApi';
import stockMovementApi from 'api/services/StockMovementApi';
import useInboundCreateForm from 'hooks/inboundV2/create/useInboundCreateForm';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';

const mockCurrentLocation = {
  id: 'loc-1', name: 'Main', locationType: { description: 'Depot' },
};
jest.mock('selectors', () => ({
  getCurrentLocation: () => mockCurrentLocation,
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
const mockParams = { stockMovementId: undefined };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
  useParams: () => mockParams,
}));
jest.mock('actions', () => ({
  ...jest.requireActual('actions'),
  fetchUsers: jest.fn(() => ({ type: 'FETCH_USERS' })),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('api/services/StockListApi', () => ({ getStockLists: jest.fn() }));
jest.mock('api/services/StockMovementApi', () => ({
  createStockMovement: jest.fn(() => Promise.resolve({ data: { data: { id: 'new-sm' } } })),
  updateStockMovement: jest.fn(() => Promise.resolve({ data: { data: { id: 'sm-1' } } })),
}));
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('utils/option-utils', () => ({
  debounceLocationsFetch: jest.fn(() => 'locations-fetch'),
  debouncePeopleFetch: jest.fn(() => 'people-fetch'),
}));
jest.mock('utils/confirmationModalUtils', () => jest.fn());
jest.mock('utils/createInboundWorkflowHeader', () => jest.fn(() => ['header']));

const flushPromises = async () => {
  for (let i = 0; i < 30; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const supplier = { id: 'sup-1', name: 'Supplier', label: 'Supplier [Supplier]' };
const otherSupplier = { id: 'sup-2', name: 'Other', label: 'Other [Supplier]' };

const savedStockMovement = {
  id: 'sm-1',
  description: 'Restock',
  origin: { id: 'sup-1', name: 'Supplier', locationType: { description: 'Supplier' } },
  destination: { id: 'loc-1', name: 'Main', locationType: { description: 'Depot' } },
  requestedBy: { id: 'u-1', name: 'Jane' },
  stocklist: { id: 'sl-1', name: 'Monthly' },
  dateRequested: '2026-09-30T00:00:00',
  displayStatus: { name: 'Pending' },
};

describe('useInboundCreateForm', () => {
  let next;

  // The origin field is driven the way the page does it, through a controller on the hook's form.
  const renderCreateForm = async () => {
    const hook = renderHook(() => {
      const createForm = useInboundCreateForm({ next });
      const { field: originField } = useController({ name: 'origin', control: createForm.form.control });
      return { ...createForm, originField };
    });
    for (let i = 0; i < 3; i += 1) {
      // eslint-disable-next-line no-await-in-loop
      await flushPromises();
      hook.rerender();
    }
    return hook;
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockParams.stockMovementId = undefined;
    next = jest.fn();
    apiClient.get.mockResolvedValue({ data: { data: savedStockMovement } });
    stockListApi.getStockLists.mockResolvedValue({
      data: { data: [{ id: 'sl-1', name: 'Monthly' }, { id: 'sl-2', name: 'Weekly' }] },
    });
  });

  describe('new stock movement', () => {
    it('loads users, clears the header and exposes the lookup helpers', async () => {
      const { result } = await renderCreateForm();
      expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_USERS' });
      expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader([], null));
      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result.current.data.origin).toBeUndefined();
      expect(result.current.data.debouncedOriginLocationsFetch).toBe('locations-fetch');
      expect(result.current.data.debouncedPeopleFetch).toBe('people-fetch');
      expect(stockListApi.getStockLists).not.toHaveBeenCalled();
    });

    it('fetches stock lists for the chosen origin and current location', async () => {
      const { result } = await renderCreateForm();
      result.current.originField.onChange(supplier);
      await flushPromises();

      expect(stockListApi.getStockLists).toHaveBeenCalledWith({
        params: { origin: 'sup-1', destination: 'loc-1' },
      });
      expect(result.current.data.stockLists).toEqual([
        { id: 'sl-1', name: 'Monthly', label: 'Monthly' },
        { id: 'sl-2', name: 'Weekly', label: 'Weekly' },
      ]);
      expect(result.current.data.origin).toEqual(supplier);
    });

    it('creates the stock movement and moves to the next step', async () => {
      const { result } = await renderCreateForm();
      result.current.originField.onChange(supplier);
      await flushPromises();

      await result.current.actions.onSubmitStockMovementDetails({
        description: 'Restock',
        origin: supplier,
        destination: { id: 'loc-1' },
        requestedBy: { id: 'u-1', name: 'Jane', label: 'Jane' },
        stocklist: null,
        dateRequested: '30/Sep/2026',
      });

      expect(stockMovementApi.createStockMovement).toHaveBeenCalledWith({
        description: 'Restock',
        name: '',
        origin: { id: 'sup-1' },
        destination: { id: 'loc-1' },
        requestedBy: { id: 'u-1' },
        stocklist: null,
        dateRequested: '09/30/2026',
      });
      expect(stockMovementApi.updateStockMovement).not.toHaveBeenCalled();
      expect(next).toHaveBeenCalledWith({ pathId: 'new-sm' });
      expect(confirmationModal).not.toHaveBeenCalled();
      expect(mockSpinner.hide).toHaveBeenCalled();
    });
  });

  describe('existing stock movement', () => {
    beforeEach(() => {
      mockParams.stockMovementId = 'sm-1';
    });

    it('loads the saved values into the form and the header', async () => {
      const { result } = await renderCreateForm();
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm-1');
      expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader(['header'], 'Pending'));
      expect(result.current.data.origin).toEqual(supplier);
      const { _formValues: values } = result.current.form.control;
      expect(values).toMatchObject({
        description: 'Restock',
        destination: { id: 'loc-1', name: 'Main', label: 'Main [Depot]' },
        requestedBy: { id: 'u-1', name: 'Jane', label: 'Jane' },
        stocklist: { id: 'sl-1', name: 'Monthly', label: 'Monthly' },
        dateRequested: '30/Sep/2026',
      });
    });

    it('keeps the stock list when it is still offered, and clears it when not', async () => {
      const { result } = await renderCreateForm();
      expect(result.current.form.control._formValues.stocklist).toMatchObject({ id: 'sl-1' });

      stockListApi.getStockLists.mockResolvedValue({ data: { data: [{ id: 'sl-9', name: 'Other' }] } });
      result.current.originField.onChange(otherSupplier);
      await flushPromises();
      expect(stockListApi.getStockLists).toHaveBeenLastCalledWith({
        params: { origin: 'sup-2', destination: 'loc-1' },
      });
      expect(result.current.form.control._formValues.stocklist).toBeNull();
    });

    it('updates without asking when origin and stock list are unchanged', async () => {
      const { result } = await renderCreateForm();
      await result.current.actions.onSubmitStockMovementDetails({
        ...result.current.form.control._formValues,
      });
      expect(confirmationModal).not.toHaveBeenCalled();
      expect(stockMovementApi.updateStockMovement).toHaveBeenCalledWith('sm-1', expect.objectContaining({
        origin: { id: 'sup-1' },
        stocklist: { id: 'sl-1' },
        dateRequested: '09/30/2026',
      }));
      expect(next).toHaveBeenCalledWith({ pathId: 'sm-1' });
    });

    it('asks before saving a changed origin and saves on Yes', async () => {
      const { result } = await renderCreateForm();
      result.current.originField.onChange(otherSupplier);
      await flushPromises();

      const values = { ...result.current.form.control._formValues };
      await result.current.actions.onSubmitStockMovementDetails(values);
      expect(stockMovementApi.updateStockMovement).not.toHaveBeenCalled();
      expect(confirmationModal).toHaveBeenCalledWith(expect.objectContaining({
        title: { label: 'react.stockMovement.message.confirmChange.label', default: 'Confirm change' },
      }));

      const onClose = jest.fn();
      const [, yes] = confirmationModal.mock.calls[0][0].buttons(onClose);
      yes.onClick();
      await flushPromises();
      expect(onClose).toHaveBeenCalled();
      expect(stockMovementApi.updateStockMovement).toHaveBeenCalledWith('sm-1', expect.objectContaining({
        origin: { id: 'sup-2' },
      }));
      expect(next).toHaveBeenCalledWith({ pathId: 'sm-1' });
    });

    it('restores the saved values when the change is declined', async () => {
      const { result, rerender } = await renderCreateForm();
      result.current.originField.onChange(otherSupplier);
      await flushPromises();

      await result.current.actions.onSubmitStockMovementDetails({
        ...result.current.form.control._formValues,
      });
      const onClose = jest.fn();
      const [no] = confirmationModal.mock.calls[0][0].buttons(onClose);
      no.onClick();
      await flushPromises();
      rerender();

      expect(onClose).toHaveBeenCalled();
      expect(stockMovementApi.updateStockMovement).not.toHaveBeenCalled();
      expect(result.current.data.origin).toEqual(supplier);
    });

    it('redirects to a fresh inbound when loading fails', async () => {
      apiClient.get.mockRejectedValue(new Error('missing'));
      await renderCreateForm();
      expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader([], null));
      expect(mockHistory.push).toHaveBeenCalledWith({
        pathname: '/openboxes/stockMovement/createInbound',
        search: 'direction=INBOUND',
      });
      expect(mockSpinner.hide).toHaveBeenCalled();
    });
  });
});
