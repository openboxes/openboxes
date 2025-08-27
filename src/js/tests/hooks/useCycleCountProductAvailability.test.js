import { renderHook } from '@testing-library/react-hooks';

import { ALL_PRODUCTS_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import cycleCountCandidateStatus from 'consts/cycleCountCandidateStatus';
import useCycleCountProductAvailability from 'hooks/cycleCount/useCycleCountProductAvailability';
import useQueryParams from 'hooks/useQueryParams';
import * as checkStatusUtils from 'utils/checkCycleCountStatus';

jest.mock('hooks/useQueryParams', () => jest.fn());

describe('useCycleCountProductAvailability', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('returns "counting" message when status is counting and tab = ALL_PRODUCTS_TAB', () => {
    useQueryParams.mockReturnValue({ tab: ALL_PRODUCTS_TAB });
    jest.spyOn(checkStatusUtils, 'isCounting').mockReturnValue(true);
    jest.spyOn(checkStatusUtils, 'isResolving').mockReturnValue(false);

    const row = { status: cycleCountCandidateStatus.COUNTING, quantityAllocated: 0 };
    const { result } = renderHook(() => useCycleCountProductAvailability(row));

    expect(result.current).toEqual({
      isRowDisabled: true,
      label: 'react.cycleCount.status.counting.label',
      defaultMessage: 'Count started on the product: find the product on To Count tab',
      isFromOtherTab: true,
      showCancelCheckbox: false,
    });
  });

  it('returns "resolving" message when status is resolving and tab = ALL_PRODUCTS_TAB', () => {
    useQueryParams.mockReturnValue({ tab: ALL_PRODUCTS_TAB });
    jest.spyOn(checkStatusUtils, 'isCounting').mockReturnValue(false);
    jest.spyOn(checkStatusUtils, 'isResolving').mockReturnValue(true);

    const row = { status: cycleCountCandidateStatus.INVESTIGATING, quantityAllocated: 0 };
    const { result } = renderHook(() => useCycleCountProductAvailability(row));

    expect(result.current).toEqual({
      isRowDisabled: true,
      label: 'react.cycleCount.status.resolving.label',
      defaultMessage: 'Resolution started on the product: find the product on To Resolve tab',
      isFromOtherTab: true,
      showCancelCheckbox: false,
    });
  });

  it('returns warning when quantityAllocated > 0 and tab = TO_RESOLVE_TAB', () => {
    useQueryParams.mockReturnValue({ tab: TO_RESOLVE_TAB });
    jest.spyOn(checkStatusUtils, 'isCounting').mockReturnValue(false);
    jest.spyOn(checkStatusUtils, 'isResolving').mockReturnValue(false);

    const row = { status: 'ANY', quantityAllocated: 5 };
    const { result } = renderHook(() => useCycleCountProductAvailability(row));

    expect(result.current).toEqual({
      isRowDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.warning.label',
      defaultMessage: 'This product is in a pending stock movement. Check quantity input carefully',
      isFromOtherTab: false,
      showCancelCheckbox: false,
    });
  });

  it('returns warning when quantityAllocated > 0 and status = COUNTING', () => {
    useQueryParams.mockReturnValue({ tab: 'RANDOM_TAB' });
    jest.spyOn(checkStatusUtils, 'isCounting').mockReturnValue(false);
    jest.spyOn(checkStatusUtils, 'isResolving').mockReturnValue(false);

    const row = { status: cycleCountCandidateStatus.COUNTING, quantityAllocated: 3 };
    const { result } = renderHook(() => useCycleCountProductAvailability(row));

    expect(result.current).toEqual({
      isRowDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.warning.label',
      defaultMessage: 'This product is in a pending stock movement. Check quantity input carefully',
      isFromOtherTab: false,
      showCancelCheckbox: false,
    });
  });

  it('returns info when quantityAllocated > 0 and no special conditions', () => {
    useQueryParams.mockReturnValue({ tab: 'RANDOM_TAB' });
    jest.spyOn(checkStatusUtils, 'isCounting').mockReturnValue(false);
    jest.spyOn(checkStatusUtils, 'isResolving').mockReturnValue(false);

    const row = { status: 'any', quantityAllocated: 2 };
    const { result } = renderHook(() => useCycleCountProductAvailability(row));

    expect(result.current).toEqual({
      isRowDisabled: true,
      label: 'react.cycleCount.pendingStockMovement.info.label',
      defaultMessage: 'Cannot start count on this product with pending stock movement',
      isFromOtherTab: false,
      showCancelCheckbox: true,
    });
  });

  it('returns default availability object when no special conditions are met', () => {
    useQueryParams.mockReturnValue({ tab: 'any' });
    jest.spyOn(checkStatusUtils, 'isCounting').mockReturnValue(false);
    jest.spyOn(checkStatusUtils, 'isResolving').mockReturnValue(false);

    const row = { status: 'any', quantityAllocated: 0 };
    const { result } = renderHook(() => useCycleCountProductAvailability(row));

    expect(result.current).toEqual({
      isRowDisabled: false,
      label: null,
      defaultMessage: null,
      isFromOtherTab: false,
      showCancelCheckbox: false,
    });
  });
});
