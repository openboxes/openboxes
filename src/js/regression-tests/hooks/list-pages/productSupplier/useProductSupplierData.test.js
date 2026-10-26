import { renderHook } from '@testing-library/react-hooks';
import { useDispatch } from 'react-redux';
import { useParams } from 'react-router-dom';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import useProductSupplierData from 'hooks/list-pages/productSupplier/useProductSupplierData';
import apiClient from 'utils/apiClient';

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useDispatch: jest.fn(),
}));
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useParams: jest.fn(),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useProductSupplierData', () => {
  let dispatch;

  beforeEach(() => {
    jest.clearAllMocks();
    dispatch = jest.fn();
    useDispatch.mockReturnValue(dispatch);
  });

  it('fetches the product source from the route id behind a spinner', async () => {
    useParams.mockReturnValue({ productSupplierId: 'ps-1' });
    apiClient.get.mockResolvedValue({ data: { data: { id: 'ps-1', code: 'PS1' } } });
    const { result } = renderHook(() => useProductSupplierData());
    expect(result.current.productSupplier).toBeNull();

    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith('/api/productSuppliers/ps-1');
    expect(result.current.productSupplier).toEqual({ id: 'ps-1', code: 'PS1' });
    expect(dispatch.mock.calls.map(([action]) => action.type))
      .toEqual([SHOW_SPINNER, HIDE_SPINNER]);
  });

  it('does not fetch without an id in the route', () => {
    useParams.mockReturnValue({});
    const { result } = renderHook(() => useProductSupplierData());

    expect(apiClient.get).not.toHaveBeenCalled();
    expect(result.current.productSupplier).toBeNull();
  });
});
