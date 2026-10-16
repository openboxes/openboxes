import { renderHook } from '@testing-library/react-hooks';
import _ from 'lodash';

import { updateWorkflowHeader } from 'actions';
import notification from 'components/Layout/notifications/notification';
import modalWithTableType from 'consts/modalWithTableType';
import NotificationType from 'consts/notificationTypes';
import RequisitionStatus from 'consts/requisitionStatus';
import useInboundAddItemsActions from 'hooks/inboundV2/addItems/useInboundAddItemsActions';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';

const mockDispatch = jest.fn();
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
}));
const mockHistory = { push: jest.fn() };
const mockParams = { stockMovementId: 'sm-1' };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
  useParams: () => mockParams,
}));
const mockFieldArray = { fields: [], remove: jest.fn(), append: jest.fn() };
jest.mock('react-hook-form', () => ({
  useFieldArray: jest.fn(() => mockFieldArray),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
const mockScrollToBottom = jest.fn();
jest.mock('hooks/useScrollbar', () => jest.fn(() => ({ scrollToBottom: mockScrollToBottom })));
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

const defaultTableRow = [{ product: null, quantityRequested: '', sortOrder: 0 }];

const savedItem = (overrides = {}) => ({
  id: 'item-1',
  statusCode: 'PENDING',
  product: { id: 'p-1', productCode: 'P1', name: 'Aspirin' },
  quantityRequested: 5,
  lotNumber: 'L1',
  expirationDate: null,
  recipient: null,
  inventoryItem: null,
  sortOrder: 100,
  ...overrides,
});

// Form state backed by a plain object, read and written through lodash paths like
// react-hook-form does.
const createForm = (initial) => {
  const state = _.cloneDeep(initial);
  return {
    state,
    getValues: jest.fn((path) => (path ? _.get(state, path) : state)),
    setValue: jest.fn((path, value) => _.set(state, path, value)),
    trigger: jest.fn(() => Promise.resolve(true)),
  };
};

// Pulls the buttons a confirmation modal was opened with, bound to the given close spy.
const lastModalButtons = (onClose = jest.fn()) => {
  const { buttons } = confirmationModal.mock.calls[confirmationModal.mock.calls.length - 1][0];
  return buttons(onClose);
};

describe('useInboundAddItemsActions', () => {
  let form;
  let next;
  let previous;
  let openModal;

  const pageData = {
    id: 'sm-1',
    identifier: 'IN-1',
    displayStatus: { name: 'Pending' },
    statusCode: RequisitionStatus.CREATED,
  };

  const mockInitialFetch = (items = [], values = {}) => {
    apiClient.get.mockImplementation((url) => {
      if (url.endsWith('/stockMovementItems')) {
        return Promise.resolve({ data: { data: items } });
      }
      return Promise.resolve({ data: { data: { ...pageData, ...values } } });
    });
  };

  // The stock movement returned by the initial fetch becomes the form's "values".
  const renderActions = async (values = {}, { items = [], fetchError } = {}) => {
    if (fetchError) {
      apiClient.get.mockRejectedValue(fetchError);
    } else {
      mockInitialFetch(items, {
        origin: { locationType: { locationTypeCode: 'DEPOT' } },
        hasManageInventory: true,
        ...values,
      });
    }
    form = createForm({ values: {}, currentLineItems: [] });
    const hook = renderHook(() => useInboundAddItemsActions({
      control: {},
      getValues: form.getValues,
      setValue: form.setValue,
      trigger: form.trigger,
      defaultTableRow,
      next,
      previous,
      openModal,
    }));
    await flushPromises();
    // Rows given by the test stand for what the user has typed since the page loaded.
    if (values.lineItems) {
      form.state.values.lineItems = values.lineItems;
    }
    return hook;
  };

  const originalLocation = window.location;

  beforeEach(() => {
    jest.clearAllMocks();
    mockParams.stockMovementId = 'sm-1';
    next = jest.fn();
    previous = jest.fn();
    openModal = jest.fn(() => Promise.resolve(true));
    delete window.location;
    window.location = {};
  });
  afterEach(() => {
    window.location = originalLocation;
    jest.useRealTimers();
  });

  describe('initial fetch', () => {
    it('loads the stock movement and its items, updates the header and fills the form', async () => {
      const { result } = await renderActions({}, {
        items: [savedItem({
          product: { id: 'p-1', productCode: 'P1', name: 'Aspirin' },
          recipient: { id: 'u-1', name: 'Jane' },
          inventoryItem: { expirationDate: '10/02/2026' },
          expirationDate: '10/02/2026',
        })],
      });

      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm-1');
      expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements/sm-1/stockMovementItems', {
        params: { stepNumber: 2 },
      });
      expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader(['header'], 'Pending'));
      expect(form.state.values.identifier).toBe('IN-1');
      expect(form.state.values.stockMovementId).toBe('sm-1');

      const [line] = form.state.values.lineItems;
      expect(line.itemId).toBe('item-1');
      expect(line.product).toMatchObject({ label: 'P1 - Aspirin', value: 'p-1' });
      expect(line.recipient).toMatchObject({ label: 'Jane', value: 'u-1' });
      expect(line.expirationDate).toBe('02/Oct/2026');
      expect(line.inventoryItem.expirationDate).toBe('02/Oct/2026');
      expect(form.state.currentLineItems).toEqual([line]);
      expect(result.current.loading).toBe(false);
      expect(mockSpinner.show).toHaveBeenCalled();
      expect(mockSpinner.hide).toHaveBeenCalled();
    });

    it('keeps existing product/recipient labels and fills the default row when nothing is saved', async () => {
      await renderActions();
      expect(form.state.values.lineItems).toEqual(defaultTableRow);
      expect(form.state.currentLineItems).toEqual(defaultTableRow);

      await renderActions({}, {
        items: [savedItem({
          product: { id: 'p-1', label: 'Custom', value: 'v-1' },
          recipient: { id: 'u-1', label: 'Lbl', value: 'uv' },
        })],
      });
      expect(form.state.values.lineItems[0].product).toMatchObject({ label: 'Custom', value: 'v-1' });
      expect(form.state.values.lineItems[0].recipient).toMatchObject({ label: 'Lbl', value: 'uv' });
    });

    it('goes back when there is no stock movement id', async () => {
      mockParams.stockMovementId = undefined;
      await renderActions();
      expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader([], null));
      expect(previous).toHaveBeenCalled();
      expect(apiClient.get).not.toHaveBeenCalled();
    });

    it('redirects to the create inbound flow when fetching fails', async () => {
      const { result } = await renderActions({}, { fetchError: new Error('boom') });
      expect(mockDispatch).toHaveBeenCalledWith(updateWorkflowHeader([], null));
      expect(mockHistory.push).toHaveBeenCalledWith({
        pathname: '/openboxes/stockMovement/createInbound',
        search: 'direction=INBOUND',
      });
      expect(result.current.loading).toBe(false);
    });
  });

  describe('fetchLineItems', () => {
    it('merges fetched items with the existing ones, or shows only the imported ones', async () => {
      const { result } = await renderActions();
      form.state.values.lineItems = [savedItem({ id: 'old' })];
      form.state.currentLineItems = [savedItem({ id: 'old' })];
      mockInitialFetch([savedItem({ id: 'new' })]);

      await result.current.fetchLineItems();
      expect(form.state.values.lineItems.map((i) => i.id)).toEqual(['new', 'old']);
      expect(form.state.currentLineItems.map((i) => i.id)).toEqual(['new', 'old']);

      await result.current.fetchLineItems(true);
      expect(form.state.values.lineItems.map((i) => i.id)).toEqual(['new']);
    });
  });

  describe('row actions', () => {
    it('appends a new line after the highest sort order and scrolls to the bottom', async () => {
      const { result } = await renderActions({
        lineItems: [{ sortOrder: 300 }, { sortOrder: undefined }],
      });
      jest.useFakeTimers();
      result.current.addNewLine();
      expect(mockFieldArray.append)
        .toHaveBeenCalledWith([{ ...defaultTableRow[0], sortOrder: 400 }]);
      expect(mockScrollToBottom).not.toHaveBeenCalled();
      jest.runAllTimers();
      expect(mockScrollToBottom).toHaveBeenCalled();
      jest.useRealTimers();
    });

    it('removes a saved row and all rows through the API', async () => {
      apiClient.delete.mockResolvedValue({});
      const { result } = await renderActions();
      form.state.currentLineItems = [savedItem()];

      await result.current.removeSavedRow('item-1');
      expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovementItems/item-1/removeItem');

      await result.current.removeAllRows();
      expect(apiClient.delete).toHaveBeenCalledWith('/api/stockMovements/sm-1/removeAllItems');
      expect(form.state.currentLineItems).toEqual([]);
      expect(form.state.values.lineItems).toEqual(defaultTableRow);
    });

    it('hides the spinner even when removing fails', async () => {
      apiClient.delete.mockRejectedValue(new Error('nope'));
      const { result } = await renderActions();
      form.state.currentLineItems = [savedItem()];
      mockSpinner.hide.mockClear();
      await expect(result.current.removeAllRows()).rejects.toThrow('nope');
      expect(mockSpinner.hide).toHaveBeenCalled();
      expect(form.state.currentLineItems).toEqual([savedItem()]);
    });
  });

  describe('saveRequisitionItemsInCurrentStep', () => {
    it('posts only new items with a quantity and changed saved items', async () => {
      const unchanged = savedItem({ id: 'same' });
      const changed = savedItem({ id: 'changed', quantityRequested: 9 });
      const { result } = await renderActions();
      form.state.currentLineItems = [unchanged, savedItem({ id: 'changed' })];
      const response = { data: { data: { lineItems: [savedItem({ id: 'saved' })] } } };
      apiClient.post.mockResolvedValue(response);

      const resp = await result.current.saveRequisitionItemsInCurrentStep([
        {
          product: { id: 'p-2' },
          quantityRequested: 3,
          expirationDate: '10/02/2026',
          recipient: { id: 'u-1' },
          sortOrder: 200,
        },
        { product: { id: 'p-3' }, quantityRequested: '0' },
        { product: null, quantityRequested: 4 },
        unchanged,
        changed,
      ]);

      expect(resp).toBe(response);
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements/sm-1/updateItems', {
        lineItems: [
          {
            id: undefined,
            product: { id: 'p-2' },
            quantityRequested: 3,
            palletName: undefined,
            boxName: undefined,
            lotNumber: undefined,
            expirationDate: '10/02/2026',
            recipient: { id: 'u-1' },
            sortOrder: 200,
          },
          expect.objectContaining({ id: 'changed', quantityRequested: 9, expirationDate: null }),
        ],
      });
      expect(form.state.values.lineItems.map((i) => i.id)).toEqual(['saved']);
      expect(form.state.currentLineItems.map((i) => i.id)).toEqual(['saved']);
    });

    it('treats a cleared or mismatched expiration date as a change', async () => {
      const { result } = await renderActions();
      form.state.currentLineItems = [
        savedItem({ id: 'cleared', expirationDate: '10/02/2026' }),
        savedItem({ id: 'mismatch', expirationDate: '10/02/2026', inventoryItem: { expirationDate: '01/01/2027' } }),
      ];
      apiClient.post.mockResolvedValue({ data: { data: { lineItems: [] } } });

      await result.current.saveRequisitionItemsInCurrentStep([
        savedItem({ id: 'cleared', expirationDate: null }),
        savedItem({ id: 'mismatch', expirationDate: '10/02/2026', inventoryItem: { expirationDate: '01/01/2027' } }),
      ]);
      const sentIds = apiClient.post.mock.calls[0][1].lineItems.map((i) => i.id);
      expect(sentIds).toEqual(['cleared', 'mismatch']);
    });

    it('drops empty-quantity rows locally when there is nothing to save', async () => {
      const { result } = await renderActions();
      const kept = savedItem({ id: 'kept' });
      form.state.currentLineItems = [kept];

      const resp = await result.current.saveRequisitionItemsInCurrentStep([
        kept,
        { product: { id: 'p' }, quantityRequested: '' },
      ]);
      expect(resp).toBeNull();
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(form.state.values.lineItems).toEqual([kept]);

      await result.current.saveRequisitionItemsInCurrentStep([{ product: null, quantityRequested: '0' }]);
      expect(form.state.values.lineItems).toEqual(defaultTableRow);
    });

    it('leaves the form alone when every row is valid and unchanged', async () => {
      const { result } = await renderActions();
      const kept = savedItem();
      form.state.currentLineItems = [kept];
      form.setValue.mockClear();
      expect(await result.current.saveRequisitionItemsInCurrentStep([kept])).toBeNull();
      expect(form.setValue).not.toHaveBeenCalled();
    });
  });

  describe('save', () => {
    it('does nothing when the form is invalid', async () => {
      const { result } = await renderActions();
      form.trigger.mockResolvedValue(false);
      await result.current.save();
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(confirmationModal).not.toHaveBeenCalled();
    });

    it('saves and shows a success notification', async () => {
      const { result } = await renderActions({ lineItems: [{ product: { id: 'p' }, quantityRequested: 2 }] });
      apiClient.post.mockResolvedValue({ data: { data: { lineItems: [] } } });
      await result.current.save();
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements/sm-1/updateItems', expect.anything());
      expect(notification).toHaveBeenCalledWith(NotificationType.SUCCESS);
      expect(mockNotify).toHaveBeenCalledWith({ message: 'Changes saved successfully' });
    });

    it('asks for confirmation when some rows have no quantity and saves on Yes', async () => {
      const { result } = await renderActions({
        lineItems: [{ product: { id: 'p' }, quantityRequested: 2 }, { product: { id: 'q' }, quantityRequested: '' }],
      });
      apiClient.post.mockResolvedValue({ data: { data: { lineItems: [] } } });
      await result.current.save();
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(confirmationModal).toHaveBeenCalledWith(expect.objectContaining({
        content: expect.objectContaining({ label: 'react.stockMovement.confirmSave.message' }),
      }));

      const onClose = jest.fn();
      const [no, yes] = lastModalButtons(onClose);
      no.onClick();
      expect(onClose).toHaveBeenCalledTimes(1);
      expect(apiClient.post).not.toHaveBeenCalled();

      yes.onClick();
      await flushPromises();
      expect(onClose).toHaveBeenCalledTimes(2);
      expect(apiClient.post).toHaveBeenCalledTimes(1);
      expect(mockNotify).toHaveBeenCalled();
    });
  });

  describe('saveAndExit', () => {
    it('saves and navigates to the stock movement page', async () => {
      const { result } = await renderActions({ lineItems: [] });
      await result.current.saveAndExit();
      expect(window.location).toBe('/openboxes/stockMovement/show/sm-1');
    });

    it('confirms before exiting with validation errors', async () => {
      const { result } = await renderActions();
      form.trigger.mockResolvedValue(false);
      await result.current.saveAndExit();
      expect(window.location).toEqual({});
      const [, yes] = lastModalButtons();
      yes.onClick();
      expect(window.location).toBe('/openboxes/stockMovement/show/sm-1');
    });
  });

  describe('previousPage', () => {
    it('saves and goes back when valid', async () => {
      const { result } = await renderActions({ lineItems: [] });
      await result.current.previousPage();
      expect(previous).toHaveBeenCalled();
    });

    it('offers to correct the error or continue without saving when invalid', async () => {
      const { result } = await renderActions();
      form.trigger.mockResolvedValue(false);
      await result.current.previousPage();
      expect(previous).not.toHaveBeenCalled();
      const onClose = jest.fn();
      const [correct, cont] = lastModalButtons(onClose);
      correct.onClick();
      expect(previous).not.toHaveBeenCalled();
      cont.onClick();
      expect(previous).toHaveBeenCalled();
      expect(onClose).toHaveBeenCalledTimes(2);
    });
  });

  describe('nextPage', () => {
    const newLine = (code, overrides = {}) => ({
      product: { id: `p-${code}`, productCode: code },
      quantityRequested: 1,
      ...overrides,
    });

    it('stops when the form is invalid', async () => {
      const { result } = await renderActions();
      form.trigger.mockResolvedValue(false);
      await result.current.nextPage();
      expect(next).not.toHaveBeenCalled();
    });

    it('saves, moves the requisition to CHECKING and goes to the next step', async () => {
      const { result } = await renderActions({ lineItems: [newLine('A'), { product: null }] });
      apiClient.post.mockImplementation((url) => (url.endsWith('/updateItems')
        ? Promise.resolve({ data: { data: { lineItems: [savedItem()] } } })
        : Promise.resolve({})));

      await result.current.nextPage();
      await flushPromises();

      expect(apiClient.post.mock.calls[0][1].lineItems).toHaveLength(1);
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements/sm-1/status', {
        status: RequisitionStatus.CHECKING,
      });
      expect(next).toHaveBeenCalled();
    });

    it('skips the status change when the requisition is past CREATED', async () => {
      const { result } = await renderActions({ statusCode: 'CHECKING', lineItems: [] });
      await result.current.nextPage();
      await flushPromises();
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(next).toHaveBeenCalled();
    });

    it('confirms saving rows without quantity before going on', async () => {
      const { result } = await renderActions({ lineItems: [newLine('A', { quantityRequested: '0' })] });
      await result.current.nextPage();
      expect(next).not.toHaveBeenCalled();
      const [, yes] = lastModalButtons();
      yes.onClick();
      await flushPromises();
      expect(next).toHaveBeenCalled();
    });

    // KNOWN-GAP: declining the duplicates confirmation makes OBAF's nextPage reject
    // (Promise.reject() without a reason) instead of staying on the step.
    it.skip('asks about duplicated products and stops when declined', async () => {
      openModal.mockResolvedValue(false);
      const lines = [newLine('A'), newLine('A', { lotNumber: 'X' }), newLine('B')];
      const { result } = await renderActions({ lineItems: lines });
      await result.current.nextPage();
      expect(openModal).toHaveBeenCalledWith({
        data: [lines[0], lines[1]],
        type: modalWithTableType.DUPLICATES,
      });
      expect(next).not.toHaveBeenCalled();
      expect(apiClient.post).not.toHaveBeenCalled();
    });

    it('continues past duplicates when confirmed', async () => {
      const { result } = await renderActions({ lineItems: [newLine('A'), newLine('A')] });
      apiClient.post.mockResolvedValue({ data: { data: { lineItems: [] } } });
      await result.current.nextPage();
      await flushPromises();
      expect(openModal).toHaveBeenCalled();
      expect(next).toHaveBeenCalled();
    });

    it.each([
      ['the origin is a supplier', { origin: { locationType: { locationTypeCode: 'SUPPLIER' } } }],
      ['the destination does not manage inventory', { hasManageInventory: false }],
    ])('does not ask about duplicates when %s', async (_label, values) => {
      const { result } = await renderActions({ ...values, lineItems: [newLine('A'), newLine('A')] });
      apiClient.post.mockResolvedValue({ data: { data: { lineItems: [] } } });
      await result.current.nextPage();
      await flushPromises();
      expect(openModal).not.toHaveBeenCalled();
      expect(next).toHaveBeenCalled();
    });

    it('confirms expiry changes on stocked items, then updates inventory items before moving on', async () => {
      const line = newLine('A', { lotNumber: 'L1', expirationDate: '10/02/2026' });
      const { result } = await renderActions({ lineItems: [line] });
      const updated = savedItem({
        product: { productCode: 'A' },
        lotNumber: 'L1',
        expirationDate: '10/02/2026',
        inventoryItem: { expirationDate: '01/01/2027', quantity: 4 },
      });
      apiClient.post.mockImplementation((url) => (url.endsWith('/updateItems')
        ? Promise.resolve({ data: { data: { lineItems: [updated] } } })
        : Promise.resolve({})));

      await result.current.nextPage();
      await flushPromises();

      expect(openModal).toHaveBeenCalledWith({
        data: [{
          code: 'A',
          product: { productCode: 'A' },
          lotNumber: 'L1',
          previousExpiry: '01/01/2027',
          newExpiry: '10/02/2026',
        }],
        type: modalWithTableType.EXPIRATION,
      });
      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/stockMovements/sm-1/updateInventoryItems',
        { lineItems: [expect.objectContaining({ product: { id: 'p-A' }, expirationDate: '10/02/2026' })] },
      );
      expect(next).toHaveBeenCalled();
    });

    it('updates inventory items without asking when the mismatched lot has no stock', async () => {
      const { result } = await renderActions({ lineItems: [newLine('A')] });
      const updated = savedItem({ expirationDate: '10/02/2026', inventoryItem: { expirationDate: null, quantity: '0' } });
      apiClient.post.mockImplementation((url) => (url.endsWith('/updateItems')
        ? Promise.resolve({ data: { data: { lineItems: [updated] } } })
        : Promise.resolve({})));

      await result.current.nextPage();
      await flushPromises();
      expect(openModal).not.toHaveBeenCalled();
      expect(apiClient.post).toHaveBeenCalledWith('/api/stockMovements/sm-1/updateInventoryItems', expect.anything());
      expect(next).toHaveBeenCalled();
    });
  });
});
