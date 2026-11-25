import { isRowUnsaved } from 'consts/rowSaveStatus';
import VerticalStripeStatus from 'consts/verticalStripeStatus';

/**
 * The VerticalStripeIndicator status for a receiving row:
 * - ERROR if the row is unsaved, meaning it failed validation, or its last save operation failed
 * - null (no stripe) otherwise
 */
const getRowStripeStatus = (item) => {
  if (isRowUnsaved(item)) {
    return VerticalStripeStatus.ERROR;
  }
  // We opt to display nothing in the success case so we return the null status for any non-error.
  return null;
};

export default getRowStripeStatus;
