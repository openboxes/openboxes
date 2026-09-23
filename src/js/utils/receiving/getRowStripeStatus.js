import RowSaveStatus from 'consts/rowSaveStatus';
import VerticalStripeStatus from 'consts/verticalStripeStatus';

/**
 * The VerticalStripeIndicator status for a receiving row:
 * - ERROR if row has an error, including when the last save operation on the row failed
 * - null (no stripe) otherwise
 */
const getRowStripeStatus = (item) => {
  if (item?.saveStatus === RowSaveStatus.ERROR) {
    return VerticalStripeStatus.ERROR;
  }
  // We opt to display nothing in the success case so we return the null status for any non-error.
  return null;
};

export default getRowStripeStatus;
