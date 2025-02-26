import { useState } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import useInventoryValidation from 'hooks/cycleCount/useInventoryValidation';
import useTranslate from 'hooks/useTranslate';

const useCountStepValidation = ({ tableData }) => {
  const [validationErrors, setValidationErrors] = useState({});

  const translate = useTranslate();

  const {
    checkDuplicatedLotNumber,
    checkDifferentExpirationDatesForTheSameLot,
    checkProductsWithLotAndExpiryControl,
    checkLotNumberRequireness,
  } = useInventoryValidation({ tableData });

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
  }).refine(checkLotNumberRequireness, {
    path: ['inventoryItem.lotNumber'],
    message: translate('react.cycleCount.requiredLotNumber', 'Lot number is required'),
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

  return {
    validationErrors,
    setValidationErrors,
    triggerValidation,
    rowValidationSchema,
    rowsValidationSchema,
  };
};

export default useCountStepValidation;
