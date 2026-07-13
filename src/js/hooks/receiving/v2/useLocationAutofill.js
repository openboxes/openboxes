import { useCallback } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId } from 'selectors';

import inventoryLevelApi from 'api/services/InventoryLevelApi';
import { LocationAutofillOption } from 'consts/receivingLocationOptions';
import ReceivingRowType from 'consts/receivingRowType';
import confirmLocationAutofillOverwrite from 'utils/receiving/confirmLocationAutofillOverwrite';

/**
 * Autofill of the putaway Location column, triggered from the column header dropdown
 * or from a pack level separator row (packing list view):
 * - PREFERRED_BIN: each row gets the preferred bin configured on the inventory level of
 *   its product (rows without one keep their value),
 * - FILL_DOWN_FROM_TOP_ROW: the top row's location is applied to all other rows,
 * - RECEIVING_BIN: all rows revert to the receiving bin generated for this shipment.
 * When triggered from a separator, the autofill only applies to the rows of that group.
 */
const useLocationAutofill = ({
  lineItemsState,
  updateLineItems,
  binLocations,
  receivingBin,
}) => {
  const facilityId = useSelector(getCurrentLocationId);

  // Rows the autofill applies to, in display order: editable line items only
  // (separator, replaced and toggle rows plus fully received lines are skipped).
  // A separator id narrows the autofill down to the rows of its pack level group.
  const getAutofillableItems = (separatorId) => lineItemsState.ids
    .map((id) => lineItemsState.entities[id])
    .filter((item) => item
      && (!separatorId || item.separatorId === separatorId)
      && item.rowType !== ReceivingRowType.REPLACED
      && item.rowType !== ReceivingRowType.TOGGLE
      && !item.isCompleted);

  const applyPreferredBins = async (items) => {
    const productIds = _.uniq(items
      .filter((item) => item.product?.id)
      .map((item) => item.product.id));
    if (!productIds.length) {
      return;
    }
    const { data: { data } } = await inventoryLevelApi
      .getPreferredBinLocations(facilityId, productIds);
    const binsById = _.keyBy(binLocations, 'id');
    const newDataByRowId = items.reduce((acc, item) => {
      // A row keeps its value when its product has no preferred bin or the preferred bin
      // is not among the facility's (active) bin options.
      const preferredBinId = data?.[item.product?.id]?.id;
      const preferredBin = binsById[preferredBinId];
      if (preferredBin) {
        acc[item.rowId] = { binLocation: preferredBin };
      }
      return acc;
    }, {});
    updateLineItems(newDataByRowId);
  };

  const applyFillDownFromTopRow = (items) => {
    const [topItem, ...otherItems] = items;
    updateLineItems(otherItems.reduce((acc, item) => {
      acc[item.rowId] = { binLocation: topItem.binLocation ?? null };
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

  const applyAutofill = (optionId, items) => {
    switch (optionId) {
      case LocationAutofillOption.PREFERRED_BIN:
        applyPreferredBins(items);
        break;
      case LocationAutofillOption.FILL_DOWN_FROM_TOP_ROW:
        applyFillDownFromTopRow(items);
        break;
      case LocationAutofillOption.RECEIVING_BIN:
        applyReceivingBin(items);
        break;
      default:
        break;
    }
  };

  const onLocationAutofill = useCallback((optionId, separatorId) => {
    const items = getAutofillableItems(separatorId);

    if (!items.length) {
      return;
    }
    // Any row that already has a location would be overwritten by the autofill, so warn first.
    const hasEditedLocations = items.some((item) => item.binLocation);
    if (hasEditedLocations) {
      confirmLocationAutofillOverwrite(() => applyAutofill(optionId, items));
      return;
    }
    applyAutofill(optionId, items);
  }, [lineItemsState, updateLineItems, facilityId, binLocations, receivingBin]);

  return { onLocationAutofill };
};

export default useLocationAutofill;
