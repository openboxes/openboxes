import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';

import { setShouldRebuildFilterParams } from 'actions';

const useCommonFiltersCleaner = ({
  clearFilterValues,
  initializeDefaultFilterValues,
  filtersInitialized,
}) => {
  const {
    currentLocation, shouldRebuildParams,
  } = useSelector(state => ({
    currentLocation: state.session.currentLocation,
    shouldRebuildParams: state.filterForm.shouldRebuildParams,
  }));
  const dispatch = useDispatch();

  useEffect(() => {
    // If FilterForm catches change of the location, it sets shouldRebuildFilterParams flag to true
    // so this useEffect knows when to rebuild the filter form values reducing amount of refetches
    // and rebuilds of filter params. After it calls the values builder, set the flag to false
    if (shouldRebuildParams) {
      if (filtersInitialized) {
        clearFilterValues();
      }
      if (currentLocation?.id) {
        initializeDefaultFilterValues();
      }
      dispatch(setShouldRebuildFilterParams(false));
    }
  }, [shouldRebuildParams]);
};

export default useCommonFiltersCleaner;
