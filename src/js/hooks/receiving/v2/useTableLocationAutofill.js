import { useCallback } from 'react';

import ReceivingRowType from 'consts/receivingRowType';
import useBinLocationAutofill from 'hooks/useBinLocationAutofill';

/**
 * Location autofill of the main receiving table, triggered from the column header dropdown
 * or from a pack level separator row (packing list view). Applies the shared autofill to
 * the normalized line items state.
 */
const useTableLocationAutofill = ({
  lineItemsState,
  updateLineItems,
}) => {
  const getRowBinLocation = useCallback((item) => item.binLocation, []);
  // Rows the autofill applies to, in display order: editable line items only
  // (separator, replaced and toggle rows plus fully received lines are skipped).
  // A separator id narrows the autofill down to the rows of its pack level group.
  const getAutofillableItems = useCallback((separatorId) => lineItemsState.ids
    .filter((id) => {
      const item = lineItemsState.entities[id];
      return item
        && (!separatorId || item.separatorId === separatorId)
        && item.rowType !== ReceivingRowType.REPLACED
        && item.rowType !== ReceivingRowType.TOGGLE
        && !item.isCompleted;
    })
    .map((id) => lineItemsState.entities[id]), [lineItemsState]);

  return useBinLocationAutofill({
    getRows: getAutofillableItems,
    getRowBinLocation,
    updateLineItems,
  });
};

export default useTableLocationAutofill;
