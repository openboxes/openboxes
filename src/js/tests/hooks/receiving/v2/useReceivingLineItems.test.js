import { act, renderHook } from '@testing-library/react-hooks';

import useReceivingLineItems from 'hooks/receiving/v2/useReceivingLineItems';

jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('hooks/receiving/v2/useReceivingLineItemColumns', () => () => ({ columns: [] }));
jest.mock('hooks/receiving/v2/useEditModalLocationAutofill', () => () => ({
  onLocationAutofill: jest.fn(),
}));

const product = { id: 'product-1', name: 'Ibuprofen 200mg' };
const splitProduct = { id: 'product-2', name: 'Paracetamol 500mg' };

const originalLine = {
  rowId: 'row-1',
  receiptItemId: 'receipt-item-1',
  product,
  lotNumber: 'LOT-1',
  quantityReceiving: 5,
  quantityShipped: 10,
  quantityReceived: 0,
  isSplitItem: false,
};

const splitLine = {
  rowId: 'row-2',
  receiptItemId: 'receipt-item-2',
  product: splitProduct,
  lotNumber: 'LOT-2',
  quantityReceiving: 3,
  isSplitItem: true,
};

const renderLineItems = (initialLineItems) => renderHook(() => useReceivingLineItems({
  lineItem: originalLine,
  initialLineItems,
}));

describe('useReceivingLineItems', () => {
  it('should open with the original line and an empty split row carrying its product', () => {
    const { result } = renderLineItems([originalLine]);

    expect(result.current.fields).toHaveLength(2);
    expect(result.current.fields[0]).toMatchObject({
      receiptItemId: 'receipt-item-1',
      product,
      lotNumber: 'LOT-1',
      quantityReceiving: 5,
      isSplitItem: false,
    });
    expect(result.current.fields[1]).toMatchObject({
      receiptItemId: null,
      product,
      lotNumber: '',
      recipient: null,
      quantityReceiving: '',
      isSplitItem: true,
    });
  });

  it('should not add the empty split row to a line that already has a split item saved', () => {
    const { result } = renderLineItems([originalLine, splitLine]);

    expect(result.current.fields).toHaveLength(2);
    expect(result.current.fields[1]).toMatchObject({
      receiptItemId: 'receipt-item-2',
      product: splitProduct,
      lotNumber: 'LOT-2',
      isSplitItem: true,
    });
  });

  it('should keep the empty split row when reverting to original', () => {
    const { result } = renderLineItems([originalLine]);

    act(() => result.current.addRow());
    expect(result.current.fields).toHaveLength(3);

    act(() => result.current.revertToOriginal());

    expect(result.current.fields).toHaveLength(2);
    expect(result.current.fields[1]).toMatchObject({
      product,
      isSplitItem: true,
    });
  });
});
