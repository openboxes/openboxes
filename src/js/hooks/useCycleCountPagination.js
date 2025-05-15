import { useState } from 'react';

import useTablePagination from 'hooks/useTablePagination';

const useCycleCountPagination = (filterParams, setShouldFetch, defaultPageSize = 5) => {
  const [totalCount, setTotalCount] = useState(0);
  const {
    paginationProps,
    offset,
    pageSize,
  } = useTablePagination({
    defaultPageSize,
    totalCount,
    filterParams,
    setShouldFetch,
  });

  return {
    paginationProps,
    offset,
    pageSize,
    setTotalCount,
  };
};

export default useCycleCountPagination;
