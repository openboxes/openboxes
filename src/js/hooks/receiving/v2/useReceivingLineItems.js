import { useCallback } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import { useFieldArray, useForm, useWatch } from 'react-hook-form';
import { useSelector } from 'react-redux';
import { getHasPartialReceivingSupport } from 'selectors';

import { DateFormatDateFns } from 'consts/timeFormat';
import useEditLineItemValidation from 'hooks/receiving/v2/useEditLineItemValidation';
import useEditModalLocationAutofill from 'hooks/receiving/v2/useEditModalLocationAutofill';
import useReceivingLineItemColumns from 'hooks/receiving/v2/useReceivingLineItemColumns';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Form state for the editable "Receiving now" table in the edit modal
 *
 * @param hasPreviousReceipts Whether the shipment being received already has a submitted receipt.
 */
const useReceivingLineItems = ({
  lineItem,
  initialLineItems,
  hasPreviousReceipts,
}) => {
  const translate = useTranslate();
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);

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
    location: item?.location ?? null,
    // Persisted flag distinguishing the original line (false) from split lines (true) - it must
    // come from the backing item, not from whether the row existed when the modal opened.
    // Rows added in the modal are always split lines: addRow/copyToReceiving override this to true.
    isSplitItem: item?.isSplitItem ?? false,
    binLocation: item?.binLocation ?? null,
  });

  // The original line of the shipment item - the row every split line is split off from.
  const originalLineItem = initialLineItems.find((item) => !item.isSplitItem);

  // New rows split the same shipment item line, so they start with the line's product.
  const buildSplitRow = () => ({
    ...buildDefaultRow({ product: originalLineItem?.product ?? lineItem?.product }),
    isSplitItem: true,
  });

  // If a line has already some persisted split items, we don't want to prefill a new split row
  // We want to prefill a new split row with filled product row, only if a line doesn't
  // have any persisted split items yet
  const defaultLineItems = initialLineItems.some((item) => item.isSplitItem)
    ? initialLineItems.map(buildDefaultRow)
    : [...initialLineItems.map(buildDefaultRow), buildSplitRow()];

  const { validationSchema } = useEditLineItemValidation();

  const {
    control, getValues, setValue, reset, handleSubmit, formState: { errors },
  } = useForm({
    mode: 'onBlur',
    defaultValues: { lineItems: defaultLineItems },
    resolver: zodResolver(validationSchema),
  });

  const hasErrors = Object.keys(errors).length > 0;

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
    errors,
  });

  const addRow = () => append(buildSplitRow());

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
  const receivingNow = Number((watchedLineItems ?? []).reduce(
    (sum, item) => sum + (Number(item?.quantityReceiving) || 0),
    0,
  ).toFixed(2));

  const quantityShipped = lineItem?.quantityShipped ?? 0;
  const received = lineItem?.quantityReceived ?? 0;
  const remainingToReceive = Number((quantityShipped - received - receivingNow).toFixed(2));

  // Received card should only be visible for a location with partial receiving or if there is
  // any previous receipt for a shipment
  const showReceived = Boolean(hasPartialReceivingSupport || hasPreviousReceipts);

  const summaryData = [
    {
      title: translate('react.receiving.quantityShipped.label', 'Quantity Shipped'),
      data: quantityShipped,
    },
    ...(showReceived ? [
      {
        title: translate('react.receiving.received.label', 'Received'),
        data: received,
      },
    ] : []),
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
    handleSubmit,
    hasErrors,
  };
};

export default useReceivingLineItems;
