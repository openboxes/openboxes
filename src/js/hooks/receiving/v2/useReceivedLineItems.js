import { useCallback } from 'react';

import _ from 'lodash';
import { useFieldArray, useForm } from 'react-hook-form';

import { DateFormatDateFns } from 'consts/timeFormat';
import useReceivedLineItemColumns from 'hooks/receiving/v2/useReceivedLineItemColumns';
import { formatDateToString } from 'utils/dateUtils';

// One row for the read-only "Received" table. The expiration date is pre-formatted
// to the same string the disabled DateField renders in the editable table.
const buildReceivedRow = (lineItem) => ({
  rowId: _.uniqueId('received-'),
  product: lineItem?.product ?? null,
  lotNumber: lineItem?.lotNumber ?? '',
  expirationDate: formatDateToString({
    date: lineItem?.expirationDate,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
  }) ?? '',
  recipient: lineItem?.recipient ?? null,
  quantityReceived: lineItem?.quantityReceived ?? '',
  location: lineItem?.location ?? null,
});

/**
 * Read-only "Received" table shown in the edit modal: the records already received
 * for the line. It owns its own react-hook-form state so the disabled fields render
 * values, kept separate from the editable "Receiving now" form (`useReceivingLineItems`).
 *
 * `onCopyToReceive` is called with the received row when its "Copy to receive"
 * action fires, so the caller can append it to the editable table.
 */
const useReceivedLineItems = (lineItem, { onCopyToReceive } = {}) => {
  // TODO: seeded from the single edited line until per-receipt records are exposed.
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

  return { receivedItems: fields, columns };
};

export default useReceivedLineItems;
