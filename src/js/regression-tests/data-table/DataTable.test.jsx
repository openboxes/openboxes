import React from 'react';

import {
  fireEvent, render, screen, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import testStore from 'regression-tests/data-table/testStore';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import TablePagination from 'components/DataTable/TablePagination';
import DateFormat from 'consts/dateFormat';

import '@testing-library/jest-dom';

// The real client imports the store, which needs the reducers this test is still loading.
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const renderWithStore = (ui) => render(
  <Provider store={testStore()}>
    <MemoryRouter>{ui}</MemoryRouter>
  </Provider>,
);

const columns = [
  { Header: 'Code', accessor: 'code', Cell: (props) => <TableCell {...props} showError /> },
  { Header: 'Name', accessor: 'name', getProps: () => ({ errorAccessor: 'productName' }) },
];

const rows = [
  { rowId: 'r1', code: 'A-1', name: 'Amoxicillin' },
  { rowId: 'r2', code: 'B-2', name: 'Bandage' },
];

describe('DataTable (react-table v6)', () => {
  it('shows the rows, the header and footer components and the range of rows shown', () => {
    renderWithStore(
      <DataTable
        data={rows}
        columns={columns}
        pageSize={10}
        headerComponent={() => <span>Table header</span>}
        footerComponent={() => <span>Table footer</span>}
      />,
    );

    expect(screen.getByText('A-1')).toBeInTheDocument();
    expect(screen.getByText('Bandage')).toBeInTheDocument();
    expect(screen.getByText('Table header')).toBeInTheDocument();
    expect(screen.getByText('Table footer')).toBeInTheDocument();
    expect(screen.getByText('1-2')).toBeInTheDocument();
    expect(screen.getByTestId('data-table').querySelector('.app-react-table'))
      .not.toHaveClass('hide-data');
  });

  it('hides the data area and the pagination when there are no rows', () => {
    renderWithStore(<DataTable data={[]} columns={columns} pageSize={10} />);

    expect(screen.getByTestId('data-table').querySelector('.app-react-table')).toHaveClass('hide-data');
    expect(screen.queryByText('1-0')).not.toBeInTheDocument();
    expect(document.querySelector('.table-pagination')).toBeNull();
  });

  it('marks the rows and the cells that have packing list errors', () => {
    renderWithStore(
      <DataTable
        data={rows}
        columns={columns}
        pageSize={10}
        errors={{ packingList: { r2: { code: 'Code is required' } } }}
      />,
    );

    const invalidRows = document.querySelectorAll('.rt-invalid');
    expect(invalidRows).toHaveLength(1);
    expect(within(invalidRows[0]).getByText('B-2')).toBeInTheDocument();
    // Only the cell with an error shows as invalid
    const invalidCells = document.querySelectorAll('.invalid-cell');
    expect(invalidCells).toHaveLength(1);
    expect(invalidCells[0]).toHaveTextContent('B-2');
  });
});

describe('TablePagination', () => {
  const props = {
    page: 1,
    pages: 3,
    pageSize: 10,
    resolvedData: [],
    pageSizeOptions: [10, 25],
    canNext: true,
    canPrevious: true,
    totalData: 25,
  };

  const renderPagination = (overrides = {}) => {
    const onPageChange = jest.fn();
    const onPageSizeChange = jest.fn();
    renderWithStore(
      <TablePagination
        {...props}
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
        {...overrides}
      />,
    );
    return { onPageChange, onPageSizeChange };
  };

  it('shows the range of rows on the page, capped at the total', () => {
    renderPagination({ page: 2 });

    expect(screen.getByText('21-25')).toBeInTheDocument();
    expect(screen.getByText('25')).toBeInTheDocument();
    expect(screen.getByRole('spinbutton')).toHaveValue(3);
  });

  it('counts the resolved rows when no total is given', () => {
    renderPagination({ page: 0, totalData: undefined, resolvedData: [{}, {}, {}] });

    expect(screen.getByText('1-3')).toBeInTheDocument();
  });

  it('moves to the next and the previous page', () => {
    const { onPageChange } = renderPagination();

    fireEvent.click(document.querySelector('.table-pagination__btn-next'));
    fireEvent.click(document.querySelector('.table-pagination__btn-previous'));

    expect(onPageChange.mock.calls).toEqual([[2], [0]]);
  });

  it('does not move past the first or the last page', () => {
    const { onPageChange } = renderPagination({ canNext: false, canPrevious: false });

    const next = document.querySelector('.table-pagination__btn-next');
    const previous = document.querySelector('.table-pagination__btn-previous');
    expect(next).toBeDisabled();
    expect(previous).toBeDisabled();
    // A click on a disabled button does nothing
    fireEvent.click(next);
    fireEvent.click(previous);
    expect(onPageChange).not.toHaveBeenCalled();
  });

  it('jumps to the typed page on Enter only', () => {
    const { onPageChange } = renderPagination();

    const input = screen.getByRole('spinbutton');
    fireEvent.change(input, { target: { value: '3' } });
    fireEvent.keyPress(input, { key: 'a', code: 'KeyA', charCode: 97 });
    expect(onPageChange).not.toHaveBeenCalled();

    fireEvent.keyPress(input, { key: 'Enter', code: 'Enter', charCode: 13 });
    expect(onPageChange).toHaveBeenCalledWith(2);
  });

  it('changes the page size from the select', () => {
    const { onPageSizeChange } = renderPagination();

    expect(screen.getByText('10 rows')).toBeInTheDocument();
    fireEvent.keyDown(document.querySelector('.table-pagination-select input'), { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(screen.getByText('25 rows'));

    expect(onPageSizeChange).toHaveBeenCalledWith(25);
  });
});

describe('TableCell', () => {
  it('falls back from the children to the value to the default value', () => {
    const { rerender } = render(<TableCell value="Value">Child</TableCell>);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('Child');

    rerender(<TableCell value="Value" />);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('Value');

    rerender(<TableCell value={null} defaultValue="-" />);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('-');
  });

  it('renders a plain link, opening in a new tab when asked', () => {
    render(<TableCell value="Order" link="/openboxes/order/show/1" openLinkInNewTab className="cell" />);

    const link = screen.getByRole('link', { name: 'Order' });
    expect(link).toHaveAttribute('href', '/openboxes/order/show/1');
    expect(link).toHaveAttribute('target', '_blank');
    expect(link).toHaveClass('cell');
  });

  it('renders a router link when asked', () => {
    render(
      <MemoryRouter>
        <TableCell value="Product" link="/product/1" reactLink />
      </MemoryRouter>,
    );

    const link = screen.getByRole('link', { name: 'Product' });
    expect(link).toHaveAttribute('href', '/product/1');
    expect(link).not.toHaveAttribute('target');
  });

  it('shows the error only when asked to', () => {
    const tdProps = { rest: { error: 'Too many' } };
    const { rerender } = render(<TableCell value="5" tdProps={tdProps} />);
    expect(screen.getByTestId('table-cell')).not.toHaveClass('invalid-cell');

    rerender(<TableCell value="5" tdProps={tdProps} showError />);
    expect(screen.getByTestId('table-cell')).toHaveClass('invalid-cell');
  });

  it('wraps the value in a tooltip or a custom tooltip', () => {
    const { rerender } = render(<TableCell value="Long name" tooltip tooltipClassname="tip" />);
    expect(screen.getByTestId('table-cell').querySelector('.tip')).not.toBeNull();
    expect(screen.getByTestId('table-cell')).toHaveTextContent('Long name');

    rerender(<TableCell value="Long name" customTooltip tooltipLabel="Full label" />);
    expect(screen.getByRole('tooltip')).toBeInTheDocument();
    expect(screen.getByTestId('table-cell')).toHaveTextContent('Long name');
  });
});

describe('TableHeaderCell', () => {
  it('sorts by the column on click when sortable', () => {
    const sortByCode = jest.fn();
    const toggleSort = jest.fn(() => sortByCode);
    render(
      <TableHeaderCell sortable columnId="code" toggleSort={toggleSort} required>
        Code
      </TableHeaderCell>,
    );

    expect(toggleSort).toHaveBeenCalledWith('code');
    fireEvent.click(screen.getByRole('button'));
    expect(sortByCode).toHaveBeenCalled();
    expect(screen.getByText('*')).toHaveClass('required');
    expect(document.querySelector('.sorting-arrows')).not.toBeNull();
  });

  it('is not clickable when not sortable, and adds the dynamic class', () => {
    render(
      <TableHeaderCell columnId="name" dynamicClassName={(id) => `col-${id}`} tooltip tooltipLabel="Help">
        Name
      </TableHeaderCell>,
    );

    expect(screen.queryByRole('button')).toBeNull();
    expect(screen.getByText('Name')).toHaveClass('rt-th', 'col-name');
    expect(screen.getByRole('tooltip')).toBeInTheDocument();
    expect(document.querySelector('.sorting-arrows')).toBeNull();
  });
});

describe('DateCell', () => {
  it('formats the date with the session display format', () => {
    renderWithStore(<DateCell value="2023-02-09" />);

    expect(screen.getByTestId('table-cell')).toHaveTextContent('Feb 09, 2023');
  });

  it('shows the session default value when there is no date, unless one is given', () => {
    const { unmount } = renderWithStore(<DateCell value={null} />);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('-');
    unmount();

    renderWithStore(<DateCell value={null} defaultValue="never" />);
    expect(screen.getByTestId('table-cell')).toHaveTextContent('never');
  });

  it('formats a localized date with the given format', () => {
    renderWithStore(<DateCell value="2023-02-09T00:00:00" localizeDate formatLocalizedDate={DateFormat.COMMON} />);

    expect(screen.getByTestId('table-cell').textContent).toBe('09/Feb/2023');
  });
});
