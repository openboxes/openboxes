import { useState } from 'react';

const useTableSorting = () => {
  const [sort, setSort] = useState(null);
  const [order, setOrder] = useState(null);

  const toggleSort = (columnId) => () => {
    // If the columnId is equal to sort, it means that we are
    // clicking the header for the second time, so we
    // have to apply the opposite sorting option
    if (columnId === sort) {
      setOrder((prevOrder) => (prevOrder === 'asc' ? 'desc' : 'asc'));
      return;
    }

    setSort(columnId);
    setOrder('asc');
  };

  const getClassName = (columnId) => {
    if (columnId !== sort) {
      return null;
    }

    return `-sort-${order}`;
  };

  return {
    sortableProps: {
      dynamicClassName: getClassName,
      toggleSort,
    },
    sort,
    order,
    setSort,
    setOrder,
  };
};

export default useTableSorting;
