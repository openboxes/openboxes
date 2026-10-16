import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react-hooks';

import { FETCH_LOT_NUMBERS_BY_PRODUCT_IDS } from 'actions/types';
import { LOT_NUMBERS_WITH_EXPIRATION_DATE } from 'api/urls';
import useOutboundImportItems from 'hooks/outboundImport/useOutboundImportItems';
import apiClient from 'utils/apiClient';

import '@testing-library/jest-dom';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('components/DataTable/DateCell', () => ({ value, defaultValue }) => (
  <span data-testid="date-cell">{value ?? defaultValue}</span>
));

const items = [
  {
    fileRowNumber: 2,
    product: {
      id: 'p1', productCode: 'P1', name: 'Paracetamol', color: 'red',
    },
    lotNumber: 'LOT-A',
    quantityPicked: 5,
    palletName: 'Pallet 1',
  },
  {
    fileRowNumber: 3,
    product: { id: 'p2', productCode: 'P2', name: 'Ibuprofen' },
    lotNumber: 'LOT-B',
    quantityPicked: 0,
  },
];

const columnByAccessor = (columns, accessor) =>
  columns.find((column) => column.accessor === accessor);

describe('useOutboundImportItems', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = {
      session: { currentLocation: { hasBinLocationSupport: true } },
      lotNumbers: {
        lotNumbersWithExpiration: {
          p1: [{ lotNumber: 'LOT-A', expirationDate: '2027-01-01' }],
        },
      },
    };
  });

  it('fetches lot numbers for the products in the table', async () => {
    renderHook(() => useOutboundImportItems({ itemsInOrder: items }));

    expect(mockDispatch).toHaveBeenCalledTimes(1);
    const thunk = mockDispatch.mock.calls[0][0];
    const innerDispatch = jest.fn();
    apiClient.get.mockResolvedValue({ data: { data: { p1: [] } } });

    await thunk(innerDispatch);

    expect(apiClient.get).toHaveBeenCalledWith(
      LOT_NUMBERS_WITH_EXPIRATION_DATE,
      expect.objectContaining({ params: { productIds: ['p1', 'p2'] } }),
    );
    expect(innerDispatch).toHaveBeenCalledWith({
      type: FETCH_LOT_NUMBERS_BY_PRODUCT_IDS,
      payload: { p1: [] },
    });
  });

  it('does not fetch lot numbers for an empty table', () => {
    renderHook(() => useOutboundImportItems({ itemsInOrder: [] }));

    expect(mockDispatch).not.toHaveBeenCalled();
  });

  it('shows the pack level columns only when some row has a value and follows bin location support', () => {
    const { result } = renderHook(() => useOutboundImportItems({ itemsInOrder: items }));

    expect(columnByAccessor(result.current.columns, 'palletName').show).toBe(true);
    expect(columnByAccessor(result.current.columns, 'boxName').show).toBe(false);
    expect(columnByAccessor(result.current.columns, 'binLocation').show).toBe(true);

    mockState = { ...mockState, session: { currentLocation: { hasBinLocationSupport: false } } };
    const { result: withoutBins } = renderHook(
      () => useOutboundImportItems({ itemsInOrder: undefined }),
    );

    expect(columnByAccessor(withoutBins.current.columns, 'binLocation').show).toBe(false);
    expect(columnByAccessor(withoutBins.current.columns, 'palletName').show).toBe(false);
  });

  it('renders product, expiry and quantity cells from the row', () => {
    const { result } = renderHook(() => useOutboundImportItems({ itemsInOrder: items }));
    const { columns } = result.current;
    const ProductCell = columnByAccessor(columns, 'product.name').Cell;
    const ExpiryCell = columnByAccessor(columns, 'expirationDate').Cell;
    const QuantityCell = columnByAccessor(columns, 'quantityPicked').Cell;

    render(
      <>
        <ProductCell original={items[0]} value="Paracetamol" />
        <ExpiryCell original={items[0]} />
        <ExpiryCell original={items[1]} />
        <QuantityCell original={items[1]} value={0} />
      </>,
    );

    expect(screen.getAllByText('Paracetamol')[0].closest('[data-testid="table-cell"]'))
      .toHaveStyle({ color: 'red' });
    const dateCells = screen.getAllByTestId('date-cell');
    expect(dateCells[0]).toHaveTextContent('2027-01-01');
    expect(dateCells[1]).toHaveTextContent('');
    expect(screen.getByText('0')).toBeInTheDocument();
    expect(columnByAccessor(columns, 'product.productCode').getProps()).toEqual({ errorAccessor: 'product' });
  });

  it('marks the text cells invalid when the row carries an error for them', () => {
    const { result } = renderHook(() => useOutboundImportItems({ itemsInOrder: items }));
    const accessors = [
      'product.productCode', 'lotNumber', 'binLocation', 'recipient', 'palletName', 'boxName',
    ];
    const cells = accessors
      .map((accessor) => columnByAccessor(result.current.columns, accessor).Cell);

    const { container } = render(
      <>
        {cells.map((Cell, index) => (
          <Cell
            key={accessors[index]}
            original={items[0]}
            value={`value-${index}`}
            tdProps={{ rest: { error: index % 2 === 0 ? 'Invalid' : undefined } }}
          />
        ))}
      </>,
    );

    accessors.forEach((accessor, index) => {
      expect(screen.getAllByText(`value-${index}`).length).toBeGreaterThan(0);
    });
    expect(container.querySelectorAll('.invalid-cell')).toHaveLength(3);
  });

  it('opens the item in stock modal for the clicked row and closes it', () => {
    const { result } = renderHook(() => useOutboundImportItems({ itemsInOrder: items }));
    const SearchCell = result.current.columns[result.current.columns.length - 1].Cell;
    const { container } = render(<SearchCell original={items[1]} />);

    act(() => {
      fireEvent.click(container.querySelector('svg'));
    });

    expect(result.current.isItemInStockModalOpen).toBe(true);
    expect(result.current.selectedItemInStock).toEqual(items[1].product);
    expect(mockDispatch).toHaveBeenLastCalledWith({ type: 'SHOW_SPINNER', payload: true });

    act(() => result.current.handleCloseItemInStockModal());

    expect(result.current.isItemInStockModalOpen).toBe(false);
    expect(result.current.selectedItemInStock).toBeNull();
  });

  it('exposes the filtering helpers', () => {
    const { result } = renderHook(() => useOutboundImportItems({ itemsInOrder: items }));

    act(() => result.current.toggleFiltering());

    expect(result.current.isFiltered).toBe(true);
    expect(result.current.getFilteredTableData([items[1]], items)).toEqual([items[1]]);
  });
});
