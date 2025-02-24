import { useState } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import useTranslate from 'hooks/useTranslate';

const useResolveStepValidation = ({ tableData }) => {
  const [validationErrors, setValidationErrors] = useState({});
  const [isRootCauseWarningSkipped, setIsRootCauseWarningSkipped] = useState(false);

  const translate = useTranslate();

  // Validation for already existing inventories and also newly added lines:
  // 1. The same lot in different bins should be accepted
  // 2. For the same lot in the same bin in multiple rows:
  //    - Should be a duplicate rows validation error
  // 3. Same lot with different exp, in different bins:
  //    - Should be an error related to valid exp
  // 4. For lot and exp date for products with lot and expiry control:
  //    - Should be an error related to required lot and exp

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

  const validateRootCauses = () => {
    const cycleCountItems = _.flatten(
      tableData.current.map((cycleCount) => cycleCount.cycleCountItems),
    );
    return cycleCountItems.reduce((acc, curr) => {
      const recountDifference = curr.quantityRecounted - (curr.quantityOnHand || 0);
      if (recountDifference !== 0 && !Number.isNaN(recountDifference)) {
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

  return {
    validationErrors,
    setValidationErrors,
    triggerValidation,
    validateRootCauses,
    shouldHaveRootCause,
    showEmptyRootCauseWarning,
    isRootCauseWarningSkipped,
    rowValidationSchema,
    rowsValidationSchema,
  };
};

export default useResolveStepValidation;
