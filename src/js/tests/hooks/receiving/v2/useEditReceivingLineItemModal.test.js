import { renderHook } from '@testing-library/react-hooks';

import ReceivingRowType from 'consts/receivingRowType';
import useEditReceivingLineItemModal from 'hooks/receiving/v2/useEditReceivingLineItemModal';

const originalLineItem = {
  rowId: 'row-original',
  receiptItemId: 'original',
  quantityReceiving: 0,
  isSplitItem: false,
};

const splitItem = {
  rowId: 'row-split',
  receiptItemId: 'split',
  rowType: ReceivingRowType.SPLIT_ITEM,
  isSplitItem: true,
};

const replacedRow = {
  rowId: 'row-replaced',
  rowType: ReceivingRowType.REPLACED,
  toggleRowId: 'row-toggle',
};

const buildState = (rows) => ({
  entities: rows.reduce((acc, row) => ({ ...acc, [row.rowId]: row }), {}),
  ids: rows.map((row) => row.rowId),
});

const renderModal = (rows) => renderHook(() => useEditReceivingLineItemModal(buildState(rows)));

describe('useEditReceivingLineItemModal', () => {
  describe('getInitialEditModalLineItems()', () => {
    it('should open a line that was never split with just that line', () => {
      const plainRow = { rowId: 'row-1', rowType: null, isSplitItem: false };
      const { result } = renderModal([plainRow]);

      expect(result.current.getInitialEditModalLineItems('row-1')).toEqual([plainRow]);
    });

    it('should open a group with the original line ahead of its split items', () => {
      const visibleOriginal = { ...originalLineItem, rowType: ReceivingRowType.SPLIT_ITEM };
      const toggleRow = {
        rowId: 'row-toggle',
        rowType: ReceivingRowType.TOGGLE,
        // The API does not sort receipt items, so the original can come after a split item.
        splitItemIds: ['row-split', 'row-original'],
        originalLineItem: null,
      };
      const { result } = renderModal([replacedRow, toggleRow, splitItem, visibleOriginal]);

      expect(result.current.getInitialEditModalLineItems('row-replaced'))
        .toEqual([visibleOriginal, splitItem]);
    });

    it('should open a group with the original line the toggle row carries when it has no row', () => {
      const toggleRow = {
        rowId: 'row-toggle',
        rowType: ReceivingRowType.TOGGLE,
        splitItemIds: ['row-split'],
        originalLineItem,
      };
      const { result } = renderModal([replacedRow, toggleRow, splitItem]);

      expect(result.current.getInitialEditModalLineItems('row-replaced'))
        .toEqual([originalLineItem, splitItem]);
    });

    it('should open a dissolved group with the original line the remaining row carries', () => {
      const remainingRow = { ...splitItem, rowType: null, originalLineItem };
      const { result } = renderModal([remainingRow]);

      expect(result.current.getInitialEditModalLineItems('row-split'))
        .toEqual([originalLineItem, remainingRow]);
    });
  });
});
