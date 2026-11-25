import { denormalizeData } from 'utils/normalizationUtils';

const allRows = (lineItemsState) => denormalizeData(lineItemsState)
  .flatMap((row) => [row, row?.originalLineItem])
  .filter(Boolean);

/**
 * Visibility of the optional columns of the receiving tables: a field no row of the shipment
 * fills does not take up space. Derived purely from the rows, so adding a lot or a recipient
 * (edit modal, another tab, a rolled back shipment) brings its column back on the next summary
 * fetch, and clearing the last value hides it again.
 */
const getOptionalColumnsVisibility = (lineItemsState) => {
  const rows = allRows(lineItemsState);
  const showLotNumber = rows.some((row) => Boolean(row.lotNumber));

  return {
    showLotNumber,
    // An expiration date belongs to a lot, so with no lot in the shipment it has nothing
    // to hang on and its column goes away together with the lot one.
    showExpirationDate: showLotNumber,
    showRecipient: rows.some((row) => Boolean(row.recipient)),
    // Pack level 1 is the container - without it there is no pack level 2 either.
    showPackLevel: rows.some((row) => Boolean(row.container)),
  };
};

export default getOptionalColumnsVisibility;
