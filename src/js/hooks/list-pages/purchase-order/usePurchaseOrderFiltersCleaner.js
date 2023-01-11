import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';

import { setShouldRebuildFilterParams } from 'actions';

const usePurchaseOrderFiltersCleaner = ({
  clearFilterValues,
  initializeDefaultFilterValues,
  filtersInitialized,
}) => {
  const {
    buyers, currentLocation, loading, shouldRebuildParams,
  } = useSelector(state => ({
    buyers: state.organizations.buyers,
    currentLocation: state.session.currentLocation,
    loading: state.session.loading,
    shouldRebuildParams: state.filterForm.shouldRebuildParams,
  }));
  const dispatch = useDispatch();

  useEffect(() => {
    // If FilterForm catches change of the location, it sets shouldRebuildFilterParams flag to true
    // so this useEffect knows when to rebuild the filter form values reducing amount of refetches
    // and rebuilds of filter params. After it calls the values builder, set the flag to false
    if (shouldRebuildParams && !loading && buyers) {
      if (filtersInitialized) {
        clearFilterValues();
      }
      if (currentLocation?.id) {
        initializeDefaultFilterValues();
      }
      dispatch(setShouldRebuildFilterParams(false));
    }
  }, [shouldRebuildParams, loading, buyers]);
};

export default usePurchaseOrderFiltersCleaner;
