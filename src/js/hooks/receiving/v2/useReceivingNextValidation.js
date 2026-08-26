import { useCallback } from 'react';

import { useSelector } from 'react-redux';
import { getCurrentLocale, getHasPartialReceivingSupport } from 'selectors';

import useTranslate from 'hooks/useTranslate';
import confirmBlankLinesAsZero from 'utils/receiving/confirmBlankLinesAsZero';
import getBlankReceivingRows, { getEditableReceivingRows } from 'utils/receiving/getBlankReceivingRows';

/**
 * Validation of the receiving step, run before the transition to the check step. Both rules
 * look at the full line items state, not at the rows the filter shows, so a filtered out line
 * cannot slip through unvalidated.
 *
 * @returns {{ isNextDisabled: boolean, validateBeforeNext: Function }}
 *   `isNextDisabled` - true while no line carries a quantity, there is nothing to review yet.
 *   `validateBeforeNext` - resolves to false when the user decides to stay on the step.
 */
const useReceivingNextValidation = ({ lineItemsState }) => {
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);
  const translate = useTranslate();
  const localeKey = useSelector(getCurrentLocale);
  const editableRows = getEditableReceivingRows(lineItemsState);

  const isNextDisabled = editableRows.length > 0
    && editableRows.every((row) => row.quantityReceiving == null);

  const validateBeforeNext = useCallback(async () => {
    const blankRows = getBlankReceivingRows(lineItemsState);
    // With partial receiving the lines left blank are simply not part of this receipt, so
    // there is nothing to warn about.
    if (hasPartialReceivingSupport || !blankRows.length) {
      return true;
    }
    // Nothing is written for the blank lines: completing the receipt zeroes them out and cancels
    // their remainder on its own (ReceiptV2Service#completeReceipt), and the check step already
    // shows them as zero. The confirmation only guards the transition.
    return confirmBlankLinesAsZero({ blankRows, translate, localeKey });
  }, [lineItemsState, hasPartialReceivingSupport, translate, localeKey]);

  return { isNextDisabled, validateBeforeNext };
};

export default useReceivingNextValidation;
