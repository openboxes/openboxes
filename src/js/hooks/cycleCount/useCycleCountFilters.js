import { useCallback, useRef, useState } from 'react';

import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';

import { setShouldRebuildFilterParams } from 'actions';
import useCommonFiltersCleaner from 'hooks/list-pages/useCommonFiltersCleaner';
import useTranslate from 'hooks/useTranslate';
import { groupBinLocationsByZone } from 'utils/groupBinLocationsByZone';
import { getParamList, transformFilterParams } from 'utils/list-utils';
import {
  debouncePeopleFetch,
  fetchBins,
  fetchProductClassifications,
  fetchProductsCatalogs,
  fetchProductsCategories,
  fetchProductsTags,
  fetchUserById,
} from 'utils/option-utils';

const useCycleCountFilters = ({ filterFields }) => {
  // Initializations can overlap (e.g. a tab is switched while the filters of the previous one
  // are still loading), so only the most recent one is allowed to apply its values
  // Example: start fetch of the table on To Count tab, and switch fast to All products tab
  const latestInitializationRef = useRef(0);
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});
  const [dateLastCount] = useState(null);
  const [negativeQuantity] = useState(false);
  const [countDeadline] = useState(null);
  const [recountDeadline] = useState(null);
  const [filtersInitialized, setFiltersInitialized] = useState(false);
  // This boolean ensures that we avoid issues with outdated bin location data
  const [isLoading, setIsLoading] = useState(false);

  const [selectOptions, setSelectOptions] = useState({
    categories: [],
    catalogs: [],
    internalLocations: [],
    tags: [],
    abcClasses: [],
    countAssignees: [],
    recountAssignees: [],
  });

  const {
    currentLocation,
    debounceTime,
    minSearchLength,
  } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));

  const dispatch = useDispatch();

  const navigate = useNavigate();
  const location = useLocation();
  const translate = useTranslate();

  const clearFilterValues = () => {
    const queryProps = queryString.parse(location.search);
    const defaultValues = Object.keys(filterFields)
      .reduce((acc, key) => ({ ...acc, [key]: '' }), { tab: queryProps.tab });
    const transformedParams = transformFilterParams(defaultValues, {
      tab: { name: 'tab' },
    });
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = location;
    // Clearing the filters only resets the current view (e.g. right after a tab switch, which
    // has already pushed its own history entry), so it shouldn't add another entry
    // for the browser back button to go through
    navigate({ pathname, search: queryFilterParams }, { replace: true });
  };

  const resetForm = () => {
    clearFilterValues();
    dispatch(setShouldRebuildFilterParams(true));
  };

  const setDefaultValue = (queryPropsParam, elementsList) => {
    if (queryPropsParam) {
      const idList = getParamList(queryPropsParam);
      return elementsList
        .filter(({ id }) => idList.includes(id))
        .map(({ id, label, name }) => ({
          id, label: label ?? name, name: name ?? label, value: id,
        }));
    }
    return null;
  };

  const initializeDefaultFilterValues = async () => {
    // Increment the ref value if any overlapping call takes place when
    // the first call is still "in progress"
    // e.g. switching fast between To Count and All products tab
    latestInitializationRef.current += 1;

    // This is NOT a shallow copy of the ref value, so if the first call is
    // still in progress, the initializationId from the first call will not be incremented
    // so we can check later if initializationId (=1) = latestInitializationRef (which can be =2)
    const initializationId = latestInitializationRef.current;
    const isOutdated = () => initializationId !== latestInitializationRef.current;
    setIsLoading(true);

    try {
      const queryProps = queryString.parse(window.location.search);
      const defaultValues = Object.keys(filterFields)
        .reduce((acc, key) => ({ ...acc, [key]: '' }), { tab: queryProps.tab });

      if (queryProps.negativeQuantity) {
        defaultValues.negativeQuantity = queryProps.negativeQuantity;
      }

      if (queryProps.dateLastCount) {
        defaultValues.dateLastCount = queryProps.dateLastCount;
      }

      if (queryProps.countDeadline) {
        defaultValues.countDeadline = queryProps.countDeadline;
      }

      if (queryProps.countAssignees) {
        const assigneeIds = Array.isArray(queryProps.countAssignees)
          ? queryProps.countAssignees
          : [queryProps.countAssignees];
        const countAssignees = await Promise.all(
          assigneeIds.map((id) => fetchUserById(id)),
        );
        defaultValues.countAssignees = countAssignees.map((assignee) => ({
          ...assignee,
          label: assignee.name,
          value: assignee.id,
        }));
      }

      if (queryProps.recountAssignees) {
        const recountAssigneeIds = Array.isArray(queryProps.recountAssignees)
          ? queryProps.recountAssignees
          : [queryProps.recountAssignees];
        const recountAssignees = await Promise.all(
          recountAssigneeIds.map((id) => fetchUserById(id)),
        );
        defaultValues.recountAssignees = recountAssignees.map((assignee) => ({
          ...assignee,
          label: assignee.name,
          value: assignee.id,
        }));
      }

      if (queryProps.recountDeadline) {
        defaultValues.recountDeadline = queryProps.recountDeadline;
      }

      const [
        categoryList,
        tagList,
        catalogList,
        classificationList,
        binList,
      ] = await Promise.all([
        fetchProductsCategories(),
        fetchProductsTags({ hideNumbers: true }),
        fetchProductsCatalogs({ hideNumbers: true }),
        fetchProductClassifications(currentLocation?.id),
        fetchBins(currentLocation?.id, [], 'sortOrder,locationType,name'),
      ]);

      if (isOutdated()) {
        return;
      }

      setSelectOptions({
        categories: categoryList,
        catalogs: catalogList,
        tags: tagList,
        internalLocations: groupBinLocationsByZone(binList, translate),
        abcClasses: classificationList,
        countAssignees: defaultValues.countAssignees,
        recountAssignees: defaultValues.recountAssignees,
      });

      defaultValues.catalogs = setDefaultValue(queryProps.catalogs, catalogList);
      defaultValues.tags = setDefaultValue(queryProps.tags, tagList);
      defaultValues.categories = setDefaultValue(queryProps.categories, categoryList);
      defaultValues.internalLocations = setDefaultValue(queryProps.internalLocations, binList);
      defaultValues.abcClasses = setDefaultValue(queryProps.abcClasses, classificationList);

      setDefaultFilterValues(defaultValues);
      // The values have just been read from the URL, so they are applied directly instead of
      // being pushed back to the browser history by the filter form
      setFilterParams({ ...defaultValues });
      setFiltersInitialized(true);
    } finally {
      if (!isOutdated()) {
        setIsLoading(false);
      }
    }
  };

  useCommonFiltersCleaner({
    filtersInitialized,
    initializeDefaultFilterValues,
    clearFilterValues,
  });

  const setFilterValues = (values) => {
    const filterAccessors = {
      dateLastCount: { name: 'dateLastCount' },
      categories: { name: 'categories', accessor: 'id' },
      internalLocations: { name: 'internalLocations', accessor: 'id' },
      tags: { name: 'tags', accessor: 'id' },
      catalogs: { name: 'catalogs', accessor: 'id' },
      abcClasses: { name: 'abcClasses', accessor: 'id' },
      countAssignees: { name: 'countAssignees', accessor: 'id' },
      recountAssignees: { name: 'recountAssignees', accessor: 'id' },
      countDeadline: { name: 'countDeadline' },
      recountDeadline: { name: 'recountDeadline' },
      negativeQuantity: { name: 'negativeQuantity' },
      tab: { name: 'tab' },
    };

    const transformedParams = transformFilterParams(values, filterAccessors);
    const queryFilterParams = queryString.stringify(transformedParams);
    const { pathname } = location;
    if (Object.keys(values).length) {
      navigate({ pathname, search: queryFilterParams });
    }
    setFilterParams(values);
  };

  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  return {
    defaultFilterValues,
    setFilterValues,
    resetForm,
    filterParams,
    dateLastCount,
    countDeadline,
    recountDeadline,
    negativeQuantity,
    isLoading,
    debouncedPeopleFetch,
    ...selectOptions,
  };
};

export default useCycleCountFilters;
