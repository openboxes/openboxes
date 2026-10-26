import React from 'react';

import { render } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import confirmExpirationDateColumn from 'consts/confirmExpirationDateColumn';
import useConfirmExpirationDateModal from 'hooks/useConfirmExpirationDateModal';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));
// Plain cells that expose what the hook passes to them
jest.mock('components/DataTable', () => ({
  // eslint-disable-next-line react/prop-types
  TableCell: ({ children, tooltipLabel, link }) => (
    <div data-testid="cell" data-tooltip={tooltipLabel} data-link={link}>{children}</div>
  ),
}));
// eslint-disable-next-line react/prop-types
jest.mock('components/DataTable/TableHeaderCell', () => ({ children }) => <div>{children}</div>);

describe('useConfirmExpirationDateModal', () => {
  const columnsIn = (activeLanguage) => {
    useSelector.mockImplementation((selector) => selector({ session: { activeLanguage } }));
    return renderHook(() => useConfirmExpirationDateModal()).result.current.columns;
  };

  const columnOf = (columns, accessorKey) =>
    columns.find((column) => column.accessorKey === accessorKey);

  const renderCell = (column, value) => render(column.cell({ getValue: () => value }));

  it('lists the code, product, lot and both expiry dates', () => {
    const columns = columnsIn('en');

    expect(columns.map(({ accessorKey }) => accessorKey)).toEqual([
      confirmExpirationDateColumn.CODE,
      confirmExpirationDateColumn.PRODUCT,
      confirmExpirationDateColumn.LOT_NUMBER,
      confirmExpirationDateColumn.PREVIOUS_EXPIRY,
      confirmExpirationDateColumn.NEW_EXPIRY,
    ]);
    expect(columns.map((column) => render(column.header()).container.textContent))
      .toEqual(['Code', 'Product', 'Lot', 'Previous expiry', 'New expiry']);
  });

  it('links the product to its stock card', () => {
    const product = columnOf(columnsIn('en'), confirmExpirationDateColumn.PRODUCT);

    const { getByTestId } = renderCell(product, { id: 'prod-1', name: 'Amoxicillin 500mg' });

    expect(getByTestId('cell').textContent).toBe('Amoxicillin 500mg');
    expect(getByTestId('cell').dataset.tooltip).toBe('Amoxicillin 500mg');
    expect(getByTestId('cell').dataset.link).toContain('/showStockCard/prod-1');
  });

  it('shows the code and lot as they are', () => {
    const columns = columnsIn('en');

    expect(renderCell(columnOf(columns, confirmExpirationDateColumn.CODE), 'AB12')
      .container.textContent).toBe('AB12');
    expect(renderCell(columnOf(columns, confirmExpirationDateColumn.LOT_NUMBER), 'LOT-7')
      .container.textContent).toBe('LOT-7');
  });

  it.each([
    confirmExpirationDateColumn.PREVIOUS_EXPIRY,
    confirmExpirationDateColumn.NEW_EXPIRY,
  ])('shows the %s date as day/month/year', (accessorKey) => {
    const column = columnOf(columnsIn('en'), accessorKey);

    const cell = renderCell(column, new Date('2028-05-01T12:00:00').toISOString()).container.firstChild;

    expect(cell.textContent).toBe('01/May/2028');
    expect(cell.dataset.tooltip).toBe('01/May/2028');
  });

  // KNOWN-GAP: OBAF passes moment's locales (a function, not a map) to date-fns,
  // so every language gets the English month names.
  it.skip.each([
    ['fr', '01/mai/2028'],
    ['es_MX', '01/may/2028'],
  ])('shows the dates in the language of the session: %s', (language, expected) => {
    const column = columnOf(columnsIn(language), confirmExpirationDateColumn.NEW_EXPIRY);

    expect(renderCell(column, new Date('2028-05-01T12:00:00').toISOString()).container.textContent).toBe(expected);
  });

  // The English case of the table above, split out because it passes on OBAF.
  it.each([
    ['en', '01/May/2028'],
  ])('shows the dates in the language of the session: %s', (language, expected) => {
    const column = columnOf(columnsIn(language), confirmExpirationDateColumn.NEW_EXPIRY);

    expect(renderCell(column, new Date('2028-05-01T12:00:00').toISOString()).container.textContent).toBe(expected);
  });

  it('shows nothing for a missing date', () => {
    const column = columnOf(columnsIn('en'), confirmExpirationDateColumn.NEW_EXPIRY);

    expect(renderCell(column, null).container.textContent).toBe('');
  });
});
