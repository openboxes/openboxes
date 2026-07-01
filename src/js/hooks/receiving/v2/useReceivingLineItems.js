import { useCallback } from 'react';

import _ from 'lodash';
import { useFieldArray, useForm } from 'react-hook-form';

import { DateFormatDateFns } from 'consts/timeFormat';
import useReceivingLineItemColumns from 'hooks/receiving/v2/useReceivingLineItemColumns';
import { formatDateToString } from 'utils/dateUtils';

const buildDefaultRow = (lineItem) => ({
  // Stable custom id so a row can be removed by identity rather than array index.
  rowId: _.uniqueId('row-'),
  product: lineItem?.product ?? null,
  lotNumber: lineItem?.lotNumber ?? '',
  expirationDate: formatDateToString({
    date: lineItem?.expirationDate,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
  }) ?? '',
  recipient: lineItem?.recipient ?? null,
  quantityReceiving: lineItem?.quantityReceiving ?? '',
  location: lineItem?.location ?? null,
});

/**
 * react-hook-form state for the editable "Receiving now" table in the edit modal.
 * Seeds one row from the edited line item. `defaultValues` is captured on mount,
 * so the modal must be mounted fresh per open (see ReceivingTable) for the seed
 * to reflect the line.
 */
const useReceivingLineItems = (lineItem) => {
  const { control, getValues, reset } = useForm({
    defaultValues: { lineItems: [buildDefaultRow(lineItem)] },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'lineItems',
  });

  // Remove by the row's stable rowId (not array index) so the right row is dropped.
  // Kept stable (reads the live values via getValues instead of closing over `fields`)
  // so the columns don't rebuild on every add/remove, which would desync cell state.
  const removeRow = useCallback((rowId) => {
    const index = getValues('lineItems').findIndex((item) => item.rowId === rowId);
    if (index !== -1) {
      remove(index);
    }
  }, [getValues, remove]);

  const { columns } = useReceivingLineItemColumns({ control, removeRow });

  // `buildDefaultRow()` with no line item yields a blank row.
  const addRow = () => append(buildDefaultRow());

  // Append a "Receiving now" row seeded from a received record (the "Copy to
  // receive" action on the read-only Received table), with a blank quantity. The
  // received fields are already shaped/formatted, so they're copied as-is.
  const copyToReceiving = useCallback((receivedItem) => append({
    rowId: _.uniqueId('row-'),
    product: receivedItem.product ?? null,
    lotNumber: receivedItem.lotNumber ?? '',
    expirationDate: receivedItem.expirationDate ?? '',
    recipient: receivedItem.recipient ?? null,
    quantityReceiving: '',
    location: receivedItem.location ?? null,
  }), [append]);

  // Restore the form to the seed captured on mount (the original line item).
  const revertToOriginal = () => reset();

  return {
    control, fields, columns, addRow, copyToReceiving, revertToOriginal,
  };
};

export default useReceivingLineItems;
