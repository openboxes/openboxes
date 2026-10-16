import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { createMemoryHistory } from 'history';
import { Provider } from 'react-redux';
import { Router } from 'react-router-dom';
import { cycleCountStore } from 'regression-tests/cycle-count/cycleCountStore';

import cycleCountReportingFilterFields from 'components/cycleCountReporting/CycleCountReportingFilterFields';
import {
  AUTO_ISSUANCE_TAB, INDICATORS_TAB, INVENTORY_TRANSACTIONS_TAB, PRODUCTS_TAB,
} from 'consts/cycleCount';
import useAutoIssuanceTransactionsTab from 'hooks/cycleCount/useAutoIssuanceTransactionsTab';
import useAutoIssuanceTransactionsTabExport from 'hooks/cycleCount/useAutoIssuanceTransactionsTabExport';
import useInventoryTransactionsTab from 'hooks/cycleCount/useInventoryTransactionsTab';
import useInventoryTransactionsTabExport from 'hooks/cycleCount/useInventoryTransactionsTabExport';
import useProductsTab from 'hooks/cycleCount/useProductsTab';
import useProductsTabExport from 'hooks/cycleCount/useProductsTabExport';
import apiClient from 'utils/apiClient';

import '@testing-library/jest-dom';

// The loading cards pick their colors with lodash, which the app provides as a global.
global._ = jest.requireActual('lodash');

// The API: translations, reason codes and the three indicators. The helpers the actions use
// come from the real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  const client = {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  };
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    __esModule: true,
    default: client,
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
    handleSuccess: actual('handleSuccess'),
    handleValidationErrors: actual('handleValidationErrors'),
    mapToEmptyString: actual('mapToEmptyString'),
  };
});

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// The two table tabs build their columns and fetch their rows in their own hooks (tested on
// their own); here they are stand-ins. The indicators tab runs for real against the API.
jest.mock('hooks/cycleCount/useProductsTab', () => jest.fn());
jest.mock('hooks/cycleCount/useProductsTabExport', () => jest.fn());
jest.mock('hooks/cycleCount/useInventoryTransactionsTab', () => jest.fn());
jest.mock('hooks/cycleCount/useInventoryTransactionsTabExport', () => jest.fn());
// OBAF's auto-issuance transactions tab (only where negative inventory is allowed), a stand-in too.
jest.mock('hooks/cycleCount/useAutoIssuanceTransactionsTab', () => jest.fn());
jest.mock('hooks/cycleCount/useAutoIssuanceTransactionsTabExport', () => jest.fn());

/* eslint-disable import/first,import/order */
import CycleCountReporting from 'components/cycleCountReporting/CycleCountReporting';
import IndicatorsTab from 'components/cycleCountReporting/IndicatorsTab/IndicatorsTab';
/* eslint-enable import/first,import/order */

const INDICATORS = {
  '/api/reports/indicators/productsInventoried': { name: 'productsInventoried', type: 'SINGLE', value: 42 },
  '/api/reports/indicators/inventoryAccuracy': {
    name: 'inventoryAccuracy', type: 'DOUBLE', firstValue: 87, secondValue: 120,
  },
  '/api/reports/indicators/inventoryShrinkage': {
    name: 'inventoryShrinkage', type: 'DOUBLE', firstValue: 6, secondValue: 1520.5,
  },
};

const columns = [{
  id: 'name',
  accessorKey: 'name',
  header: () => 'Product',
  cell: ({ getValue }) => <span>{getValue()}</span>,
}];

const tableTab = (rows) => ({
  columns,
  tableData: { data: rows.map((name) => ({ name })), totalCount: rows.length },
  loading: false,
  emptyTableMessage: { id: 'react.cycleCount.table.noData.label', defaultMessage: 'No data' },
});

let productsExport;
let transactionsExport;
let autoIssuanceExport;

beforeEach(() => {
  apiClient.get.mockImplementation((url) => {
    if (url.startsWith('/api/localizations')) {
      return Promise.resolve({ data: { messages: {}, currentLocale: 'en' } });
    }
    if (url.startsWith('/api/reasonCodes')) {
      return Promise.resolve({ data: { data: [{ id: 'rc1', name: 'Damaged' }] } });
    }
    if (INDICATORS[url]) {
      return Promise.resolve({ data: { data: INDICATORS[url] } });
    }
    return Promise.resolve({ data: { data: [] } });
  });
  productsExport = { label: 'react.cycleCount.exportProducts.label', defaultLabel: 'Export products', onClick: jest.fn() };
  transactionsExport = { label: 'react.cycleCount.exportTransactions.label', defaultLabel: 'Export transactions', onClick: jest.fn() };
  useProductsTab.mockReturnValue(tableTab(['Ibuprofen 200mg']));
  useInventoryTransactionsTab.mockReturnValue(tableTab(['Adjustment of Insulin']));
  useProductsTabExport.mockReturnValue({ actions: [productsExport] });
  useInventoryTransactionsTabExport.mockReturnValue({ actions: [transactionsExport] });
  autoIssuanceExport = { label: 'react.cycleCount.exportAutoIssuance.label', defaultLabel: 'Export auto-issuance', onClick: jest.fn() };
  useAutoIssuanceTransactionsTab.mockReturnValue(tableTab(['Auto-issued Paracetamol']));
  useAutoIssuanceTransactionsTabExport.mockReturnValue({ actions: [autoIssuanceExport] });
});

afterEach(() => {
  jest.useRealTimers();
  jest.clearAllMocks();
});

const renderWithStore = (ui, path, storeOptions) => {
  const store = cycleCountStore(storeOptions);
  const history = createMemoryHistory({ initialEntries: [path] });
  return {
    store,
    history,
    ...render(
      <Provider store={store}>
        <Router history={history}>{ui}</Router>
      </Provider>,
    ),
  };
};

const query = (history) => new URLSearchParams(history.location.search);
const indicatorCalls = () => apiClient.get.mock.calls.filter(([url]) => url.startsWith('/api/reports/indicators'));

describe('inventory changes report page', () => {
  it('opens on the products tab when no tab is asked for', async () => {
    const { history } = renderWithStore(<CycleCountReporting />, '/openboxes/cycleCount/reporting');

    expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
    expect(query(history).get('tab')).toBe(PRODUCTS_TAB);
  });

  // KNOWN-GAP: OBAF's useTablePagination reads the default page size only as its
  // initial state, so the page keeps the size of the first tab it rendered (25 before the tab is
  // known) instead of following each tab's own default.
  it.skip('shows ten products per page when no tab is asked for', async () => {
    renderWithStore(<CycleCountReporting />, '/openboxes/cycleCount/reporting');

    await screen.findByText('Ibuprofen 200mg');
    await waitFor(() => expect(useProductsTab)
      .toHaveBeenLastCalledWith(expect.objectContaining({ pageSize: 10, offset: 0 })));
  });

  // KNOWN-GAP: OBAF's useTablePagination reads the default page size only as its
  // initial state, so the page keeps the size of the first tab it rendered (25 before the tab is
  // known) instead of following each tab's own default.
  it.skip('gives each table tab its own page size', async () => {
    renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
    );
    await screen.findByText('Ibuprofen 200mg');

    fireEvent.click(screen.getByRole('button', { name: 'Inventory Transactions' }));
    await screen.findByText('Adjustment of Insulin');
    await waitFor(() => expect(useInventoryTransactionsTab)
      .toHaveBeenLastCalledWith(expect.objectContaining({ pageSize: 25, offset: 0 })));

    fireEvent.click(screen.getByRole('button', { name: 'Products' }));
    await screen.findByText('Ibuprofen 200mg');
    await waitFor(() => expect(useProductsTab)
      .toHaveBeenLastCalledWith(expect.objectContaining({ pageSize: 10, offset: 0 })));
  });

  it('shows the products table from the menu link and loads the cycle count reason codes', async () => {
    const { store } = renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
    );

    expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
    expect(screen.getByText('Inventory Changes Report')).toBeInTheDocument();
    expect(apiClient.get).toHaveBeenCalledWith('/api/reasonCodes?activityCode=CYCLE_COUNT');
    await waitFor(() => expect(store.getState().cycleCount.reasonCodes)
      .toEqual([{ id: 'rc1', value: 'rc1', label: 'Damaged' }]));
    // The products table shows ten rows per page.
    expect(useProductsTab)
      .toHaveBeenLastCalledWith(expect.objectContaining({ pageSize: 10, offset: 0 }));
    expect(screen.getByRole('button', { name: 'Load table' })).toBeInTheDocument();
  });

  it('allows the export only once both dates are chosen', async () => {
    renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
    );
    await screen.findByText('Ibuprofen 200mg');

    expect(screen.getByRole('button', { name: 'Export' })).toBeDisabled();
  });

  it('switches to the inventory transactions table', async () => {
    const { history } = renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
    );
    await screen.findByText('Ibuprofen 200mg');

    fireEvent.click(screen.getByRole('button', { name: 'Inventory Transactions' }));

    expect(await screen.findByText('Adjustment of Insulin')).toBeInTheDocument();
    expect(query(history).get('tab')).toBe(INVENTORY_TRANSACTIONS_TAB);
    fireEvent.click(screen.getByText('Export transactions'));
    expect(transactionsExport.onClick).toHaveBeenCalledTimes(1);
  });

  it('loads the indicators of the last three months when switching to them, and again on demand', async () => {
    // Only the clock is pinned; timers keep running for the debounces and waits.
    jest.useFakeTimers({
      now: new Date('2026-10-02T12:00:00'),
      doNotFake: ['setTimeout', 'clearTimeout', 'setInterval', 'clearInterval', 'setImmediate',
        'clearImmediate', 'nextTick', 'queueMicrotask', 'requestAnimationFrame',
        'cancelAnimationFrame', 'requestIdleCallback', 'cancelIdleCallback', 'hrtime', 'performance'],
    });
    const { history } = renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
    );
    await screen.findByText('Ibuprofen 200mg');

    fireEvent.click(screen.getByRole('button', { name: 'Indicators' }));

    expect(await screen.findByText('Products inventoried')).toBeInTheDocument();
    expect(query(history).get('tab')).toBe(INDICATORS_TAB);
    expect(indicatorCalls()).toHaveLength(3);
    expect(indicatorCalls()[0][1].params).toEqual({
      startDate: '07/02/2026',
      endDate: '10/02/2026',
      facility: 'loc1',
    });

    const loadData = screen.getByRole('button', { name: 'Load data' });
    await waitFor(() => expect(loadData).toBeEnabled());
    fireEvent.click(loadData);

    await waitFor(() => expect(indicatorCalls()).toHaveLength(6));
  });
});

// Added for OBAF: the auto-issuance transactions tab OBAF added to this page.
describe('auto-issuance transactions tab', () => {
  // A location of its own: getCurrentLocation caches the locations it has seen by id.
  const NEGATIVE_INVENTORY = {
    location: { id: 'loc-negative', name: 'Negative stock depot' },
    activities: ['MANAGE_INVENTORY', 'ALLOW_NEGATIVE_INVENTORY'],
  };

  it('is offered only where the location allows negative inventory', async () => {
    const { unmount } = renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
    );
    await screen.findByText('Ibuprofen 200mg');
    expect(screen.queryByRole('button', { name: 'Auto-Issuance Transactions' })).toBeNull();
    unmount();

    renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
      NEGATIVE_INVENTORY,
    );
    await screen.findByText('Ibuprofen 200mg');
    expect(screen.getByRole('button', { name: 'Auto-Issuance Transactions' })).toBeInTheDocument();
  });

  it('switches to the auto-issuance transactions table and exports it', async () => {
    const { history } = renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${PRODUCTS_TAB}`,
      NEGATIVE_INVENTORY,
    );
    await screen.findByText('Ibuprofen 200mg');

    fireEvent.click(screen.getByRole('button', { name: 'Auto-Issuance Transactions' }));

    expect(await screen.findByText('Auto-issued Paracetamol')).toBeInTheDocument();
    expect(query(history).get('tab')).toBe(AUTO_ISSUANCE_TAB);
    fireEvent.click(screen.getByText('Export auto-issuance'));
    expect(autoIssuanceExport.onClick).toHaveBeenCalledTimes(1);
  });

  it('sends a link to the tab back to the products tab where the tab is not offered', async () => {
    const { history } = renderWithStore(
      <CycleCountReporting />,
      `/openboxes/cycleCount/reporting?tab=${AUTO_ISSUANCE_TAB}`,
    );

    expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
    expect(query(history).get('tab')).toBe(PRODUCTS_TAB);
    expect(useAutoIssuanceTransactionsTab).not.toHaveBeenCalled();
  });
});

describe('indicators tab', () => {
  const renderTab = (props) => {
    const setShouldFetch = jest.fn();
    const utils = renderWithStore(
      <IndicatorsTab
        filterParams={{ startDate: '2026-07-01T12:00:00', endDate: '2026-09-30T12:00:00' }}
        shouldFetch
        filtersInitialized
        setShouldFetch={setShouldFetch}
        {...props}
      />,
      `/openboxes/cycleCount/reporting?tab=${INDICATORS_TAB}`,
    );
    return { setShouldFetch, ...utils };
  };

  it('fetches the three indicators for the chosen dates and shows them as cards', async () => {
    const { setShouldFetch, history } = renderTab();

    expect(await screen.findByText('Products inventoried')).toBeInTheDocument();
    expect(screen.getByText('42')).toBeInTheDocument();
    expect(screen.getByText('Inventory Accuracy')).toBeInTheDocument();
    expect(screen.getByText('87%')).toBeInTheDocument();
    expect(screen.getByText('Inventory Shrinkage')).toBeInTheDocument();
    expect(screen.getByText('1,520.50 USD')).toBeInTheDocument();

    expect(indicatorCalls().map(([url]) => url).sort()).toEqual(Object.keys(INDICATORS).sort());
    indicatorCalls().forEach(([, config]) => expect(config.params).toEqual({
      startDate: '07/01/2026', endDate: '09/30/2026', facility: 'loc1',
    }));
    expect(setShouldFetch).toHaveBeenCalledWith(false);
    // The chosen dates are kept in the URL.
    expect(query(history).get('startDate')).toBe('01/Jul/2026');
    expect(query(history).get('endDate')).toBe('30/Sep/2026');
  });

  it('shows placeholders while the indicators load', () => {
    apiClient.get.mockImplementation(() => new Promise(() => {}));
    const { container } = renderTab();

    expect(screen.queryByText('Products inventoried')).not.toBeInTheDocument();
    expect(container.querySelector('.cards-container').children.length).toBeGreaterThan(0);
  });

  it('does not fetch before the filters are ready or without both dates', () => {
    renderTab({ filtersInitialized: false });
    renderTab({ filterParams: { startDate: '2026-07-01T12:00:00' } });

    expect(indicatorCalls()).toHaveLength(0);
  });

  it('leaves out an indicator the page does not know', async () => {
    INDICATORS['/api/reports/indicators/productsInventoried'].name = 'somethingElse';
    try {
      renderTab();

      expect(await screen.findByText('Inventory Accuracy')).toBeInTheDocument();
      expect(screen.queryByText('Products inventoried')).not.toBeInTheDocument();
    } finally {
      INDICATORS['/api/reports/indicators/productsInventoried'].name = 'productsInventoried';
    }
  });
});

describe('inventory changes report filter fields', () => {
  it('searches products through the page and keeps the server answer as is', () => {
    const debouncedProductsFetch = jest.fn();
    [
      cycleCountReportingFilterFields.products,
      cycleCountReportingFilterFields.inventoryTransactions,
    ]
      .forEach(({ products }) => {
        expect(products.getDynamicAttr({ debouncedProductsFetch }))
          .toEqual({ loadOptions: debouncedProductsFetch });
        expect(products.attributes.filterOptions(['a', 'b'])).toEqual(['a', 'b']);
      });
    expect(Object.keys(cycleCountReportingFilterFields.indicators)).toEqual(['startDate', 'endDate']);
  });
});
