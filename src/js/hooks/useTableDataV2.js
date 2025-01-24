import {
  useEffect, useRef, useState,
} from 'react';

import { CancelToken } from 'axios';
import queryString from 'query-string';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

// Hook handling logic for DataTable V2 component.
// It handles data about pagination, sorting and filtering and reactively fetches
// appropriate data.
const useTableDataV2 = ({
  url,
  errorMessageId,
  defaultErrorMessage,
  getParams,
  offset,
  pageSize,
  sort,
  order,
  searchTerm,
  filterParams,
  shouldFetch,
}) => {
  const sourceRef = useRef(CancelToken.source());

  const translate = useTranslate();

  const [loading, setLoading] = useState(false);
  const [tableData, setTableData] = useState({
    data: [],
    totalCount: 0,
  });

  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const fetchData = () => {
    // Each time we fetch, we want to 'reset' the token/signal
    sourceRef.current = CancelToken.source();
    const params = getParams({
      sortingParams: { sort, order },
    });
    setLoading(true);
    apiClient.get(url, {
      params,
      paramsSerializer: (parameters) => queryString.stringify(parameters),
      cancelToken: sourceRef.current?.token,
    })
      .then((res) => {
        setTableData({
          data: res.data.data,
          totalCount: res.data.totalCount,
        });
      })
      .catch(() => Promise.reject(new Error(translate(errorMessageId, defaultErrorMessage))))
      .finally(() => setLoading(false));
  };

  // fetching data after changing page size, filters, page number and sorting
  useEffect(() => {
    if (shouldFetch) {
      fetchData();
    }
  }, [
    filterParams,
    pageSize,
    offset,
    sort,
    order,
    searchTerm,
  ]);

  // Start displaying the loader in the table when
  // accessing the page first time, before sending a request
  useEffect(() => {
    setLoading(true);
  }, []);

  useEffect(() => () => {
    if (currentLocation?.id) {
      sourceRef.current.cancel('Fetching canceled');
    }
  }, [currentLocation?.id]);

  return {
    sourceRef,
    loading,
    setLoading,
    tableData,
    fetchData,
  };
};

export default useTableDataV2;
