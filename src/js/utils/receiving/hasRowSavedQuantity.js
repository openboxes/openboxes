import RowSaveStatus from 'consts/rowSaveStatus';

/**
 * Whether a receiving row should show the saved-changes stripe: its quantity is persisted and
 * no newer edit is pending (an edit flips saveStatus to PENDING/SAVING, hiding the stripe until
 * the save settles). `initialQuantityReceiving` is the baseline moved forward on every
 * successful save, so it mirrors exactly what the server holds - a row cleared and saved as
 * empty shows no stripe even though its receipt item still exists.
 */
const hasRowSavedQuantity = (item) => item?.saveStatus === RowSaveStatus.SAVED
  && item?.initialQuantityReceiving != null;

export default hasRowSavedQuantity;
