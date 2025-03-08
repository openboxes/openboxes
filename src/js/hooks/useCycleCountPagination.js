import { useState } from 'react';

import useTablePagination from 'hooks/useTablePagination';

const useCycleCountPagination = (filterParams, defaultPageSize = 5) => {
  const [totalCount, setTotalCount] = useState(0);

  const {
    paginationProps,
    offset,
    pageSize,
  } = useTablePagination({
    defaultPageSize,
    totalCount,
    filterParams,
  });

  return {
    paginationProps,
    offset,
    pageSize,
    setTotalCount,
  };
};

export default useCycleCountPagination;
