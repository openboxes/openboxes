import { act, renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import { UPDATE_CYCLE_COUNT_IDS } from 'actions/types';
import notification from 'components/Layout/notifications/notification';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_RESOLVE_TAB } from 'consts/cycleCount';
import NotificationType from 'consts/notificationTypes';
import useResolveStep from 'hooks/cycleCount/useResolveStep';
import apiClient, { apiClientCustomResponseHandler } from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';
import exportFileFromApi from 'utils/file-download-util';

const mockDispatch = jest.fn();
const mockPush = jest.fn();
const mockShow = jest.fn();
const mockHide = jest.fn();
const mockNotify = jest.fn();

jest.mock('utils/apiClient', () => jest.requireActual('regression-tests/__mocks__/apiClient').default());
jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
  useDispatch: () => mockDispatch,
}));
jest.mock('react-router-dom', () => ({
  useHistory: () => ({ push: mockPush }),
}));
jest.mock('selectors', () => ({
  getUsers: (state) => state.users,
  getCycleCountIds: (state) => state.cycleCountIds,
  getReasonCodes: (state) => state.reasonCodes,
  getCurrentLocation: (state) => state.currentLocation,
  getCurrentUser: (state) => state.currentUser,
  getCurrentLocale: (state) => state.locale,
}));
jest.mock('actions', () => ({
  eraseDraft: (locationId, tab) => ({ type: 'ERASE_DRAFT', payload: { locationId, tab } }),
  fetchBinLocations: (...args) => ({ type: 'FETCH_BIN_LOCATIONS', args }),
  fetchLotNumbersByProductIds: (ids) => ({ type: 'FETCH_LOT_NUMBERS', ids }),
  fetchUsers: () => ({ type: 'FETCH_USERS' }),
}));
jest.mock('hooks/useSpinner', () => () => ({ show: mockShow, hide: mockHide }));
jest.mock('hooks/useTranslate');
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockNotify));
jest.mock('utils/confirmationModalUtils', () => jest.fn());
jest.mock('utils/file-download-util', () => jest.fn(() => Promise.resolve()));

// The hook uses String.prototype.replaceAll, which the Node 14 test runtime lacks
// (browsers have it)
if (!String.prototype.replaceAll) {
  // eslint-disable-next-line no-extend-native
  String.prototype.replaceAll = function replaceAll(search, replacement) {
    return this.split(search).join(replacement);
  };
}

// Resolves every pending promise of the mocked API (they resolve immediately)
const flushPromises = () => new Promise((resolve) => { setTimeout(resolve, 0); });

const PRODUCT = { id: 'prod-1', name: 'Paracetamol', productCode: 'PC1' };
const USER_A = { id: 'user-a', name: 'Alice' };
const USER_B = { id: 'user-b', name: 'Bob' };
const USER_C = { id: 'user-c', name: 'Carol' };
const COUNT_DATE = '2026-09-01T10:00:00Z';
const RECOUNT_DATE = '2026-09-02T10:00:00Z';

const item = (id, overrides = {}) => ({
  id,
  product: PRODUCT,
  countIndex: 1,
  custom: false,
  binLocation: { id: 'bin-1', name: 'Bin 1' },
  inventoryItem: { lotNumber: 'LOT-A', expirationDate: null },
  quantityOnHand: 5,
  quantityCounted: 5,
  assignee: USER_B,
  dateCounted: RECOUNT_DATE,
  comment: '',
  ...overrides,
});

// Cycle count with: a count+recount pair, two custom rows added on the recount
// and a count-only row whose inventory was removed in the meantime.
// The API returns the items already in display order, which the hook keeps.
const buildCycleCount = () => ({
  id: 'cc-1',
  requestId: 'req-1',
  cycleCountItems: [
    item('item-count', {
      countIndex: 0, quantityCounted: 4, comment: 'shelf was messy', assignee: USER_A, dateCounted: COUNT_DATE,
    }),
    item('item-recount', { quantityOnHand: 6, quantityCounted: 6, discrepancyReasonCode: 'DAMAGED' }),
    item('item-custom-early', {
      custom: true,
      dateCreated: '2026-09-02T09:00:00Z',
      binLocation: { id: 'bin-2', name: 'Bin 2' },
      inventoryItem: { lotNumber: 'LOT-C', expirationDate: null },
      quantityOnHand: 2,
      quantityCounted: 2,
    }),
    item('item-custom-late', {
      custom: true,
      dateCreated: '2026-09-02T12:00:00Z',
      binLocation: { id: 'bin-2', name: 'Bin 2' },
      inventoryItem: { lotNumber: 'LOT-B', expirationDate: null },
      quantityOnHand: 3,
      quantityCounted: 3,
    }),
    item('item-gone', {
      countIndex: 0, assignee: USER_A, inventoryItem: { lotNumber: 'LOT-D', expirationDate: null },
    }),
  ],
});

// Cycle count without any recount item: its table is empty on the resolve step
const buildEmptyCycleCount = () => ({
  id: 'cc-2',
  requestId: 'req-2',
  verificationCount: { assignee: USER_C },
  cycleCountItems: [
    item('item-only-count', {
      countIndex: 0,
      assignee: USER_A,
      product: { id: 'prod-2', name: 'Ibuprofen', productCode: 'PC2' },
      dateCounted: COUNT_DATE,
    }),
  ],
});

describe('useResolveStep', () => {
  let state;
  let serverCycleCounts;

  const serverItem = (id) => serverCycleCounts[0].cycleCountItems.find((row) => row.id === id);

  const getRequests = () => apiClient.get.mock.calls.map(([url]) => url);

  const renderUseResolveStep = async () => {
    const hook = renderHook(() => useResolveStep());
    await flushPromises();
    // tableData lives in a ref, so a re-render exposes the fetched data
    hook.rerender();
    return hook;
  };

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      users: [USER_A],
      cycleCountIds: ['cc-1'],
      reasonCodes: [{ id: 'DAMAGED', label: 'Damaged', value: 'DAMAGED' }],
      currentLocation: { id: 'loc-1', supportedActivities: ['PUTAWAY_STOCK', 'PICK_STOCK'] },
      currentUser: { id: 'current-user' },
      locale: 'en',
    };
    serverCycleCounts = [buildCycleCount()];
    useSelector.mockImplementation((selector) => selector(state));
    apiClient.get.mockImplementation(() => Promise.resolve({
      data: { data: serverCycleCounts.map((cc) => ({ ...cc })) },
    }));
    apiClient.patch.mockResolvedValue({});
    apiClient.post.mockResolvedValue({});
    apiClient.delete.mockResolvedValue({});
    apiClientCustomResponseHandler.post.mockResolvedValue({});
  });

  describe('loading', () => {
    it('fetches the cycle counts and merges count and recount items', async () => {
      const { result } = await renderUseResolveStep();

      expect(getRequests()).toEqual(['/api/facilities/loc-1/cycle-counts?id=cc-1&sortBy=false']);
      const [cycleCount] = result.current.tableData;
      expect(cycleCount.cycleCountItems.map((row) => row.id))
        .toEqual(['item-recount', 'item-custom-early', 'item-custom-late']);
      expect(cycleCount.cycleCountItems[0]).toEqual(expect.objectContaining({
        quantityOnHand: 6,
        quantityCounted: 4,
        quantityRecounted: 6,
        commentFromCount: 'shelf was messy',
        dateCounted: COUNT_DATE,
        dateRecounted: RECOUNT_DATE,
        countedBy: USER_A,
        recountedBy: USER_B,
        rootCause: { id: 'DAMAGED', label: 'Damaged', value: 'DAMAGED' },
      }));
      // Rows added on the recount have no counting data
      expect(cycleCount.cycleCountItems[1]).toEqual(expect.objectContaining({
        quantityRecounted: 2,
        quantityCounted: null,
        dateCounted: null,
        countedBy: null,
        commentFromCount: null,
        recountedBy: USER_B,
        rootCause: null,
      }));
    });

    it('keeps cycle counts without recount items empty and remembers their count items', async () => {
      state.cycleCountIds = ['cc-1', 'cc-2'];
      serverCycleCounts = [buildCycleCount(), buildEmptyCycleCount()];

      const { result } = await renderUseResolveStep();

      expect(result.current.tableData[1].cycleCountItems).toEqual([]);
      // cc-1 keeps its count-only row whose inventory is gone, cc-2 its only count row
      expect(result.current.cycleCountsWithItemsWithoutRecount
        .map((cc) => cc.cycleCountItems.map((row) => row.id)))
        .toEqual([['item-gone'], ['item-only-count']]);
      expect(result.current.getRecountedBy('cc-2')).toEqual(USER_C);
      expect(result.current.getCountedBy('cc-2')).toEqual(USER_A);
      expect(result.current.getProduct('cc-2')).toEqual({ id: 'prod-2', name: 'Ibuprofen', productCode: 'PC2' });
      expect(result.current.getDateCounted('cc-2')).toBe(COUNT_DATE);
    });

    it('exposes the recount header data of a cycle count', async () => {
      const { result } = await renderUseResolveStep();

      expect(result.current.getRecountedBy('cc-1')).toEqual(USER_B);
      expect(result.current.getDefaultRecountedBy('cc-1')).toEqual(USER_B);
      expect(result.current.getCountedBy('cc-1')).toEqual(USER_A);
      expect(result.current.getRecountedDate('cc-1')).toBe(RECOUNT_DATE);
      expect(result.current.getProduct('cc-1')).toEqual(PRODUCT);
      expect(result.current.getDateCounted('cc-1')).toBe(COUNT_DATE);
    });

    it('fetches bin locations, lot numbers and users when needed', async () => {
      state.users = [];

      await renderUseResolveStep();

      expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_USERS' });
      expect(mockDispatch).toHaveBeenCalledWith({
        type: 'FETCH_BIN_LOCATIONS', args: ['loc-1', [], 'sortOrder,locationType,name'],
      });
      expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_LOT_NUMBERS', ids: ['prod-1'] });
    });

    it('skips bin locations and users when not needed', async () => {
      state.currentLocation = { id: 'loc-1', supportedActivities: [] };

      await renderUseResolveStep();

      const types = mockDispatch.mock.calls.map(([action]) => action.type);
      expect(types).not.toContain('FETCH_USERS');
      expect(types).not.toContain('FETCH_BIN_LOCATIONS');
    });

    it('does not fetch anything without cycle count ids', async () => {
      state.cycleCountIds = [];

      const { result } = await renderUseResolveStep();

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(result.current.tableData).toEqual([]);
    });

    it('refetches sorted by product name when the sorting is switched on', async () => {
      const { result } = await renderUseResolveStep();

      act(() => result.current.setSortByProductName(true));
      await flushPromises();

      expect(getRequests()[1]).toBe('/api/facilities/loc-1/cycle-counts?id=cc-1&sortBy=productName');
      expect(result.current.sortByProductName).toBe(true);
    });
  });

  describe('editing rows', () => {
    it('updateData changes the value and marks the row as updated only on a change', async () => {
      const { result, rerender } = await renderUseResolveStep();

      result.current.tableMeta.updateData('cc-1', 'item-recount', 'quantityRecounted', 6);
      rerender();
      expect(result.current.tableData[0].cycleCountItems[0].updated).toBeUndefined();

      result.current.tableMeta.updateData('cc-1', 'item-recount', 'inventoryItem_lotNumber', 'LOT-Z');
      rerender();
      expect(result.current.tableData[0].cycleCountItems[0].inventoryItem.lotNumber).toBe('LOT-Z');
      expect(result.current.tableData[0].cycleCountItems[0].updated).toBe(true);
    });

    it('addEmptyRow appends an empty row to the right table', async () => {
      const { result } = await renderUseResolveStep();

      act(() => result.current.addEmptyRow('prod-1', 'cc-1'));

      const rows = result.current.tableData[0].cycleCountItems;
      expect(rows).toHaveLength(4);
      expect(rows[3]).toEqual(expect.objectContaining({
        id: expect.stringMatching(/^newRow/),
        product: { id: 'prod-1' },
        inventoryItem: { lotNumber: null, expirationDate: null },
        quantityRecounted: null,
        comment: '',
      }));
    });

    it('removeRow deletes persisted rows on the server and drops new rows locally', async () => {
      const { result, rerender } = await renderUseResolveStep();
      act(() => result.current.addEmptyRow('prod-1', 'cc-1'));
      const newRowId = result.current.tableData[0].cycleCountItems[3].id;

      result.current.removeRow('cc-1', newRowId);
      expect(apiClient.delete).not.toHaveBeenCalled();

      result.current.removeRow('cc-1', 'item-custom-late');
      rerender();

      expect(apiClient.delete).toHaveBeenCalledWith('/api/facilities/loc-1/cycle-counts/items/item-custom-late');
      expect(result.current.tableData[0].cycleCountItems.map((row) => row.id))
        .toEqual(['item-recount', 'item-custom-early']);
      expect(mockHide).toHaveBeenCalled();
    });
  });

  describe('save', () => {
    it('updates edited and unassigned rows and creates new rows', async () => {
      serverItem('item-custom-early').assignee = null;
      const { result } = await renderUseResolveStep();
      act(() => result.current.addEmptyRow('prod-1', 'cc-1'));
      const newRowId = result.current.tableData[0].cycleCountItems[3].id;
      result.current.tableMeta.updateData('cc-1', newRowId, 'quantityRecounted', 9);
      result.current.tableMeta.updateData('cc-1', newRowId, 'inventoryItem_lotNumber', ' LOT-NEW ');
      result.current.tableMeta.updateData('cc-1', newRowId, 'inventoryItem_expirationDate', 'Jan 15, 2027');
      result.current.tableMeta.updateData('cc-1', 'item-recount', 'comment', 'checked twice');

      await result.current.save({});

      expect(apiClient.patch).toHaveBeenCalledTimes(1);
      const [patchUrl, patchBody] = apiClient.patch.mock.calls[0];
      expect(patchUrl).toBe('/api/facilities/loc-1/cycle-counts/cc-1/items/batch');
      expect(patchBody.itemsToUpdate.map((row) => row.id)).toEqual(['item-recount', 'item-custom-early']);
      expect(patchBody.itemsToUpdate[0]).toEqual(expect.objectContaining({
        quantityCounted: 6,
        countIndex: 1,
        comment: 'checked twice',
        dateCounted: RECOUNT_DATE,
        discrepancyReasonCode: 'DAMAGED',
        assignee: 'user-b',
        recount: true,
        inventoryItem: { lotNumber: 'LOT-A', expirationDate: null, product: 'prod-1' },
      }));

      const [postUrl, postBody] = apiClient.post.mock.calls[0];
      expect(postUrl).toBe('/api/facilities/loc-1/cycle-counts/cc-1/items/batch');
      expect(postBody.itemsToCreate).toEqual([expect.objectContaining({
        id: newRowId,
        quantityCounted: 9,
        inventoryItem: { lotNumber: 'LOT-NEW', expirationDate: '01/15/2027', product: 'prod-1' },
      })]);
      // Existence check + refetch after the save
      expect(getRequests()).toHaveLength(3);
    });

    it('sends the date and person picked in the header for every row of the cycle count', async () => {
      const { result } = await renderUseResolveStep();
      result.current.assignRecountedBy('cc-1')(USER_C);
      result.current.updateRecountedDate('cc-1')({ format: () => '2026-09-03T08:00:00Z' });

      await result.current.save({ shouldRefetch: false });

      const { itemsToUpdate } = apiClient.patch.mock.calls[0][1];
      expect(itemsToUpdate).toHaveLength(3);
      itemsToUpdate.forEach((row) => {
        expect(row.assignee).toBe('user-c');
        expect(row.dateCounted).toBe('2026-09-03T08:00:00Z');
      });
      expect(result.current.getDefaultRecountedBy('cc-1')).toEqual(USER_C);
    });

    it('falls back to the current user as assignee when requested', async () => {
      serverItem('item-custom-early').assignee = null;
      const { result } = await renderUseResolveStep();
      result.current.assignRecountedBy('cc-1')(null);

      await result.current.save({ shouldSetDefaultAssignee: true, shouldRefetch: false });

      expect(apiClient.patch.mock.calls[0][1].itemsToUpdate.map((row) => row.assignee))
        .toEqual(['current-user', 'current-user', 'current-user']);
    });

    it('clearing the recount date falls back to the current time', async () => {
      const { result } = await renderUseResolveStep();
      jest.spyOn(Date, 'now').mockReturnValue(1790000000000);

      result.current.updateRecountedDate('cc-1')(null);

      expect(result.current.getRecountedDate('cc-1')).toBe(1790000000000);
      Date.now.mockRestore();
    });

    it('refetches lot numbers after a save when asked to', async () => {
      const { result } = await renderUseResolveStep();
      mockDispatch.mockClear();

      await result.current.save({ shouldRefetchLotNumbers: true });

      expect(mockDispatch).toHaveBeenCalledWith({ type: 'FETCH_LOT_NUMBERS', ids: ['prod-1'] });
    });

    it('stops and reports when a cycle count was canceled in the meantime', async () => {
      state.cycleCountIds = ['cc-1', 'cc-2'];
      serverCycleCounts = [buildCycleCount(), buildEmptyCycleCount()];
      const { result } = await renderUseResolveStep();
      serverCycleCounts = [buildCycleCount()];
      result.current.tableMeta.updateData('cc-1', 'item-recount', 'comment', 'changed');

      await result.current.save({ shouldRefetch: false });

      expect(apiClient.patch).not.toHaveBeenCalled();
      expect(mockDispatch).toHaveBeenCalledWith({
        type: UPDATE_CYCLE_COUNT_IDS,
        payload: { locationId: 'loc-1', cycleCounts: ['cc-1'] },
      });
      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_FILLED);
      expect(mockNotify).toHaveBeenCalledWith(expect.objectContaining({ message: 'Error' }));
    });
  });

  describe('next', () => {
    it('does not save when the validation fails', async () => {
      const { result } = await renderUseResolveStep();
      result.current.tableMeta.updateData('cc-1', 'item-recount', 'quantityRecounted', null);

      await result.current.next();

      expect(apiClient.patch).not.toHaveBeenCalled();
      expect(result.current.isStepEditable).toBe(true);
    });

    it('warns once about missing root causes, then saves and locks the step', async () => {
      // A recount difference without a root cause
      serverItem('item-custom-early').quantityCounted = 1;
      serverItem('item-custom-early').assignee = null;
      const { result } = await renderUseResolveStep();

      await result.current.next();
      expect(mockNotify).toHaveBeenCalledWith({
        message: 'Are you sure you want to continue with empty root cause? Click next if you want to continue.',
      });
      expect(apiClient.patch).not.toHaveBeenCalled();

      await result.current.next();
      await flushPromises();
      expect(mockNotify).toHaveBeenCalledTimes(1);
      expect(apiClient.patch).toHaveBeenCalledTimes(1);
      expect(result.current.isStepEditable).toBe(false);
    });

    it('saves with the default assignee and locks the step when everything is valid', async () => {
      serverItem('item-custom-early').assignee = null;
      const { result } = await renderUseResolveStep();

      await result.current.next();
      await flushPromises();

      expect(apiClient.patch.mock.calls[0][1].itemsToUpdate.map((row) => row.id)).toEqual(['item-custom-early']);
      expect(result.current.isStepEditable).toBe(false);

      act(() => result.current.back());
      expect(result.current.isStepEditable).toBe(true);
    });

    it('stops when a cycle count was canceled in the meantime', async () => {
      state.cycleCountIds = ['cc-1', 'cc-2'];
      serverCycleCounts = [buildCycleCount(), buildEmptyCycleCount()];
      const { result } = await renderUseResolveStep();
      serverCycleCounts = [buildCycleCount()];

      await result.current.next();

      expect(confirmationModal).not.toHaveBeenCalled();
      expect(mockNotify).toHaveBeenCalledWith(expect.objectContaining({ message: 'Error' }));
      expect(result.current.isStepEditable).toBe(true);
    });

    it('asks to cancel cycle counts without any recount row', async () => {
      state.cycleCountIds = ['cc-1', 'cc-2'];
      serverCycleCounts = [buildCycleCount(), buildEmptyCycleCount()];
      const { result } = await renderUseResolveStep();

      await result.current.next();

      expect(confirmationModal).toHaveBeenCalledTimes(1);
      const modal = confirmationModal.mock.calls[0][0];
      expect(modal.title).toEqual({
        label: 'react.cycleCount.modal.zeroRecountItems.title.label',
        default: 'Cancel Counts?',
      });
      expect(modal.content.data).toEqual({ productCodes: ['prod-2'] });
      expect(apiClient.patch).not.toHaveBeenCalled();

      const onClose = jest.fn();
      const [notNow, cancelProducts] = modal.buttons(onClose);
      notNow.onClick();
      expect(onClose).toHaveBeenCalledTimes(1);

      serverCycleCounts = [buildCycleCount()];
      await cancelProducts.onClick();

      expect(apiClient.delete).toHaveBeenCalledWith('/api/facilities/loc-1/cycle-counts/requests/batch?id=req-2');
      expect(mockDispatch).toHaveBeenCalledWith({
        type: UPDATE_CYCLE_COUNT_IDS,
        payload: { locationId: 'loc-1', cycleCounts: ['cc-1'] },
      });
      expect(mockPush).not.toHaveBeenCalled();
    });

    it('goes back to the list when every cycle count was canceled', async () => {
      state.cycleCountIds = ['cc-2'];
      serverCycleCounts = [buildEmptyCycleCount()];
      const { result } = await renderUseResolveStep();

      await result.current.next();
      serverCycleCounts = [];
      const [, cancelProducts] = confirmationModal.mock.calls[0][0].buttons(jest.fn());
      await cancelProducts.onClick();

      expect(mockPush).toHaveBeenCalledWith(CYCLE_COUNT.list(TO_RESOLVE_TAB));
    });
  });

  describe('submitRecount', () => {
    it('submits every cycle count, erases the draft and goes back to the list', async () => {
      const { result } = await renderUseResolveStep();

      await result.current.submitRecount();

      const [url, payload] = apiClientCustomResponseHandler.post.mock.calls[0];
      expect(url).toBe('/api/facilities/loc-1/cycle-counts/cc-1/recount');
      expect(payload).toEqual(expect.objectContaining({
        refreshQuantityOnHand: true,
        failOnOutdatedQuantity: true,
        requireRecountOnDiscrepancy: false,
      }));
      expect(payload.cycleCountItems[0]).toEqual({
        assignee: USER_B,
        binLocation: { id: 'bin-1', name: 'Bin 1' },
        comment: '',
        countIndex: 1,
        dateCounted: RECOUNT_DATE,
        discrepancyReasonCode: 'DAMAGED',
        facility: 'loc-1',
        id: 'item-recount',
        inventoryItem: { lotNumber: 'LOT-A', expirationDate: null },
        product: PRODUCT,
        quantityCounted: 6,
      });
      expect(mockDispatch).toHaveBeenCalledWith({
        type: 'ERASE_DRAFT', payload: { locationId: 'loc-1', tab: TO_RESOLVE_TAB },
      });
      expect(mockPush).toHaveBeenCalledWith(CYCLE_COUNT.list(TO_RESOLVE_TAB));
      expect(mockHide).toHaveBeenCalled();
    });

    it('asks to review products whose inventory changed and refreshes them', async () => {
      apiClientCustomResponseHandler.post.mockRejectedValue(new Error('outdated'));
      const { result } = await renderUseResolveStep();

      await result.current.submitRecount();

      expect(mockPush).not.toHaveBeenCalled();
      expect(mockDispatch).toHaveBeenCalledWith({
        type: UPDATE_CYCLE_COUNT_IDS,
        payload: { locationId: 'loc-1', cycleCounts: ['cc-1'] },
      });
      const modal = confirmationModal.mock.calls[0][0];
      expect(modal.title.data).toEqual({ outdatedProductsCount: 1 });
      expect(modal.hideCloseButton).toBe(true);

      const onClose = jest.fn();
      const [review] = modal.buttons(onClose);
      await review.onClick();

      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/facilities/loc-1/cycle-counts/cc-1/refresh?removeOutOfStockItemsImplicitly=true',
        {},
        { params: { countIndex: 1 } },
      );
      expect(onClose).toHaveBeenCalled();
      expect(result.current.isStepEditable).toBe(true);
    });

    it('does not submit when a cycle count was canceled', async () => {
      state.cycleCountIds = ['cc-1', 'cc-2'];
      serverCycleCounts = [buildCycleCount(), buildEmptyCycleCount()];
      const { result } = await renderUseResolveStep();
      serverCycleCounts = [buildCycleCount()];

      await result.current.submitRecount();

      expect(apiClientCustomResponseHandler.post).not.toHaveBeenCalled();
      expect(mockHide).toHaveBeenCalled();
    });
  });

  describe('refreshCountItems', () => {
    it('saves, refreshes the given cycle counts and refetches them', async () => {
      const { result } = await renderUseResolveStep();
      result.current.tableMeta.updateData('cc-1', 'item-recount', 'comment', 'changed');

      await result.current.refreshCountItems(['cc-1']);

      expect(apiClient.patch).toHaveBeenCalledTimes(1);
      expect(apiClient.post).toHaveBeenCalledWith(
        '/api/facilities/loc-1/cycle-counts/cc-1/refresh?removeOutOfStockItemsImplicitly=true',
        {},
        { params: { countIndex: 1 } },
      );
      expect(getRequests()).toHaveLength(4);
    });

    it('stops when a cycle count was canceled', async () => {
      state.cycleCountIds = ['cc-1', 'cc-2'];
      serverCycleCounts = [buildCycleCount(), buildEmptyCycleCount()];
      const { result } = await renderUseResolveStep();
      serverCycleCounts = [buildCycleCount()];

      await result.current.refreshCountItems();

      expect(apiClient.post).not.toHaveBeenCalled();
    });
  });

  it('printRecountForm downloads the recount sheet in the requested format', async () => {
    const { result } = await renderUseResolveStep();

    await result.current.printRecountForm('pdf');

    expect(exportFileFromApi).toHaveBeenCalledWith({
      url: '/api/facilities/loc-1/cycle-counts',
      params: { id: ['cc-1'], sortBy: false },
      format: 'pdf',
    });
    expect(mockShow).toHaveBeenCalled();
    expect(mockHide).toHaveBeenCalled();
  });
});
