import RowSaveStatus from 'consts/rowSaveStatus';
import hasRowSavedQuantity from 'utils/receiving/hasRowSavedQuantity';

describe('hasRowSavedQuantity', () => {
  it('returns true for a saved row with a persisted quantity', () => {
    expect(hasRowSavedQuantity({
      saveStatus: RowSaveStatus.SAVED,
      initialQuantityReceiving: 5,
    })).toBe(true);
  });

  it('returns true when the persisted quantity is 0', () => {
    expect(hasRowSavedQuantity({
      saveStatus: RowSaveStatus.SAVED,
      initialQuantityReceiving: 0,
    })).toBe(true);
  });

  it.each([
    RowSaveStatus.PENDING,
    RowSaveStatus.SAVING,
    RowSaveStatus.ERROR,
  ])('returns false while the row is being edited or saved (%s)', (saveStatus) => {
    expect(hasRowSavedQuantity({
      saveStatus,
      initialQuantityReceiving: 5,
    })).toBe(false);
  });

  it('returns false for a row without a persisted quantity, even with a receipt item', () => {
    expect(hasRowSavedQuantity({
      saveStatus: RowSaveStatus.SAVED,
      receiptItemId: 'receipt-item-1',
      initialQuantityReceiving: null,
    })).toBe(false);
  });

  it('returns false for missing entities (toggle/separator rows)', () => {
    expect(hasRowSavedQuantity(undefined)).toBe(false);
    expect(hasRowSavedQuantity(null)).toBe(false);
  });
});
