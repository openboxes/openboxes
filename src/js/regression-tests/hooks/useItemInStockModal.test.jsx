import React from 'react';

import { render } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';

import { HIDE_SPINNER } from 'actions/types';
import outboundImportColumn from 'consts/outboundImportColumn';
import useItemInStockModal from 'hooks/useItemInStockModal';
import apiClient from 'utils/apiClient';

jest.mock('react-redux', () => ({
  useDispatch: jest.fn(),
  useSelector: jest.fn(),
  connect: jest.fn(() => (Component) => Component),
}));
jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: { get: jest.fn() },
  apiClientCustomResponseHandler: { get: jest.fn() },
}));
jest.mock('components/DataTable', () => ({
  // eslint-disable-next-line react/prop-types
  TableCell: ({ children }) => <div data-testid="cell">{children}</div>,
}));
// eslint-disable-next-line react/prop-types
jest.mock('components/DataTable/TableHeaderCell', () => ({ children }) => <div>{children}</div>);

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('useItemInStockModal', () => {
  const dispatch = jest.fn();
  let currentLocation;

  const items = [{ binLocation: { name: 'A-01' }, lotNumber: 'L1', quantityOnHand: 4 }];

  beforeEach(() => {
    jest.clearAllMocks();
    currentLocation = { id: 'loc-1' };
    useDispatch.mockReturnValue(dispatch);
    useSelector.mockImplementation((selector) => selector({ session: { currentLocation } }));
    apiClient.get.mockResolvedValue({ data: { data: items } });
  });

  it('fetches the available items of the product at the current location', async () => {
    const { result } = renderHook(() => useItemInStockModal({ productId: 'prod-1' }));
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith('/api/products/availableItems', expect.objectContaining({
      params: { 'product.id': 'prod-1', 'location.id': 'loc-1' },
    }));
    expect(result.current.data).toEqual(items);
    expect(dispatch).toHaveBeenCalledWith(expect.objectContaining({ type: HIDE_SPINNER }));
  });

  it('fetches nothing without a product or a location', async () => {
    renderHook(() => useItemInStockModal({ productId: undefined }));
    currentLocation = undefined;
    renderHook(() => useItemInStockModal({ productId: 'prod-1' }));
    await flushPromises();

    expect(apiClient.get).not.toHaveBeenCalled();
  });

  it('fetches again for another product', async () => {
    const { rerender } = renderHook((props) => useItemInStockModal(props), {
      initialProps: { productId: 'prod-1' },
    });
    await flushPromises();

    rerender({ productId: 'prod-2' });
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledTimes(2);
    expect(apiClient.get.mock.calls[1][1].params['product.id']).toBe('prod-2');
  });

  it('lists the bin, lot, expiry and quantities of every item', () => {
    const { result } = renderHook(() => useItemInStockModal({ productId: undefined }));
    const { columns } = result.current;

    expect(columns.map(({ accessorKey }) => accessorKey)).toEqual([
      outboundImportColumn.BIN_LOCATION,
      outboundImportColumn.LOT_NUMBER,
      outboundImportColumn.EXPIRATION_DATE,
      outboundImportColumn.QUANTITY_ON_HAND,
      outboundImportColumn.QUANTITY_AVAILABLE,
    ]);
    expect(columns.map((column) => render(column.header()).container.textContent))
      .toEqual(['Bin Location', 'Lot', 'Expires', 'On Hand', 'Available']);
    expect(columns.slice(0, 3).map((column, index) =>
      render(column.cell({ getValue: () => ['A-01', 'L1', '05/01/2028'][index] })).container.textContent))
      .toEqual(['A-01', 'L1', '05/01/2028']);
  });

  it.each([
    [12, '12'],
    [0, '0'],
    [undefined, '0'],
    [null, '0'],
  ])('shows the quantity %p as %p', (quantity, shown) => {
    const { result } = renderHook(() => useItemInStockModal({ productId: undefined }));
    const [, , , onHand, available] = result.current.columns;

    expect(render(onHand.cell({ getValue: () => quantity })).container.textContent).toBe(shown);
    expect(render(available.cell({ getValue: () => quantity })).container.textContent).toBe(shown);
  });
});
