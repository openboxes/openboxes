import { useCallback, useMemo, useState } from 'react';

import {
  computeVisibleShipmentItemIds,
  filterLineItemsState,
} from 'utils/receiving/receivingRowFilter';

// Receiving filter (search + receipt status), applied on the frontend against the full
// line items state. The matching shipment items are snapshotted on submit and only refresh
// on the next submit (not on sort/view reloads), so the row the user is editing does not
// disappear mid-edit.
const useReceivingFilters = ({ lineItemsState }) => {
  const [visibleShipmentItemIds, setVisibleShipmentItemIds] = useState(null);

  const updateFilterParams = useCallback((values) => {
    const params = {
      receiptStatusCodes: (values.receiptStatusCode ?? []).map(({ value }) => value),
      searchTerm: values.q,
    };
    setVisibleShipmentItemIds(computeVisibleShipmentItemIds(lineItemsState, params));
  }, [lineItemsState]);

  const clearFilterParams = useCallback(() => setVisibleShipmentItemIds(null), []);

  // The same `lineItemsState`, but `ids` only has the rows that match the filter.
  // `entities` still has all rows, so callers can find any row by its rowId.
  const visibleLineItemsState = useMemo(
    () => filterLineItemsState(lineItemsState, visibleShipmentItemIds),
    [lineItemsState, visibleShipmentItemIds],
  );

  return { visibleLineItemsState, updateFilterParams, clearFilterParams };
};

export default useReceivingFilters;
