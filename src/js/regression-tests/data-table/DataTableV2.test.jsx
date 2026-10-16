import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import testStore from 'regression-tests/data-table/testStore';

import DataTable from 'components/DataTable/v2/DataTable';
import ValueIndicator from 'components/DataTable/v2/ValueIndicator';
import valueIndicatorVariant from 'consts/valueIndicatorVariant';

import '@testing-library/jest-dom';

// The real client imports the store, which needs the reducers this test is still loading.
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// Adapted for OBAF (OBAF lacks the code: separator rows, arrow navigation,
// the footer row, the disabled style, the top scrollbar and sub rows).
// Removed: 'marks the arrow-navigable cells, and every cell when enabled for all fields',
// 'styles the table as disabled and adds the top scrollbar when asked', 'renders sub rows
// expanded by default, merging rows when asked', 'does not merge a collapsed row with its hidden
// sub rows'. Trimmed: the separator assertions of 'renders the headers, the cells and the
// separator rows, leaving out hidden columns', the extra 'disabled' row class of 'greys out
// disabled rows' (OBAF marks the row with bg-light only) and the footer row cell of 'shows the
// footer row and the pagination with the footer component' (no showFooter in OBAF).

const columns = [
  {
    accessorKey: 'code',
    header: () => 'Code',
    cell: ({ getValue }) => `#${getValue()}`,
    footer: () => 'Total',
    meta: { pinned: 'left', getCellContext: () => ({ className: 'code-cell' }), arrowNavigable: true },
  },
  {
    accessorKey: 'quantity',
    header: () => 'Quantity',
    footer: () => '6',
    meta: { renderSeparator: () => 'Separator' },
  },
  {
    accessorKey: 'secret', header: () => 'Hidden', meta: { hide: true },
  },
];

const data = [
  { code: 'A1', quantity: 4, secret: 'x' },
  {
    code: 'B2', quantity: 2, secret: 'y', meta: { isRowDisabled: true, label: 'react.row.disabled', defaultMessage: 'Locked' },
  },
  { code: 'S', isSeparator: true },
];

// What the list pages pass from their pagination hook; the table needs it whenever it paginates.
const paginationProps = {
  pagination: { pageIndex: 0, pageSize: 10 },
  onPageChange: () => {},
  onPageSizeChange: () => {},
  canPrevious: () => false,
  canNext: () => false,
  pages: 1,
  pageSizeSelectOptions: [10, 20],
};

const renderTable = (props = {}) => render(
  <Provider store={testStore()}>
    <DataTable
      columns={columns}
      data={data}
      totalCount={data.length}
      paginationProps={paginationProps}
      {...props}
    />
  </Provider>,
);

describe('DataTable v2', () => {
  it('renders the headers, the cells and the separator rows, leaving out hidden columns', () => {
    renderTable();

    expect(screen.getByText('Code')).toHaveClass('header-cell', 'code-cell');
    expect(screen.getByText('Quantity')).toBeInTheDocument();
    expect(screen.queryByText('Hidden')).toBeNull();
    expect(screen.getByText('#A1')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.queryByText('x')).toBeNull();
  });

  it('greys out disabled rows', () => {
    renderTable();

    const disabledCell = screen.getByText('#B2');
    expect(disabledCell).toHaveClass('text-muted');
    expect(disabledCell.closest('.rt-tr')).toHaveClass('bg-light');
    expect(screen.getByText('#A1')).not.toHaveClass('text-muted');
  });

  it('shows the default empty message, or the given one, when there is no data', () => {
    const { unmount } = renderTable({ data: [], totalCount: 0 });
    expect(screen.getByText('No rows match the given criteria')).toBeInTheDocument();
    unmount();

    renderTable({ data: [], totalCount: 0, emptyTableMessage: { id: 'react.none', defaultMessage: 'Nothing here' } });
    expect(screen.getByText('Nothing here')).toBeInTheDocument();
  });

  it('shows only the loading message while loading, without pagination', () => {
    renderTable({ loading: true, loadingMessage: { id: 'react.wait', defaultMessage: 'Please wait' } });

    expect(screen.getByText('Please wait')).toBeInTheDocument();
    expect(screen.queryByText('#A1')).toBeNull();
    expect(document.querySelector('.table-pagination')).toBeNull();
  });

  it('shows the footer row and the pagination with the footer component', () => {
    const onPageChange = jest.fn();
    renderTable({
      footerComponent: () => <span>Footer actions</span>,
      totalCount: 30,
      paginationProps: {
        ...paginationProps, onPageChange, canNext: () => true, pages: 3,
      },
    });

    expect(screen.getByText('Footer actions')).toBeInTheDocument();
    expect(screen.getByText('1-10')).toBeInTheDocument();
    fireEvent.click(document.querySelector('.table-pagination__btn-next'));
    expect(onPageChange).toHaveBeenCalledWith(1);
  });

  it('leaves the pagination out when it is disabled', () => {
    renderTable({ disablePagination: true });

    expect(document.querySelector('.table-pagination')).toBeNull();
  });

  // Added for OBAF (the overflow-visible part of the removed 'styles the table as disabled
  // and adds the top scrollbar when asked').
  it('lets the content overflow the table when asked', () => {
    renderTable({ overflowVisible: true });

    expect(screen.getByRole('grid')).toHaveClass('overflow-visible');
  });

  it('positions the rows absolutely once virtualization kicks in', () => {
    renderTable({
      virtualize: {
        enabled: true, minSize: 2, estimateSize: 40, overscan: 5, customRowsHeight: true,
      },
    });

    const body = document.querySelector('.rt-tbody-v2 > div');
    // The total height is the estimated size of every row
    expect(body).toHaveStyle({ position: 'relative', height: '120px' });
  });
});

describe('ValueIndicator', () => {
  it.each([
    [valueIndicatorVariant.POSITIVE, 'value-indicator--positive', '5'],
    [valueIndicatorVariant.NEGATIVE, 'value-indicator--negative', '5'],
    [valueIndicatorVariant.EQUAL, 'value-indicator--equal', 'EQUAL'],
    [valueIndicatorVariant.MORE, 'value-indicator--more', 'MORE'],
    [valueIndicatorVariant.LESS, 'value-indicator--less', 'LESS'],
    [valueIndicatorVariant.EMPTY, 'value-indicator--empty', '-'],
    [valueIndicatorVariant.TRANSACTION, 'value-indicator--transaction', '-5'],
  ])('shows the %s variant', (variant, className, text) => {
    render(
      <Provider store={testStore()}>
        <ValueIndicator value={-5} variant={variant} showAbsoluteValue={variant !== 'TRANSACTION'} />
      </Provider>,
    );

    expect(screen.getByText(text)).toHaveClass(className);
  });

  it('shows the signed value unless asked for the absolute one', () => {
    render(
      <Provider store={testStore()}>
        <ValueIndicator value={-3} variant={valueIndicatorVariant.NEGATIVE} />
      </Provider>,
    );

    expect(screen.getByText('-3')).toBeInTheDocument();
  });
});
