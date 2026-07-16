import { useCallback } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId, getReceivingBin } from 'selectors';

import inventoryLevelApi from 'api/services/InventoryLevelApi';
import { LocationAutofillOption } from 'consts/receivingLocationOptions';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';
import confirmLocationAutofillOverwrite from 'utils/receiving/confirmLocationAutofillOverwrite';

/**
 * Autofill of a putaway Location column:
 * - PREFERRED_BIN: each row gets the preferred bin configured on the inventory level of
 *   its product (rows without one keep their value),
 * - FILL_DOWN_FROM_TOP_ROW: the top row's location is applied to all other rows,
 * - RECEIVING_BIN: all rows revert to the receiving bin generated for the shipment.
 * The row access is injected: getRows (the rows the autofill applies to, in display order),
 * getRowBinLocation (the current bin location of a row) and updateLineItems (writes
 * { rowId: { binLocation } } updates back).
 */
const useBinLocationAutofill = ({
  getRows,
  getRowBinLocation,
  updateLineItems,
}) => {
  const facilityId = useSelector(getCurrentLocationId);
  const receivingBin = useSelector(getReceivingBin);

  const applyPreferredBins = async (items) => {
    const productIds = _.uniq(items
      .filter((item) => item.product?.id)
      .map((item) => item.product.id));
    if (!productIds.length) {
      return;
    }
    const { data: { data } } = await inventoryLevelApi
      .getPreferredBinLocations(facilityId, productIds);
    const newDataByRowId = items.reduce((acc, item) => {
      // A row keeps its value when its product has no preferred bin or the preferred bin
      // is inactive.
      const preferredBin = data?.[item.product?.id];
      if (preferredBin?.active) {
        acc[item.rowId] = { binLocation: mapToFormSelectOption(preferredBin) };
      }
      return acc;
    }, {});
    updateLineItems(newDataByRowId);
  };

  const applyFillDownFromTopRow = (items) => {
    const [topItem, ...otherItems] = items;
    updateLineItems(otherItems.reduce((acc, item) => {
      acc[item.rowId] = { binLocation: getRowBinLocation(topItem) };
      return acc;
    }, {}));
  };

  const applyReceivingBin = (items) => {
    if (!receivingBin) {
      return;
    }
    updateLineItems(items.reduce((acc, item) => {
      acc[item.rowId] = { binLocation: receivingBin };
      return acc;
    }, {}));
  };

  const autofillHandlers = {
    [LocationAutofillOption.PREFERRED_BIN]: applyPreferredBins,
    [LocationAutofillOption.FILL_DOWN_FROM_TOP_ROW]: applyFillDownFromTopRow,
    [LocationAutofillOption.RECEIVING_BIN]: applyReceivingBin,
  };

  const applyAutofill = (optionId, items) => autofillHandlers[optionId]?.(items);

  const onLocationAutofill = useCallback((optionId, separatorId) => {
    const items = getRows(separatorId);

    if (!items.length) {
      return;
    }
    // An empty top row is not filled down (it would clear the other rows), so don't even
    // show the overwrite warning.
    if (optionId === LocationAutofillOption.FILL_DOWN_FROM_TOP_ROW
      && !getRowBinLocation(items[0])) {
      return;
    }
    // Any row with a location other than the default receiving bin would lose an entered
    // value on autofill, so warn first.
    const hasEditedBinLocations = items.some((item) => {
      const binLocation = getRowBinLocation(item);
      return binLocation && binLocation.id !== receivingBin?.id;
    });
    if (hasEditedBinLocations) {
      confirmLocationAutofillOverwrite(() => applyAutofill(optionId, items));
      return;
    }
    applyAutofill(optionId, items);
  }, [getRows, getRowBinLocation, updateLineItems, facilityId, receivingBin]);

  return { onLocationAutofill };
};

export default useBinLocationAutofill;
