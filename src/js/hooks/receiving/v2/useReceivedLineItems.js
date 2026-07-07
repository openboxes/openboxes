import { useCallback } from 'react';

import _ from 'lodash';
import { useFieldArray, useForm } from 'react-hook-form';

import { DateFormatDateFns } from 'consts/timeFormat';
import useReceivedLineItemColumns from 'hooks/receiving/v2/useReceivedLineItemColumns';
import { formatDateToString } from 'utils/dateUtils';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';

/**
 * Form state for the read-only "Received" table in the edit modal.
 * Rows are built from receipt items of already submitted receipts - items saved
 * on the pending receipt are not received yet, so they never show up here.
 */
const useReceivedLineItems = (lineItem, { onCopyToReceive } = {}) => {
  const buildReceivedRow = (item) => ({
    rowId: _.uniqueId('received-'),
    product: item?.productLot?.product ?? lineItem?.product ?? null,
    lotNumber: item?.productLot?.lotNumber ?? '',
    expirationDate: formatDateToString({
      date: item?.productLot?.expirationDate,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    }) ?? '',
    recipient: mapToFormSelectOption(item?.recipient),
    quantityReceived: item?.quantityReceived ?? '',
    location: mapToFormSelectOption(item?.binLocation),
  });

  const { control } = useForm({
    defaultValues: {
      receivedItems: (lineItem?.previousReceiptItems ?? [])
        .filter((item) => Number(item?.quantityReceived) > 0)
        .map(buildReceivedRow),
    },
  });

  const { fields } = useFieldArray({ control, name: 'receivedItems' });

  const copyToReceive = useCallback((rowId) => {
    const item = fields.find((received) => received.rowId === rowId);
    if (item) {
      onCopyToReceive?.(item);
    }
  }, [fields, onCopyToReceive]);

  const { columns } = useReceivedLineItemColumns({ control, copyToReceive });

  const totalReceived = fields.reduce(
    (sum, item) => sum + (Number(item?.quantityReceived) || 0),
    0,
  );

  return { receivedItems: fields, columns, totalReceived };
};

export default useReceivedLineItems;
