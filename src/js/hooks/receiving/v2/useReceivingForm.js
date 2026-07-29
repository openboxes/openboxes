import { useState } from 'react';

import { ReceivingView } from 'consts/receivingViewOptions';
import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingBinLocations from 'hooks/receiving/v2/useReceivingBinLocations';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';
import useTableLocationAutofill from 'hooks/receiving/v2/useTableLocationAutofill';

const useReceivingForm = () => {
  const [view, setView] = useState(ReceivingView.TABLE);
  const [putawayEnabled, setPutawayEnabled] = useState(false);
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
  } = useReceivingActions(view);
  useReceivingBinLocations();
  const { onLocationAutofill } = useTableLocationAutofill({
    lineItemsState,
    updateLineItems,
  });
  const { columns } = useReceivingColumns({ view, putawayEnabled });

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
    },
  };
};

export default useReceivingForm;
