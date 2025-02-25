import _ from 'lodash';
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

  const checkLotNumberRequireness = (data) =>
    data?.inventoryItem?.lotNumber || !data?.inventoryItem?.expirationDate;

  return {
    checkDuplicatedLotNumber,
    checkProductsWithLotAndExpiryControl,
    checkDifferentExpirationDatesForTheSameLot,
    checkLotNumberRequireness,
  };
};

export default useInventoryValidation;
