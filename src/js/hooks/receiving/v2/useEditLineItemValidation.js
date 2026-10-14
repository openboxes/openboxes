import { isBefore, parse } from 'date-fns';
import _ from 'lodash';
import { z } from 'zod';

import { DateFormatDateFns } from 'consts/timeFormat';
import useQuantityReceivingValidation from 'hooks/receiving/v2/useQuantityReceivingValidation';
import useTranslate from 'hooks/useTranslate';

export const EditLineItemErrorMessage = {
  LOT_NUMBER_REQUIRED: {
    id: 'react.receiving.error.lotNumberRequired.label',
    defaultMessage: 'Lot or serial number required for this product',
  },
  EXPIRATION_DATE_REQUIRED: {
    id: 'react.receiving.error.expirationDateRequired.label',
    defaultMessage: 'Expiration date required for this product',
  },
  EXPIRATION_DATE_WITHOUT_LOT: {
    id: 'react.receiving.error.expirationDateWithoutLot.label',
    defaultMessage: 'Cannot enter an expiration date without a lot number',
  },
  DUPLICATED_LINE: {
    id: 'react.receiving.error.duplicatedLine.label',
    defaultMessage: 'Duplicate rows for this inventory item',
  },
  DIFFERENT_EXPIRATION_DATES: {
    id: 'react.receiving.error.differentExpirationDates.label',
    defaultMessage: 'You have entered two different expiration dates for the same lot number',
  },
  INVALID_EXPIRATION_DATE: {
    id: 'react.stockMovement.error.invalidDate.label',
    defaultMessage: 'This date is invalid. Please enter a date after 2000.',
  },
};

const MIN_EXPIRATION_DATE = new Date(2000, 0, 1);

// The date picker only accepts valid dates, but lets through the ones before 2000.
const isExpirationDateAfterMinimum = (date) => !date
  || !isBefore(parse(date, DateFormatDateFns.DD_MMM_YYYY, new Date()), MIN_EXPIRATION_DATE);

// Uniqueness of a line is based on all of these fields, so a duplicate is reported on each of them.
const DUPLICATE_KEY_FIELDS = ['product', 'lotNumber', 'recipient', 'binLocation'];

const getLotNumber = (row) => row?.lotNumber?.trim() || '';

// Blank values are a part of the key too, so two rows of the same product with a blank lot,
// a blank recipient and the same bin are also duplicates.
const getDuplicateKey = (row) => [
  row.product.id,
  getLotNumber(row),
  row.recipient?.id ?? '',
  row.binLocation?.id ?? '',
].join('|');

/**
 * Zod schema for the editable rows in the edit modal.
 *
 * @returns {{ validationSchema: z.ZodType }}
 */
const useEditLineItemValidation = () => {
  const translate = useTranslate();
  const translateMessage = ({ id, defaultMessage }) => translate(id, defaultMessage);
  const { requiredQuantityReceivingSchema } = useQuantityReceivingValidation();

  const checkLotAndExpiryRequired = (row, ctx) => {
    if (!row.isSplitItem || !row.product?.lotAndExpiryControl) {
      return;
    }

    if (!getLotNumber(row)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: translateMessage(EditLineItemErrorMessage.LOT_NUMBER_REQUIRED),
        path: ['lotNumber'],
      });
    }

    if (!row.expirationDate) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: translateMessage(EditLineItemErrorMessage.EXPIRATION_DATE_REQUIRED),
        path: ['expirationDate'],
      });
    }
  };

  const checkExpirationDateWithoutLot = (row, ctx) => {
    if (!row.expirationDate || getLotNumber(row)) {
      return;
    }

    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: translateMessage(EditLineItemErrorMessage.EXPIRATION_DATE_WITHOUT_LOT),
      path: ['expirationDate'],
    });
  };

  const checkDuplicatedLines = (lineItems, ctx) => {
    const message = translateMessage(EditLineItemErrorMessage.DUPLICATED_LINE);
    const rowsWithProduct = lineItems.filter((row) => row.product?.id);
    Object.values(_.groupBy(rowsWithProduct, getDuplicateKey))
      .filter((group) => group.length > 1)
      .flat()
      .forEach((row) => DUPLICATE_KEY_FIELDS.forEach((field) => ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message,
        path: [lineItems.indexOf(row), field],
      })));
  };

  const checkDifferentExpirationDatesForTheSameLot = (lineItems, ctx) => {
    const message = translateMessage(EditLineItemErrorMessage.DIFFERENT_EXPIRATION_DATES);
    const rowsWithLot = lineItems.filter((row) => row.product?.id && getLotNumber(row));
    Object.values(_.groupBy(rowsWithLot, (row) => `${row.product.id}|${getLotNumber(row)}`))
      .filter((group) => _.uniqBy(group, (row) => row.expirationDate || '').length > 1)
      .flat()
      .forEach((row) => ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message,
        path: [lineItems.indexOf(row), 'expirationDate'],
      }));
  };

  // Map the errors by their associated field name
  const lineItemSchema = z.object({
    quantityReceiving: requiredQuantityReceivingSchema,
    expirationDate: z.string()
      .nullish()
      .refine(
        isExpirationDateAfterMinimum,
        translateMessage(EditLineItemErrorMessage.INVALID_EXPIRATION_DATE),
      ),
  })
    // By default z.object() strips the keys missing from the schema, and the refinements receive
    // the parsed row - without passthrough they wouldn't see the product, lot, recipient, bin etc.
    .passthrough()
    .superRefine(checkLotAndExpiryRequired)
    .superRefine(checkExpirationDateWithoutLot);

  const validationSchema = z.object({
    lineItems: z.array(lineItemSchema)
      .superRefine(checkDuplicatedLines)
      .superRefine(checkDifferentExpirationDatesForTheSameLot),
  });

  return { validationSchema };
};

export default useEditLineItemValidation;
