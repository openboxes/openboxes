import { useCallback, useState } from 'react';

import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { getReceivingPutawayEnabled, getReceivingView } from 'selectors';

import { removeReceivingPutawayEnabled } from 'actions';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
import useConfirmReceiptActions from 'hooks/receiving/v2/useConfirmReceiptActions';
import useConfirmReceiptColumns from 'hooks/receiving/v2/useConfirmReceiptColumns';

const useConfirmReceiptForm = () => {
  const { control } = useForm({
    defaultValues: {
      dateDelivered: null,
    },
  });
  const dispatch = useDispatch();
  // The check step renders in the view selected on the receiving step.
  const view = useSelector(getReceivingView);
  const {
    loading, receiptIdRef, lineItemsState, updateLineItemComment,
  } = useConfirmReceiptActions(view);
  const putawayEnabled = useSelector(
    (state) => getReceivingPutawayEnabled(state, receiptIdRef.current),
  );
  const { columns } = useConfirmReceiptColumns({ view, putawayEnabled });
  const commentModal = useCommentModal({ updateLineItemComment });
  // Rows ticked for cancel remaining. Only the selection lives here for now - acting on
  // it is out of scope of OBPIH-7899.
  const [cancelRemainingIds, setCancelRemainingIds] = useState(() => new Set());
  const toggleCancelRemaining = useCallback((rowId) => {
    setCancelRemainingIds((prev) => {
      const next = new Set(prev);
      return next.delete(rowId) ? next : next.add(rowId);
    });
  }, []);

  // Completing the receipt itself (API call) is out of scope for now; the per-receipt
  // state kept in redux is cleaned up here.
  const onCompleteReceipt = useCallback(() => {
    const receiptId = receiptIdRef.current;
    if (!receiptId) {
      return;
    }
    dispatch(removeReceivingPutawayEnabled(receiptId));
  }, [dispatch]);

  return {
    onCompleteReceipt,
    control,
    table: {
      lineItemsState,
      columns,
    },
    loading,
    commentModal,
    cancelRemaining: {
      ids: cancelRemainingIds,
      toggle: toggleCancelRemaining,
    },
  };
};

export default useConfirmReceiptForm;
