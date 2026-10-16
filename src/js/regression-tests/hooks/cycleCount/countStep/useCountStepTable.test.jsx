import React from 'react';

import { render } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react-hooks';

import cycleCountColumn from 'consts/cycleCountColumn';
import useCountStepTable from 'hooks/cycleCount/countStep/useCountStepTable';

import '@testing-library/jest-dom';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useSelector: (selector) => selector(mockState),
}));
jest.mock('actions', () => ({
  removeRow: jest.fn((...args) => ({ type: 'REMOVE_ROW', args })),
}));
// Stubbed cells: the tests read the props each column passes to them.
jest.mock('components/cycleCount/tableCell/ActionsCell', () => () => null);
jest.mock('components/cycleCount/tableCell/BinLocationCell', () => () => null);
jest.mock('components/cycleCount/tableCell/CommentCell', () => () => null);
jest.mock('components/cycleCount/tableCell/ExpirationDateCell', () => () => null);
jest.mock('components/cycleCount/tableCell/LotNumberCell', () => () => null);
jest.mock('components/cycleCount/tableCell/QuantityCell', () => () => null);
jest.mock('components/cycleCount/tableHeader/HeaderCell', () => ({ defaultMessage }) => <span>{defaultMessage}</span>);
jest.mock('components/DataTable/TableHeaderCell', () => () => <span>actions-header</span>);

const buildState = (locationId, supportedActivities) => ({
  session: { currentLocation: { id: locationId, supportedActivities } },
  countWorkflow: {
    entities: {
      'cc-1': { cycleCountItems: [{ id: 'item-1' }, { id: 'item-2' }] },
    },
  },
});

const cellProps = (column, original = 'item-1', id = '0') =>
  column.cell({ row: { original, id } }).props;

describe('useCountStepTable', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderTable = (props = {}) => renderHook(() => useCountStepTable({
    cycleCountId: 'cc-1',
    isStepEditable: true,
    isFormDisabled: false,
    ...props,
  }));

  it('returns the item ids and count of the given cycle count', () => {
    mockState = buildState('loc-t1', ['PUTAWAY_STOCK', 'PICK_STOCK']);
    const { result } = renderTable();

    expect(result.current.cycleCountItemIds).toEqual(['item-1', 'item-2']);
    expect(result.current.cycleCountItemsTotalCount).toBe(2);
  });

  it('returns no rows for an unknown cycle count', () => {
    mockState = buildState('loc-t1', []);
    const { result } = renderTable({ cycleCountId: 'missing' });

    expect(result.current.cycleCountItemIds).toEqual([]);
    expect(result.current.cycleCountItemsTotalCount).toBe(0);
  });

  it('builds the columns in order with their headers and widths', () => {
    mockState = buildState('loc-t2', ['PUTAWAY_STOCK', 'PICK_STOCK']);
    const { result } = renderTable();
    const { columns } = result.current;

    expect(columns.map((column) => column.id ?? column.accessorKey)).toEqual([
      cycleCountColumn.BIN_LOCATION,
      cycleCountColumn.LOT_NUMBER,
      cycleCountColumn.EXPIRATION_DATE,
      cycleCountColumn.QUANTITY_COUNTED,
      cycleCountColumn.COMMENT,
      cycleCountColumn.ACTIONS,
    ]);
    const headers = columns.map((column) => render(column.header()).container.textContent);
    expect(headers).toEqual([
      'Bin Location', 'Serial / Lot Number', 'Expiration Date', 'Quantity Counted', 'Comment', 'actions-header',
    ]);
    expect(columns.map((column) => column.meta.flexWidth)).toEqual([100, 100, 100, 50, 100, 25]);
    expect(columns[2].meta.getCellContext()).toEqual({ className: 'split-table-right' });
    expect(columns[5].meta.getCellContext()).toEqual({ className: 'count-step-actions' });
  });

  it('shows the bin location column only where putaway and picking are supported', () => {
    mockState = buildState('loc-t3', ['PUTAWAY_STOCK', 'PICK_STOCK']);
    expect(renderTable().result.current.columns[0].meta.hide).toBe(false);

    mockState = buildState('loc-t4', ['PUTAWAY_STOCK']);
    const { result } = renderTable();
    expect(result.current.columns[0].meta.hide).toBe(true);
    expect(cellProps(result.current.columns[0]).showBinLocation).toBe(false);
  });

  it('displays the bin location name through the accessor', () => {
    mockState = buildState('loc-t5', []);
    const { result } = renderTable();

    expect(result.current.columns[0].accessorFn({ binLocation: { name: 'A-01' } })).toBe('A-01');
  });

  it('passes the row, cycle count and editability to every cell', () => {
    mockState = buildState('loc-t6', ['PUTAWAY_STOCK', 'PICK_STOCK']);
    const { result } = renderTable({ isStepEditable: false, isFormDisabled: true });
    const [bin, lot, expiry, quantity, comment, actions] = result.current.columns;

    expect(cellProps(bin, 'item-2', '1')).toEqual({
      index: '1', id: 'item-2', cycleCountId: 'cc-1', showBinLocation: true, isStepEditable: false,
    });
    expect(cellProps(lot)).toEqual(expect.objectContaining({
      index: '0', id: 'item-1', cycleCountId: 'cc-1', isStepEditable: false,
    }));
    expect(cellProps(expiry)).toEqual(expect.objectContaining({
      id: 'item-1', disabledExpirationDateFields: {}, isStepEditable: false,
    }));
    expect(cellProps(quantity)).toEqual({
      index: '0', id: 'item-1', cycleCountId: 'cc-1', isStepEditable: false,
    });
    expect(cellProps(comment)).toEqual({ id: 'item-1', cycleCountId: 'cc-1', isStepEditable: false });
    expect(cellProps(actions)).toEqual(expect.objectContaining({
      id: 'item-1', isStepEditable: false, isFormDisabled: true,
    }));
  });

  it('removes a row through the actions cell', () => {
    mockState = buildState('loc-t7', []);
    const { result } = renderTable();

    cellProps(result.current.columns[5], 'item-2').removeRow();

    expect(mockDispatch).toHaveBeenCalledWith({ type: 'REMOVE_ROW', args: ['cc-1', 'item-2', 'loc-t7'] });
  });

  it('rebuilds the expiration date cells when the lot cell disables a field', () => {
    mockState = buildState('loc-t8', []);
    const { result } = renderTable();

    act(() => cellProps(result.current.columns[1]).setDisabledExpirationDateFields({ 'item-1': true }));

    expect(cellProps(result.current.columns[2]).disabledExpirationDateFields).toEqual({ 'item-1': true });
  });
});
