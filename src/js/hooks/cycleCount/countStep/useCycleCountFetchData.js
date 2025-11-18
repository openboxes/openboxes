import { useEffect, useMemo } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import {
  getAllCycleCountProducts,
  getCurrentLocationSupportedActivities,
} from 'selectors';

import {
  fetchBinLocations,
  fetchCycleCounts,
  fetchLotNumbersByProductIds,
  fetchUsers,
} from 'actions';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';

const useCycleCountFetchData = (
  currentLocationId,
  cycleCountIds,
  sortByProductName,
  isStepEditable,
) => {
  const dispatch = useDispatch();
  const supportedActivities = useSelector(getCurrentLocationSupportedActivities);
  const uniqueProductIds = useSelector(getAllCycleCountProducts);

  const showBinLocation = useMemo(
    () => checkBinLocationSupport(supportedActivities),
    [JSON.stringify(supportedActivities)],
  );

  useEffect(() => {
    dispatch(fetchUsers());
  }, []);

  useEffect(() => {
    if (showBinLocation) {
      dispatch(fetchBinLocations(currentLocationId, [], 'sortOrder,locationType,name'));
    }
  }, [currentLocationId, showBinLocation]);

  useEffect(() => {
    dispatch(fetchCycleCounts(cycleCountIds, currentLocationId, sortByProductName));
  }, [cycleCountIds, currentLocationId, sortByProductName]);

  useEffect(() => {
    if (isStepEditable && uniqueProductIds.length > 0) {
      dispatch(fetchLotNumbersByProductIds(uniqueProductIds));
    }
  }, [JSON.stringify(uniqueProductIds), isStepEditable]);

  return { uniqueProductIds };
};

export default useCycleCountFetchData;
