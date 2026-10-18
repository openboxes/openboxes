import React from 'react';

import {
  fireEvent, render, screen, waitFor, within,
} from '@testing-library/react';
import { createMemoryHistory } from 'history';
import { Provider } from 'react-redux';
import { Router } from 'react-router-dom';
import { cycleCountItem, cycleCountStore } from 'regression-tests/cycle-count/cycleCountStore';

import { FETCH_USERS } from 'actions/types';
import AssignCycleCountModal from 'components/cycleCount/AssignCycleCountModal';
import useCountStep from 'hooks/cycleCount/countStep/useCountStep';
import useAssignCycleCountModal from 'hooks/cycleCount/useAssignCycleCountModal';
import useResolveStep from 'hooks/cycleCount/useResolveStep';
import useResolveStepTable from 'hooks/cycleCount/useResolveStepTable';
import apiClient from 'utils/apiClient';

import '@testing-library/jest-dom';

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

// The steps' workflow hooks (fetching, saving, validating) are stand-ins here: the screens are
// checked for what they show from the store and which workflow action each control triggers.
jest.mock('hooks/cycleCount/countStep/useCountStep', () => jest.fn());
jest.mock('hooks/cycleCount/useResolveStep', () => jest.fn());
jest.mock('hooks/cycleCount/useResolveStepTable', () => jest.fn());
jest.mock('hooks/cycleCount/useAssignCycleCountModal', () => jest.fn());

/* eslint-disable import/first,import/order */
import CountStep from 'components/cycleCount/toCountTab/CountStep';
import ResolveStep from 'components/cycleCount/toResolveTab/ResolveStep';
import ResolveStepTable from 'components/cycleCount/toResolveTab/ResolveStepTable';
/* eslint-enable import/first,import/order */

// A location of its own: getCurrentLocation caches the locations it has seen by id.
const BIN_LOCATION = {
  location: { id: 'loc-bins', name: 'Bin warehouse' },
  activities: ['MANAGE_INVENTORY', 'PUTAWAY_STOCK', 'PICK_STOCK'],
};

const cycleCounts = () => [
  {
    id: 'cc1',
    cycleCountItems: [
      cycleCountItem(),
      cycleCountItem({
        id: 'item2', custom: true, quantityCounted: 3, comment: 'Found behind', inventoryItem: { lotNumber: 'LOT-9', expirationDate: null },
      }),
    ],
  },
  {
    id: 'cc2',
    cycleCountItems: [cycleCountItem({
      id: 'item3',
      quantityCounted: 11,
      comment: 'Fridge',
      product: { id: 'p2', productCode: 'P-0002', name: 'Insulin glargine' },
    })],
  },
];

const renderScreen = (ui, store) => {
  const history = createMemoryHistory({ initialEntries: ['/openboxes/inventory/cycleCount/count'] });
  return {
    history,
    store,
    ...render(
      <Provider store={store}>
        <Router history={history}>{ui}</Router>
      </Provider>,
    ),
  };
};

const button = (name) => screen.getByRole('button', { name });

beforeEach(() => {
  apiClient.get.mockResolvedValue({ data: { messages: {}, currentLocale: 'en' } });
  apiClient.delete.mockResolvedValue({ data: {} });
  useAssignCycleCountModal.mockReturnValue({ columns: [], handleAssign: jest.fn() });
});

afterEach(() => jest.clearAllMocks());

describe('count step', () => {
  const countStep = (overrides = {}) => ({
    currentLocationId: 'loc1',
    cycleCountIds: ['cc1', 'cc2'],
    printCountForm: jest.fn(),
    next: jest.fn(),
    resolveDiscrepancies: jest.fn(),
    back: jest.fn(),
    isStepEditable: true,
    isSaveDisabled: false,
    setIsSaveDisabled: jest.fn(),
    validateExistenceOfCycleCounts: jest.fn((then) => then()),
    importItems: jest.fn(),
    sortByProductName: false,
    setSortByProductName: jest.fn(),
    importErrors: [],
    isAssignCountModalOpen: false,
    handleBackToList: jest.fn(),
    closeAssignCountModal: jest.fn(),
    assignCountModalData: { current: [] },
    handleCountStepHeaderSave: jest.fn(),
    ...overrides,
  });

  it('shows one editable table per product with the counted rows from the store', () => {
    const hook = countStep();
    useCountStep.mockReturnValue(hook);
    renderScreen(
      <CountStep />,
      cycleCountStore({ cycleCounts: cycleCounts(), ...BIN_LOCATION }),
    );

    expect(screen.getByText('P-0001 Ibuprofen 200mg')).toBeInTheDocument();
    expect(screen.getByText('P-0002 Insulin glargine')).toBeInTheDocument();
    const quantities = screen.getAllByRole('spinbutton').map((input) => input.value);
    expect(quantities).toEqual(['7', '3', '11']);
    expect(screen.getByDisplayValue('Found behind')).toBeInTheDocument();
    // The location uses bins, so each table has a bin column.
    expect(screen.getAllByText('Bin Location')).toHaveLength(2);

    fireEvent.click(button('Next'));
    expect(hook.validateExistenceOfCycleCounts).toHaveBeenCalledWith(hook.next);
    expect(hook.next).toHaveBeenCalledTimes(1);
    fireEvent.click(button('Save progress'));
    expect(hook.handleCountStepHeaderSave).toHaveBeenCalledTimes(1);
  });

  it('adds and removes rows of a table', async () => {
    useCountStep.mockReturnValue(countStep({ cycleCountIds: ['cc1'] }));
    const store = cycleCountStore({ cycleCounts: cycleCounts() });
    const { container } = renderScreen(<CountStep />, store);
    expect(screen.getAllByRole('spinbutton')).toHaveLength(2);
    // The default location does not use bins.
    expect(screen.queryByText('Bin Location')).not.toBeInTheDocument();

    fireEvent.click(button('Add new record'));
    expect(screen.getAllByRole('spinbutton')).toHaveLength(3);

    // The row added on screen goes without a request, the saved custom row is deleted on the
    // server.
    const deleteIcons = () => container.querySelectorAll('.count-step-actions svg');
    expect(deleteIcons()).toHaveLength(2);
    fireEvent.click(deleteIcons()[1]);
    await waitFor(() => expect(screen.getAllByRole('spinbutton')).toHaveLength(2));
    expect(apiClient.delete).not.toHaveBeenCalled();

    fireEvent.click(deleteIcons()[0]);
    await waitFor(() => expect(screen.getAllByRole('spinbutton')).toHaveLength(1));
    expect(apiClient.delete).toHaveBeenCalledWith('/api/facilities/loc1/cycle-counts/items/item2');
  });

  it('shows the counted values read only on the confirm step and saves through the validation', () => {
    const hook = countStep({ isStepEditable: false });
    useCountStep.mockReturnValue(hook);
    renderScreen(<CountStep />, cycleCountStore({ cycleCounts: cycleCounts() }));

    expect(screen.queryByRole('spinbutton')).not.toBeInTheDocument();
    expect(screen.getByText('Found behind')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Add new record' })).not.toBeInTheDocument();

    fireEvent.click(button('Save'));
    expect(hook.setIsSaveDisabled).toHaveBeenCalledWith(true);
    expect(hook.validateExistenceOfCycleCounts).toHaveBeenCalledWith(hook.resolveDiscrepancies);
    expect(hook.resolveDiscrepancies).toHaveBeenCalledTimes(1);
    fireEvent.click(button('Back'));
    expect(hook.back).toHaveBeenCalledTimes(1);
  });

  it('locks the form when the location changes while counting', () => {
    useCountStep.mockReturnValue(countStep());
    const store = cycleCountStore({ cycleCounts: cycleCounts() });
    const { rerender } = renderScreen(<CountStep />, store);
    expect(button('Next')).toBeEnabled();

    useCountStep.mockReturnValue(countStep({ currentLocationId: 'loc2' }));
    rerender(
      <Provider store={store}>
        <Router history={createMemoryHistory()}><CountStep /></Router>
      </Provider>,
    );

    expect(button('Next')).toBeDisabled();
    expect(button('Save progress')).toBeDisabled();
  });

  it('asks who recounts after the count when its hook opens the assign modal', () => {
    useCountStep.mockReturnValue(countStep({
      isAssignCountModalOpen: true,
      assignCountModalData: { current: [{ id: 'cc1' }] },
    }));
    renderScreen(<CountStep />, cycleCountStore({ cycleCounts: cycleCounts() }));

    expect(screen.getByText('Assign products to recount')).toBeInTheDocument();
    expect(button('Skip')).toBeInTheDocument();
    expect(useAssignCycleCountModal).toHaveBeenCalledWith(expect.objectContaining({
      isRecount: true, assignDataDirectly: true,
    }));
  });
});

describe('AssignCycleCountModal', () => {
  it('renders nothing while closed and gives the page its scrollbar back', () => {
    document.body.style.overflowY = 'hidden';
    const { container } = renderScreen(
      <AssignCycleCountModal
        isOpen={false}
        closeModal={jest.fn()}
        selectedCycleCounts={{ current: [] }}
      />,
      cycleCountStore(),
    );

    expect(container).toBeEmptyDOMElement();
    expect(document.body.style.overflowY).toBe('auto');
  });

  it('assigns only once someone or a deadline is picked, and closes', () => {
    const handleAssign = jest.fn();
    useAssignCycleCountModal.mockReturnValue({ columns: [], handleAssign });
    const closeModal = jest.fn();
    const selected = { current: [{ id: 'r1' }] };
    const { rerender, store } = renderScreen(
      <AssignCycleCountModal
        isOpen
        closeModal={closeModal}
        selectedCycleCounts={selected}
        showSkipButton
      />,
      cycleCountStore(),
    );
    expect(screen.getByText('Assign products to count')).toBeInTheDocument();
    expect(button('Assign')).toBeDisabled();

    selected.current = [{ id: 'r1' }, { id: 'r2', deadline: '2026-10-10' }];
    rerender(
      <Provider store={store}>
        <Router history={createMemoryHistory()}>
          <AssignCycleCountModal
            isOpen
            closeModal={closeModal}
            selectedCycleCounts={selected}
            showSkipButton
          />
        </Router>
      </Provider>,
    );
    fireEvent.click(button('Assign'));
    expect(handleAssign).toHaveBeenCalledTimes(1);

    fireEvent.click(button('Skip'));
    expect(closeModal).toHaveBeenCalledTimes(1);
  });
});

describe('resolve step', () => {
  const columns = [{
    id: 'quantityRecounted',
    accessorKey: 'quantityRecounted',
    header: () => 'Quantity recounted',
    cell: ({ getValue }) => <span>{`recounted ${getValue()}`}</span>,
  }];

  const resolveStep = (overrides = {}) => ({
    tableData: [
      { id: 'cc1', cycleCountItems: [{ id: 'i1', quantityRecounted: 5 }] },
      { id: 'cc2', cycleCountItems: [{ id: 'i2', quantityRecounted: 8 }] },
    ],
    validationErrors: {},
    printRecountForm: jest.fn(),
    refreshCountItems: jest.fn(),
    next: jest.fn(),
    tableMeta: { updateData: jest.fn() },
    addEmptyRow: jest.fn(),
    removeRow: jest.fn(),
    assignRecountedBy: jest.fn(() => jest.fn()),
    getRecountedDate: jest.fn(() => new Date('2026-09-30T12:00:00').toISOString()),
    updateRecountedDate: jest.fn(() => jest.fn()),
    shouldHaveRootCause: jest.fn(),
    back: jest.fn(),
    save: jest.fn(),
    isStepEditable: true,
    getRecountedBy: jest.fn(() => ({ id: 'u2', label: 'Jane Counter' })),
    getDefaultRecountedBy: jest.fn(() => ({ id: 'u2', label: 'Jane Counter' })),
    getCountedBy: jest.fn(() => ({ id: 'u1', name: 'Admin' })),
    submitRecount: jest.fn(),
    getProduct: jest.fn((id) => (id === 'cc1'
      ? { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' }
      : { id: 'p2', productCode: 'P-0002', name: 'Insulin glargine' })),
    getDateCounted: jest.fn(() => new Date('2026-09-29T12:00:00').toISOString()),
    triggerValidation: jest.fn(),
    isSaveDisabled: false,
    setIsSaveDisabled: jest.fn(),
    cycleCountsWithItemsWithoutRecount: [],
    sortByProductName: false,
    setSortByProductName: jest.fn(),
    forceRerender: jest.fn(),
    importErrors: [],
    importItems: jest.fn(),
    ...overrides,
  });

  beforeEach(() => {
    useResolveStepTable.mockReturnValue({ columns, defaultColumn: {}, users: [] });
  });

  it('shows one recount table per product with its count details', () => {
    const hook = resolveStep();
    useResolveStep.mockReturnValue(hook);
    renderScreen(<ResolveStep />, cycleCountStore());

    expect(screen.getByText('P-0001 Ibuprofen 200mg')).toBeInTheDocument();
    expect(screen.getByText('P-0002 Insulin glargine')).toBeInTheDocument();
    expect(screen.getByText('recounted 5')).toBeInTheDocument();
    expect(screen.getByText('recounted 8')).toBeInTheDocument();
    expect(screen.getAllByText('29/Sep/2026')).toHaveLength(2);
    expect(screen.getAllByText('30/Sep/2026')).toHaveLength(2);
    expect(hook.updateRecountedDate).toHaveBeenCalledWith('cc1');
    expect(useResolveStepTable).toHaveBeenCalledWith(expect.objectContaining({
      cycleCountId: 'cc2', productId: 'p2', isStepEditable: true, isFormDisabled: false,
    }));

    fireEvent.click(button('Save progress'));
    expect(hook.save).toHaveBeenCalledWith({ shouldRefetchLotNumbers: true });
    fireEvent.click(button('Next'));
    expect(hook.next).toHaveBeenCalledTimes(1);

    const firstTable = screen.getByText('P-0001 Ibuprofen 200mg').closest('.list-page-list-section');
    fireEvent.click(within(firstTable).getByRole('button', { name: 'Add new record' }));
    expect(hook.addEmptyRow).toHaveBeenCalledWith('p1', 'cc1');
  });

  it('shows the recount read only on the confirm step and submits it', () => {
    const hook = resolveStep({ isStepEditable: false });
    useResolveStep.mockReturnValue(hook);
    renderScreen(<ResolveStep />, cycleCountStore());

    expect(screen.getAllByText('30/Sep/2026')).toHaveLength(2);
    expect(screen.getAllByText('Jane Counter')).toHaveLength(2);
    expect(screen.queryByRole('button', { name: 'Add new record' })).not.toBeInTheDocument();

    fireEvent.click(button('Save'));
    expect(hook.submitRecount).toHaveBeenCalledTimes(1);
    fireEvent.click(button('Back'));
    expect(hook.back).toHaveBeenCalledTimes(1);
  });

  it('warns about lines whose stock ran out during the recount', () => {
    useResolveStep.mockReturnValue(resolveStep({
      cycleCountsWithItemsWithoutRecount: [{
        id: 'cc1',
        cycleCountItems: [
          {
            id: 'i1', quantityOnHand: 0, quantityCounted: 4, binLocation: { name: 'A-01' }, inventoryItem: { lotNumber: 'LOT-1' },
          },
          { id: 'i3', quantityOnHand: 6, quantityCounted: 6 },
        ],
      }],
    }));
    renderScreen(<ResolveStep />, cycleCountStore());

    expect(screen.getByText(/Inventory was updated for bin A-01, lot LOT-1/)).toBeInTheDocument();
    expect(screen.getAllByText(/Inventory was updated/)).toHaveLength(1);
  });
});

describe('resolve step table', () => {
  it('assigns the person picked as the recounter of this count', () => {
    useResolveStepTable.mockReturnValue({
      columns: [],
      defaultColumn: {},
      users: [{ id: 'u3', value: 'u3', label: 'Sam Recounter' }],
    });
    const assignToCount = jest.fn();
    const assignRecountedBy = jest.fn(() => assignToCount);
    const store = cycleCountStore();
    store.dispatch({ type: FETCH_USERS, payload: { data: [] } });
    const { container } = renderScreen(
      <ResolveStepTable
        id="cc1"
        product={{ id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' }}
        dateCounted={new Date('2026-09-29T12:00:00').toISOString()}
        dateRecounted={new Date('2026-09-30T12:00:00').toISOString()}
        recountedBy={null}
        defaultRecountedBy={null}
        countedBy={{ name: 'Admin' }}
        tableData={[]}
        tableMeta={{ updateData: jest.fn() }}
        addEmptyRow={jest.fn()}
        removeRow={jest.fn()}
        assignRecountedBy={assignRecountedBy}
        updateRecountedDate={jest.fn()}
        validationErrors={{}}
        shouldHaveRootCause={jest.fn()}
        isStepEditable
        triggerValidation={jest.fn()}
        isFormDisabled={false}
        forceRerender={jest.fn()}
      />,
      store,
    );
    expect(screen.getByText('No inventory item in stock for this product')).toBeInTheDocument();

    const input = container.querySelector('.min-width-250 input');
    fireEvent.focus(input);
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(screen.getByText('Sam Recounter'));

    expect(assignRecountedBy).toHaveBeenCalledWith('cc1');
    expect(assignToCount).toHaveBeenCalledWith(expect.objectContaining({ id: 'u3' }));
  });
});
