import { useCallback } from 'react';

import _ from 'lodash';
import { useFieldArray, useForm, useWatch } from 'react-hook-form';

import { DateFormatDateFns } from 'consts/timeFormat';
import useEditModalLocationAutofill from 'hooks/receiving/v2/useEditModalLocationAutofill';
import useReceivingLineItemColumns from 'hooks/receiving/v2/useReceivingLineItemColumns';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Form state for the editable "Receiving now" table in the edit modal
 */
const useReceivingLineItems = ({
  lineItem,
  initialLineItems,
}) => {
  const translate = useTranslate();

  const buildDefaultRow = (item) => ({
    // Stable id so rows can be removed by identity, not index.
    rowId: _.uniqueId('row-'),
    // Existing receipt item backing this row - on save it is updated instead of created.
    receiptItemId: item?.receiptItemId ?? null,
    product: item?.product ?? null,
    lotNumber: item?.lotNumber ?? '',
    expirationDate: formatDateToString({
      date: item?.expirationDate,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    }) ?? '',
    recipient: item?.recipient ?? null,
    quantityReceiving: item?.quantityReceiving ?? '',
    binLocation: item?.binLocation ?? null,
    // Rows added in the modal (not the original shipment item line) are marked as split lines.
    isSplitItem: false,
  });

  const {
    control, getValues, setValue, reset,
  } = useForm({
    defaultValues: { lineItems: initialLineItems.map(buildDefaultRow) },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'lineItems',
  });

  const removeRow = useCallback((rowId) => {
    const index = getValues('lineItems').findIndex((item) => item.rowId === rowId);
    if (index !== -1) {
      remove(index);
    }
  }, [getValues, remove]);

  const { onLocationAutofill } = useEditModalLocationAutofill({
    getValues,
    setValue,
  });

  const { columns } = useReceivingLineItemColumns({
    control,
    removeRow,
    onLocationAutofill,
  });

  // New rows split the same shipment item line, so they start with the line's product.
  const addRow = () => append({
    ...buildDefaultRow({ product: lineItem?.product }),
    isSplitItem: true,
  });

  const copyToReceiving = useCallback((receivedItem) => append({
    rowId: _.uniqueId('row-'),
    receiptItemId: null,
    product: receivedItem.product ?? null,
    lotNumber: receivedItem.lotNumber ?? '',
    expirationDate: receivedItem.expirationDate ?? '',
    recipient: receivedItem.recipient ?? null,
    quantityReceiving: receivedItem.quantityReceived ?? '',
    binLocation: receivedItem.binLocation ?? null,
    isSplitItem: true,
  }), [append]);

  const revertToOriginal = () => reset();

  // Current form rows, read on demand (e.g. when building the save payload).
  const getLineItems = useCallback(() => getValues('lineItems'), [getValues]);

  const watchedLineItems = useWatch({ control, name: 'lineItems' });
  const receivingNow = (watchedLineItems ?? []).reduce(
    (sum, item) => sum + (Number(item?.quantityReceiving) || 0),
    0,
  );

  const quantityShipped = lineItem?.quantityShipped ?? 0;
  const received = lineItem?.quantityReceived ?? 0;
  const remainingToReceive = quantityShipped - received - receivingNow;

  const summaryData = [
    {
      title: translate('react.receiving.quantityShipped.label', 'Quantity Shipped'),
      data: quantityShipped,
    },
    {
      title: translate('react.receiving.received.label', 'Received'),
      data: received,
    },
    {
      title: translate('react.receiving.receivingNow.label', 'Receiving Now'),
      data: receivingNow,
    },
    {
      title: translate('react.receiving.remainingToReceive.label', 'Remaining to Receive'),
      data: remainingToReceive,
    },
  ];

  return {
    fields,
    columns,
    addRow,
    copyToReceiving,
    revertToOriginal,
    receivingNow,
    summaryData,
    getLineItems,
  };
};

export default useReceivingLineItems;
