import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import {
  getCurrentLocationId,
  getReceivingShipmentId,
  getReceivingShipmentNumber,
} from 'selectors';

import { updateReceivingBinLocations } from 'actions';
import locationApi from 'api/services/LocationApi';
import mapToFormSelectOption from 'utils/mapToFormSelectOption';

/**
 * Fetches the bin location options for the putaway Location column into the redux store:
 * the facility's bin locations plus the receiving bin generated for the shipment being
 * received. Read them with the getReceivingBinLocations / getReceivingBin selectors.
 */
const useReceivingBinLocations = () => {
  const dispatch = useDispatch();
  const { shipmentId } = useParams();
  const facilityId = useSelector(getCurrentLocationId);
  const shipmentNumber = useSelector(getReceivingShipmentNumber);
  // The shipmentId in the store which data was fetched for.
  // If this doesn't match the shipmentId in the URL, we don't fetch bin locations,
  // because then we would fetch the bin locations for the wrong shipment.
  const storedShipmentId = useSelector(getReceivingShipmentId);

  const fetchBinLocations = async () => {
    const { data: { data } } = await locationApi
      .getReceivingInternalLocations(facilityId, shipmentNumber);
    dispatch(updateReceivingBinLocations(
      (data ?? []).map((bin) => mapToFormSelectOption(bin)),
      shipmentId,
    ));
  };

  useEffect(() => {
    if (!facilityId || !shipmentNumber || storedShipmentId !== shipmentId) {
      return;
    }
    fetchBinLocations();
  }, [facilityId, shipmentNumber, storedShipmentId, shipmentId]);
};

export default useReceivingBinLocations;
