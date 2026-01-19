import { isValid, parseISO } from 'date-fns';
import _ from 'lodash';
import { z } from 'zod';

import { NEW_ROW } from 'consts/cycleCount';
import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString } from 'utils/dateUtils';

// Normalize date to 'MM/DD/YYYY' format for comparison
const normalizeDate = (raw) => {
  if (!raw) {
    return null;
  }

  const isoDate = parseISO(raw);

  if (isValid(isoDate)) {
    return formatDateToString({
      date: isoDate,
      dateFormat: DateFormatDateFns.MM_DD_YYYY,
    });
  }

  return raw;
};

// 1. The same lot in different bins should be accepted
// 2. For the same lot in the same bin in multiple rows:
//   - Should be a duplicate rows validation error
const checkDuplicatedLotNumber = (items, ctx) => {
  const grouped = _.groupBy(
    items,
    (item) => `${item?.binLocation?.id}-${item?.inventoryItem?.lotNumber?.trim() || ''}`,
  );

  Object.values(grouped).forEach((group) => {
    if (group.length > 1) {
      group.forEach((item) => {
        const index = items.indexOf(item);
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Duplicate rows for this inventory item.',
          path: ['cycleCountItems', index, 'binLocation'],
        });
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Duplicate rows for this inventory item.',
          path: ['cycleCountItems', index, 'inventoryItem.lotNumber'],
        });
      });
    }
  });
};

// 3. Same lot with different exp, in different bins:
//   - Should be an error related to valid exp
const checkDifferentExpirationDatesForTheSameLot = (items, ctx) => {
  const grouped = _.groupBy(
    items,
    (item) => item?.inventoryItem?.lotNumber?.trim() || '',
  );

  Object.values(grouped).forEach((group) => {
    const dates = group.map(
      (date) => normalizeDate(date?.inventoryItem?.expirationDate),
    );

    const uniqueDates = new Set(dates);

    if (uniqueDates.size > 1) {
      group.forEach((item) => {
        const index = items.findIndex((i) => i.id === item.id);

        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'Multiple expiry dates for this lot/batch.',
          path: ['cycleCountItems', index, 'inventoryItem.expirationDate'],
        });
      });
    }
  });
};

// 4. For lot and exp date for products with lot and expiry control:
//   - Should be an error related to required lot and exp
const checkProductsWithLotAndExpiryControl = (items, ctx) => {
  items.forEach((row, index) => {
    if (!row?.id.includes(NEW_ROW)) {
      return;
    }

    const needsControl = row?.product?.lotAndExpiryControl;

    if (!needsControl) {
      return;
    }

    if (!row?.inventoryItem?.lotNumber?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Lot number and expiry date are required.',
        path: ['cycleCountItems', index, 'inventoryItem.lotNumber'],
      });
    }
    if (!row?.inventoryItem?.expirationDate) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Lot number and expiry date are required.',
        path: ['cycleCountItems', index, 'inventoryItem.expirationDate'],
      });
    }
  });
};

// 5. Lot number is required if expiration date is provided
const checkLotNumberRequireness = (items, ctx) => {
  items.forEach((row, index) => {
    if (!row?.id.includes(NEW_ROW)) {
      return;
    }

    const expirationWithoutLot = !row?.inventoryItem?.lotNumber?.trim()
      && row?.inventoryItem?.expirationDate;

    if (expirationWithoutLot) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Lot number is required.',
        path: ['cycleCountItems', index, 'inventoryItem.lotNumber'],
      });
    }
  });
};

const getRowValidationSchema = (translate) => z.object({
  id: z
    .string(),
  product: z.object({
    id: z
      .string()
      .optional()
      .nullable(),
    lotAndExpiryControl: z
      .boolean()
      .optional()
      .nullable(),
  }),
  quantityCounted: z
    .number({
      required_error: translate('react.cycleCount.requiredQuantityCounted', 'Quantity counted is required'),
      invalid_type_error: translate('react.cycleCount.requiredQuantityCounted', 'Quantity counted is required'),
    })
    .gte(0),
  inventoryItem: z.object({
    expirationDate: z
      .string()
      .optional()
      .nullable(),
    lotNumber: z
      .string()
      .optional()
      .nullable(),
  }).optional(),
  binLocation: z.object({
    id: z.string(),
    name: z.string(),
    label: z.string().optional(),
  }).optional().nullish(),
});

const getTableValidationSchema = (translate) => z.array(getRowValidationSchema(translate));

const getCountEntitySchema = (translate) => z.object({
  cycleCountItems: getTableValidationSchema(translate),
}).superRefine((entity, ctx) => {
  const { cycleCountItems } = entity;

  checkDuplicatedLotNumber(cycleCountItems, ctx);
  checkDifferentExpirationDatesForTheSameLot(cycleCountItems, ctx);
  checkProductsWithLotAndExpiryControl(cycleCountItems, ctx);
  checkLotNumberRequireness(cycleCountItems, ctx);
});

const getCountStepValidationSchema = (translate) => z.record(getCountEntitySchema(translate));

export default {
  getRowValidationSchema,
  getTableValidationSchema,
  getCountEntitySchema,
  getCountStepValidationSchema,
};
