import { act, renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useReceivingLineItems from 'hooks/receiving/v2/useReceivingLineItems';

// The hook only reads the partial receiving activity code of the current location.
jest.mock('react-redux', () => ({
  useSelector: jest.fn(() => true),
}));
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('hooks/receiving/v2/useReceivingLineItemColumns', () => () => ({ columns: [] }));
jest.mock('hooks/receiving/v2/useEditModalLocationAutofill', () => () => ({
  onLocationAutofill: jest.fn(),
}));

const product = { id: 'product-1', name: 'Ibuprofen 200mg' };
const splitProduct = { id: 'product-2', name: 'Paracetamol 500mg' };
const shipmentRecipient = { id: 'person-1', name: 'John Doe', label: 'John Doe' };
const editedRecipient = { id: 'person-2', name: 'Jane Roe', label: 'Jane Roe' };
const receivingBin = { id: 'bin-1', name: 'R-00001', label: 'R-00001' };
const putawayBin = { id: 'bin-2', name: 'Zone A', label: 'Zone A' };

const originalLine = {
  rowId: 'row-1',
  receiptItemId: 'receipt-item-1',
  product,
  lotNumber: 'LOT-1',
  recipient: shipmentRecipient,
  binLocation: receivingBin,
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

const renderLineItems = (initialLineItems, hasPreviousReceipts = false) =>
  renderHook(() => useReceivingLineItems({
    lineItem: originalLine,
    initialLineItems,
    hasPreviousReceipts,
  }));

const summaryTitles = (result) => result.current.summaryData.map(({ title }) => title);

describe('useReceivingLineItems', () => {
  beforeEach(() => {
    useSelector.mockImplementation(() => true);
  });

  it('should open with the original line and an empty split row carrying its autofilled values', () => {
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
      recipient: shipmentRecipient,
      binLocation: receivingBin,
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

  describe('autofill of an added row', () => {
    it('should carry the product, the recipient and the bin of the line', () => {
      const { result } = renderLineItems([originalLine, splitLine]);

      act(() => result.current.addRow());

      expect(result.current.fields[2]).toMatchObject({
        receiptItemId: null,
        product,
        lotNumber: '',
        recipient: shipmentRecipient,
        binLocation: receivingBin,
        quantityReceiving: '',
        isSplitItem: true,
      });
    });

    it('should take the bin the original line sits in, not the receiving bin it started in', () => {
      const { result } = renderLineItems([
        { ...originalLine, binLocation: putawayBin },
        splitLine,
      ]);

      act(() => result.current.addRow());

      expect(result.current.fields[2]).toMatchObject({ binLocation: putawayBin });
    });

    it('should take the recipient of the shipment item, not the one entered on the line', () => {
      const { result } = renderLineItems([
        { ...originalLine, recipient: editedRecipient },
        splitLine,
      ]);

      act(() => result.current.addRow());

      expect(result.current.fields[2]).toMatchObject({ recipient: shipmentRecipient });
    });
  });

  describe('the Received summary card', () => {
    it('should be shown when the location supports partial receiving', () => {
      const { result } = renderLineItems([originalLine]);

      expect(summaryTitles(result)).toEqual([
        'Quantity Shipped',
        'Received',
        'Receiving Now',
        'Remaining to Receive',
      ]);
    });

    it('should be hidden without partial receiving support and without a previous receipt', () => {
      useSelector.mockImplementation(() => false);

      const { result } = renderLineItems([originalLine]);

      expect(summaryTitles(result)).toEqual([
        'Quantity Shipped',
        'Receiving Now',
        'Remaining to Receive',
      ]);
    });

    it('should be shown without partial receiving support when the shipment has a previous receipt', () => {
      useSelector.mockImplementation(() => false);

      const { result } = renderLineItems([originalLine], true);

      expect(summaryTitles(result)).toEqual([
        'Quantity Shipped',
        'Received',
        'Receiving Now',
        'Remaining to Receive',
      ]);
    });
  });
});
