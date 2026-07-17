import {
  useCallback, useEffect, useRef, useState,
} from 'react';

import { updateNormalizedItem } from 'utils/normalizationUtils';

/**
 * Owns the autosave rows. `stateRef` is the source of truth, updated synchronously so that
 * queued tasks (which run between renders) always see the effects of earlier tasks.
 *
 * @returns {{
 *   rowsState: { entities: Object, ids: Array },
 *   stateRef: Object,
 *   setRows: Function,
 *   setRowStatus: Function,
 *   resetRows: Function,
 *   isMounted: Function,
 * }} `rowsState` - the rows for rendering, a mirror of `stateRef`.
 *   `stateRef` - the source of truth, safe to read between renders.
 *   `setRows(updater)` - applies the updater to both the ref and the mirror.
 *   `setRowStatus(rowId, saveStatus)` - changes only the save status of one row.
 *   `resetRows(rows)` - adopts a whole new normalized state (consumer refetch).
 *   `isMounted()` - false after unmount.
 */
const useAutosaveRows = (initialRows) => {
  const [rowsState, setRowsState] = useState(initialRows);
  const stateRef = useRef(initialRows);
  const mountedRef = useRef(true);

  const setRows = useCallback((updater) => {
    stateRef.current = updater(stateRef.current);
    // The React state is only set while mounted - requests that finish after unmount
    // have nothing left to render, but the ref must stay correct for queued tasks.
    if (mountedRef.current) {
      setRowsState(stateRef.current);
    }
  }, []);

  const setRowStatus = useCallback((rowId, saveStatus) =>
    setRows((state) => updateNormalizedItem(state, rowId, { saveStatus })), [setRows]);

  const resetRows = useCallback((rows) => {
    stateRef.current = rows;
    setRowsState(rows);
  }, []);

  const isMounted = useCallback(() => mountedRef.current, []);

  useEffect(() => () => {
    mountedRef.current = false;
  }, []);

  return {
    rowsState,
    stateRef,
    setRows,
    setRowStatus,
    resetRows,
    isMounted,
  };
};

export default useAutosaveRows;
