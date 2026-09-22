import { useCallback } from 'react';

import { useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getHasBinLocationSupport,
  getHasPartialReceivingSupport,
} from 'selectors';

import useTranslate from 'hooks/useTranslate';
import alertMissingBinLocations from 'utils/receiving/alertMissingBinLocations';
import confirmBlankLinesAsZero from 'utils/receiving/confirmBlankLinesAsZero';
import getBlankReceivingRows, { getEditableReceivingRows } from 'utils/receiving/getBlankReceivingRows';
import getRowsMissingBinLocation from 'utils/receiving/getRowsMissingBinLocation';

/**
 * Validation of the receiving step, run before the transition to the check step. Every rule
 * looks at the full line items state, not at the rows the filter shows, so a filtered out line
 * cannot slip through unvalidated.
 *
 * @returns {{ isNextDisabled: boolean, validateBeforeNext: Function }}
 *   `isNextDisabled` - true while no line carries a quantity, there is nothing to review yet.
 *   `validateBeforeNext` - resolves to false when the user decides to stay on the step.
 */
const useReceivingNextValidation = ({ lineItemsState }) => {
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);
  const translate = useTranslate();
  const localeKey = useSelector(getCurrentLocale);
  const editableRows = getEditableReceivingRows(lineItemsState);

  const isNextDisabled = editableRows.length > 0
    && editableRows.every((row) => row.quantityReceiving === null);

  const validateBeforeNext = useCallback(async () => {
    // An edge case if creating a receiving bin is disabled in the config,
    // but we have the bin tracking enabled - in that case, don't allow to proceed to the next step
    // if any of the rows don't have the bin location set
    const rowsMissingBinLocation = hasBinLocationSupport
      ? getRowsMissingBinLocation(lineItemsState)
      : [];
    if (rowsMissingBinLocation.length) {
      alertMissingBinLocations(rowsMissingBinLocation.length);
      return false;
    }

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
  }, [lineItemsState, hasPartialReceivingSupport, hasBinLocationSupport, translate, localeKey]);

  return { isNextDisabled, validateBeforeNext };
};

export default useReceivingNextValidation;
