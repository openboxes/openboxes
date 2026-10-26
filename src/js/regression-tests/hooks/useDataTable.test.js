import { renderHook } from '@testing-library/react-hooks';

import useDataTable from 'hooks/useDataTable';

// Not covered on OBAF (OBAF lacks the code): 'shows the sub rows collapsed by
// default' and 'shows the sub rows expanded when asked to' (no getSubRows/defaultExpandedSubRows).

describe('useDataTable', () => {
  const columns = [
    { accessorKey: 'product.code', meta: { pinned: 'left' } },
    { id: 'name', accessorKey: 'product.name' },
    { accessorKey: 'quantity' },
    { id: 'actions', meta: { pinned: 'right' } },
  ];

  const data = [
    {
      product: { code: 'A1', name: 'Amoxicillin' },
      quantity: 4,
      subRows: [{ product: { code: 'A1-L1' }, quantity: 1 }],
    },
    { product: { code: 'B2', name: 'Bandage' }, quantity: 2 },
  ];

  const renderTable = (props) =>
    renderHook(() => useDataTable({ columns, data, ...props })).result.current;

  it('pins the columns marked as pinned, by their id', () => {
    const { table } = renderTable();

    // The table names an accessor column after its key, with the dots replaced
    expect(table.getState().columnPinning).toEqual({ left: ['product_code'], right: ['actions'] });
    expect(table.getLeftLeafColumns().map(({ id }) => id)).toEqual(['product_code']);
    expect(table.getRightLeafColumns().map(({ id }) => id)).toEqual(['actions']);
  });

  it('shows the given rows as they are, leaving sorting, filtering and paging to the server', () => {
    const { table } = renderTable();

    expect(table.getRowModel().rows.map((row) => row.original.product.code)).toEqual(['A1', 'B2']);
    expect(table.options.manualSorting).toBe(true);
    expect(table.options.manualFiltering).toBe(true);
    expect(table.options.manualPagination).toBe(true);
  });

  it('passes the meta and the default column on to the table', () => {
    const meta = { updateRow: jest.fn() };
    const { table } = renderTable({ meta, defaultColumn: { size: 120 } });

    expect(table.options.meta).toBe(meta);
    expect(table.getColumn('quantity').getSize()).toBe(120);
  });

  it('provides the empty and loading messages', () => {
    const { defaultEmptyTableMessage, defaultLoadingTableMessage } = renderTable();

    expect(defaultEmptyTableMessage).toEqual({
      id: 'react.default.table.emptyTable.label',
      defaultMessage: 'No rows match the given criteria',
    });
    expect(defaultLoadingTableMessage).toEqual({
      id: 'react.default.loading.label',
      defaultMessage: 'Loading...',
    });
  });
});
