import { useState } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

const useCountStepValidation = ({ tableData }) => {
  const [validationErrors, setValidationErrors] = useState({});

  const translate = useTranslate();

  const checkLotNumberUniqueness = (data) => {
    const foundCycleCount = tableData.current.find(
      (cycleCount) => cycleCount.cycleCountItems.find((row) => row.id === data.id),
    );
    const groupedLotNumbers = _.groupBy(
      foundCycleCount.cycleCountItems, 'inventoryItem.lotNumber',
    );
    return groupedLotNumbers[data.inventoryItem?.lotNumber]?.length === 1;
  };

  const rowValidationSchema = z.object({
    id: z
      .string(),
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
    internalLocation: z.object({
      id: z.string(),
      name: z.string(),
      label: z.string().optional(),
    }).optional(),
  }).refine((data) => data?.inventoryItem?.lotNumber || !data?.inventoryItem?.expirationDate, {
    path: ['inventoryItem.lotNumber'],
    message: translate('react.cycleCount.requiredLotNumber', 'Lot number is required'),
  }).refine(checkLotNumberUniqueness, {
    path: ['inventoryItem.lotNumber'],
    message: translate('react.cycleCount.uniqueLotNumber', 'Lot number should be unique'),
  });

  const rowsValidationSchema = z.array(rowValidationSchema);

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

export default useCountStepValidation;
