import { useCallback } from 'react';

import _ from 'lodash';
import { useFieldArray, useForm, useWatch } from 'react-hook-form';

import { DateFormatDateFns } from 'consts/timeFormat';
import useReceivingLineItemColumns from 'hooks/receiving/v2/useReceivingLineItemColumns';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Form state for the editable "Receiving now" table in the edit modal
 */
const useReceivingLineItems = (lineItem) => {
  const translate = useTranslate();

  const buildDefaultRow = (item) => ({
    // Stable id so rows can be removed by identity, not index.
    rowId: _.uniqueId('row-'),
    product: item?.product ?? null,
    lotNumber: item?.lotNumber ?? '',
    expirationDate: formatDateToString({
      date: item?.expirationDate,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    }) ?? '',
    recipient: item?.recipient ?? null,
    quantityReceiving: item?.quantityReceiving ?? '',
    location: item?.location ?? null,
  });

  const { control, getValues, reset } = useForm({
    defaultValues: { lineItems: [buildDefaultRow(lineItem)] },
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

  const { columns } = useReceivingLineItemColumns({ control, removeRow });

  const addRow = () => append(buildDefaultRow());

  const copyToReceiving = useCallback((receivedItem) => append({
    rowId: _.uniqueId('row-'),
    product: receivedItem.product ?? null,
    lotNumber: receivedItem.lotNumber ?? '',
    expirationDate: receivedItem.expirationDate ?? '',
    recipient: receivedItem.recipient ?? null,
    quantityReceiving: '',
    location: receivedItem.location ?? null,
  }), [append]);

  const revertToOriginal = () => reset();

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
    fields, columns, addRow, copyToReceiving, revertToOriginal, receivingNow, summaryData,
  };
};

export default useReceivingLineItems;
