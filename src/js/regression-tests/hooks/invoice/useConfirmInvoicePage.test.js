import { act, renderHook } from '@testing-library/react-hooks';

import {
  INVOICE_BY_ID, INVOICE_ITEMS, INVOICE_POST, INVOICE_SUBMIT,
} from 'api/urls';
import useConfirmInvoicePage from 'hooks/invoice/useConfirmInvoicePage';
import apiClient from 'utils/apiClient';

const mockDispatch = jest.fn();
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector({ session: { pageSize: 25, isSuperuser: true } })),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const itemsResponse = (data, totalCount = data.length) => ({ data: { data, totalCount } });

describe('useConfirmInvoicePage', () => {
  const originalLocation = window.location;

  beforeEach(() => {
    jest.clearAllMocks();
    apiClient.get.mockImplementation((url) => {
      if (url === INVOICE_BY_ID('inv-1')) {
        return Promise.resolve({ data: { data: { documents: ['doc'], totalValue: -1234.5 } } });
      }
      return Promise.resolve(itemsResponse([]));
    });
    apiClient.post.mockResolvedValue({});
  });

  afterEach(() => {
    window.location = originalLocation;
  });

  const render = (initialValues = { id: 'inv-1', totalValue: 10 }) =>
    renderHook(() => useConfirmInvoicePage({ initialValues }));

  it('fetches the invoice on mount and formats its total in accounting style', async () => {
    const { result } = render();

    expect(result.current.totalValue).toBe('10.00');
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith(INVOICE_BY_ID('inv-1'), undefined);
    expect(result.current.stateValues.documents).toEqual(['doc']);
    expect(result.current.totalValue).toBe('(1,234.50)');
    expect(result.current.isSuperuser).toBe(true);
    expect(mockDispatch.mock.calls.map(([action]) => action.type)).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('does not fetch without an invoice id', () => {
    const { result } = render({ totalValue: 0 });

    expect(apiClient.get).not.toHaveBeenCalled();
    expect(result.current.totalValue).toBe('0.00');
  });

  it('loads a page of items using the page size or the requested range', async () => {
    const { result } = render();

    await result.current.loadMoreRows({ startIndex: 0 });
    expect(apiClient.get).toHaveBeenLastCalledWith(INVOICE_ITEMS('inv-1'), { params: { offset: 0, max: 25 } });

    await result.current.loadMoreRows({ startIndex: 10, stopIndex: 14 });
    expect(apiClient.get).toHaveBeenLastCalledWith(INVOICE_ITEMS('inv-1'), { params: { offset: 10, max: 5 } });
  });

  it('appends loaded items at their index or replaces them when overriding', () => {
    const { result } = render();

    act(() => result.current.setInvoiceItems(itemsResponse([{ id: 'a' }, { id: 'b' }], 4)));
    act(() => result.current.setInvoiceItems(itemsResponse([{ id: 'c' }, { id: 'd' }], 4), false, 2));

    expect(result.current.stateValues.invoiceItems.map((item) => item.id)).toEqual(['a', 'b', 'c', 'd']);
    expect(result.current.stateValues.totalCount).toBe(4);
    expect(result.current.invoiceItemsMap.get(3)).toEqual({ id: 'd' });

    act(() => result.current.setInvoiceItems(itemsResponse([{ id: 'z' }])));

    expect(result.current.stateValues.invoiceItems).toEqual([{ id: 'z' }]);
    expect(result.current.stateValues.totalCount).toBe(1);
  });

  it('updates a single item field locally and forwards the change', () => {
    const { result } = render();
    const updateRow = jest.fn();
    act(() => result.current.setInvoiceItems(itemsResponse([{ id: 'a', quantity: 1 }, { id: 'b', quantity: 2 }])));

    act(() => result.current.updateInvoiceItemData(updateRow)('b', 'quantity')(7));

    expect(updateRow).toHaveBeenCalledWith('b', { quantity: 7 });
    expect(result.current.stateValues.invoiceItems).toEqual([{ id: 'a', quantity: 1 }, { id: 'b', quantity: 7 }]);

    act(() => result.current.updateInvoiceItemData()('a', 'quantity')(3));

    expect(result.current.stateValues.invoiceItems[0]).toEqual({ id: 'a', quantity: 3 });
  });

  it('refetches the invoice and all items, then runs the callback', async () => {
    const { result } = render();
    act(() => result.current.setInvoiceItems(itemsResponse([{ id: 'a' }], 3)));
    apiClient.get.mockClear();
    apiClient.get.mockResolvedValueOnce({ data: { data: { documents: [], totalValue: 5 } } });
    apiClient.get.mockResolvedValueOnce(itemsResponse([{ id: 'x' }, { id: 'y' }]));
    const callback = jest.fn();

    await result.current.refetchData({ callback, overrideInvoiceItems: true });
    await flushPromises();

    expect(apiClient.get).toHaveBeenNthCalledWith(1, INVOICE_BY_ID('inv-1'), undefined);
    expect(apiClient.get).toHaveBeenNthCalledWith(2, INVOICE_ITEMS('inv-1'), { params: { offset: 0, max: 4 } });
    expect(callback).toHaveBeenCalledTimes(1);
    expect(result.current.stateValues.invoiceItems).toEqual([{ id: 'x' }, { id: 'y' }]);
  });

  it('still runs the callback when refetching items fails', async () => {
    const { result } = render();
    await flushPromises();
    apiClient.get.mockResolvedValueOnce({ data: { data: { documents: [], totalValue: 5 } } });
    apiClient.get.mockRejectedValueOnce(new Error('offline'));
    const callback = jest.fn();

    await expect(result.current.refetchData({ callback })).rejects.toThrow('offline');

    expect(callback).toHaveBeenCalledTimes(1);
    expect(mockDispatch).toHaveBeenLastCalledWith({ type: 'HIDE_SPINNER', payload: false });
  });

  it.each([
    ['submitInvoice', INVOICE_SUBMIT],
    ['postInvoice', INVOICE_POST],
  ])('%s calls the API and redirects to the invoice page', async (action, url) => {
    delete window.location;
    window.location = {};
    const { result } = render();

    result.current[action]();
    await flushPromises();

    expect(apiClient.post).toHaveBeenCalledWith(url('inv-1'));
    expect(window.location).toMatch(/\/invoice\/show\/inv-1$/);
  });
});
