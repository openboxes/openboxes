const RowSaveStatus = {
  /** The row was saved successfully */
  SAVED: 'SAVED',
  /** The row is newly created or edited and a save request has not yet been issue */
  PENDING: 'PENDING',
  /** A save request is currently in progress. We're awaiting a response from the server */
  SAVING: 'SAVING',
  /** The save request for the row failed */
  ERROR: 'ERROR',
  /** The row failed validation (so a save request was not sent to the server) */
  INVALID: 'INVALID',
};

const UNSAVED_ROW_STATUSES = [RowSaveStatus.ERROR, RowSaveStatus.INVALID];

/**
 * @param row
 * @return {boolean} True if the row is unsaved, either due to a request error or failed validation
 */
export const isRowUnsaved = (row) => UNSAVED_ROW_STATUSES.includes(row?.saveStatus);

export default RowSaveStatus;
