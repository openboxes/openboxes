import { useCallback } from 'react';

import ReceivingRowType from 'consts/receivingRowType';
import useBinLocationAutofill from 'hooks/useBinLocationAutofill';

/**
 * Location autofill of the main receiving table, triggered from the column header dropdown,
 * plus the location picked on a pack level separator row (packing list view). Both apply
 * to the normalized line items state.
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

  const { onLocationAutofill } = useBinLocationAutofill({
    getRows: getAutofillableItems,
    getRowBinLocation,
    updateLineItems,
  });

  // The location picked on a pack level separator row is applied to all rows of its group.
  const onPackLevelLocationChange = useCallback((binLocation, separatorId) => {
    updateLineItems(getAutofillableItems(separatorId).reduce((acc, item) => {
      acc[item.rowId] = { binLocation };
      return acc;
    }, {}));
  }, [getAutofillableItems, updateLineItems]);

  return { onLocationAutofill, onPackLevelLocationChange };
};

export default useTableLocationAutofill;
