import { renderHook } from '@testing-library/react-hooks';
import { z } from 'zod';

import useInventoryValidation from 'hooks/cycleCount/useInventoryValidation';

jest.mock('hooks/useTranslate');

const DUPLICATED_ROW = 'Duplicate rows for this inventory item.';
const MULTIPLE_EXPIRATION_DATES = 'Multiple expiry dates for this lot/batch.';
const REQUIRED_LOT_AND_EXPIRY = 'Lot number and expiry date are required.';

const buildItem = (id, { bin = 'bin-1', lot = 'LOT-1', expirationDate = null } = {}) => ({
  id,
  binLocation: bin ? { id: bin } : null,
  inventoryItem: { lotNumber: lot, expirationDate },
});

describe('useInventoryValidation', () => {
  // Runs one check through zod's superRefine and returns the issues it raised
  const runCheck = (check, rows) => {
    const result = z.array(z.any()).superRefine(check).safeParse(rows);
    return result.success
      ? []
      : result.error.issues.map(({ path, message }) => ({ path, message }));
  };

  const renderUseInventoryValidation = (cycleCountItems, product) => {
    const items = cycleCountItems.map((item) => ({ ...item, product }));
    return {
      items,
      checks: renderHook(() => useInventoryValidation({
        tableData: { current: [{ id: 'cc-1', cycleCountItems: items }] },
      })).result.current,
    };
  };

  describe('checkDuplicatedLotNumber', () => {
    it('accepts the same lot in different bins', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('a', { bin: 'bin-1' }),
        buildItem('b', { bin: 'bin-2' }),
      ]);

      expect(runCheck(checks.checkDuplicatedLotNumber, items)).toEqual([]);
    });

    it('flags both bin and lot of every row sharing a bin and lot, ignoring lot whitespace', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('a', { lot: 'LOT-1' }),
        buildItem('b', { lot: ' LOT-1 ' }),
        buildItem('c', { lot: 'LOT-2' }),
      ]);

      expect(runCheck(checks.checkDuplicatedLotNumber, items)).toEqual([
        { path: [0, 'binLocation'], message: DUPLICATED_ROW },
        { path: [0, 'inventoryItem.lotNumber'], message: DUPLICATED_ROW },
        { path: [1, 'binLocation'], message: DUPLICATED_ROW },
        { path: [1, 'inventoryItem.lotNumber'], message: DUPLICATED_ROW },
      ]);
    });

    it('treats two rows without a lot in the same bin as duplicates', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('a', { lot: null }),
        buildItem('b', { lot: '' }),
      ]);

      expect(runCheck(checks.checkDuplicatedLotNumber, items)).toHaveLength(4);
    });
  });

  describe('checkDifferentExpirationDatesForTheSameLot', () => {
    it('accepts equal dates written in the backend and ISO formats', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('a', { bin: 'bin-1', expirationDate: '2027-01-15T00:00:00Z' }),
        buildItem('b', { bin: 'bin-2', expirationDate: '2027-01-15' }),
      ]);

      expect(runCheck(checks.checkDifferentExpirationDatesForTheSameLot, items)).toEqual([]);
    });

    it('flags every row of a lot that has different expiration dates', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('a', { bin: 'bin-1', expirationDate: '2027-01-15' }),
        buildItem('b', { bin: 'bin-2', expirationDate: '2027-02-15' }),
        buildItem('c', { bin: 'bin-3', lot: 'LOT-2', expirationDate: '2030-01-01' }),
      ]);

      expect(runCheck(checks.checkDifferentExpirationDatesForTheSameLot, items)).toEqual([
        { path: [0, 'inventoryItem.expirationDate'], message: MULTIPLE_EXPIRATION_DATES },
        { path: [1, 'inventoryItem.expirationDate'], message: MULTIPLE_EXPIRATION_DATES },
      ]);
    });

    it('flags a lot where only one of the rows has an expiration date', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('a', { bin: 'bin-1', expirationDate: null }),
        buildItem('b', { bin: 'bin-2', expirationDate: '2027-01-15' }),
      ]);

      expect(runCheck(checks.checkDifferentExpirationDatesForTheSameLot, items)).toHaveLength(2);
    });

    it('accepts a lot where no row has an expiration date', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('a', { bin: 'bin-1' }),
        buildItem('b', { bin: 'bin-2' }),
      ]);

      expect(runCheck(checks.checkDifferentExpirationDatesForTheSameLot, items)).toEqual([]);
    });
  });

  describe('checkProductsWithLotAndExpiryControl', () => {
    it('requires lot and expiry on new rows of a lot-controlled product', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('newRow1', { lot: '  ' }),
      ], { lotAndExpiryControl: true });

      expect(runCheck(checks.checkProductsWithLotAndExpiryControl, items)).toEqual([
        { path: [0, 'inventoryItem.lotNumber'], message: REQUIRED_LOT_AND_EXPIRY },
        { path: [0, 'inventoryItem.expirationDate'], message: REQUIRED_LOT_AND_EXPIRY },
      ]);
    });

    it('only reports the missing field', () => {
      const { items, checks } = renderUseInventoryValidation([
        buildItem('newRow1', { lot: 'LOT-1' }),
      ], { lotAndExpiryControl: true });

      expect(runCheck(checks.checkProductsWithLotAndExpiryControl, items)).toEqual([
        { path: [0, 'inventoryItem.expirationDate'], message: REQUIRED_LOT_AND_EXPIRY },
      ]);
    });

    it('does not validate existing rows or products without lot control', () => {
      const existing = renderUseInventoryValidation([
        buildItem('item-1', { lot: null }),
      ], { lotAndExpiryControl: true });
      const uncontrolled = renderUseInventoryValidation([
        buildItem('newRow1', { lot: null }),
      ], { lotAndExpiryControl: false });

      expect(runCheck(existing.checks.checkProductsWithLotAndExpiryControl, existing.items))
        .toEqual([]);
      expect(runCheck(uncontrolled.checks.checkProductsWithLotAndExpiryControl, uncontrolled.items))
        .toEqual([]);
    });
  });

  describe('checkLotNumberRequireness', () => {
    it('requires a lot number only when an expiration date is set', () => {
      const { checks } = renderUseInventoryValidation([]);

      expect(checks.checkLotNumberRequireness({ inventoryItem: { lotNumber: 'L1', expirationDate: '2027-01-01' } })).toBeTruthy();
      expect(checks.checkLotNumberRequireness({
        inventoryItem: { lotNumber: null, expirationDate: null },
      })).toBe(true);
      expect(checks.checkLotNumberRequireness({ inventoryItem: { lotNumber: ' ', expirationDate: '2027-01-01' } })).toBe(false);
      expect(checks.checkLotNumberRequireness({})).toBe(true);
    });
  });
});
