import { useState } from 'react';

import { ReceivingView } from 'consts/receivingViewOptions';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
import useLocationAutofill from 'hooks/receiving/v2/useLocationAutofill';
import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingBinLocations from 'hooks/receiving/v2/useReceivingBinLocations';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';

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
  } = useReceivingActions(view);
  const { binLocations, receivingBin } = useReceivingBinLocations(receiptId);
  const { onLocationAutofill } = useLocationAutofill({
    lineItemsState,
    updateLineItems,
    binLocations,
    receivingBin,
  });
  const { columns } = useReceivingColumns({ view, putawayEnabled, binLocations });
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
    binLocations,
    receivingBin,
    actions: {
      loading,
      receiptId,
      updateLineItem,
      autofillQuantities,
      removeSplitItem,
      loadReceipt,
      onSaveAndExit,
      onLocationAutofill,
    },
    commentModal,
  };
};

export default useReceivingForm;
