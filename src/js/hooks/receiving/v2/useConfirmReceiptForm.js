import { useEffect, useMemo } from 'react';

import { useForm, useWatch } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getReceivingDateDelivered, getReceivingView } from 'selectors';

import { updateReceivingDateDelivered } from 'actions';
import { DateFormatDateFns } from 'consts/timeFormat';
import useCancelRemaining from 'hooks/receiving/v2/useCancelRemaining';
import useCommentModal from 'hooks/receiving/v2/useCommentModal';
import useConfirmReceiptActions from 'hooks/receiving/v2/useConfirmReceiptActions';
import useConfirmReceiptColumns from 'hooks/receiving/v2/useConfirmReceiptColumns';
import useConfirmReceiptSaveActions from 'hooks/receiving/v2/useConfirmReceiptSaveActions';
import useReceivingFilters from 'hooks/receiving/v2/useReceivingFilters';
import { formatDateToString } from 'utils/dateUtils';
import getOptionalColumnsVisibility from 'utils/receiving/getOptionalColumnsVisibility';
import hasAnyPreviousReceipt from 'utils/receiving/hasAnyPreviousReceipt';

const currentDateTime = () => formatDateToString({
  date: new Date(),
  dateFormat: DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS,
});

const useConfirmReceiptForm = () => {
  const dispatch = useDispatch();
  const { shipmentId } = useParams();
  // Going back to the receiving step unmounts this one, so the delivery date lives in the store:
  // read here to seed the form, written back on every change.
  const storedDateDelivered = useSelector(
    (state) => getReceivingDateDelivered(state, shipmentId),
  );
  const { control, handleSubmit } = useForm({
    defaultValues: {
      dateDelivered: storedDateDelivered ?? currentDateTime(),
    },
  });
  const dateDelivered = useWatch({ control, name: 'dateDelivered' });
  useEffect(() => {
    dispatch(updateReceivingDateDelivered(shipmentId, dateDelivered));
  }, [dateDelivered]);

  // The check step renders in the view selected on the receiving step.
  const view = useSelector(getReceivingView);
  const {
    loading, receiptIdRef, lineItemsState, updateLineItemComment,
  } = useConfirmReceiptActions(view);
  const hasPreviousReceipts = hasAnyPreviousReceipt(lineItemsState);
  // Optional columns are read from the full state, so filtering the table down to rows
  // without a lot or a recipient does not collapse their columns.
  const columnsVisibility = useMemo(
    () => getOptionalColumnsVisibility(lineItemsState),
    [lineItemsState],
  );
  const { columns } = useConfirmReceiptColumns({
    view,
    hasPreviousReceipts,
    ...columnsVisibility,
  });
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
