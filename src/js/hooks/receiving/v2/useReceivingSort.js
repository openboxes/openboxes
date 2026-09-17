import { useCallback } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { getReceivingSort } from 'selectors';

import { resetReceivingSort, updateReceivingSort } from 'actions';

/**
 * Table sorting of the receiving screens. The column picked on the receiving
 * step still applies after moving to the check step (each step mounts its
 * own instance of this hook).
 *
 * The sorting itself is done by the backend - the column and the direction are query params
 * and no column sorted means the shipment order, which is what the reset goes back to.
 */
const useReceivingSort = () => {
  const dispatch = useDispatch();
  const { sort, order } = useSelector(getReceivingSort);

  // Clicking the header of the column already sorted by flips the direction; any other column
  // starts ascending.
  const toggleSort = (columnId) => () => dispatch(columnId === sort
    ? updateReceivingSort(sort, order === 'asc' ? 'desc' : 'asc')
    : updateReceivingSort(columnId, 'asc'));

  const getClassName = (columnId) => (columnId === sort ? `-sort-${order}` : null);

  const resetSort = useCallback(() => dispatch(resetReceivingSort()), [dispatch]);

  return {
    sortableProps: {
      dynamicClassName: getClassName,
      toggleSort,
    },
    sort,
    order,
    resetSort,
  };
};

export default useReceivingSort;
