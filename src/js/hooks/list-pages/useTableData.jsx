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
  messageId,
  defaultMessage,
  getSortingParams,
  getParams,
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
    totalPrice: 0,
  });

  const { currentLocation, translate } = useSelector(state => ({
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
      const sortingParams = getSortingParams(tableState);
      const params = getParams(offset, currentLocation, tableState, sortingParams);

      // Fetch data
      setLoading(true);
      apiClient.get(url, {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
        cancelToken: sourceRef.current?.token,
      })
        .then((res) => {
          // totalPrice is not defined in all tables,
          // but because of moving this function to the custom hook
          // I put it here to make it more generic
          setTableData({
            data: res.data.data,
            totalCount: res.data.totalCount,
            pages: Math.ceil(res.data.totalCount / tableState.pageSize),
            currentParams: params,
            totalPrice: res.data.totalPrice,
          });
        })
        .catch(() => Promise.reject(new Error(translate(messageId, defaultMessage))))
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
