import { useEffect, useState } from 'react';

import queryString from 'query-string';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import filterFields from 'components/putaway-task/list/FilterFields';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import { fetchLocationById } from 'utils/option-utils';

const usePutawayTaskFilters = () => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [filtersInitialized, setFiltersInitialized] = useState(false);

  const history = useHistory();
  useSelector((state) => ({
    currentLocation: state.session.currentLocation,
    shouldRebuildParams: state.filterForm.shouldRebuildParams,
  }));

  const statusCategoryOptions = filterFields.statusCategory
    .getDynamicAttr().options;

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.push({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);

    if (queryProps.statusCategory) {
      defaultValues.statusCategory = statusCategoryOptions
        .find(({ id }) => id === queryProps.statusCategory) || '';
    } else {
      defaultValues.statusCategory = statusCategoryOptions
        .find(({ id }) => id === 'OPEN') || '';
    }

    if (queryProps.status) {
      const statusesFromParams = getParamList(queryProps.status);
      const statusOptions = filterFields.status.getDynamicAttr().options;
      defaultValues.status = statusOptions
        .filter(({ id }) => statusesFromParams.includes(id));
    }

    if (queryProps.searchTerm) {
      defaultValues.searchTerm = queryProps.searchTerm;
    }

    if (queryProps.container) {
      const location = await fetchLocationById(queryProps.container);
      if (location) {
        defaultValues.container = { id: location.id, value: location.id, label: location.name };
      }
    }

    if (queryProps.destination) {
      const location = await fetchLocationById(queryProps.destination);
      if (location) {
        defaultValues.destination = { id: location.id, value: location.id, label: location.name };
      }
    }

    setDefaultFilterValues({ ...defaultValues });
    setFiltersInitialized(true);
  };

  useEffect(() => {
    initializeDefaultFilterValues();
  }, []);

  useCommonFiltersCleaner({ clearFilterValues, initializeDefaultFilterValues, filtersInitialized });

  const setFilterValues = (values) => {
    const filterAccessors = {
      statusCategory: { name: 'statusCategory', accessor: 'id' },
      status: { name: 'status', accessor: 'id' },
      searchTerm: { name: 'searchTerm' },
      container: { name: 'container', accessor: 'id' },
      destination: { name: 'destination', accessor: 'id' },
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

export default usePutawayTaskFilters;
