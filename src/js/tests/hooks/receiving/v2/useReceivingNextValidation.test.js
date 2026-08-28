import { renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useReceivingNextValidation from 'hooks/receiving/v2/useReceivingNextValidation';
import confirmBlankLinesAsZero from 'utils/receiving/confirmBlankLinesAsZero';

import '@testing-library/jest-dom';

// The hook only reads the partial receiving activity code of the current location.
jest.mock('react-redux', () => ({
  useSelector: jest.fn(() => false),
}));
jest.mock('utils/receiving/confirmBlankLinesAsZero', () => jest.fn());

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  rowType: null,
  quantityReceiving: null,
  isCompleted: false,
  ...overrides,
});

const buildState = (rows) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: rows.map((row) => row.rowId),
});

const renderValidation = (rows, { hasPartialReceivingSupport = false } = {}) => {
  useSelector.mockImplementation(() => hasPartialReceivingSupport);
  const { result } = renderHook(() => useReceivingNextValidation({
    lineItemsState: buildState(rows),
  }));
  return { result };
};

describe('useReceivingNextValidation', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('isNextDisabled', () => {
    it('should be disabled when no line carries a quantity', () => {
      const { result } = renderValidation([buildRow('row-1'), buildRow('row-2')]);

      expect(result.current.isNextDisabled).toBe(true);
    });

    it('should be enabled as soon as one line carries a quantity, including 0', () => {
      const { result } = renderValidation([
        buildRow('row-1'),
        buildRow('row-2', { quantityReceiving: 0 }),
      ]);

      expect(result.current.isNextDisabled).toBe(false);
    });

    it('should be enabled when every line is already completed', () => {
      const { result } = renderValidation([buildRow('row-1', { isCompleted: true })]);

      expect(result.current.isNextDisabled).toBe(false);
    });
  });

  describe('validateBeforeNext', () => {
    it('should not ask anything at a location with partial receiving', async () => {
      const { result } = renderValidation(
        [buildRow('row-1'), buildRow('row-2', { quantityReceiving: 2 })],
        { hasPartialReceivingSupport: true },
      );

      await expect(result.current.validateBeforeNext()).resolves.toBe(true);
      expect(confirmBlankLinesAsZero).not.toHaveBeenCalled();
    });

    it('should not ask anything when every line carries a quantity', async () => {
      const { result } = renderValidation([
        buildRow('row-1', { quantityReceiving: 2 }),
        buildRow('row-2', { quantityReceiving: 0 }),
      ]);

      await expect(result.current.validateBeforeNext()).resolves.toBe(true);
      expect(confirmBlankLinesAsZero).not.toHaveBeenCalled();
    });

    it('should warn about the blank lines and let the confirmed transition through', async () => {
      confirmBlankLinesAsZero.mockResolvedValue(true);
      const { result } = renderValidation([
        buildRow('row-1'),
        buildRow('row-2', { quantityReceiving: 2 }),
        buildRow('row-3'),
      ]);

      await expect(result.current.validateBeforeNext()).resolves.toBe(true);
      expect(confirmBlankLinesAsZero).toHaveBeenCalledWith(expect.objectContaining({
        blankRows: [
          expect.objectContaining({ rowId: 'row-1' }),
          expect.objectContaining({ rowId: 'row-3' }),
        ],
      }));
    });

    it('should block the transition when the user declines', async () => {
      confirmBlankLinesAsZero.mockResolvedValue(false);
      const { result } = renderValidation([
        buildRow('row-1'),
        buildRow('row-2', { quantityReceiving: 2 }),
      ]);

      await expect(result.current.validateBeforeNext()).resolves.toBe(false);
    });

    it('should not count completed lines as blank', async () => {
      const { result } = renderValidation([
        buildRow('row-1', { isCompleted: true }),
        buildRow('row-2', { quantityReceiving: 2 }),
      ]);

      await expect(result.current.validateBeforeNext()).resolves.toBe(true);
      expect(confirmBlankLinesAsZero).not.toHaveBeenCalled();
    });
  });
});
