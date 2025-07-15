import { useRef } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import useInventoryValidation from 'hooks/cycleCount/useInventoryValidation';
import useForceRender from 'hooks/useForceRender';
import useTranslate from 'hooks/useTranslate';

const useCountStepValidation = ({ tableData }) => {
  const validationErrors = useRef({});
  // isValid is null only at the beginning, after submitting
  const isValid = useRef(null);

  const translate = useTranslate();

  const {
    checkDuplicatedLotNumber,
    checkDifferentExpirationDatesForTheSameLot,
    checkProductsWithLotAndExpiryControl,
    checkLotNumberRequireness,
  } = useInventoryValidation({ tableData });

  const { forceRerender } = useForceRender();

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
    binLocation: z.object({
      id: z.string(),
      name: z.string(),
      label: z.string().optional(),
    }).optional().nullish(),
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

    const isFormValid = _.every(Object.values(errors), (val) => val.success);
    validationErrors.current = errors;
    isValid.current = isFormValid;
    return isFormValid;
  };

  const resetValidationState = () => {
    validationErrors.current = {};
    isValid.current = null;
  };
  return {
    validationErrors: validationErrors.current,
    triggerValidation,
    forceRerender,
    rowValidationSchema,
    rowsValidationSchema,
    resetValidationState,
  };
};

export default useCountStepValidation;
