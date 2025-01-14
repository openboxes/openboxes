import {
  useEffect, useRef, useState,
} from 'react';

import { CancelToken } from 'axios';
import queryString from 'query-string';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import cycleCountMockedData from 'consts/cycleCountMockedData';
import useQueryParamsListener from 'hooks/useQueryParamsListener';
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
  paramKeys,
  sort,
  order,
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
    const params = getParams({
      sortingParams: { sort, order },
    });
    setLoading(true);
    // Remove after integrating with backend,
    // temporary returning mocked data for cycle count
    if (url === 'cycleCount') {
      console.log('params: ', params);
      setTableData({
        data: cycleCountMockedData.data,
        totalCount: cycleCountMockedData.data.length,
      });
      setLoading(false);
      return;
    }
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

  // Fetching data after changing page size, page number and sorting
  useEffect(() => {
    fetchData();
  }, [pageSize, offset, sort, order]);

  // Fetching data after changes in filters
  useQueryParamsListener({
    callback: () => {
      fetchData();
    },
    params: paramKeys,
  });

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
