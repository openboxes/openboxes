import { useCallback } from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocationId } from 'selectors';

import inventoryLevelApi from 'api/services/InventoryLevelApi';
import { LocationAutofillOption } from 'consts/receivingLocationOptions';
import confirmLocationAutofillOverwrite from 'utils/receiving/confirmLocationAutofillOverwrite';

const useEditModalLocationAutofill = ({
  getValues,
  setValue,
  binLocationOptions,
  receivingBin,
}) => {
  const facilityId = useSelector(getCurrentLocationId);

  const setRowLocation = (index, location) => setValue(`lineItems.${index}.location`, location);

  const applyPreferredBins = async (rows) => {
    const productIds = _.uniq(rows
      .filter((row) => row.product?.id)
      .map((row) => row.product.id));
    if (!productIds.length) {
      return;
    }
    const { data: { data } } = await inventoryLevelApi
      .getPreferredBinLocations(facilityId, productIds);
    const binsById = _.keyBy(binLocationOptions, 'id');
    rows.forEach((row, index) => {
      // A row keeps its value when its product has no preferred bin or the preferred bin
      // is not among the facility's (active) bin options.
      const preferredBinId = data?.[row.product?.id]?.id;
      const preferredBin = binsById[preferredBinId];
      if (preferredBin) {
        setRowLocation(index, preferredBin);
      }
    });
  };

  const applyFillDownFromTopRow = (rows) => {
    rows.forEach((row, index) => {
      // The top row is the source of the fill down, so it keeps its own location.
      if (index > 0) {
        setRowLocation(index, rows[0].location ?? null);
      }
    });
  };

  const applyReceivingBin = (rows) => {
    if (!receivingBin) {
      return;
    }
    rows.forEach((row, index) => setRowLocation(index, receivingBin));
  };

  const applyAutofill = (optionId, rows) => {
    switch (optionId) {
      case LocationAutofillOption.PREFERRED_BIN:
        applyPreferredBins(rows);
        break;
      case LocationAutofillOption.FILL_DOWN_FROM_TOP_ROW:
        applyFillDownFromTopRow(rows);
        break;
      case LocationAutofillOption.RECEIVING_BIN:
        applyReceivingBin(rows);
        break;
      default:
        break;
    }
  };

  const onLocationAutofill = useCallback((optionId) => {
    const rows = getValues('lineItems');
    if (!rows.length) {
      return;
    }
    // Any row that already has a location would be overwritten by the autofill, so warn first.
    const hasEditedLocations = rows.some((row) => row.location);
    if (hasEditedLocations) {
      confirmLocationAutofillOverwrite(() => applyAutofill(optionId, rows));
      return;
    }
    applyAutofill(optionId, rows);
  }, [getValues, setValue, facilityId, binLocationOptions, receivingBin]);

  return { onLocationAutofill };
};

export default useEditModalLocationAutofill;
