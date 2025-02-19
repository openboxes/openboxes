import { useState } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

const useResolveStepValidation = ({ tableData }) => {
  const [validationErrors, setValidationErrors] = useState({});

  const translate = useTranslate();

  const checkRootCauseRequireness = (arr, ctx) => {
    arr.forEach((row, index) => {
      if ((row.quantityRecounted - (row?.quantityOnHand || 0)) || row.id.includes('newRow')) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.cycleCount.requiredRootCause', 'Root cause is required'),
          path: [index, 'rootCause'],
        });
      }
    });
  };

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

  const rowValidationSchema = z.object({
    id: z
      .string(),
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
    internalLocation: z.object({
      id: z.string(),
      name: z.string(),
      label: z.string().optional(),
    }).optional(),
    rootCause: z.object({
      id: z.string(),
      value: z.string(),
      label: z.string(),
    }).nullish(),
  });

  const rowsValidationSchema = z
    .array(rowValidationSchema)
    .superRefine(checkRootCauseRequireness)
    .superRefine(checkDuplicatedLotNumber);

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

  return {
    validationErrors,
    setValidationErrors,
    triggerValidation,
    rowValidationSchema,
    rowsValidationSchema,
  };
};

export default useResolveStepValidation;
