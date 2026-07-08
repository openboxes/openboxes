import { useState } from 'react';

import { ReceivingView } from 'consts/receivingViewOptions';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
import useReceivingActions from 'hooks/receiving/v2/useReceivingActions';
import useReceivingColumns from 'hooks/receiving/v2/useReceivingColumns';

const useReceivingForm = () => {
  const [view, setView] = useState(ReceivingView.TABLE);
  const [putawayEnabled, setPutawayEnabled] = useState(false);
  const {
    loading,
    receiptId,
    lineItemsState,
    updateLineItem,
    removeSplitItem,
    getInitialLineItems,
    loadReceipt,
    onSaveAndExit,
  } = useReceivingActions(view);
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
      removeSplitItem,
      getInitialLineItems,
      loadReceipt,
      onSaveAndExit,
    },
    commentModal,
  };
};

export default useReceivingForm;
