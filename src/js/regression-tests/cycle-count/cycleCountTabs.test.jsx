import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { createMemoryHistory } from 'history';
import { Provider } from 'react-redux';
import { Router } from 'react-router-dom';
import { cycleCountStore, HERE } from 'regression-tests/cycle-count/cycleCountStore';

import { START_COUNT, START_RESOLUTION } from 'actions/types';
import cycleCountFilterFields from 'components/cycleCount/CycleCountFilterFields';
import { ALL_PRODUCTS_TAB, TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import FileFormat from 'consts/fileFormat';
import useAllProductsTab from 'hooks/cycleCount/useAllProductsTab';
import useToCountTab from 'hooks/cycleCount/useToCountTab';
import useToResolveTab from 'hooks/cycleCount/useToResolveTab';
import apiClient from 'utils/apiClient';

import '@testing-library/jest-dom';

// The API: the filters' option lists and the translations; every list is empty except the
// categories. The helpers the actions use come from the real module, loaded on first use.
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

// Each tab's data, columns and actions come from its own hook (tested on its own); here they
// are stand-ins so the screen around them can be checked.
jest.mock('hooks/cycleCount/useAllProductsTab', () => jest.fn());
jest.mock('hooks/cycleCount/useToCountTab', () => jest.fn());
jest.mock('hooks/cycleCount/useToResolveTab', () => jest.fn());
jest.mock('hooks/cycleCount/useAssignCycleCountModal', () => jest.fn(() => ({ columns: [], handleAssign: jest.fn() })));

/* eslint-disable import/first,import/order */
import CycleCount from 'components/cycleCount/CycleCount';
/* eslint-enable import/first,import/order */

const columns = [{
  id: 'product',
  accessorKey: 'product',
  header: () => 'Product',
  cell: ({ getValue }) => <span>{getValue()}</span>,
}];

const tabHook = (rows, extra = {}) => ({
  columns,
  tableData: { data: rows.map((product) => ({ product })), totalCount: rows.length },
  loading: false,
  emptyTableMessage: { id: 'react.cycleCount.table.noProducts.label', defaultMessage: 'No products' },
  exportTableData: jest.fn(),
  ...extra,
});

let allProducts;
let toCount;
let toResolve;

beforeEach(() => {
  apiClient.get.mockImplementation((url) => {
    if (url.startsWith('/api/localizations')) {
      return Promise.resolve({ data: { messages: {}, currentLocale: 'en' } });
    }
    if (url === '/api/categoryOptions') {
      return Promise.resolve({ data: { data: [{ id: 'cat1', label: 'Medicines' }] } });
    }
    return Promise.resolve({ data: { data: [] } });
  });
  allProducts = tabHook(['Ibuprofen 200mg'], { selectedCheckboxesAmount: 1, countSelected: jest.fn() });
  toCount = tabHook(['Paracetamol 500mg'], {
    moveToCounting: jest.fn(),
    printCountForm: jest.fn(),
    openCancelCountsModal: jest.fn(),
    openAssignCountModal: jest.fn(),
    closeAssignCountModal: jest.fn(),
    isAssignCountModalOpen: false,
    assignCountModalData: { current: [] },
    fetchData: jest.fn(),
  });
  toResolve = tabHook(['Amoxicillin 250mg'], {
    selectedCheckboxesAmount: 2,
    moveToResolving: jest.fn(),
    printResolveForm: jest.fn(),
    openCancelCountsModal: jest.fn(),
    openAssignCountModal: jest.fn(),
    closeAssignCountModal: jest.fn(),
    isAssignCountModalOpen: false,
    assignCountModalData: { current: [] },
    fetchData: jest.fn(),
  });
  useAllProductsTab.mockImplementation(() => allProducts);
  useToCountTab.mockImplementation(() => toCount);
  useToResolveTab.mockImplementation(() => toResolve);
  window.scrollTo = jest.fn();
});

afterEach(() => jest.clearAllMocks());

const renderPage = ({ search = '', store = cycleCountStore() } = {}) => {
  const history = createMemoryHistory({ initialEntries: [`/openboxes/inventory/cycleCount${search}`] });
  const utils = render(
    <Provider store={store}>
      <Router history={history}>
        <CycleCount />
      </Router>
    </Provider>,
  );
  return { history, store, ...utils };
};

const button = (name) => screen.getByRole('button', { name });
const tab = (search) => new URLSearchParams(search).get('tab');

describe('cycle count list page', () => {
  it('opens on the all products tab and lists what its hook returns', async () => {
    const { history } = renderPage();

    expect(await screen.findByText('Ibuprofen 200mg')).toBeInTheDocument();
    expect(tab(history.location.search)).toBe(ALL_PRODUCTS_TAB);
    expect(screen.getByText('Cycle Count')).toBeInTheDocument();
    expect(screen.getByText(/1\s+selected/)).toBeInTheDocument();
    // The tab's hook gets the page's pagination.
    expect(useAllProductsTab)
      .toHaveBeenLastCalledWith(expect.objectContaining({ offset: 0, pageSize: 5 }));
  });

  it('marks the selected products as to count and exports the list', async () => {
    renderPage({ search: `?tab=${ALL_PRODUCTS_TAB}` });
    await screen.findByText('Ibuprofen 200mg');

    fireEvent.click(button('Mark as To Count'));
    fireEvent.click(button('Export'));

    expect(allProducts.countSelected).toHaveBeenCalledTimes(1);
    expect(allProducts.exportTableData).toHaveBeenCalledTimes(1);
  });

  it('disables marking as to count when nothing is selected', async () => {
    allProducts.selectedCheckboxesAmount = 0;
    renderPage({ search: `?tab=${ALL_PRODUCTS_TAB}` });
    await screen.findByText('Ibuprofen 200mg');

    expect(button('Mark as To Count')).toBeDisabled();
  });

  it('switches between the tabs through the URL', async () => {
    const { history } = renderPage({ search: `?tab=${ALL_PRODUCTS_TAB}` });
    await screen.findByText('Ibuprofen 200mg');

    fireEvent.click(screen.getByRole('button', { name: 'To count' }));
    expect(tab(history.location.search)).toBe(TO_COUNT_TAB);
    expect(await screen.findByText('Paracetamol 500mg')).toBeInTheDocument();
    expect(screen.queryByText('Ibuprofen 200mg')).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'To resolve' }));
    expect(tab(history.location.search)).toBe(TO_RESOLVE_TAB);
    expect(await screen.findByText('Amoxicillin 250mg')).toBeInTheDocument();
  });

  it('shows the filters of the tab', async () => {
    renderPage({ search: `?tab=${TO_RESOLVE_TAB}` });
    await screen.findByText('Amoxicillin 250mg');

    expect(screen.getByText('Category')).toBeInTheDocument();
    expect(screen.getByText('Negative quantity')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Filter' })).toBeInTheDocument();
  });
});

describe('to count tab', () => {
  it('runs the actions on the selected counts', async () => {
    const store = cycleCountStore();
    renderPage({ search: `?tab=${TO_COUNT_TAB}`, store });
    await screen.findByText('Paracetamol 500mg');
    // The selection comes from the page; nothing is selected yet.
    expect(button('Start count')).toBeDisabled();
    expect(useToCountTab).toHaveBeenLastCalledWith(expect.objectContaining({
      toCountTabCheckboxes: expect.objectContaining({ selectedCheckboxesAmount: 0 }),
    }));

    fireEvent.click(screen.getByText('Print Count form PDF'));
    fireEvent.click(screen.getByText('Export Count sheet'));
    fireEvent.click(button('Export'));

    expect(toCount.printCountForm.mock.calls).toEqual([[FileFormat.PDF], [FileFormat.XLS]]);
    expect(toCount.exportTableData).toHaveBeenCalledTimes(1);
  });

  it('offers to continue an unfinished count of this location', async () => {
    const store = cycleCountStore();
    store.dispatch({ type: START_COUNT, payload: { locationId: HERE.id, requests: ['r1'] } });
    renderPage({ search: `?tab=${TO_COUNT_TAB}`, store });

    expect(await screen.findByText(/You have an unfinished count/)).toBeInTheDocument();
  });

  it('opens the assign modal its hook asks for', async () => {
    toCount.isAssignCountModalOpen = true;
    toCount.assignCountModalData = { current: [{ id: 'r1', assignee: { id: 'u1' } }] };
    renderPage({ search: `?tab=${TO_COUNT_TAB}` });

    expect(await screen.findByText('Assign products to count')).toBeInTheDocument();
  });
});

describe('to resolve tab', () => {
  it('runs the actions on the selected counts', async () => {
    renderPage({ search: `?tab=${TO_RESOLVE_TAB}` });
    await screen.findByText('Amoxicillin 250mg');
    expect(screen.getByText(/2\s+selected/)).toBeInTheDocument();

    fireEvent.click(button('Cancel count'));
    fireEvent.click(button('Assign recount'));
    fireEvent.click(button('Start resolution'));
    fireEvent.click(screen.getByText('Print Recount Form PDF'));
    fireEvent.click(screen.getByText('Export Recount Sheet'));
    fireEvent.click(button('Export'));

    expect(toResolve.openCancelCountsModal).toHaveBeenCalledTimes(1);
    expect(toResolve.openAssignCountModal).toHaveBeenCalledTimes(1);
    expect(toResolve.moveToResolving).toHaveBeenCalledTimes(1);
    expect(toResolve.printResolveForm.mock.calls).toEqual([[FileFormat.PDF], [FileFormat.XLS]]);
    expect(toResolve.exportTableData).toHaveBeenCalledTimes(1);
  });

  it('offers to continue an unfinished recount and opens the recount assign modal', async () => {
    const store = cycleCountStore();
    store.dispatch({ type: START_RESOLUTION, payload: { locationId: HERE.id, cycleCounts: ['cc1'] } });
    toResolve.isAssignCountModalOpen = true;
    toResolve.assignCountModalData = { current: [] };
    renderPage({ search: `?tab=${TO_RESOLVE_TAB}`, store });

    expect(await screen.findByText(/You have an unfinished count/)).toBeInTheDocument();
    expect(screen.getByText('Assign products to recount')).toBeInTheDocument();
  });

  it('greys the actions out when nothing is selected', async () => {
    toResolve.selectedCheckboxesAmount = 0;
    renderPage({ search: `?tab=${TO_RESOLVE_TAB}` });
    await screen.findByText('Amoxicillin 250mg');

    ['Cancel count', 'Assign recount', 'Start resolution', 'Print recount form'].forEach((name) =>
      expect(button(name)).toBeDisabled());
  });
});

describe('cycle count filter fields', () => {
  it('feeds each select its options from the page', () => {
    const props = {
      categories: ['c'],
      internalLocations: ['b'],
      tags: ['t'],
      catalogs: ['k'],
      abcClasses: ['a'],
      debouncedPeopleFetch: jest.fn(),
    };

    Object.values(cycleCountFilterFields).forEach((fields) => {
      Object.entries(fields).forEach(([name, field]) => {
        if (!field.getDynamicAttr) {
          return;
        }
        const attributes = field.getDynamicAttr(props);
        if (name.endsWith('Assignees')) {
          expect(attributes).toEqual({ loadOptions: props.debouncedPeopleFetch });
          // The people search answers with what the server found, unfiltered.
          expect(field.attributes.filterOptions(['x', 'y'])).toEqual(['x', 'y']);
        } else {
          expect(attributes).toEqual({ options: props[name] });
        }
      });
    });
    expect(Object.keys(cycleCountFilterFields.toCountTab)).toContain('countAssignees');
    expect(Object.keys(cycleCountFilterFields.toResolveTab)).toContain('recountAssignees');
    expect(Object.keys(cycleCountFilterFields.allProductsTab)).toContain('dateLastCount');
  });
});
