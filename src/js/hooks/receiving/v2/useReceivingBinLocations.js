import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getCurrentLocationId, getReceivingShipmentNumber } from 'selectors';

import { updateReceivingBinLocations } from 'actions';
import locationApi from 'api/services/LocationApi';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';

/**
 * Fetches the bin location options for the putaway Location column into the redux store:
 * the facility's bin locations plus the receiving bin generated for the shipment being
 * received. Read them with the getReceivingBinLocations / getReceivingBin selectors.
 */
const useReceivingBinLocations = ({ receiptId }) => {
  const dispatch = useDispatch();
  const facilityId = useSelector(getCurrentLocationId);
  const shipmentNumber = useSelector(getReceivingShipmentNumber);

  const fetchBinLocations = async () => {
    const { data: { data } } = await locationApi
      .getReceivingInternalLocations(facilityId, shipmentNumber);
    dispatch(updateReceivingBinLocations((data ?? []).map((bin) => mapToFormSelectOption(bin))));
  };

  useEffect(() => {
    if (!facilityId || !shipmentNumber || !receiptId) {
      return;
    }
    fetchBinLocations();
  }, [facilityId, shipmentNumber, receiptId]);
};

export default useReceivingBinLocations;
