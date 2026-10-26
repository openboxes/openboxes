import React from 'react';

import { render, screen, within } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { ColumnsTable } from 'regression-tests/hooks/inboundV2/columnTestUtils';

import stockMovementApi from 'api/services/StockMovementApi';
import useInboundSendTable from 'hooks/inboundV2/send/useInboundSendTable';

import '@testing-library/jest-dom';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(() => 'en'),
}));
jest.mock('react-router-dom', () => ({
  useParams: () => ({ stockMovementId: 'sm-1' }),
}));
jest.mock('api/services/StockMovementApi', () => ({
  getStockMovementItems: jest.fn(),
}));
jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('components/DataTable', () => ({
  TableCell: jest.requireActual('regression-tests/hooks/inboundV2/columnTestUtils').MockTableCell,
}));
jest.mock('components/DataTable/TableHeaderCell', () => jest.requireActual('regression-tests/hooks/inboundV2/columnTestUtils').MockTableHeaderCell);

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const item = (overrides = {}) => ({
  palletName: null,
  boxName: null,
  productCode: 'P1',
  product: { name: 'Aspirin', displayNames: { default: 'Aspirin 100mg' } },
  inventoryItem: { lotNumber: 'L-1', expirationDate: '2026-12-31T00:00:00' },
  quantityRequested: 12,
  recipient: { name: 'Jane' },
  ...overrides,
});

const hiddenColumns = (columns) => columns
  .filter((column) => column.meta?.hide)
  .map((column) => column.accessorKey);

describe('useInboundSendTable', () => {
  const renderSendTable = async (items) => {
    stockMovementApi.getStockMovementItems.mockResolvedValue({ data: { data: items } });
    const hook = renderHook(() => useInboundSendTable());
    await flushPromises();
    return hook;
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('fetches the send-step items and stops loading', async () => {
    const { result } = await renderSendTable([item()]);
    expect(stockMovementApi.getStockMovementItems).toHaveBeenCalledWith('sm-1', { stepNumber: 6 });
    expect(result.current.loading).toBe(false);
    expect(result.current.tableData).toEqual([item()]);
    expect(mockSpinner.show).toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('keeps the pack level columns visible while loading', () => {
    stockMovementApi.getStockMovementItems.mockReturnValue(new Promise(() => {}));
    const { result } = renderHook(() => useInboundSendTable());
    expect(result.current.loading).toBe(true);
    expect(hiddenColumns(result.current.columns)).toEqual([]);
  });

  it('hides the pack level columns when no line uses them', async () => {
    const { result } = await renderSendTable([item()]);
    expect(hiddenColumns(result.current.columns)).toEqual(['palletName', 'boxName']);
  });

  it('shows only the pack levels some line uses', async () => {
    const { result } = await renderSendTable([item(), item({ boxName: 'Box 1' })]);
    expect(hiddenColumns(result.current.columns)).toEqual(['palletName']);
  });

  it('stops loading even when the request fails', async () => {
    stockMovementApi.getStockMovementItems.mockRejectedValue(new Error('down'));
    const { result } = renderHook(() => useInboundSendTable());
    await flushPromises();
    expect(result.current.loading).toBe(false);
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('renders the headers and each line read-only', async () => {
    const { result } = await renderSendTable([]);
    render(<ColumnsTable
      columns={result.current.columns}
      data={[item({ palletName: 'Pallet A', boxName: 'Box 1' })]}
    />);

    expect(screen.getAllByTestId('header-cell').map((cell) => cell.textContent)).toEqual([
      'Pack Level 1', 'Pack Level 2', 'Code', 'Product', 'Lot', 'Expiry', 'Qty Picked', 'Recipient',
    ]);
    const row = within(screen.getByTestId('row-0'));
    expect(row.getAllByTestId('label-field').map((field) => field.textContent))
      .toEqual(['Pallet A', 'Box 1', 'P1', 'L-1', '31/Dec/2026', '12', 'Jane']);
    expect(row.getByLabelText('Expiry')).toHaveTextContent('31/Dec/2026');
    expect(row.getByText('Aspirin 100mg')).toBeInTheDocument();
    expect(row.getByLabelText('Qty Picked').parentElement).toHaveAttribute('data-tooltip', '12');
  });
});
