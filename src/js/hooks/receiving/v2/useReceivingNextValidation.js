import { useCallback, useMemo } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getHasBinLocationSupport,
  getHasPartialReceivingSupport,
} from 'selectors';

import useReceivingLineItemValidation from 'hooks/receiving/v2/useReceivingLineItemValidation';
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
 * @returns {{ isNextDisabled: boolean, validateBeforeNext: Function, lineItemErrors: Object }}
 *   `isNextDisabled` - true while no line carries a quantity, or any line fails validation.
 *   `validateBeforeNext` - resolves to false when the user decides to stay on the step.
 *   `lineItemErrors` - error messages of the invalid lines, keyed by row id and then by field,
 *                      e.g. { 'row-2': { quantityReceiving: 'Decimals are not allowed' } }.
 */
const useReceivingNextValidation = ({ lineItemsState }) => {
  const hasPartialReceivingSupport = useSelector(getHasPartialReceivingSupport);
  const hasBinLocationSupport = useSelector(getHasBinLocationSupport);
  const translate = useTranslate();
  const localeKey = useSelector(getCurrentLocale);
  const { lineItemSchema } = useReceivingLineItemValidation();
  const editableRows = getEditableReceivingRows(lineItemsState);

  // TODO: support rendering multiple errors in the tooltip of a TableCell
  // Cells only show a single error so reduce down to only the first message of each field.
  const lineItemErrors = useMemo(() => editableRows.reduce((errors, row) => {
    const { success, error } = lineItemSchema.safeParse(row);
    if (success) {
      return errors;
    }
    const fieldErrors = _.mapValues(error.flatten().fieldErrors, ([message]) => message);
    return { ...errors, [row.rowId]: fieldErrors };
  }, {}), [lineItemsState, translate]);

  const isNextDisabled = Object.keys(lineItemErrors).length > 0 || (editableRows.length > 0
    && editableRows.every((row) => row.quantityReceiving === null));

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

  return { isNextDisabled, validateBeforeNext, lineItemErrors };
};

export default useReceivingNextValidation;
