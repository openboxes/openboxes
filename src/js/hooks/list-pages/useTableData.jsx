import { useEffect, useRef } from 'react';

import { CancelToken } from 'axios';
import { useSelector } from 'react-redux';

const useTableData = (filterParams) => {
  // Util ref for react-table to force the fetch of data
  const tableRef = useRef(null);
  // Cancel token/signal for fetching data
  const sourceRef = useRef(CancelToken.source());

  const { currentLocation } = useSelector(state => ({
    currentLocation: state.session.currentLocation,
  }));

  const fireFetchData = () => {
    sourceRef.current = CancelToken.source();
    tableRef.current.fireFetchData();
  };

  useEffect(() => fireFetchData(), [filterParams]);

  useEffect(() => {
    if (currentLocation?.id) {
      sourceRef.current.cancel('Fetching canceled');
    }
  }, [currentLocation?.id]);

  return {
    sourceRef, tableRef, fireFetchData,
  };
};

export default useTableData;
