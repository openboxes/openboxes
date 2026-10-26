import { act, renderHook } from '@testing-library/react-hooks';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useResolveStepValidation from 'hooks/cycleCount/useResolveStepValidation';

const mockNotify = jest.fn();

jest.mock('hooks/useTranslate');
jest.mock('components/Layout/notifications/notification', () => jest.fn(() => mockNotify));

const buildRow = (id, overrides = {}) => ({
  id,
  product: { productCode: 'P1', lotAndExpiryControl: false },
  quantityCounted: 5,
  quantityOnHand: 5,
  quantityRecounted: 5,
  inventoryItem: { lotNumber: `LOT-${id}`, expirationDate: null },
  binLocation: { id: 'bin-1', name: 'Bin 1' },
  rootCause: null,
  ...overrides,
});

// validationErrors is read from a ref at render time, so the tests re-render (as the screen does
// through forceRerender) after triggering the validation.
describe('useResolveStepValidation', () => {
  const renderUseResolveStepValidation = (cycleCounts) => {
    const tableData = { current: cycleCounts };
    const hook = renderHook(() => useResolveStepValidation({ tableData }));
    return { ...hook, tableData };
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('triggerValidation', () => {
    it('returns true and stores a success entry per cycle count for valid rows', () => {
      const { result } = renderUseResolveStepValidation([
        { id: 'cc-1', cycleCountItems: [buildRow('a'), buildRow('b')] },
        { id: 'cc-2', cycleCountItems: [buildRow('c')] },
      ]);

      let isValid;
      act(() => {
        isValid = result.current.triggerValidation();
        result.current.forceRerender();
      });

      expect(isValid).toBe(true);
      expect(result.current.validationErrors).toEqual({
        'cc-1': { errors: undefined, success: true },
        'cc-2': { errors: undefined, success: true },
      });
    });

    it('reports a missing recount quantity on the failing cycle count only', () => {
      const { result } = renderUseResolveStepValidation([
        { id: 'cc-1', cycleCountItems: [buildRow('a', { quantityRecounted: null })] },
        { id: 'cc-2', cycleCountItems: [buildRow('b')] },
      ]);

      let isValid;
      act(() => {
        isValid = result.current.triggerValidation();
        result.current.forceRerender();
      });

      expect(isValid).toBe(false);
      const { 'cc-1': failed, 'cc-2': passed } = result.current.validationErrors;
      expect(failed.success).toBe(false);
      expect(failed.errors[0].quantityRecounted._errors).toEqual(['Quantity recounted is required']);
      expect(passed.success).toBe(true);
    });

    it('rejects negative recount quantities', () => {
      const { result } = renderUseResolveStepValidation([
        { id: 'cc-1', cycleCountItems: [buildRow('a', { quantityRecounted: -1 })] },
      ]);

      expect(result.current.triggerValidation()).toBe(false);
    });

    it('requires a lot number when an expiration date is set', () => {
      const { result } = renderUseResolveStepValidation([
        {
          id: 'cc-1',
          cycleCountItems: [buildRow('a', { inventoryItem: { lotNumber: '', expirationDate: '01/15/2027' } })],
        },
      ]);

      act(() => {
        result.current.triggerValidation();
        result.current.forceRerender();
      });

      expect(result.current.validationErrors['cc-1'].errors[0]['inventoryItem.lotNumber']._errors)
        .toEqual(['Lot number is required']);
    });

    it('applies the inventory checks across the rows of a cycle count', () => {
      const duplicate = { lotNumber: 'LOT-1', expirationDate: null };
      const { result } = renderUseResolveStepValidation([
        {
          id: 'cc-1',
          cycleCountItems: [
            buildRow('a', { inventoryItem: duplicate }),
            buildRow('b', { inventoryItem: duplicate }),
          ],
        },
      ]);

      const parsed = result.current.rowsValidationSchema.safeParse(
        [buildRow('a', { inventoryItem: duplicate }), buildRow('b', { inventoryItem: duplicate })],
      );

      expect(parsed.success).toBe(false);
      expect(parsed.error.format()[1].binLocation._errors).toEqual(['Duplicate rows for this inventory item.']);
    });
  });

  it('resetValidationState clears the stored errors', () => {
    const { result, rerender } = renderUseResolveStepValidation([
      { id: 'cc-1', cycleCountItems: [buildRow('a', { quantityRecounted: null })] },
    ]);
    act(() => {
      result.current.triggerValidation();
      result.current.forceRerender();
    });
    expect(result.current.validationErrors['cc-1'].success).toBe(false);

    result.current.resetValidationState();
    rerender();

    expect(result.current.validationErrors).toEqual({});
  });

  describe('root causes', () => {
    const cycleCounts = () => [
      {
        id: 'cc-1',
        cycleCountItems: [
          buildRow('equal'),
          buildRow('diff', { quantityRecounted: 3 }),
          buildRow('diff-with-cause', { quantityRecounted: 3, rootCause: { id: 'DAMAGED' } }),
          buildRow('no-qoh', { quantityOnHand: null, quantityRecounted: 2 }),
          buildRow('not-recounted', { quantityRecounted: null, quantityOnHand: 0 }),
        ],
      },
      { id: 'cc-2', cycleCountItems: [buildRow('nan', { quantityRecounted: undefined })] },
    ];

    it('validateRootCauses lists rows with a recount difference and no root cause', () => {
      const { result } = renderUseResolveStepValidation(cycleCounts());

      expect(result.current.validateRootCauses()).toEqual(['diff', 'no-qoh']);
    });

    it('shouldHaveRootCause is true only for those rows', () => {
      const { result } = renderUseResolveStepValidation(cycleCounts());

      expect(result.current.shouldHaveRootCause('diff')).toBe(true);
      expect(result.current.shouldHaveRootCause('diff-with-cause')).toBe(false);
      expect(result.current.shouldHaveRootCause('equal')).toBe(false);
      expect(result.current.shouldHaveRootCause('unknown')).toBe(false);
    });

    it('showEmptyRootCauseWarning notifies and marks the warning as skipped', () => {
      const { result } = renderUseResolveStepValidation(cycleCounts());
      expect(result.current.isRootCauseWarningSkipped).toBe(false);

      act(() => result.current.showEmptyRootCauseWarning());

      expect(notification).toHaveBeenCalledWith(NotificationType.INFO);
      expect(mockNotify).toHaveBeenCalledWith({
        message: 'Are you sure you want to continue with empty root cause? Click next if you want to continue.',
      });
      expect(result.current.isRootCauseWarningSkipped).toBe(true);
    });
  });
});
