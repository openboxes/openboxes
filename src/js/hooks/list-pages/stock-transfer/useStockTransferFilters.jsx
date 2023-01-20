import { useEffect, useState } from 'react';

import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { fetchStockTransferStatuses } from 'actions';
import filterFields from 'components/stock-transfer/list/FilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import { fetchUserById } from 'utils/option-utils';

const useStockTransferFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();
  const {
    statuses,
    currentUser,
    currentLocale,
  } = useSelector(state => ({
    statuses: state.stockTransfer.statuses,
    currentUser: state.session.user,
    currentLocation: state.session.currentLocation,
    shouldRebuildParams: state.filterForm.shouldRebuildParams,
    currentLocale: state.session.activeLanguage,
  }));
  const dispatch = useDispatch();


  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    // INITIALIZE EMPTY FILTER OBJECT
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);

    // SET STATIC DEFAULT VALUES
    if (queryProps.status) {
      const statusesFromParams = getParamList(queryProps.status);
      defaultValues.status = statuses.filter(({ id }) => statusesFromParams.includes(id));
    }
    if (queryProps.createdBy) {
      defaultValues.createdBy = queryProps.createdBy === currentUser.id
        ? currentUser
        : await fetchUserById(queryProps.createdBy);
    }
    if (queryProps.lastUpdatedStartDate) {
      defaultValues.lastUpdatedStartDate = queryProps.lastUpdatedStartDate;
    }
    if (queryProps.lastUpdatedEndDate) {
      defaultValues.lastUpdatedEndDate = queryProps.lastUpdatedEndDate;
    }
    setDefaultFilterValues({ ...defaultValues });
    setFiltersInitialized(true);
  };

  useEffect(() => {
    // TODO: When having full React, if once fetched, fetch only if a current language differs
    // TODO: from the language, that we were fetching this for
    dispatch(fetchStockTransferStatuses());
  }, [currentLocale]);

  // Custom hook for changing location/filters rebuilding logic
  useCommonFiltersCleaner({ clearFilterValues, initializeDefaultFilterValues, filtersInitialized });

  const setFilterValues = (values) => {
    const filterAccessors = {
      status: { name: 'status', accessor: 'id' },
      createdBy: { name: 'createdBy', accessor: 'id' },
      lastUpdatedStartDate: { name: 'lastUpdatedStartDate' },
      lastUpdatedEndDate: { name: 'lastUpdatedEndDate' },
    };

    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (Object.keys(values).length) {
      history.push({ pathname, search: queryFilterParams });
    }
    setFilterParams({ ...values });
  };

  return { setFilterValues, defaultFilterValues, filterParams };
};

export default useStockTransferFilters;
