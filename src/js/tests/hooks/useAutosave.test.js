import { act, renderHook } from '@testing-library/react-hooks';

import { AutosaveStatus } from 'consts/autosaveStatuses';
import RowSaveStatus from 'consts/rowSaveStatus';
import useAutosave from 'hooks/useAutosave';
import { normalizeData } from 'utils/normalizationUtils';

import '@testing-library/jest-dom';

const DEBOUNCE_TIME = 2000;
const RETRY_DELAY = 5000;

jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector()),
}));
jest.mock('selectors', () => ({
  getAutosaveBatchSize: jest.fn(() => 4),
  getAutosaveDebounceTime: jest.fn(() => 2000),
  getAutosaveMaxRetries: jest.fn(() => 3),
  getAutosaveRetryDelay: jest.fn(() => 5000),
  getAutosaveMaxRetryDelay: jest.fn(() => 40000),
}));

const createDeferred = () => {
  let resolve;
  let reject;
  const promise = new Promise((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
};

const buildRow = (rowId, overrides = {}) => ({
  rowId,
  receiptItemId: null,
  quantityReceiving: null,
  initialQuantityReceiving: null,
  saveStatus: RowSaveStatus.SAVED,
  ...overrides,
});

const buildInitialRows = (rows) => normalizeData(rows, 'rowId');

const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useAutosave', () => {
  let deferreds;
  let updateFn;

  const renderUseAutosave = ({
    requests, flushOptions, retryOptions, ...options
  } = {}) => renderHook(
    (props) => useAutosave(props),
    {
      initialProps: {
        initialRows: buildInitialRows([buildRow('row-1'), buildRow('row-2'), buildRow('row-3')]),
        requests: { updateFn, ...requests },
        flushOptions: { debounceTime: DEBOUNCE_TIME, ...flushOptions },
        retryOptions: { retryDelay: RETRY_DELAY, ...retryOptions },
        ...options,
      },
    },
  );

  const receivingReconcilers = {
    reconcileRow: (row, line) => ({
      receiptItemId: line.id,
      quantityReceiving: line.quantityReceived,
      initialQuantityReceiving: line.quantityReceived,
    }),
    reconcileStaleRow: (row, line) => ({ receiptItemId: line.id }),
  };

  beforeEach(() => {
    jest.useFakeTimers();
    deferreds = [];
    updateFn = jest.fn(() => {
      const deferred = createDeferred();
      deferreds.push(deferred);
      return deferred.promise;
    });
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('debounces the save and tracks row statuses through PENDING, SAVING and SAVED', async () => {
    const { result } = renderUseAutosave();

    act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));

    expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.PENDING);
    expect(result.current.isSavePending).toBe(true);
    expect(result.current.autosaveStatus).toBe(AutosaveStatus.SAVING);

    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME - 1));
    expect(updateFn).not.toHaveBeenCalled();

    act(() => jest.advanceTimersByTime(1));
    expect(updateFn).toHaveBeenCalledTimes(1);
    expect(updateFn).toHaveBeenCalledWith([
      expect.objectContaining({ rowId: 'row-1', quantityReceiving: 5 }),
    ]);
    expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.SAVING);
    expect(result.current.isRowSaving('row-1')).toBe(true);

    deferreds[0].resolve([{ rowId: 'row-1', quantityReceiving: 5 }]);
    await flushPromises();

    expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.SAVED);
    expect(result.current.isSavePending).toBe(false);
    expect(result.current.autosaveStatus).toBe(AutosaveStatus.SAVED);
  });

  it('flushes immediately when the batch size is reached', () => {
    const { result } = renderUseAutosave({ flushOptions: { batchSize: 2 } });

    act(() => {
      result.current.updateRow('row-1', { quantityReceiving: 1 });
      result.current.updateRow('row-2', { quantityReceiving: 2 });
    });
    expect(updateFn).not.toHaveBeenCalled();

    // Zero delay instead of a synchronous call, so a burst of edits ends up
    // in one batch request.
    act(() => jest.advanceTimersByTime(0));

    expect(updateFn).toHaveBeenCalledTimes(1);
    expect(updateFn).toHaveBeenCalledWith([
      expect.objectContaining({ rowId: 'row-1', quantityReceiving: 1 }),
      expect.objectContaining({ rowId: 'row-2', quantityReceiving: 2 }),
    ]);
  });

  it('does not let a response of an older request overwrite a newer edit', async () => {
    const { result } = renderUseAutosave({ rowOptions: receivingReconcilers });

    // Edit 1
    act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));
    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
    expect(updateFn).toHaveBeenCalledTimes(1);

    // Edit 2 happens while the request is still running...
    act(() => result.current.updateRow('row-1', { quantityReceiving: 7 }));

    deferreds[0].resolve([{ rowId: 'row-1', id: 'receipt-item-1', quantityReceived: 5 }]);
    await flushPromises();

    expect(result.current.rows['row-1']).toEqual(expect.objectContaining({
      quantityReceiving: 7,
      receiptItemId: 'receipt-item-1',
      saveStatus: RowSaveStatus.PENDING,
    }));

    // The next flush sends the newest value against the saved receipt item.
    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
    expect(updateFn).toHaveBeenCalledTimes(2);
    expect(updateFn).toHaveBeenLastCalledWith([
      expect.objectContaining({
        rowId: 'row-1',
        quantityReceiving: 7,
        receiptItemId: 'receipt-item-1',
      }),
    ]);
  });

  it('marks a failed row as ERROR and retries it with the current data', async () => {
    const { result } = renderUseAutosave();

    act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));
    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
    deferreds[0].reject(new Error('server error'));
    await flushPromises();

    expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.ERROR);
    expect(result.current.autosaveStatus).toBe(AutosaveStatus.ERROR);

    // The row is edited before the retry fires - the retry sends the newest value.
    act(() => result.current.updateRow('row-1', { quantityReceiving: 8 }));
    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));

    expect(updateFn).toHaveBeenCalledTimes(2);
    expect(updateFn).toHaveBeenLastCalledWith([
      expect.objectContaining({ rowId: 'row-1', quantityReceiving: 8 }),
    ]);

    deferreds[1].resolve([{ rowId: 'row-1' }]);
    await flushPromises();
    expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.SAVED);
  });

  it('retries a server failure with exponential backoff and gives up after maxRetries', async () => {
    const { result } = renderUseAutosave({ retryOptions: { maxRetries: 1 } });

    act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));
    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
    deferreds[0].reject(new Error('server error'));
    await flushPromises();
    expect(updateFn).toHaveBeenCalledTimes(1);

    // First retry
    act(() => jest.advanceTimersByTime(RETRY_DELAY));
    expect(updateFn).toHaveBeenCalledTimes(2);
    deferreds[1].reject(new Error('server error'));
    await flushPromises();

    // After exhausting maxRetries the row is left in ERROR, no more requests.
    act(() => jest.advanceTimersByTime(RETRY_DELAY * 10));
    expect(updateFn).toHaveBeenCalledTimes(2);
    expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.ERROR);

    // A new edit resets the retry budget and saving works again.
    act(() => result.current.updateRow('row-1', { quantityReceiving: 6 }));
    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
    expect(updateFn).toHaveBeenCalledTimes(3);
  });

  describe('network failures', () => {
    // Shape of an axios error without a response (connection lost, timeout).
    const networkError = () => Object.assign(new Error('Network Error'), { isAxiosError: true });

    it('retries forever with a limited delay, without using up the retry budget', async () => {
      const { result } = renderUseAutosave({
        retryOptions: { maxRetries: 1, maxRetryDelay: RETRY_DELAY * 2 },
      });

      act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));
      act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
      deferreds[0].reject(networkError());
      await flushPromises();
      expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.ERROR);

      act(() => jest.advanceTimersByTime(RETRY_DELAY));
      expect(updateFn).toHaveBeenCalledTimes(2);
      deferreds[1].reject(networkError());
      await flushPromises();

      act(() => jest.advanceTimersByTime(RETRY_DELAY * 2));
      expect(updateFn).toHaveBeenCalledTimes(3);
      deferreds[2].reject(networkError());
      await flushPromises();

      act(() => jest.advanceTimersByTime(RETRY_DELAY * 2));
      expect(updateFn).toHaveBeenCalledTimes(4);

      deferreds[3].resolve([{ rowId: 'row-1' }]);
      await flushPromises();
      expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.SAVED);
    });

    it('flushes immediately when the connection returns (online event)', async () => {
      const { result } = renderUseAutosave();

      act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));
      act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
      deferreds[0].reject(networkError());
      await flushPromises();
      expect(updateFn).toHaveBeenCalledTimes(1);

      // The browser reports the connection back
      act(() => window.dispatchEvent(new Event('online')));
      expect(updateFn).toHaveBeenCalledTimes(2);

      deferreds[1].resolve([{ rowId: 'row-1' }]);
      await flushPromises();
      expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.SAVED);
    });

    it('flush() gives up on a persistent network failure instead of looping forever', async () => {
      const failingUpdateFn = jest.fn(() => Promise.reject(networkError()));
      const { result } = renderUseAutosave({
        requests: { updateFn: failingUpdateFn },
        retryOptions: { maxRetries: 0 },
      });

      act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));

      let flushPromise;
      act(() => {
        flushPromise = result.current.flush();
      });
      await expect(flushPromise).rejects.toThrow();
      await flushPromises();
      expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.ERROR);
    });
  });

  it('adds a new row with a generated id and matches the response back to it', async () => {
    const { result } = renderUseAutosave({ rowOptions: receivingReconcilers });

    let rowId;
    act(() => {
      rowId = result.current.addRow({ shipmentItemId: 'shipment-item-1', quantityReceiving: 2 });
    });

    expect(rowId).toEqual(expect.any(String));
    expect(result.current.rowsById).toContain(rowId);
    expect(result.current.rows[rowId].saveStatus).toBe(RowSaveStatus.PENDING);

    act(() => jest.advanceTimersByTime(DEBOUNCE_TIME));
    expect(updateFn).toHaveBeenCalledWith([
      expect.objectContaining({ rowId, quantityReceiving: 2 }),
    ]);

    deferreds[0].resolve([{ rowId, id: 'receipt-item-9', quantityReceived: 2 }]);
    await flushPromises();

    expect(result.current.rows[rowId]).toEqual(expect.objectContaining({
      receiptItemId: 'receipt-item-9',
      saveStatus: RowSaveStatus.SAVED,
    }));
  });

  describe('deleteRow', () => {
    it('ignores repeated delete clicks while the delete request is running', async () => {
      const deleteDeferred = createDeferred();
      const deleteFn = jest.fn(() => deleteDeferred.promise);
      const { result } = renderUseAutosave({ requests: { deleteFn } });

      act(() => {
        result.current.deleteRow('row-1');
        result.current.deleteRow('row-1');
        result.current.deleteRow('row-1');
      });

      expect(deleteFn).toHaveBeenCalledTimes(1);
      expect(result.current.rows['row-1'].isDeleteInProgress).toBe(true);
      expect(result.current.isRowSaving('row-1')).toBe(true);

      deleteDeferred.resolve();
      await flushPromises();
      expect(result.current.rows['row-1']).toBeUndefined();
      expect(result.current.rowsById).not.toContain('row-1');
    });

    it('restores the row with ERROR status when the delete request fails', async () => {
      const deleteFn = jest.fn(() => Promise.reject(new Error('delete failed')));
      const { result } = renderUseAutosave({ requests: { deleteFn } });

      act(() => {
        result.current.deleteRow('row-1');
      });
      await flushPromises();

      expect(result.current.rows['row-1']).toEqual(expect.objectContaining({
        isDeleteInProgress: false,
        saveStatus: RowSaveStatus.ERROR,
      }));
      expect(result.current.rowsById).toContain('row-1');
    });
  });

  describe('flush', () => {
    it('sends pending edits immediately and resolves once everything settled', async () => {
      const { result } = renderUseAutosave();

      act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));

      let flushPromise;
      act(() => {
        flushPromise = result.current.flush();
      });
      expect(updateFn).toHaveBeenCalledTimes(1);

      deferreds[0].resolve([{ rowId: 'row-1' }]);
      await flushPromise;
      await flushPromises();

      expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.SAVED);
      expect(result.current.isSavePending).toBe(false);
    });

    it('rejects when a row still cannot be saved', async () => {
      const failingUpdateFn = jest.fn(() => Promise.reject(new Error('server error')));
      const { result } = renderUseAutosave({
        requests: { updateFn: failingUpdateFn },
        retryOptions: { maxRetries: 0 },
      });

      act(() => result.current.updateRow('row-1', { quantityReceiving: 5 }));

      let flushPromise;
      act(() => {
        flushPromise = result.current.flush();
      });
      await expect(flushPromise).rejects.toThrow();
      await flushPromises();
      expect(result.current.rows['row-1'].saveStatus).toBe(RowSaveStatus.ERROR);
    });
  });
});
