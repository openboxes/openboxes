import _ from 'lodash';

import ProductApi from 'api/services/ProductApi';
import { DateFormatDateFns } from 'consts/timeFormat';
import useHandleModalAction from 'hooks/useHandleModalAction';
import useSpinner from 'hooks/useSpinner';
import { formatDateToString, parseStringToDate } from 'utils/dateUtils';

/**
 * Confirms the expiration dates entered in the edit line item modal, since a lot's date is shared
 * by every depot holding it.
 */
const useConfirmExpirationDateChange = () => {
  const {
    isOpen,
    data,
    openModal,
    handleResponse,
  } = useHandleModalAction();
  const spinner = useSpinner();

  const findLotAvailability = (lotAvailabilities, lineItem) => lotAvailabilities.find(
    (lotAvailability) => lotAvailability.productId === lineItem.product.id
      && (lotAvailability.lotNumber || '') === (lineItem.lotNumber || ''),
  );

  /**
   * The row's entered date against the lot as the backend reports it.
   *
   * @returns the lot's expiration date change, or null when there is nothing to confirm.
   */
  const getLotChange = (lineItem, lotAvailability) => {
    const newExpiry = formatDateToString({
      date: parseStringToDate({
        date: lineItem.expirationDate,
        dateOnly: true,
        options: { providedDateFormat: DateFormatDateFns.DD_MMM_YYYY },
      }),
      dateFormat: DateFormatDateFns.YYYY_MM_DD,
    });

    // Only a lot that someone holds and whose date actually changes is worth confirming.
    if (!lotAvailability?.quantityOnHand
      || newExpiry === (lotAvailability.expirationDate ?? null)) {
      return null;
    }

    return {
      code: lineItem.product.productCode,
      product: lineItem.product,
      lotNumber: lineItem.lotNumber,
      previousExpiry: lotAvailability.expirationDate,
      newExpiry,
      depots: lotAvailability.depots,
    };
  };

  const toProductLot = (lineItem) => ({
    product: { id: lineItem.product.id },
    lotNumber: lineItem.lotNumber || null,
  });

  const isSameChange = (change, otherChange) => change.product.id === otherChange.product.id
    && change.lotNumber === otherChange.lotNumber
    && change.newExpiry === otherChange.newExpiry;

  const findLotChangesToConfirm = async (lineItems) => {
    // A row can come in without a product, since nothing validates that column yet.
    const rows = lineItems.filter((item) => item.product?.id);

    // The same lot can sit on several rows, so every lot is read only once.
    const productLots = _.uniqWith(rows.map(toProductLot), _.isEqual);
    if (!productLots.length) {
      return [];
    }

    try {
      spinner.show();
      const { data: { data: lotAvailabilities } } = await ProductApi
        .getAvailabilityInAllDepots(productLots);

      const lotChanges = rows
        .map((row) => getLotChange(row, findLotAvailability(lotAvailabilities, row)))
        .filter(Boolean);

      return _.uniqWith(lotChanges, isSameChange);
    } finally {
      spinner.hide();
    }
  };

  /**
   * Asks the user to confirm every lot whose expiration date the entered rows would change.
   *
   * @returns true when the save can go ahead, false when the user cancels.
   */
  const confirmExpirationDateChange = async (lineItems) => {
    const lotChanges = await findLotChangesToConfirm(lineItems);
    if (!lotChanges.length) {
      return true;
    }
    return openModal({ data: lotChanges });
  };

  return {
    confirmExpirationDateChange,
    isExpirationModalOpen: isOpen,
    lotChangesToConfirm: data ?? [],
    handleExpirationModalResponse: handleResponse,
  };
};

export default useConfirmExpirationDateChange;
