import { renderHook } from '@testing-library/react-hooks';

import { CYCLE_COUNT } from 'consts/applicationUrls';
import { ALL_PRODUCTS_TAB, TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import cycleCountStatus from 'consts/cycleCountStatus';
import useResolveDiscrepancies from 'hooks/cycleCount/countStep/useResolveDiscrepancies';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const mockDispatch = jest.fn((action) => action);
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useStore: () => ({ getState: () => mockState }),
}));
const mockHistory = { push: jest.fn() };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
}));
jest.mock('actions', () => ({
  clearCountWorkflow: 'CLEAR_COUNT_WORKFLOW',
  eraseDraft: jest.fn((locationId, tab) => ({ type: 'ERASE_DRAFT', locationId, tab })),
  startResolution: jest.fn((ids, locationId) => ({ type: 'START_RESOLUTION', ids, locationId })),
}));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('hooks/useTranslate', () => jest.fn(() => (key, defaultMessage) => defaultMessage));
const mockNotify = jest.fn();
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockNotify));
jest.mock('utils/confirmationModalUtils', () => jest.fn());

const LOCATION_ID = 'loc-1';

// Adapted for OBAF (OBAF lacks the code): OBAF does not clear the count
// workflow after a successful submit, so 'submits every count and redirects to the resolve tab
// when it has pending requests' no longer expects the clearCountWorkflow dispatch.

const submitResponse = (requestId, status) => ({ data: { data: { requestId, status } } });

describe('useResolveDiscrepancies', () => {
  let openAssignCountModal;
  let setIsSaveDisabled;

  const renderResolve = (props = {}) => renderHook(() => useResolveDiscrepancies({
    currentLocationId: LOCATION_ID,
    openAssignCountModal,
    setIsSaveDisabled,
    ...props,
  }));

  beforeEach(() => {
    jest.clearAllMocks();
    openAssignCountModal = jest.fn();
    setIsSaveDisabled = jest.fn();
    mockState = {
      countWorkflow: {
        entities: {
          'cc-1': { id: 'cc-1', cycleCountItems: [{ id: 'item-1' }] },
          'cc-2': { id: 'cc-2', cycleCountItems: [{ id: 'item-2' }] },
        },
      },
    };
  });

  it('submits every count and redirects to the resolve tab when it has pending requests', async () => {
    apiClient.post
      .mockResolvedValueOnce(submitResponse('req-1', cycleCountStatus.COMPLETED))
      .mockResolvedValueOnce(submitResponse('req-2', cycleCountStatus.COMPLETED));
    apiClient.get.mockResolvedValueOnce({ data: { totalCount: 3 } });

    const { result } = renderResolve();
    await result.current.resolveDiscrepancies();

    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/facilities/loc-1/cycle-counts/cc-1/count',
      {
        refreshQuantityOnHand: true,
        failOnOutdatedQuantity: false,
        requireRecountOnDiscrepancy: true,
        cycleCountItems: [{ id: 'item-1' }],
      },
    );
    expect(apiClient.post).toHaveBeenCalledWith(
      '/api/facilities/loc-1/cycle-counts/cc-2/count',
      expect.objectContaining({ cycleCountItems: [{ id: 'item-2' }] }),
    );
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'ERASE_DRAFT', locationId: LOCATION_ID, tab: TO_COUNT_TAB });
    expect(mockNotify).toHaveBeenCalledWith({ message: 'Successfully counted 2 products' });

    expect(apiClient.get).toHaveBeenCalledTimes(1);
    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe('/api/facilities/loc-1/cycle-counts/requests/pending');
    expect(config.params).toEqual({
      facility: LOCATION_ID,
      statuses: [cycleCountStatus.COUNTED, cycleCountStatus.INVESTIGATING],
      tab: TO_RESOLVE_TAB,
      max: 1,
      offset: 0,
    });
    expect(config.paramsSerializer({ a: [1, 2] })).toBe('a=1&a=2');
    expect(mockHistory.push).toHaveBeenCalledWith(CYCLE_COUNT.list(TO_RESOLVE_TAB));
    expect(confirmationModal).not.toHaveBeenCalled();
    expect(setIsSaveDisabled).toHaveBeenCalledWith(false);
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('falls through to the count tab, then to all products, when earlier tabs are empty', async () => {
    apiClient.post.mockResolvedValue(submitResponse('req-1', cycleCountStatus.COMPLETED));
    apiClient.get
      .mockResolvedValueOnce({ data: { totalCount: 0 } })
      .mockResolvedValueOnce({ data: { totalCount: 1 } });

    const { result } = renderResolve();
    await result.current.resolveDiscrepancies();
    expect(apiClient.get.mock.calls[1][1].params.tab).toBe(TO_COUNT_TAB);
    expect(mockHistory.push).toHaveBeenCalledWith(CYCLE_COUNT.list(TO_COUNT_TAB));

    mockHistory.push.mockClear();
    apiClient.get.mockResolvedValue({ data: { totalCount: 0 } });
    await result.current.resolveDiscrepancies();
    expect(mockHistory.push).toHaveBeenCalledWith(CYCLE_COUNT.list(ALL_PRODUCTS_TAB));
  });

  describe('with discrepancies', () => {
    const submitWithOneDiscrepancy = async (props) => {
      apiClient.post
        .mockResolvedValueOnce(submitResponse('req-1', cycleCountStatus.COUNTED))
        .mockResolvedValueOnce(submitResponse('req-2', cycleCountStatus.COMPLETED));
      const hook = renderResolve(props);
      await hook.result.current.resolveDiscrepancies();
      return hook;
    };

    it('opens the resolve modal with the discrepancy count and keeps the ids in the ref', async () => {
      const ref = { current: [] };
      await submitWithOneDiscrepancy({ requestIdsWithDiscrepanciesRef: ref });

      expect(ref.current).toEqual(['req-1']);
      expect(confirmationModal).toHaveBeenCalledTimes(1);
      const modal = confirmationModal.mock.calls[0][0];
      expect(modal.hideCloseButton).toBe(true);
      expect(modal.closeOnClickOutside).toBe(false);
      expect(modal.content.data).toEqual({ count: 1 });
      expect(modal.content.default).toContain('There are 1 products');
      expect(apiClient.get).not.toHaveBeenCalled();
      expect(mockNotify).not.toHaveBeenCalled();
      expect(setIsSaveDisabled).toHaveBeenCalledWith(false);
    });

    it('uses its own ref when none is injected', async () => {
      const { result } = await submitWithOneDiscrepancy();
      expect(result.current.requestIdsWithDiscrepancies.current).toEqual(['req-1']);
    });

    it('"Not now" reports the counted products and goes to the resolve tab', async () => {
      await submitWithOneDiscrepancy();
      const onClose = jest.fn();
      const [notNow] = confirmationModal.mock.calls[0][0].buttons(onClose);

      notNow.onClick();

      expect(mockNotify).toHaveBeenCalledWith({ message: 'Successfully counted 1 products' });
      expect(mockDispatch).toHaveBeenCalledWith('CLEAR_COUNT_WORKFLOW');
      expect(onClose).toHaveBeenCalled();
      expect(mockHistory.push).toHaveBeenCalledWith(CYCLE_COUNT.list(TO_RESOLVE_TAB));
    });

    it('"Resolve" starts the resolution and opens the assign count modal', async () => {
      await submitWithOneDiscrepancy();
      const [, resolve] = confirmationModal.mock.calls[0][0].buttons(undefined);

      await resolve.onClick();

      expect(mockDispatch).toHaveBeenCalledWith({ type: 'START_RESOLUTION', ids: ['req-1'], locationId: LOCATION_ID });
      expect(mockNotify).toHaveBeenCalledWith({ message: 'Successfully counted 1 products' });
      expect(openAssignCountModal).toHaveBeenCalled();
      expect(mockHistory.push).not.toHaveBeenCalled();
    });

    it('does not show a success message when every count had a discrepancy', async () => {
      apiClient.post.mockResolvedValue(submitResponse('req-x', cycleCountStatus.COUNTED));
      const { result } = renderResolve();
      await result.current.resolveDiscrepancies();
      const [notNow, resolve] = confirmationModal.mock.calls[0][0].buttons();

      notNow.onClick();
      await resolve.onClick();

      expect(mockNotify).not.toHaveBeenCalled();
      expect(openAssignCountModal).toHaveBeenCalled();
    });
  });

  it('re-enables saving even when a submission fails', async () => {
    apiClient.post.mockRejectedValue(new Error('fail'));
    const { result } = renderResolve();

    await expect(result.current.resolveDiscrepancies()).rejects.toThrow('fail');
    expect(setIsSaveDisabled).toHaveBeenCalledWith(false);
    expect(mockSpinner.hide).toHaveBeenCalled();
  });
});
