import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  fetchCycleCounts,
  removeRow,
  startCount,
  startResolution,
} from 'actions';
import {
  FETCH_CYCLE_COUNTS,
  HIDE_SPINNER,
  REMOVE_ROW,
  SHOW_SPINNER,
  START_COUNT,
  START_RESOLUTION,
} from 'actions/types';
import {
  CYCLE_COUNT,
  CYCLE_COUNT_ITEM,
  CYCLE_COUNT_RECOUNT_START,
  CYCLE_COUNT_START,
} from 'api/urls';
import apiClient from 'utils/apiClient';

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
  apiClientCustomResponseHandler: { post: jest.fn() },
  parseResponse: jest.fn(),
}));

const createStore = () => configureStore([thunk])({});
const SHOW = { type: SHOW_SPINNER, payload: true };
const HIDE = { type: HIDE_SPINNER, payload: false };

const cycleCount = (id) => ({
  id,
  cycleCountItems: [{ id: `${id}-i1`, binLocation: { id: 'bin1', name: 'A-1' }, quantityCounted: 2 }],
});

describe('cycle count actions', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('startCount', () => {
    it('starts the requested counts and keeps the ids of the created cycle counts', async () => {
      apiClient.post.mockResolvedValue({ data: { data: [{ id: 'cc1' }, { id: 'cc2' }] } });
      const payload = { requests: [{ cycleCountRequest: 'r1', countIndex: 0 }] };
      const store = createStore();

      await store.dispatch(startCount(payload, 'loc1'));

      expect(apiClient.post).toHaveBeenCalledWith(CYCLE_COUNT_START('loc1', null), payload, {});
      expect(store.getActions()).toEqual([{
        type: START_COUNT,
        payload: { locationId: 'loc1', requests: ['cc1', 'cc2'] },
      }]);
    });

    it('stores no ids when the response has no list', async () => {
      apiClient.post.mockResolvedValue({ data: {} });
      const store = createStore();

      await store.dispatch(startCount({}, 'loc1'));

      expect(store.getActions()).toEqual([{
        type: START_COUNT,
        payload: { locationId: 'loc1', requests: undefined },
      }]);
    });

    it('dispatches nothing when the server refuses', async () => {
      apiClient.post.mockRejectedValue(new Error('400'));
      const store = createStore();

      await expect(store.dispatch(startCount({}, 'loc1'))).rejects.toThrow('400');
      expect(store.getActions()).toEqual([]);
    });
  });

  it('startResolution starts the recount of each request as count 1', async () => {
    apiClient.post.mockResolvedValue({ data: { data: [{ id: 'cc7' }] } });
    const store = createStore();

    await store.dispatch(startResolution(['r1', 'r2'], 'loc1'));

    expect(apiClient.post).toHaveBeenCalledWith(
      CYCLE_COUNT_RECOUNT_START('loc1', null),
      {
        requests: [
          { cycleCountRequest: 'r1', countIndex: 1 },
          { cycleCountRequest: 'r2', countIndex: 1 },
        ],
      },
      {},
    );
    expect(store.getActions()).toEqual([{
      type: START_RESOLUTION,
      payload: { locationId: 'loc1', cycleCounts: ['cc7'] },
    }]);
  });

  describe('fetchCycleCounts', () => {
    it('loads the cycle counts sorted by product name inside a spinner', async () => {
      const counts = [cycleCount('cc1')];
      apiClient.get.mockResolvedValue({ data: { data: counts } });
      const store = createStore();

      await store.dispatch(fetchCycleCounts(['cc1', 'cc2'], 'loc1', true));

      expect(apiClient.get).toHaveBeenCalledWith(`${CYCLE_COUNT('loc1')}?id=cc1&id=cc2&sortBy=productName`);
      expect(store.getActions()).toEqual([
        SHOW,
        { type: FETCH_CYCLE_COUNTS, payload: counts },
        HIDE,
      ]);
    });

    it('drops the bin locations when they should not be shown', async () => {
      apiClient.get.mockResolvedValue({ data: { data: [cycleCount('cc1')] } });
      const store = createStore();

      await store.dispatch(fetchCycleCounts(['cc1'], 'loc1', undefined, false));

      expect(apiClient.get).toHaveBeenCalledWith(`${CYCLE_COUNT('loc1')}?id=cc1`);
      expect(store.getActions()[1]).toEqual({
        type: FETCH_CYCLE_COUNTS,
        payload: [{
          id: 'cc1',
          cycleCountItems: [{ id: 'cc1-i1', binLocation: null, quantityCounted: 2 }],
        }],
      });
    });

    it('hides the spinner and rejects when loading fails', async () => {
      apiClient.get.mockRejectedValue(new Error('500'));
      const store = createStore();

      await expect(store.dispatch(fetchCycleCounts(['cc1'], 'loc1', true))).rejects.toThrow('500');
      expect(store.getActions()).toEqual([SHOW, HIDE]);
    });
  });

  describe('removeRow', () => {
    it('deletes a saved row on the server before removing it', async () => {
      apiClient.delete.mockResolvedValue({});
      const store = createStore();

      await store.dispatch(removeRow('cc1', 'item-1', 'loc1'));

      expect(apiClient.delete).toHaveBeenCalledWith(CYCLE_COUNT_ITEM('loc1', 'item-1'));
      expect(store.getActions()).toEqual([
        SHOW,
        { type: REMOVE_ROW, payload: { id: 'cc1', rowId: 'item-1' } },
        HIDE,
      ]);
    });

    it('removes a row that was never saved without calling the server', async () => {
      const store = createStore();

      await store.dispatch(removeRow('cc1', 'newRow12', 'loc1'));

      expect(apiClient.delete).not.toHaveBeenCalled();
      expect(store.getActions()).toEqual([
        SHOW,
        { type: REMOVE_ROW, payload: { id: 'cc1', rowId: 'newRow12' } },
        HIDE,
      ]);
    });

    it('keeps the row and hides the spinner when the delete fails', async () => {
      apiClient.delete.mockRejectedValue(new Error('409'));
      const store = createStore();

      await expect(store.dispatch(removeRow('cc1', 'item-1', 'loc1'))).rejects.toThrow('409');
      expect(store.getActions()).toEqual([SHOW, HIDE]);
    });
  });
});
