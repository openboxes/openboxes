import { useCallback, useEffect, useRef, useState } from 'react';

import { CancelToken } from 'axios';
import _ from 'lodash';
import queryString from 'query-string';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

const useTableData = ({
  filterParams,
  url,
  errorMessageId,
  defaultErrorMessage,
  getParams,
  onFetchedData,
  defaultSorting,
}) => {
  // Util ref for react-table to force the fetch of data
  const tableRef = useRef(null);
  // Cancel token/signal for fetching data
  const sourceRef = useRef(CancelToken.source());

  const [loading, setLoading] = useState(false);
  const [tableData, setTableData] = useState({
    data: [],
    pages: -1,
    totalCount: 0,
    currentParams: {},
  });

  const {
    currentLocation,
    translate,
  } = useSelector(state => ({
    currentLocation: state.session.currentLocation,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const fireFetchData = () => {
    // Each time we fetch, we want to 'reset' the token/signal
    sourceRef.current = CancelToken.source();
    tableRef.current.fireFetchData();
  };

  // If filterParams change, refetch the data with applied filters
  useEffect(() => fireFetchData(), [filterParams]);

  useEffect(() => () => {
    if (currentLocation?.id) {
      sourceRef.current.cancel('Fetching canceled');
    }
  }, [currentLocation?.id]);

  const onFetchHandler = useCallback((tableState) => {
    if (!_.isEmpty(filterParams)) {
      const offset = tableState.page > 0 ? (tableState.page) * tableState.pageSize : 0;
      const sortingParams = (tableState.sorted.length > 0 ? {
        sort: tableState.sorted[0].id,
        order: tableState.sorted[0].desc ? 'desc' : 'asc',
      } : defaultSorting);
      const params = getParams({
        offset,
        currentLocation,
        state: tableState,
        sortingParams,
      });
      // Fetch data
      setLoading(true);
      apiClient.get(url, {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
        cancelToken: sourceRef.current?.token,
      })
        .then((res) => {
          setTableData({
            data: res.data.data,
            totalCount: res.data.totalCount,
            pages: Math.ceil(res.data.totalCount / tableState.pageSize),
            currentParams: params,
          });
          if (onFetchedData) {
            onFetchedData(res.data);
          }
        })
        .catch(() => Promise.reject(new Error(translate(errorMessageId, defaultErrorMessage))))
        .finally(() => setLoading(false));
    }
  }, [filterParams]);

  return {
    sourceRef,
    tableRef,
    fireFetchData,
    loading,
    setLoading,
    tableData,
    setTableData,
    onFetchHandler,
  };
};

export default useTableData;
