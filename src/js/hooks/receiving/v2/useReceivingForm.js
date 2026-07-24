import { useEffect, useRef, useState } from 'react';

import { ReceivingView } from 'consts/receivingViewOptions';
import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingBinLocations from 'hooks/receiving/v2/useReceivingBinLocations';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';
import useTableLocationAutofill from 'hooks/receiving/v2/useTableLocationAutofill';
import useTableSorting from 'hooks/useTableSorting';

const useReceivingForm = () => {
  const [view, setView] = useState(ReceivingView.TABLE);
  const [putawayEnabled, setPutawayEnabled] = useState(false);
  const {
    sortableProps, sort, order, resetSort,
  } = useTableSorting();
  const {
    loading,
    receiptId,
    lineItemsState,
    updateLineItem,
    updateLineItems,
    updateLineItemComment,
    autofillQuantities,
    removeSplitItem,
    loadReceipt,
    onSaveAndExit,
    flush,
    autosaveStatus,
    updateFilterParams,
  } = useReceivingActions({ view, sort, sortOrder: order });
  useReceivingBinLocations();
  const { onLocationAutofill } = useTableLocationAutofill({
    lineItemsState,
    updateLineItems,
  });
  // Auto-enable once when a reopened receipt has at least one row with a saved bin location,
  // so the column is visible.
  const putawayInitialized = useRef(false);
  useEffect(() => {
    if (putawayInitialized.current || !lineItemsState?.ids?.length) {
      return;
    }
    putawayInitialized.current = true;
    if (Object.values(lineItemsState.entities).some((item) => item?.binLocation)) {
      setPutawayEnabled(true);
    }
  }, [lineItemsState]);
  const { columns } = useReceivingColumns({
    view,
    putawayEnabled,
    sortableProps,
    sort,
    order,
  });

  return {
    view,
    setView,
    putawayEnabled,
    setPutawayEnabled,
    table: {
      lineItemsState,
      columns,
    },
    actions: {
      loading,
      receiptId,
      updateLineItem,
      updateLineItemComment,
      autofillQuantities,
      removeSplitItem,
      loadReceipt,
      onSaveAndExit,
      flush,
      onLocationAutofill,
      autosaveStatus,
      resetSort,
      updateFilterParams,
    },
  };
};

export default useReceivingForm;
