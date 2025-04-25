import _ from 'lodash';
import moment from 'moment/moment';
import { z } from 'zod';

import useTranslate from 'hooks/useTranslate';

const useInventoryValidation = ({ tableData }) => {
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
        (item) => `${item?.binLocation?.id}-${item?.inventoryItem?.lotNumber?.trim() || ''}`,
      );
      const key = `${row?.binLocation?.id}-${row?.inventoryItem?.lotNumber?.trim() || ''}`;
      if (dataGroupedByBinLocationAndLotNumber[key]?.length > 1) {
        // Display the error on both the bin and lot because uniqueness is based on both of them.
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.cycleCount.duplicatedRow.label', 'Duplicate rows for this inventory item.'),
          path: [index, 'binLocation'],
        });
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
        (item) => item?.inventoryItem?.lotNumber?.trim() || '',
      );
      // The backend returns expiration date in the "MM/dd/yyyy" format, but we use the ISO format
      // "yyyy-MM-dd'T'hh:mm:ssXXX" when generating dates on the frontend. We convert the dates to
      // a moment (defaulting to UTC if no timezone information is provided) for easy comparison.
      const expirationDates = dataGroupedByLotNumber[row?.inventoryItem?.lotNumber?.trim() || '']
        .map((item) => {
          const expirationDate = item?.inventoryItem?.expirationDate;
          return expirationDate == null ? null : moment.utc(expirationDate);
        });
      const uniqueDates = _.uniqWith(
        expirationDates,
        (arrVal, othVal) => (arrVal === null ? othVal === null : arrVal.isSame(othVal)),
      );
      if (uniqueDates.length > 1) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: translate('react.cycleCount.multipleExpirationDates.label', 'Multiple expiry dates for this lot/batch.'),
          path: [index, 'inventoryItem.expirationDate'],
        });
      }
    });
  };

  const checkProductsWithLotAndExpiryControl = (arr, ctx) => {
    arr.forEach((row, index) => {
      // Already existing rows shouldn't be validated in case of required lot and expiry control
      if (!row?.id.includes('newRow')) {
        return;
      }
      const cycleCountItems = _.find(tableData.current,
        (cycleCount) => _.find(cycleCount.cycleCountItems,
          (item) => item?.id === row?.id))?.cycleCountItems;
      const lotAndExpiryControl = cycleCountItems?.[0]?.product?.lotAndExpiryControl;
      if (lotAndExpiryControl && !row?.inventoryItem?.lotNumber?.trim()) {
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

  const checkLotNumberRequireness = (data) =>
    data?.inventoryItem?.lotNumber?.trim() || !data?.inventoryItem?.expirationDate;

  return {
    checkDuplicatedLotNumber,
    checkProductsWithLotAndExpiryControl,
    checkDifferentExpirationDatesForTheSameLot,
    checkLotNumberRequireness,
  };
};

export default useInventoryValidation;
