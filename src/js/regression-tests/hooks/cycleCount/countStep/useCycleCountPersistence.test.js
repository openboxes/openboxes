import { act, renderHook } from '@testing-library/react-hooks';

import { UPDATE_CYCLE_COUNT_IDS } from 'actions/types';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useCycleCountPersistence from 'hooks/cycleCount/countStep/useCycleCountPersistence';
import apiClient from 'utils/apiClient';
import exportFileFromApi from 'utils/file-download-util';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useStore: () => ({ getState: () => mockState }),
}));
jest.mock('actions', () => ({
  fetchCycleCounts: jest.fn((...args) => ({ type: 'FETCH_CYCLE_COUNTS', args })),
  markAllItemsAsNotUpdated: jest.fn((ids) => ({ type: 'MARK_ALL_AS_NOT_UPDATED', ids })),
}));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('hooks/useTranslate', () => jest.fn(() => (key, defaultMessage) => defaultMessage));
const mockNotify = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockNotify));
jest.mock('utils/file-download-util', () => jest.fn(() => Promise.resolve()));

const LOCATION_ID = 'loc-1';
const CYCLE_COUNT_IDS = ['cc-1', 'cc-2'];
const USER_ID = 'user-1';

const item = (id, overrides = {}) => ({
  id,
  product: { id: 'prod-1' },
  quantityCounted: 5,
  assignee: 'someone',
  updated: false,
  inventoryItem: { lotNumber: ' LOT-1 ', expirationDate: 'Jan 05, 2027' },
  ...overrides,
});

const renderPersistence = ({ sortByProductName = false } = {}) => renderHook(() =>
  useCycleCountPersistence(LOCATION_ID, CYCLE_COUNT_IDS, sortByProductName, USER_ID, 'en'));

describe('useCycleCountPersistence', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      countWorkflow: {
        entities: {},
        countedBy: {},
        dateCounted: {},
      },
    };
    apiClient.patch.mockResolvedValue({ data: {} });
    apiClient.post.mockResolvedValue({ data: {} });
  });

  describe('save', () => {
    it('patches updated and unassigned items and posts new rows with the mapped payload', async () => {
      mockState.countWorkflow.entities = {
        'cc-1': {
          id: 'cc-1',
          cycleCountItems: [
            item('item-updated', { updated: true }),
            item('item-unassigned', { assignee: null }),
            item('item-untouched'),
            item('newRow-1', { assignee: null }),
          ],
        },
      };
      mockState.countWorkflow.countedBy = { 'cc-1': { id: 'counter-1' } };
      mockState.countWorkflow.dateCounted = { 'cc-1': '2026-10-01' };

      const { result } = renderPersistence();
      await result.current.save();

      expect(mockDispatch).toHaveBeenNthCalledWith(1, { type: 'MARK_ALL_AS_NOT_UPDATED', ids: CYCLE_COUNT_IDS });
      expect(apiClient.patch).toHaveBeenCalledTimes(1);
      const [patchUrl, patchBody] = apiClient.patch.mock.calls[0];
      expect(patchUrl).toBe('/api/facilities/loc-1/cycle-counts/items/batch');
      expect(patchBody.itemsToUpdate.map(({ id }) => id)).toEqual(['item-updated', 'item-unassigned']);
      expect(patchBody.itemsToUpdate[0]).toEqual(expect.objectContaining({
        recount: false,
        assignee: 'counter-1',
        dateCounted: '2026-10-01',
        cycleCount: 'cc-1',
        cycleCountId: 'cc-1',
        inventoryItem: {
          lotNumber: 'LOT-1',
          expirationDate: '01/05/2027',
          product: 'prod-1',
        },
      }));

      expect(apiClient.post).toHaveBeenCalledTimes(1);
      const [postUrl, postBody] = apiClient.post.mock.calls[0];
      expect(postUrl).toBe('/api/facilities/loc-1/cycle-counts/items/batch');
      expect(postBody.itemsToCreate.map(({ id }) => id)).toEqual(['newRow-1']);

      expect(mockDispatch).toHaveBeenLastCalledWith({
        type: 'FETCH_CYCLE_COUNTS', args: [CYCLE_COUNT_IDS, LOCATION_ID, false],
      });
      expect(mockSpinner.show).toHaveBeenCalled();
      expect(mockSpinner.hide).toHaveBeenCalled();
    });

    it('falls back to the current user as assignee only when asked to set the default assignee', async () => {
      mockState.countWorkflow.entities = {
        'cc-1': { id: 'cc-1', cycleCountItems: [item('item-1', { updated: true })] },
      };

      const { result } = renderPersistence();
      await result.current.save(true);
      expect(apiClient.patch.mock.calls[0][1].itemsToUpdate[0].assignee).toBe(USER_ID);

      apiClient.patch.mockClear();
      await result.current.save(false);
      expect(apiClient.patch.mock.calls[0][1].itemsToUpdate[0].assignee).toBeUndefined();
    });

    it('sends no requests when nothing changed, but still refetches the cycle counts', async () => {
      mockState.countWorkflow.entities = {
        'cc-1': { id: 'cc-1', cycleCountItems: [item('item-1')] },
      };

      const { result } = renderPersistence({ sortByProductName: true });
      await result.current.save();

      expect(apiClient.patch).not.toHaveBeenCalled();
      expect(apiClient.post).not.toHaveBeenCalled();
      expect(mockDispatch).toHaveBeenLastCalledWith({
        type: 'FETCH_CYCLE_COUNTS', args: [CYCLE_COUNT_IDS, LOCATION_ID, true],
      });
    });

    it('refetches and hides the spinner even when the update request fails', async () => {
      mockState.countWorkflow.entities = {
        'cc-1': { id: 'cc-1', cycleCountItems: [item('item-1', { updated: true })] },
      };
      apiClient.patch.mockRejectedValue(new Error('boom'));

      const { result } = renderPersistence();
      await expect(result.current.save()).rejects.toThrow('boom');

      expect(mockDispatch).toHaveBeenLastCalledWith(expect.objectContaining({ type: 'FETCH_CYCLE_COUNTS' }));
      expect(mockSpinner.hide).toHaveBeenCalled();
    });
  });

  describe('validateExistenceOfCycleCounts', () => {
    it('runs the callback when every cycle count still exists', async () => {
      mockState.countWorkflow.entities = { 'cc-1': { id: 'cc-1' }, 'cc-2': { id: 'cc-2' } };
      apiClient.get.mockResolvedValue({ data: { data: [{ id: 'cc-1' }, { id: 'cc-2' }] } });
      const callback = jest.fn(() => 'saved');

      const { result } = renderPersistence();
      const outcome = await result.current.validateExistenceOfCycleCounts(callback);

      expect(apiClient.get).toHaveBeenCalledWith('/api/facilities/loc-1/cycle-counts?id=cc-1&id=cc-2');
      expect(callback).toHaveBeenCalled();
      expect(outcome).toBe('saved');
      expect(mockNotify).not.toHaveBeenCalled();
    });

    it('drops canceled cycle counts, notifies an error and skips the callback', async () => {
      mockState.countWorkflow.entities = { 'cc-1': { id: 'cc-1' }, 'cc-2': { id: 'cc-2' } };
      apiClient.get.mockResolvedValue({ data: { data: [{ id: 'cc-2' }] } });
      const callback = jest.fn();

      const { result } = renderPersistence();
      const outcome = await result.current.validateExistenceOfCycleCounts(callback);

      expect(outcome).toBe(false);
      expect(callback).not.toHaveBeenCalled();
      expect(mockDispatch).toHaveBeenCalledWith({
        type: UPDATE_CYCLE_COUNT_IDS,
        payload: { locationId: LOCATION_ID, cycleCounts: ['cc-2'] },
      });
      expect(notification).toHaveBeenCalledWith(NotificationType.ERROR_FILLED);
      expect(mockNotify).toHaveBeenCalledWith(expect.objectContaining({ message: 'Error' }));
    });
  });

  it('printCountForm saves first, then exports the count form in the requested format', async () => {
    mockState.countWorkflow.entities = {
      'cc-1': { id: 'cc-1', cycleCountItems: [item('item-1', { updated: true })] },
    };

    const { result } = renderPersistence({ sortByProductName: true });
    await result.current.printCountForm('pdf');

    expect(apiClient.patch).toHaveBeenCalled();
    expect(exportFileFromApi).toHaveBeenCalledWith({
      url: '/api/facilities/loc-1/cycle-counts',
      params: { id: CYCLE_COUNT_IDS, sortBy: 'productName' },
      format: 'pdf',
    });
  });

  it('exposes a toggleable save-disabled flag', () => {
    const { result } = renderPersistence();
    expect(result.current.isSaveDisabled).toBe(false);
    act(() => result.current.setIsSaveDisabled(true));
    expect(result.current.isSaveDisabled).toBe(true);
  });
});
