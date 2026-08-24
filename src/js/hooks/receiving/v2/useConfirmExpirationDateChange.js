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

  /** @returns the lot's expiration date change, or null when there is nothing to confirm. */
  const getLotChange = async (lineItem) => {
    const { data: { data: lot } } = await ProductApi.getLotAvailabilityInAllDepots(
      lineItem.product.id,
      lineItem.lotNumber,
    );

    const newExpiry = formatDateToString({
      date: parseStringToDate({
        date: lineItem.expirationDate,
        dateOnly: true,
        options: { providedDateFormat: DateFormatDateFns.DD_MMM_YYYY },
      }),
      dateFormat: DateFormatDateFns.YYYY_MM_DD,
    });

    if (!lot.quantityOnHand || newExpiry === (lot.expirationDate ?? null)) {
      return null;
    }

    return {
      code: lineItem.product.productCode,
      product: lineItem.product,
      lotNumber: lineItem.lotNumber,
      previousExpiry: lot.expirationDate,
      newExpiry,
      depots: lot.depots,
    };
  };

  const isSameLot = (item, otherItem) => item.product.id === otherItem.product.id
    && item.lotNumber === otherItem.lotNumber
    && item.expirationDate === otherItem.expirationDate;

  const findLotChangesToConfirm = async (lineItems) => {
    const lotsToCheck = _.uniqWith(
      lineItems.filter((item) => item.product?.id && item.lotNumber),
      isSameLot,
    );
    try {
      spinner.show();
      const changes = await Promise.all(lotsToCheck.map(getLotChange));
      return changes.filter(Boolean);
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
