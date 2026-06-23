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
    lineItemsState,
    updateLineItem,
    isShipmentFromPurchaseOrder,
  } = useReceivingActions(view);
  const { columns } = useReceivingColumns({ view, putawayEnabled, isShipmentFromPurchaseOrder });
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
      updateLineItem,
    },
    commentModal,
  };
};

export default useReceivingForm;
