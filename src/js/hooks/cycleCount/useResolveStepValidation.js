import { useState } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

const useResolveStepValidation = ({ tableData }) => {
  const [validationErrors, setValidationErrors] = useState({});

  const translate = useTranslate();

  const checkDuplicatedLotNumber = (arr, ctx) => {
    arr.forEach((row, index) => {
      const cycleCountItems = _.find(tableData.current,
        (cycleCount) => _.find(cycleCount.cycleCountItems,
          (item) => item?.id === row?.id))?.cycleCountItems;
      const dataGroupedByBinLocationAndLotNumber = _.groupBy(
        cycleCountItems,
        (item) => `${item?.binLocation?.id}-${item?.inventoryItem?.lotNumber}`,
      );
      const key = `${row?.binLocation?.id}-${row?.inventoryItem?.lotNumber}`;
      if (dataGroupedByBinLocationAndLotNumber[key]?.length > 1 && row?.inventoryItem?.lotNumber) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.cycleCount.duplicatedRow.label', 'Duplicate rows for this inventory item.'),
          path: [index, 'inventoryItem.lotNumber'],
        });
      }
    });
  };

  const checkDifferentExpirationDatesForTheSameLot = (arr, ctx) => {
    arr.forEach((row, index) => {
      const cycleCountItems = _.find(tableData.current,
        (cycleCount) => _.find(cycleCount.cycleCountItems,
          (item) => item?.id === row?.id))?.cycleCountItems;
      const dataGroupedByLotNumber = _.groupBy(
        cycleCountItems,
        (item) => item?.inventoryItem?.lotNumber,
      );
      const expirationDates = dataGroupedByLotNumber[row?.inventoryItem?.lotNumber]
        .map((item) => item?.inventoryItem?.expirationDate);
      if (_.uniq(expirationDates).length !== 1) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('rreact.cycleCount.multipleExpirationDates.label', 'Multiple expiry dates for this lot/batch.'),
          path: [index, 'inventoryItem.expirationDate'],
        });
      }
    });
  };

  const checkProductsWithLotAndExpiryControl = (arr, ctx) => {
    arr.forEach((row, index) => {
      const cycleCountItems = _.find(tableData.current,
        (cycleCount) => _.find(cycleCount.cycleCountItems,
          (item) => item?.id === row?.id))?.cycleCountItems;
      const lotAndExpiryControl = cycleCountItems?.[0]?.product?.lotAndExpiryControl;
      console.log(cycleCountItems)
      if (lotAndExpiryControl && !row?.inventoryItem?.lotNumber) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.cycleCount.requiredLotAndExpirationDate.label', 'Lot number and expiry date are required.'),
          path: [index, 'inventoryItem.lotNumber'],
        });
      }
      if (lotAndExpiryControl && !row?.inventoryItem?.expirationDate) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.cycleCount.requiredLotAndExpirationDate.label', 'Lot number and expiry date are required.'),
          path: [index, 'inventoryItem.expirationDate'],
        });
      }
    });
  };

  const rowValidationSchema = z.object({
    id: z
      .string(),
    product: z.object({
      productCode: z
        .string()
        .nullish(),
      lotAndExpiryControl: z
        .boolean()
        .nullish(),
    }).nullish(),
    quantityCounted: z
      .number({
        required_error: translate('react.cycleCount.requiredQuantityCounted', 'Quantity counted is required'),
        invalid_type_error: translate('react.cycleCount.requiredQuantityCounted', 'Quantity counted is required'),
      })
      .gte(0)
      .nullish(),
    quantityOnHand: z
      .number()
      .nullish(),
    quantityRecounted: z
      .number({
        required_error: translate('react.cycleCount.requiredQuantityRecounted.label', 'Quantity recounted is required'),
        invalid_type_error: translate('react.cycleCount.requiredQuantityRecounted.label', 'Quantity recounted is required'),
      }).gte(0),
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
    }).nullish(),
    rootCause: z.object({
      id: z.string(),
      value: z.string(),
      label: z.string(),
    }).nullish(),
  });

  const rowsValidationSchema = z
    .array(rowValidationSchema)
    .superRefine(checkDuplicatedLotNumber)
    .superRefine(checkDifferentExpirationDatesForTheSameLot)
    .superRefine(checkProductsWithLotAndExpiryControl);

  const triggerValidation = () => {
    const errors = tableData.current.reduce((acc, cycleCount) => {
      const parsedValidation = rowsValidationSchema.safeParse(cycleCount.cycleCountItems);
      return {
        ...acc,
        [cycleCount.id]: {
          errors: parsedValidation?.error?.format(),
          success: parsedValidation.success,
        },
      };
    }, {});

    setValidationErrors(errors);
    return _.every(Object.values(errors), (val) => val.success);
  };

  console.log(validationErrors)

  return {
    validationErrors,
    setValidationErrors,
    triggerValidation,
    rowValidationSchema,
    rowsValidationSchema,
  };
};

export default useResolveStepValidation;
