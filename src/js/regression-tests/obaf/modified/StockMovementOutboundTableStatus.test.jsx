// OBAF-modified: the outbound list's status cell (status tooltip plus a warning icon carrying the
// requisition's error message) and the new Priority column (0 when unset).
import React from 'react';

import { render, screen, within } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { sessionStore } from 'regression-tests/__mocks__/screenHarness';

import '@testing-library/jest-dom';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/screenApiClient').default());
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));
// Tooltips render inline with their content, so the texts can be read.
jest.mock('react-tippy', () => ({
  // eslint-disable-next-line react/prop-types
  Tooltip: ({ children, html, title }) => (
    <span data-testid="tooltip">
      {children}
      <span data-testid="tooltip-content">{html || title}</span>
    </span>
  ),
}));
jest.mock('hooks/list-pages/outbound/useOutboundListTableData', () => jest.fn());

/* eslint-disable import/first,import/order */
import StockMovementOutboundTable from 'components/stock-movement/outbound/StockMovementOutboundTable';
import useOutboundListTableData from 'hooks/list-pages/outbound/useOutboundListTableData';
/* eslint-enable import/first,import/order */

const movement = (overrides) => ({
  id: 'sm1',
  identifier: 'SM-1',
  name: 'Restock',
  origin: { id: 'loc1', name: 'Main Warehouse' },
  destination: { id: 'loc2', name: 'Clinic' },
  requestedBy: { id: 'u1', name: 'Admin' },
  dateRequested: '2026-09-01T12:00:00Z',
  dateCreated: '2026-09-01T12:00:00Z',
  statusCode: 'PICKING',
  displayStatus: { name: 'PICKING', label: 'Picking', variant: 'primary' },
  ...overrides,
});

const ROWS = [
  movement({
    id: 'sm1',
    identifier: 'SM-1',
    statusCode: 'STAGED',
    displayStatus: { name: 'STAGED', label: 'Staged', variant: 'primary' },
    hasError: true,
    errorMessage: 'Pick failed: bin A-01 is empty',
    priority: 5,
  }),
  movement({ id: 'sm2', identifier: 'SM-2', priority: null }),
];

const renderTable = () => render(
  <Provider store={sessionStore()}>
    <MemoryRouter>
      <StockMovementOutboundTable filterParams={{ direction: 'OUTBOUND' }} />
    </MemoryRouter>
  </Provider>,
);

const rowOf = (text) => screen.getByText(text).closest('.rt-tr');

describe('StockMovementOutboundTable status and priority (OBAF)', () => {
  beforeEach(() => {
    useOutboundListTableData.mockReturnValue({
      tableData: { data: ROWS, pages: 1, totalCount: 2 },
      tableRef: { current: null },
      loading: false,
      onFetchHandler: jest.fn(),
      exportStockMovements: jest.fn(),
      exportPendingShipmentItems: jest.fn(),
      deleteConfirmAlert: jest.fn(),
      approveRequest: jest.fn(),
      rejectRequest: jest.fn(),
      rollbackRequest: jest.fn(),
    });
  });

  afterEach(() => jest.clearAllMocks());

  it('shows the status with its description tooltip, and the error icon only on failed requisitions', () => {
    renderTable();

    const failed = rowOf('SM-1');
    expect(within(failed).getByText('Staged')).toBeInTheDocument();
    expect(within(failed).getByText('staged')).toBeInTheDocument();
    expect(within(failed).getByTestId('requisition-error-icon')).toBeInTheDocument();
    expect(within(failed).getByText('Pick failed: bin A-01 is empty')).toBeInTheDocument();

    const fine = rowOf('SM-2');
    expect(within(fine).getByText('Picking')).toBeInTheDocument();
    expect(within(fine).queryByTestId('requisition-error-icon')).toBeNull();
  });

  it('adds a Priority column, showing 0 when no priority is set', () => {
    renderTable();

    const headers = [...document.querySelectorAll('.rt-thead.-header .rt-th')];
    const priorityIndex = headers.findIndex((th) => th.textContent === 'Priority');
    expect(priorityIndex).toBeGreaterThan(-1);
    const priorityOf = (text) => rowOf(text).querySelectorAll('.rt-td')[priorityIndex].textContent;
    expect(priorityOf('SM-1')).toBe('5');
    expect(priorityOf('SM-2')).toBe('0');
  });
});
