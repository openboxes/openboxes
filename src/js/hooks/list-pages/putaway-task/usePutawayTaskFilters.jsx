import { useEffect, useRef, useState } from 'react';

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

  // statusCategory defaults to OPEN only for the very first load of the page (e.g.
  // landing here from the dashboard tile with no statusCategory in the URL at all).
  // Once that's happened, an absent statusCategory means the user explicitly cleared
  // it (or switched facility) and wants every status, not that it should snap back.
  const hasAppliedDefaultStatusCategoryRef = useRef(false);

  const clearFilterValues = () => {
    const { pathname } = history.location;
    history.replace({ pathname });
  };

  const initializeDefaultFilterValues = async () => {
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), {});

    const queryProps = queryString.parse(history.location.search);

    if (queryProps.statusCategory) {
      defaultValues.statusCategory = statusCategoryOptions
        .find(({ id }) => id === queryProps.statusCategory) || '';
    } else if (!hasAppliedDefaultStatusCategoryRef.current) {
      defaultValues.statusCategory = statusCategoryOptions
        .find(({ id }) => id === 'OPEN') || '';
    }
    hasAppliedDefaultStatusCategoryRef.current = true;

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

    if (queryProps.createdAfter) {
      defaultValues.createdAfter = queryProps.createdAfter;
    }

    if (queryProps.createdBefore) {
      defaultValues.createdBefore = queryProps.createdBefore;
    }

    setDefaultFilterValues({ ...defaultValues });
    setFiltersInitialized(true);
  };

  useEffect(() => {
    initializeDefaultFilterValues();
  }, []);

  useCommonFiltersCleaner({ clearFilterValues, initializeDefaultFilterValues, filtersInitialized });

  const setFilterValues = (values, { replace = false } = {}) => {
    const filterAccessors = {
      statusCategory: { name: 'statusCategory', accessor: 'id' },
      status: { name: 'status', accessor: 'id' },
      searchTerm: { name: 'searchTerm' },
      container: { name: 'container', accessor: 'id' },
      destination: { name: 'destination', accessor: 'id' },
      createdAfter: { name: 'createdAfter' },
      createdBefore: { name: 'createdBefore' },
    };

    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = history.location;
    if (Object.keys(values).length) {
      history[replace ? 'replace' : 'push']({ pathname, search: queryFilterParams });
    }
    setFilterParams({ ...values });
  };

  // Resets all other filters and filters down to every task belonging to the given
  // putaway order, regardless of status - used by the "Show All Tasks for This
  // Putaway" row action. Routed through the same free-text search box used for
  // everything else (searchTerm isn't a filterFields key, so it's untouched by the
  // blanking reduce below) - this is a substring match rather than an exact one, but
  // putaway order numbers are unique so that's not expected to over-match in practice.
  const filterByOrder = (orderNumber) => {
    const clearedValues = {
      ...Object.keys(filterFields).reduce((acc, key) => ({ ...acc, [key]: '' }), {}),
      searchTerm: orderNumber,
    };

    const { pathname } = history.location;
    history.push({ pathname, search: queryString.stringify({ searchTerm: orderNumber }) });

    setDefaultFilterValues(clearedValues);
    setFilterParams(clearedValues);
  };

  return {
    setFilterValues, filterByOrder, defaultFilterValues, filterParams,
  };
};

export default usePutawayTaskFilters;
