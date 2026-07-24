import { useState } from 'react';

import { ReceivingView } from 'consts/receivingViewOptions';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
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
  const commentModal = useCommentModal();
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
      autofillQuantities,
      removeSplitItem,
      loadReceipt,
      onSaveAndExit,
      flush,
      onLocationAutofill,
      autosaveStatus,
    },
    commentModal,
  };
};

export default useReceivingForm;
