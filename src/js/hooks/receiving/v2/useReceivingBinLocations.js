import { useEffect, useMemo, useState } from 'react';

import { useSelector } from 'react-redux';
import { getCurrentLocationId, getReceivingShipmentNumber } from 'selectors';

import locationApi from 'api/services/LocationApi';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';

/**
 * Bin location options for the putaway Location column: the facility's bin locations plus
 * the receiving bin generated for the shipment being received. The receiving bin (named
 * after the shipment number) is also returned separately - it is the default putaway value,
 * used by the "Receiving bin" autofill option and by the overwrite confirmation check.
 */
const useReceivingBinLocations = () => {
  const [binLocations, setBinLocations] = useState([]);
  const facilityId = useSelector(getCurrentLocationId);
  const shipmentNumber = useSelector(getReceivingShipmentNumber);

  const fetchBinLocations = async () => {
    const { data: { data } } = await locationApi
      .getReceivingInternalLocations(facilityId, shipmentNumber);
    setBinLocations((data ?? []).map((bin) => mapToFormSelectOption(bin)));
  };

  useEffect(() => {
    if (!facilityId || !shipmentNumber) {
      return;
    }
    fetchBinLocations();
  }, [facilityId, shipmentNumber]);

  // The receiving bin generated for the shipment is named "<prefix>-<shipment number>",
  // where the prefix is configurable ("R" by default).
  const receivingBin = useMemo(() => {
    if (!shipmentNumber) {
      return null;
    }
    return binLocations.find((bin) => bin.name.endsWith(`-${shipmentNumber}`)) ?? null;
  }, [binLocations, shipmentNumber]);

  return { binLocations, receivingBin };
};

export default useReceivingBinLocations;
