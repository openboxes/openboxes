import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import {
  getCurrentLocationId,
  getIsReceivingShipmentLoaded,
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
  // Guards fetchBinLocations below, so that it doesn't run for the wrong shipment.
  const isCurrentShipmentLoaded = useSelector(
    (state) => getIsReceivingShipmentLoaded(state, shipmentId),
  );

  const fetchBinLocations = async () => {
    const { data: { data } } = await locationApi
      .getReceivingInternalLocations(facilityId, shipmentNumber);
    dispatch(updateReceivingBinLocations(
      (data ?? []).map((bin) => mapToFormSelectOption(bin)),
      shipmentId,
    ));
  };

  useEffect(() => {
    if (!facilityId || !shipmentNumber || !isCurrentShipmentLoaded) {
      return;
    }
    fetchBinLocations();
  }, [facilityId, shipmentNumber, isCurrentShipmentLoaded, shipmentId]);
};

export default useReceivingBinLocations;
