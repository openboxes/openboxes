// OBAF-new: the Auto-Issuance Transactions tab component wiring (hooks, export button, table).
import React from 'react';

import { render, screen } from '@testing-library/react';

import AutoIssuanceTransactionsTab from 'components/cycleCountReporting/AutoIssuanceTransactionsTab';
import useAutoIssuanceTransactionsTab from 'hooks/cycleCount/useAutoIssuanceTransactionsTab';
import useAutoIssuanceTransactionsTabExport from 'hooks/cycleCount/useAutoIssuanceTransactionsTabExport';

import '@testing-library/jest-dom';

let mockTableProps;
let mockDropdownProps;
jest.mock('components/DataTable/v2/DataTable', () => (props) => {
  mockTableProps = props;
  return <div data-testid="data-table" />;
});
jest.mock('utils/DropdownButton', () => (props) => {
  mockDropdownProps = props;
  return <button type="button" disabled={props.disabled}>{props.buttonDefaultLabel}</button>;
});
jest.mock('hooks/cycleCount/useAutoIssuanceTransactionsTab', () => jest.fn());
jest.mock('hooks/cycleCount/useAutoIssuanceTransactionsTabExport', () => jest.fn());

describe('AutoIssuanceTransactionsTab (OBAF)', () => {
  const actions = [{ label: 'full' }, { label: 'filtered' }];
  const columns = [{ id: 'product' }];
  const tableData = { data: [{ id: 'r1' }], totalCount: 7 };
  const emptyTableMessage = { id: 'empty', defaultMessage: 'Empty' };
  let tablePaginationProps;

  const renderTab = (props = {}) => render(
    <AutoIssuanceTransactionsTab
      filterParams={{ startDate: '2026-03-01', endDate: '2026-03-31' }}
      tablePaginationProps={tablePaginationProps}
      shouldFetch
      setShouldFetch={jest.fn()}
      filtersInitialized
      {...props}
    />,
  );

  beforeEach(() => {
    jest.clearAllMocks();
    tablePaginationProps = {
      paginationProps: { page: 1 },
      setTotalCount: jest.fn(),
      pageSize: 10,
      offset: 0,
      serializedParams: 1,
    };
    useAutoIssuanceTransactionsTab.mockReturnValue({
      columns, tableData, loading: false, emptyTableMessage,
    });
    useAutoIssuanceTransactionsTabExport.mockReturnValue({ actions });
  });

  it('passes pagination and fetch state to the hook and filters to the export', () => {
    const setShouldFetch = jest.fn();
    const filterParams = { startDate: '2026-03-01', endDate: '2026-03-31' };
    renderTab({ setShouldFetch, filterParams });

    expect(useAutoIssuanceTransactionsTab).toHaveBeenCalledWith({
      paginationProps: { page: 1 },
      filterParams,
      pageSize: 10,
      offset: 0,
      shouldFetch: true,
      setShouldFetch,
      serializedParams: 1,
      filtersInitialized: true,
    });
    expect(useAutoIssuanceTransactionsTabExport).toHaveBeenCalledWith(filterParams);
  });

  it('renders the table with the hook output and reports the total count', () => {
    renderTab();

    expect(screen.getByTestId('data-table')).toBeInTheDocument();
    expect(mockTableProps).toMatchObject({
      columns,
      data: tableData.data,
      emptyTableMessage,
      loading: false,
      totalCount: 7,
      paginationProps: { page: 1 },
      tableWithPinnedColumns: true,
    });
    expect(tablePaginationProps.setTotalCount).toHaveBeenCalledWith(7);
  });

  it('enables the export only when both dates are set', () => {
    const { unmount } = renderTab();
    expect(screen.getByText('Export')).not.toBeDisabled();
    expect(mockDropdownProps.actions).toBe(actions);
    unmount();

    renderTab({ filterParams: { startDate: '2026-03-01' } });
    expect(screen.getByText('Export')).toBeDisabled();
  });
});
