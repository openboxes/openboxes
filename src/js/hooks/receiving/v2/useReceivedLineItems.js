import { useCallback } from 'react';

import _ from 'lodash';
import { useFieldArray, useForm } from 'react-hook-form';

import { DateFormatDateFns } from 'consts/timeFormat';
import useReceivedLineItemColumns from 'hooks/receiving/v2/useReceivedLineItemColumns';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Form state for the read-only "Received" table in the edit modal
 */
const useReceivedLineItems = (lineItem, { onCopyToReceive } = {}) => {
  const buildReceivedRow = (item) => ({
    rowId: _.uniqueId('received-'),
    product: item?.product ?? null,
    lotNumber: item?.lotNumber ?? '',
    expirationDate: formatDateToString({
      date: item?.expirationDate,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    }) ?? '',
    recipient: item?.recipient ?? null,
    quantityReceived: item?.quantityReceived ?? '',
    location: item?.location ?? null,
  });

  const { control } = useForm({
    defaultValues: {
      receivedItems: Number(lineItem?.quantityReceived) > 0 ? [buildReceivedRow(lineItem)] : [],
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
