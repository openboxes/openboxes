import { renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';
import { getHasBinLocationSupport, getHasPartialReceivingSupport } from 'selectors';

import useReceivingNextValidation from 'hooks/receiving/v2/useReceivingNextValidation';
import alertMissingBinLocations from 'utils/receiving/alertMissingBinLocations';
import confirmBlankLinesAsZero from 'utils/receiving/confirmBlankLinesAsZero';

import '@testing-library/jest-dom';

// The hook only reads the activity codes of the current location (and the locale, unused here).
jest.mock('react-redux', () => ({
  useSelector: jest.fn(() => false),
}));
jest.mock('utils/receiving/confirmBlankLinesAsZero', () => jest.fn());
jest.mock('utils/receiving/alertMissingBinLocations', () => jest.fn());

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

const renderValidation = (rows, {
  hasPartialReceivingSupport = false,
  hasBinLocationSupport = false,
} = {}) => {
  useSelector.mockImplementation((selector) => {
    if (selector === getHasPartialReceivingSupport) {
      return hasPartialReceivingSupport;
    }
    if (selector === getHasBinLocationSupport) {
      return hasBinLocationSupport;
    }
    return undefined;
  });
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

    it('should block the transition when a line being received has no location', async () => {
      const { result } = renderValidation(
        [buildRow('row-1', { quantityReceiving: 2 })],
        { hasBinLocationSupport: true },
      );

      await expect(result.current.validateBeforeNext()).resolves.toBe(false);
      expect(alertMissingBinLocations).toHaveBeenCalledWith(1);
      expect(confirmBlankLinesAsZero).not.toHaveBeenCalled();
    });

    it('should let the lines that carry a location through', async () => {
      const { result } = renderValidation(
        [buildRow('row-1', { quantityReceiving: 2, binLocation: { id: 'bin-1' } })],
        { hasBinLocationSupport: true },
      );

      await expect(result.current.validateBeforeNext()).resolves.toBe(true);
      expect(alertMissingBinLocations).not.toHaveBeenCalled();
    });

    it('should not ask for a location on a line receiving nothing', async () => {
      const { result } = renderValidation(
        [
          buildRow('row-1', { quantityReceiving: 0 }),
          buildRow('row-2', { quantityReceiving: 2, binLocation: { id: 'bin-1' } }),
        ],
        { hasBinLocationSupport: true },
      );

      await expect(result.current.validateBeforeNext()).resolves.toBe(true);
      expect(alertMissingBinLocations).not.toHaveBeenCalled();
    });

    it('should not ask for a location at a location that does not track bins', async () => {
      const { result } = renderValidation([buildRow('row-1', { quantityReceiving: 2 })]);

      await expect(result.current.validateBeforeNext()).resolves.toBe(true);
      expect(alertMissingBinLocations).not.toHaveBeenCalled();
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
