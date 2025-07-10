import { useState } from 'react';

import useTablePagination from 'hooks/useTablePagination';

const useCycleCountPagination = (filterParams, setShouldFetch, defaultPageSize = 5,
  disableAutoUpdateFilterParams = false) => {
  const [totalCount, setTotalCount] = useState(0);
  const {
    paginationProps,
    offset,
    pageSize,
    serializedParams,
    setSerializedParams,
  } = useTablePagination({
    defaultPageSize,
    totalCount,
    filterParams,
    setShouldFetch,
    disableAutoUpdateFilterParams,
  });

  return {
    paginationProps,
    offset,
    pageSize,
    setTotalCount,
    serializedParams,
    setSerializedParams,
  };
};

export default useCycleCountPagination;
