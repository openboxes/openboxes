import React from 'react';

import { render, screen, within } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { ColumnsTable } from 'regression-tests/hooks/inboundV2/columnTestUtils';

import useConfirmDuplicatedItemsColumns from 'hooks/inboundV2/addItems/useConfirmDuplicatedItemsColumns';

import '@testing-library/jest-dom';

jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));
jest.mock('components/DataTable', () => ({
  TableCell: jest.requireActual('regression-tests/hooks/inboundV2/columnTestUtils').MockTableCell,
}));
jest.mock('components/DataTable/TableHeaderCell', () => jest.requireActual('regression-tests/hooks/inboundV2/columnTestUtils').MockTableHeaderCell);

describe('useConfirmDuplicatedItemsColumns', () => {
  const items = [
    { product: { id: 'p-1', productCode: 'P1', name: 'Aspirin' }, quantityRequested: 10 },
    { product: { id: 'p-1', productCode: 'P1', name: 'Aspirin' }, quantityRequested: 5 },
  ];

  const renderColumns = () => {
    const { result } = renderHook(() => useConfirmDuplicatedItemsColumns());
    render(<ColumnsTable columns={result.current.columns} data={items} />);
    return result.current.columns;
  };

  it('shows code, product and quantity headers', () => {
    const columns = renderColumns();
    expect(columns.map((column) => column.size)).toEqual([50, 150, 50]);
    expect(screen.getAllByTestId('header-cell').map((cell) => cell.textContent))
      .toEqual(['Code', 'Product', 'Quantity']);
  });

  it('renders each duplicated line with a stock card link on the product', () => {
    renderColumns();
    const row = within(screen.getByTestId('row-1'));
    const [code, product, quantity] = row.getAllByTestId('table-cell');
    expect(code).toHaveTextContent('P1');
    expect(code).toHaveAttribute('data-tooltip', 'P1');
    expect(product).toHaveTextContent('Aspirin');
    expect(product).toHaveAttribute('data-tooltip', 'Aspirin');
    expect(product.getAttribute('data-link')).toBe('/openboxes/inventoryItem/showStockCard/p-1');
    expect(quantity).toHaveTextContent('5');
  });
});
