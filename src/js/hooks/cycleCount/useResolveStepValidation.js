import { useRef, useState } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useInventoryValidation from 'hooks/cycleCount/useInventoryValidation';
import useForceRender from 'hooks/useForceRender';
import useTranslate from 'hooks/useTranslate';

const useResolveStepValidation = ({ tableData }) => {
  const validationErrors = useRef({});
  const [isRootCauseWarningSkipped, setIsRootCauseWarningSkipped] = useState(false);
  // isValid is null only at the beginning, after submitting
  const isValid = useRef(null);
  const translate = useTranslate();

  const { forceRerender } = useForceRender();

  const {
    checkDuplicatedLotNumber,
    checkProductsWithLotAndExpiryControl,
    checkDifferentExpirationDatesForTheSameLot,
    checkLotNumberRequireness,
  } = useInventoryValidation({ tableData });

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
    // Quantity recounted field is mandatory on each row of inventory record
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

  const validateRootCauses = () => {
    const cycleCountItems = _.flatten(
      tableData.current.map((cycleCount) => cycleCount.cycleCountItems),
    );
    return cycleCountItems.reduce((acc, curr) => {
      const recountDifference = curr.quantityRecounted - (curr.quantityOnHand || 0);
      if (recountDifference !== 0
        && !Number.isNaN(recountDifference)
        && curr.quantityRecounted !== null
        && !curr.rootCause) {
        return [...acc, curr?.id];
      }

      return acc;
    }, []);
  };

  const shouldHaveRootCause = (id) => {
    const selectedRootCause = _.flatten(
      tableData.current.map((cycleCount) => cycleCount.cycleCountItems),
    ).find((row) => row.id === id)?.rootCause;
    const missingRootCauses = validateRootCauses();
    return missingRootCauses.includes(id) && !selectedRootCause;
  };

  const showEmptyRootCauseWarning = () => {
    setIsRootCauseWarningSkipped(true);
    notification(NotificationType.INFO)({
      message: translate(
        'react.cycleCount.popup.emptyRootCause.label',
        'Are you sure you want to continue with empty root cause? Click next if you want to continue.',
      ),
    });
  };

  const resetValidationState = () => {
    validationErrors.current = {};
    isValid.current = null;
  };

  return {
    validationErrors: validationErrors.current,
    triggerValidation,
    validateRootCauses,
    shouldHaveRootCause,
    showEmptyRootCauseWarning,
    forceRerender,
    isRootCauseWarningSkipped,
    rowValidationSchema,
    rowsValidationSchema,
    resetValidationState,
  };
};

export default useResolveStepValidation;
