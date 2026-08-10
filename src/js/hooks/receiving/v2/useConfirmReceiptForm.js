import { useForm } from 'react-hook-form';
import { useSelector } from 'react-redux';
import { getReceivingPutawayEnabled, getReceivingView } from 'selectors';

import useCancelRemaining from 'hooks/receiving/v2/useCancelRemaining';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
import useConfirmReceiptActions from 'hooks/receiving/v2/useConfirmReceiptActions';
import useConfirmReceiptColumns from 'hooks/receiving/v2/useConfirmReceiptColumns';
import useConfirmReceiptSaveActions from 'hooks/receiving/v2/useConfirmReceiptSaveActions';
import useReceivingFilters from 'hooks/receiving/v2/useReceivingFilters';

const useConfirmReceiptForm = () => {
  const { control, handleSubmit } = useForm({
    defaultValues: {
      dateDelivered: null,
    },
  });
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
  const {
    visibleLineItemsState,
    updateFilterParams,
    clearFilterParams,
  } = useReceivingFilters({ lineItemsState });
  // Cancel all remaining only covers the rows the filter shows
  const cancelRemaining = useCancelRemaining({ lineItemsState: visibleLineItemsState });
  const { onSaveAndExit, onCompleteReceipt } = useConfirmReceiptSaveActions({
    receiptIdRef,
    itemsToComplete: cancelRemaining.itemsToComplete,
  });

  return {
    // Submitting through the form runs the delivery date validation first, so an empty date
    // blocks the completion instead of reaching the API.
    onCompleteReceipt: handleSubmit(onCompleteReceipt),
    onSaveAndExit,
    control,
    table: {
      lineItemsState: visibleLineItemsState,
      columns,
    },
    lineItemsState,
    filters: {
      updateFilterParams,
      clearFilterParams,
    },
    loading,
    commentModal,
    cancelRemaining,
  };
};

export default useConfirmReceiptForm;
