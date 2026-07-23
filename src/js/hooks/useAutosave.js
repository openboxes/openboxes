import {
  useCallback, useEffect, useMemo, useRef, useState,
} from 'react';

import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getAutosaveBatchSize, getAutosaveDebounceTime } from 'selectors';

import { AutosaveStatus } from 'consts/autosaveStatuses';
import RowSaveStatus from 'consts/rowSaveStatus';
import useAutosaveQueue from 'hooks/useAutosaveQueue';
import useAutosaveRetry from 'hooks/useAutosaveRetry';
import useAutosaveRows from 'hooks/useAutosaveRows';
import {
  removeNormalizedItem,
  updateNormalizedItem,
  updateNormalizedItems,
  upsertNormalizedItem,
} from 'utils/normalizationUtils';

/**
 * Generic autosave over a normalized rows state ({ entities, ids }). The hook owns the rows:
 * the consumer fetches data once (or on every refetch), normalizes it into `initialRows` and
 * reads the up-to-date rows back from the hook.
 *
 * How autosaving works:
 * - every `updateRow`/`addRow` marks the row as dirty (saveStatus PENDING) and schedules a
 *   flush: when `batchSize` dirty rows are waiting, or after `debounceTime`
 *   with no edits. A flush sends all dirty rows in one `updateFn` call.
 * - requests are sent using a queue (one at a time), so two saves can never race each
 *   other, and a delete request queued after the save that created its row always
 *   sees the server-assigned id.
 * - each row has a version, incremented on every edit. The full response is applied
 *   (`reconcileRow`) only when the row was not edited while the request was running; a stale
 *   response only copies in server-assigned ids (`reconcileStaleRow`), so an older request
 *   can never overwrite a newer edit. The row stays dirty and the next flush sends its
 *   latest value.
 * - a failed batch marks its rows dirty again (saveStatus ERROR) and retries with exponential
 *   backoff (`retryDelay`, doubled on each try, limited at `maxRetryDelay`). Failures answered
 *   by the server are added until `maxRetries` is reached; network failures (`isNetworkError`)
 *   don't - the row keeps retrying until the connection is back, so the retry also works as the
 *   connectivity check (the first saved batch means "back online"). The browser `online`
 *   event flushes right away, without waiting for the backoff. Retries read the current row
 *   data, so a newer edit simply replaces the failed payload. A new edit resets the retry
 *   count.
 *
 * @param {Object} options
 * @param {{ entities: Object, ids: Array }} options.initialRows - normalized rows fetched by
 *   the consumer
 *
 * @param {Object} options.requests
 * @param {Function} options.requests.updateFn - saves a batch of dirty rows on the backend:
 *   async (dirtyRows) => updatedRows. Called on every flush with all rows to save. Each row
 *   in the response must include the client row id (`keyField`), so the hook can match it
 *   with the local row.
 * @param {Function} [options.requests.deleteFn] - deletes a single row on the backend:
 *   async (row) => void. Called by `deleteRow` with the current row data.
 *
 * @param {Object} [options.rowOptions]
 * @param {string} [options.rowOptions.keyField] - row id field, used to key added rows and to
 *   match `updateFn` response rows back to local rows
 * @param {Function} [options.rowOptions.generateRowId] - () => id for added rows
 * @param {Function} [options.rowOptions.shouldSaveRow] - decides whether a dirty row really
 *   needs a request: (row) => boolean. Checked at flush time; when it returns false, the row
 *   is marked SAVED without a request.
 * @param {Function} [options.rowOptions.reconcileRow] - builds the changes to apply to a row
 *   from a fresh response (the row was not edited while its request was running):
 *   (row, serverRow) => partial. The returned partial is merged into the row.
 * @param {Function} [options.rowOptions.reconcileStaleRow] - like `reconcileRow`, but for a
 *   stale response (the row was edited while its request was running). Should return only
 *   server-assigned ids, never values the user can edit, so the newer edit stays.
 * @param {Function} [options.rowOptions.removeRowFromState] - removes a deleted row from the
 *   normalized state: (state, rowId) => state. Override when removing a row needs extra
 *   clean-up (e.g. the grouping of receiving split items).
 *
 * @param {Object} [options.flushOptions]
 * @param {number} [options.flushOptions.batchSize] - dirty rows count that triggers an
 *   immediate flush, defaults to session settings fetched from backend
 * @param {number} [options.flushOptions.debounceTime] - time (ms) without edits after which
 *   dirty rows are sent; defaults to session settings fetched from backend
 *
 * @param {Object} [options.retryOptions]
 * @param {number} [options.retryOptions.maxRetries] - retry attempts per row before giving up
 * @param {number} [options.retryOptions.retryDelay] - base retry delay (ms), doubled on each
 *   try; defaults to session settings fetched from backend
 * @param {number} [options.retryOptions.maxRetryDelay] - upper limit (ms) for the backoff
 *   delay; defaults to session settings fetched from backend
 * @param {Function} [options.retryOptions.isNetworkError] - checks if a save failed because
 *   there was no internet connection: (error) => boolean
 *
 * @returns {{
 *   rows: Object,
 *   rowsById: Array,
 *   updateRow: Function,
 *   updateRows: Function,
 *   addRow: Function,
 *   deleteRow: Function,
 *   isSavePending: boolean,
 *   isRowSaving: Function,
 *   autosaveStatus: string,
 *   flush: Function,
 * }} `rows`/`rowsById` - the current rows, kept up to date by the hook.
 *   `updateRow`/`updateRows`/`addRow`/`deleteRow` - row operations that also handle saving;
 *   `updateRows({ rowId: newData })` edits many rows in one state update.
 *   `isSavePending` - true while anything is still unsaved.
 *   `isRowSaving(rowId)` - true while the row has a running request.
 *   `autosaveStatus` - one of AutosaveStatus, drives the indicator for all rows.
 *   `flush()` - sends everything still unsaved; rejects when some row cannot be saved.
 */
const useAutosave = ({
  initialRows,
  requests: {
    updateFn,
    deleteFn,
  } = {},
  rowOptions: {
    keyField = 'rowId',
    generateRowId = () => _.uniqueId('row-'),
    shouldSaveRow = () => true,
    reconcileRow = (row, serverRow) => serverRow,
    reconcileStaleRow = () => ({}),
    removeRowFromState = removeNormalizedItem,
  } = {},
  flushOptions: {
    batchSize,
    debounceTime,
  } = {},
  retryOptions = {},
}) => {
  const configBatchSize = useSelector(getAutosaveBatchSize);
  const configDebounceTime = useSelector(getAutosaveDebounceTime);
  const [isSavePending, setIsSavePending] = useState(false);

  // The rows hook owns the rows: stateRef is the synchronous source of truth
  const {
    rowsState, stateRef, setRows, setRowStatus, resetRows, isMounted,
  } = useAutosaveRows(initialRows);
  // Dirty rows waiting for a save: rowId -> { attempts, backoffLevel }. Being in the map
  // means dirty.
  const dirtyRef = useRef(new Map());
  // Edit counter per row, used to detect rows edited while their save request was running.
  const versionsRef = useRef(new Map());
  const debounceTimerRef = useRef(null);
  // True while flush() runs
  const isFlushingRef = useRef(false);
  // The current flushDirty function, assigned on every render. Timers, the retry hook
  // and flush() call it through this ref, so they always run the newest version.
  const flushDirtyRef = useRef(() => Promise.resolve());
  // The retry hook owns the backoff timer, the online listener and error classification;
  const {
    maxRetries, isNetworkError, scheduleRetry, cancelRetry,
  } = useAutosaveRetry({ ...retryOptions, onRetry: () => flushDirtyRef.current() });
  // Serial queue for save/delete requests
  const {
    enqueueTask, hasPendingTasks, awaitPendingTasks,
  } = useAutosaveQueue();
  // Latest options for the stable callbacks below
  // See latest ref pattern: https://www.epicreact.dev/the-latest-ref-pattern-in-react
  const optionsRef = useRef({});
  optionsRef.current = {
    updateFn,
    deleteFn,
    shouldSaveRow,
    reconcileRow,
    reconcileStaleRow,
    removeRowFromState,
    generateRowId,
    keyField,
    batchSize: batchSize ?? configBatchSize,
    debounceTime: debounceTime ?? configDebounceTime,
    maxRetries,
    isNetworkError,
  };

  // Recomputes the `isSavePending` flag: true while any row is dirty or any request
  // is running.
  const recomputePending = useCallback(() => {
    if (!isMounted()) {
      return;
    }
    setIsSavePending(dirtyRef.current.size > 0 || hasPendingTasks());
  }, [isMounted, hasPendingTasks]);

  // (Re)starts the debounce timer after an edit: a full batch flushes right away,
  // otherwise the flush waits for `debounceTime` without edits.
  const scheduleFlush = useCallback(() => {
    clearTimeout(debounceTimerRef.current);
    const delay = dirtyRef.current.size >= optionsRef.current.batchSize
      ? 0
      : optionsRef.current.debounceTime;
    debounceTimerRef.current = setTimeout(() => flushDirtyRef.current(), delay);
  }, []);

  // Collects the rows to send. Rows that no longer exist are dropped, edits
  // (shouldSaveRow) are marked SAVED without a request, and rows that ran out of retries
  // stay dirty but are skipped until edited again (or retried by flush()).
  const drainDirtyRows = useCallback(() => {
    const batchIds = [];
    dirtyRef.current.forEach(({ attempts }, rowId) => {
      const row = stateRef.current.entities[rowId];
      if (!row) {
        dirtyRef.current.delete(rowId);
        return;
      }
      if (!optionsRef.current.shouldSaveRow(row)) {
        dirtyRef.current.delete(rowId);
        setRowStatus(rowId, RowSaveStatus.SAVED);
        return;
      }
      if (attempts > optionsRef.current.maxRetries) {
        return;
      }
      batchIds.push(rowId);
    });
    return batchIds;
  }, [setRowStatus]);

  // Merges a successful batch response into the rows
  const applyUpdateResponse = useCallback((batchIds, sentVersions, updatedRows) => {
    const responseByRowId = _.keyBy(updatedRows ?? [], optionsRef.current.keyField);
    setRows((state) => batchIds.reduce((acc, rowId) => {
      const row = acc.entities[rowId];
      // Row deleted while the request was running - nothing to merge.
      if (!row) {
        return acc;
      }
      const serverRow = responseByRowId[rowId];
      const isFresh = versionsRef.current.get(rowId) === sentVersions.get(rowId);
      if (!serverRow) {
        return isFresh
          ? updateNormalizedItem(acc, rowId, { saveStatus: RowSaveStatus.SAVED })
          : acc;
      }
      // A row edited while its request was running keeps its local values - only
      // server-assigned ids are copied in. The row is already dirty again, so the next
      // flush sends the newest data.
      return updateNormalizedItem(acc, rowId, isFresh
        ? { ...optionsRef.current.reconcileRow(row, serverRow), saveStatus: RowSaveStatus.SAVED }
        : optionsRef.current.reconcileStaleRow(row, serverRow));
    }, state));
  }, [setRows]);

  // Handles a failed batch: marks its rows as ERROR, puts them back into the dirty map
  // with updated retry counters and schedules the next retry.
  const handleUpdateError = useCallback(({
    batchIds, sentVersions, dirtyStateByRow, error,
  }) => {
    // A network failure (no server response) doesn't use up the retry budget - the row keeps
    // retrying until the connection is back
    const isNetworkFailure = optionsRef.current.isNetworkError(error) && !isFlushingRef.current;
    // Rows edited while the request was running are already dirty again with a fresh retry
    // budget - only the untouched ones go back into the dirty map with one more attempt.
    const failedIds = batchIds.filter((rowId) => stateRef.current.entities[rowId]
      && versionsRef.current.get(rowId) === sentVersions.get(rowId));
    failedIds.forEach((rowId) => {
      const { attempts = 0, backoffLevel = 0 } = dirtyStateByRow.get(rowId) ?? {};
      dirtyRef.current.set(rowId, {
        attempts: attempts + (isNetworkFailure ? 0 : 1),
        backoffLevel: backoffLevel + 1,
      });
      setRowStatus(rowId, RowSaveStatus.ERROR);
    });
    const retriableLevels = failedIds
      .map((rowId) => dirtyRef.current.get(rowId))
      .filter(({ attempts }) => attempts <= optionsRef.current.maxRetries)
      .map(({ backoffLevel }) => backoffLevel);
    if (retriableLevels.length) {
      scheduleRetry(Math.min(...retriableLevels));
    }
  }, [setRowStatus, scheduleRetry]);

  // Sends one batch: collects the dirty rows, marks them SAVING and runs `updateFn` through
  // the queue. Assigned to the ref on every render, so timers, the retry hook and
  // flush() always call the newest version.
  flushDirtyRef.current = () => {
    const batchIds = drainDirtyRows();
    if (!batchIds.length) {
      recomputePending();
      return Promise.resolve();
    }
    // Version snapshot taken at send time - compared on response to detect rows edited
    // while the request was running.
    const sentVersions = new Map(batchIds.map((rowId) => [rowId, versionsRef.current.get(rowId)]));
    const dirtyStateByRow = new Map(
      batchIds.map((rowId) => [rowId, dirtyRef.current.get(rowId)]),
    );
    batchIds.forEach((rowId) => {
      dirtyRef.current.delete(rowId);
      setRowStatus(rowId, RowSaveStatus.SAVING);
    });
    // The response is merged into state inside the queued task - the queue starts the next
    // task (e.g. a delete of one of these rows) only after that.
    const task = enqueueTask(async () => {
      // Row data is read when the queue actually runs the task, so a request delayed
      // behind another one still sends the latest values.
      const dirtyRows = batchIds
        .map((rowId) => stateRef.current.entities[rowId])
        .filter(Boolean);
      const updatedRows = await optionsRef.current.updateFn(dirtyRows);
      applyUpdateResponse(batchIds, sentVersions, updatedRows);
    }, {
      onError: (error) => handleUpdateError({
        batchIds, sentVersions, dirtyStateByRow, error,
      }),
      onSettled: recomputePending,
    });
    recomputePending();
    return task;
  };

  // Bumps the row's version and puts it into the dirty map with a fresh retry budget.
  const markRowDirty = useCallback((rowId) => {
    versionsRef.current.set(rowId, (versionsRef.current.get(rowId) ?? 0) + 1);
    dirtyRef.current.set(rowId, { attempts: 0, backoffLevel: 0 });
  }, []);

  // Applies an edit to a row: bumps its version, marks it dirty (PENDING) and schedules
  // a flush.
  const updateRow = useCallback((rowId, newData) => {
    const row = stateRef.current.entities[rowId];
    if (!row || row.isDeleteInProgress) {
      return;
    }
    markRowDirty(rowId);
    setRows((state) => updateNormalizedItem(state, rowId, {
      ...newData,
      saveStatus: RowSaveStatus.PENDING,
    }));
    recomputePending();
    scheduleFlush();
  }, [markRowDirty, setRows, recomputePending, scheduleFlush]);

  // Batch counterpart of updateRow: applies edits to many rows in one state update.
  // React 16 doesn't batch updates fired after an await (e.g. the location autofill),
  // so per-row updates would re-render the whole table once per row.
  const updateRows = useCallback((newDataByRowId) => {
    const updates = _.pickBy(newDataByRowId, (newData, rowId) => {
      const row = stateRef.current.entities[rowId];
      return row && !row.isDeleteInProgress;
    });
    if (_.isEmpty(updates)) {
      return;
    }
    Object.keys(updates).forEach((rowId) => markRowDirty(rowId));
    setRows((state) => updateNormalizedItems(
      state,
      _.mapValues(updates, (newData) => ({ ...newData, saveStatus: RowSaveStatus.PENDING })),
    ));
    recomputePending();
    scheduleFlush();
  }, [markRowDirty, setRows, recomputePending, scheduleFlush]);

  // Adds a new row to the state and queues it for saving like an edit.
  const addRow = useCallback((rowData) => {
    const rowId = rowData[optionsRef.current.keyField] ?? optionsRef.current.generateRowId();
    markRowDirty(rowId);
    setRows((state) => upsertNormalizedItem(state, {
      ...rowData,
      [optionsRef.current.keyField]: rowId,
      saveStatus: RowSaveStatus.PENDING,
    }, optionsRef.current.keyField));
    recomputePending();
    scheduleFlush();
    return rowId;
  }, [markRowDirty, setRows, recomputePending, scheduleFlush]);

  // Deletes a row: runs `deleteFn` through the queue (behind any pending save) and
  // removes the row locally only on success.
  const deleteRow = useCallback((rowId) => {
    const row = stateRef.current.entities[rowId];
    // The isDeleteInProgress guard ignores repeated clicks so a row can never be deleted twice.
    if (!row || row.isDeleteInProgress) {
      return Promise.resolve();
    }
    // Pending edits of a deleted row no longer matter.
    dirtyRef.current.delete(rowId);
    versionsRef.current.delete(rowId);
    setRows((state) => updateNormalizedItem(state, rowId, {
      isDeleteInProgress: true,
      saveStatus: RowSaveStatus.SAVING,
    }));
    const task = enqueueTask(async () => {
      // Read the row when the queue runs the task: if the save that created it was still
      // running at click time, the serial queue guarantees it has finished by now and the
      // server-assigned id is already there.
      const freshRow = stateRef.current.entities[rowId];
      await optionsRef.current.deleteFn?.(freshRow);
      // Removed inside the task so any task queued behind sees the row gone.
      setRows((state) => optionsRef.current.removeRowFromState(state, rowId));
    }, {
      onError: () => setRows((state) => updateNormalizedItem(state, rowId, {
        isDeleteInProgress: false,
        saveStatus: RowSaveStatus.ERROR,
      })),
      onSettled: recomputePending,
    });
    recomputePending();
    return task;
  }, [setRows, recomputePending, enqueueTask]);

  // Sends everything still unsaved and resolves once nothing is left (no dirty rows, no
  // running requests)
  const flush = useCallback(async () => {
    clearTimeout(debounceTimerRef.current);
    cancelRetry();
    isFlushingRef.current = true;
    try {
      dirtyRef.current.forEach((entry, rowId) => {
        dirtyRef.current.set(rowId, {
          ...entry,
          attempts: Math.min(entry.attempts, optionsRef.current.maxRetries),
        });
      });
      const hasRetriableDirtyRows = () => [...dirtyRef.current.values()]
        .some(({ attempts }) => attempts <= optionsRef.current.maxRetries);
      while (hasPendingTasks() || hasRetriableDirtyRows()) {
        flushDirtyRef.current();
        // eslint-disable-next-line no-await-in-loop
        await awaitPendingTasks();
      }
    } finally {
      isFlushingRef.current = false;
    }
    const hasUnsavedRows = Object.values(stateRef.current.entities)
      .some((row) => row?.saveStatus === RowSaveStatus.ERROR);
    if (hasUnsavedRows) {
      throw new Error('Autosave failed: some rows could not be saved');
    }
  }, [cancelRetry, hasPendingTasks, awaitPendingTasks]);

  // Whether the row has a save or delete request running.
  const isRowSaving = useCallback((rowId) => {
    const row = stateRef.current.entities[rowId];
    return row?.saveStatus === RowSaveStatus.SAVING || Boolean(row?.isDeleteInProgress);
  }, []);

  // General status for the autosave indicator
  const autosaveStatus = useMemo(() => {
    const hasFailedRows = Object.values(rowsState.entities || {})
      .some((row) => row?.saveStatus === RowSaveStatus.ERROR);

    if (hasFailedRows) {
      return AutosaveStatus.ERROR;
    }
    if (isSavePending) {
      return AutosaveStatus.SAVING;
    }
    return AutosaveStatus.SAVED;
  }, [rowsState, isSavePending]);

  // A new initialRows reference means the consumer refetched (view switch, modal reload) -
  // adopt it and drop all local tracking. Callers should flush() before refetching.
  useEffect(() => {
    clearTimeout(debounceTimerRef.current);
    cancelRetry();
    dirtyRef.current.clear();
    versionsRef.current.clear();
    resetRows(initialRows);
    recomputePending();
  }, [initialRows]);

  useEffect(() => () => {
    clearTimeout(debounceTimerRef.current);
  }, []);

  return {
    rows: rowsState.entities,
    rowsById: rowsState.ids,
    updateRow,
    updateRows,
    addRow,
    deleteRow,
    isSavePending,
    isRowSaving,
    autosaveStatus,
    flush,
  };
};

export default useAutosave;
