import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { createMemoryHistory } from 'history';
import { Provider } from 'react-redux';
import { Router } from 'react-router-dom';
import {
  cycleCountItem, cycleCountStore, HERE, storedItem,
} from 'regression-tests/cycle-count/cycleCountStore';

import { FETCH_USERS, START_COUNT, START_RESOLUTION } from 'actions/types';
import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
import CycleCountDraftInfoBar from 'components/cycleCount/CycleCountDraftInfoBar';
import CycleCountHeader from 'components/cycleCount/CycleCountHeader';
import CycleCountStockDiscrepancyInfoBar from 'components/cycleCount/CycleCountStockDiscrepancyInfoBar';
import AddNewRecordFooter from 'components/cycleCount/tableFooter/AddNewRecordFooter';
import CountedByHeader from 'components/cycleCount/tableHeader/CountedByHeader';
import DateCountedHeader from 'components/cycleCount/tableHeader/DateCountedHeader';
import HeaderCell from 'components/cycleCount/tableHeader/HeaderCell';
import ProductDataHeader from 'components/cycleCount/tableHeader/ProductDataHeader';
import CountStepHeader from 'components/cycleCount/toCountTab/CountStepHeader';
import ResolveStepHeader from 'components/cycleCount/toResolveTab/ResolveStepHeader';
import { TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import FileFormat from 'consts/fileFormat';

import '@testing-library/jest-dom';

// The API client is never reached here; mocking it keeps the real one (which loads the app's
// store) out of the import graph.
jest.mock('utils/apiClient', () => ({ __esModule: true, default: { get: jest.fn() } }));

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

const cycleCounts = () => [{ id: 'cc1', cycleCountItems: [cycleCountItem()] }];

const renderWith = (ui, { store = cycleCountStore({ cycleCounts: cycleCounts() }), path = '/openboxes/x' } = {}) => {
  const history = createMemoryHistory({ initialEntries: [path] });
  const utils = render(
    <Provider store={store}>
      <Router history={history}>{ui}</Router>
    </Provider>,
  );
  return { store, history, ...utils };
};

const button = (name) => screen.getByRole('button', { name });

describe('cycle count page header', () => {
  it('shows the page title', () => {
    renderWith(<CycleCountHeader />);

    expect(screen.getByText('Cycle Count')).toBeInTheDocument();
  });

  it('shows a translated column header', () => {
    renderWith(<HeaderCell id="react.cycleCount.table.lotNumber.label" defaultMessage="Lot" />);

    expect(screen.getByText('Lot')).toBeInTheDocument();
  });
});

describe('ProductDataHeader', () => {
  it('names the product the count is for', () => {
    renderWith(<ProductDataHeader cycleCountId="cc1" />);

    expect(screen.getByText('P-0001 Ibuprofen 200mg')).toBeInTheDocument();
  });
});

describe('DateCountedHeader', () => {
  it('shows the date the product was counted on when the step is read only', () => {
    renderWith(<DateCountedHeader cycleCountId="cc1" isStepEditable={false} isFormDisabled={false} />);

    expect(screen.getByText('Date counted')).toBeInTheDocument();
    expect(screen.getByText('30/Sep/2026')).toBeInTheDocument();
  });

  it('stores the date picked as the count date and marks the count as changed', () => {
    const root = document.createElement('div');
    root.id = 'root';
    document.body.appendChild(root);
    const { store, container } = renderWith(
      <DateCountedHeader cycleCountId="cc1" isStepEditable isFormDisabled={false} />,
    );
    expect(screen.getByText('30/Sep/2026')).toBeInTheDocument();

    fireEvent.click(container.querySelector('.date-field-input'));
    fireEvent.click(screen.getByRole('option', { name: 'day-14' }));

    const dateCounted = store.getState().countWorkflow.dateCounted.cc1;
    expect(dateCounted.format('YYYY-MM-DD')).toBe('2026-09-14');
    expect(storedItem(store, 'cc1', 'item1').updated).toBe(true);
    root.remove();
  });

  it('keeps the date fixed while the form is disabled', () => {
    const { container } = renderWith(
      <DateCountedHeader cycleCountId="cc1" isStepEditable isFormDisabled />,
    );

    expect(container.querySelector('.date-field-input')).toHaveClass('disabled');
  });
});

describe('CountedByHeader', () => {
  const storeWithUsers = () => {
    const store = cycleCountStore({ cycleCounts: cycleCounts() });
    store.dispatch({
      type: FETCH_USERS,
      payload: { data: [{ id: 'u1', name: 'Admin' }, { id: 'u2', name: 'Jane Counter' }] },
    });
    return store;
  };

  it('shows who counted when the step is read only', () => {
    renderWith(
      <CountedByHeader cycleCountId="cc1" isStepEditable={false} isFormDisabled={false} />,
    );

    expect(screen.getByText('Counted by')).toBeInTheDocument();
    expect(screen.getByText('Admin')).toBeInTheDocument();
  });

  it('stores the person picked as the counter', () => {
    const { store, container } = renderWith(
      <CountedByHeader cycleCountId="cc1" isStepEditable isFormDisabled={false} />,
      { store: storeWithUsers() },
    );

    const input = container.querySelector('input');
    fireEvent.focus(input);
    fireEvent.keyDown(input, { key: 'ArrowDown', keyCode: 40 });
    fireEvent.click(screen.getByText('Jane Counter'));

    expect(store.getState().countWorkflow.countedBy.cc1).toMatchObject({ id: 'u2', name: 'Jane Counter' });
    expect(storedItem(store, 'cc1', 'item1').updated).toBe(true);
  });
});

describe('AddNewRecordFooter', () => {
  it('adds an empty row to the count', () => {
    const { store } = renderWith(
      <AddNewRecordFooter cycleCountId="cc1" isStepEditable isFormDisabled={false} />,
    );

    fireEvent.click(button('Add new record'));

    const items = store.getState().countWorkflow.entities.cc1.cycleCountItems;
    expect(items).toHaveLength(2);
    expect(items[1].id).toMatch(/^newRow/);
    expect(items[1].product.id).toBe('p1');
  });

  it('is disabled while the form is disabled and absent when the step is read only', () => {
    const { rerender, store } = renderWith(
      <AddNewRecordFooter cycleCountId="cc1" isStepEditable isFormDisabled />,
    );
    expect(button('Add new record')).toBeDisabled();

    rerender(
      <Provider store={store}>
        <Router history={createMemoryHistory()}>
          <AddNewRecordFooter cycleCountId="cc1" isStepEditable={false} isFormDisabled={false} />
        </Router>
      </Provider>,
    );
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });
});

describe('ConfirmStepHeader', () => {
  const props = () => ({
    back: jest.fn(),
    save: jest.fn(),
    setIsSaveDisabled: jest.fn(),
    isSaveDisabled: false,
    isFormDisabled: false,
    redirectTab: TO_RESOLVE_TAB,
    redirectLabel: 'react.cycleCount.redirectToResolveTab.label',
    redirectDefaultMessage: 'Back to Resolve tab',
  });

  it('blocks saving twice: save disables the button, back enables it again', () => {
    const p = props();
    renderWith(<ConfirmStepHeader {...p} />);

    fireEvent.click(button('Save'));
    expect(p.setIsSaveDisabled).toHaveBeenLastCalledWith(true);
    expect(p.save).toHaveBeenCalledTimes(1);

    fireEvent.click(button('Back'));
    expect(p.setIsSaveDisabled).toHaveBeenLastCalledWith(false);
    expect(p.back).toHaveBeenCalledTimes(1);
  });

  it('disables save while a save is running or the form is disabled', () => {
    const { rerender, store } = renderWith(<ConfirmStepHeader {...props()} isSaveDisabled />);
    expect(button('Save')).toBeDisabled();
    expect(button('Back')).toBeEnabled();

    rerender(
      <Provider store={store}>
        <Router history={createMemoryHistory()}>
          <ConfirmStepHeader {...props()} isFormDisabled />
        </Router>
      </Provider>,
    );
    expect(button('Save')).toBeDisabled();
    expect(button('Back')).toBeDisabled();
  });

  it('leads back to the list tab it came from', () => {
    const { history } = renderWith(<ConfirmStepHeader {...props()} />);

    fireEvent.click(screen.getByText('Back to Resolve tab'));

    expect(`${history.location.pathname}${history.location.search}`)
      .toBe('/openboxes/inventory/cycleCount?tab=TO_RESOLVE');
  });
});

describe('CountStepHeader', () => {
  const props = () => ({
    printCountForm: jest.fn(),
    next: jest.fn(),
    save: jest.fn(),
    importItems: jest.fn(),
    isFormDisabled: false,
    sortByProductName: false,
    setSortByProductName: jest.fn(),
    importErrors: [],
    handleBackToList: jest.fn(),
  });

  it('wires every action of the count step', () => {
    const p = props();
    renderWith(<CountStepHeader {...p} />);

    fireEvent.click(screen.getByText('Print Count form PDF'));
    fireEvent.click(screen.getByText('Export Count sheet'));
    expect(p.printCountForm.mock.calls).toEqual([[FileFormat.PDF], [FileFormat.XLS]]);

    fireEvent.click(button('Save progress'));
    expect(p.save).toHaveBeenCalledTimes(1);
    fireEvent.click(button('Next'));
    expect(p.next).toHaveBeenCalledTimes(1);
    fireEvent.click(screen.getByText('Back to Cycle Count List'));
    expect(p.handleBackToList).toHaveBeenCalledTimes(1);

    fireEvent.click(button('Sort alphabetically'));
    const toggle = p.setSortByProductName.mock.calls[0][0];
    expect(toggle(false)).toBe(true);
    expect(toggle(true)).toBe(false);
  });

  it('marks the sort button while the list is sorted and shows import errors', () => {
    renderWith(<CountStepHeader {...props()} sortByProductName importErrors={['Row 3: unknown lot']} />);

    expect(button('Sort alphabetically')).toHaveClass('secondary-button');
    expect(screen.getByText('Row 3: unknown lot')).toBeInTheDocument();
  });

  it('disables the actions while the form is disabled', () => {
    renderWith(<CountStepHeader {...props()} isFormDisabled />);

    ['Print count form', 'Sort alphabetically', 'Save progress', 'Next'].forEach((name) =>
      expect(button(name)).toBeDisabled());
  });
});

describe('ResolveStepHeader', () => {
  const props = () => ({
    printRecountForm: jest.fn(),
    refreshCountItems: jest.fn(),
    next: jest.fn(),
    save: jest.fn(),
    importItems: jest.fn(),
    isFormDisabled: false,
    sortByProductName: true,
    setSortByProductName: jest.fn(),
    importErrors: [],
  });

  it('wires every action of the resolve step', () => {
    const p = props();
    const { history } = renderWith(<ResolveStepHeader {...p} />);

    fireEvent.click(screen.getByText('Print Recount Form PDF'));
    fireEvent.click(screen.getByText('Export Recount Sheet'));
    expect(p.printRecountForm.mock.calls).toEqual([[FileFormat.PDF], [FileFormat.XLS]]);

    fireEvent.click(button('Reload'));
    expect(p.refreshCountItems).toHaveBeenCalledWith();
    fireEvent.click(button('Save progress'));
    expect(p.save).toHaveBeenCalledTimes(1);
    fireEvent.click(button('Next'));
    expect(p.next).toHaveBeenCalledTimes(1);

    expect(button('Sort alphabetically')).toHaveClass('secondary-button');
    fireEvent.click(button('Sort alphabetically'));
    expect(p.setSortByProductName.mock.calls[0][0](true)).toBe(false);

    fireEvent.click(screen.getByText('Back to Resolve tab'));
    expect(history.location.search).toBe('?tab=TO_RESOLVE');
  });

  it('shows import errors and disables the actions while the form is disabled', () => {
    renderWith(<ResolveStepHeader {...props()} isFormDisabled importErrors={['Bad file']} />);

    expect(screen.getByText('Bad file')).toBeInTheDocument();
    ['Reload', 'Save progress', 'Next'].forEach((name) => expect(button(name)).toBeDisabled());
  });
});

describe('CycleCountDraftInfoBar', () => {
  const storeWithDrafts = () => {
    const store = cycleCountStore();
    store.dispatch({ type: START_COUNT, payload: { locationId: HERE.id, requests: ['r1'] } });
    store.dispatch({ type: START_RESOLUTION, payload: { locationId: HERE.id, cycleCounts: ['cc1'] } });
    return store;
  };

  it('continues an unfinished count on the count step', () => {
    const { history } = renderWith(
      <CycleCountDraftInfoBar tab={TO_COUNT_TAB} />,
      { store: storeWithDrafts() },
    );

    expect(screen.getByText(/You have an unfinished count/)).toBeInTheDocument();
    fireEvent.click(button('Continue'));

    expect(history.location.pathname).toBe('/openboxes/inventory/cycleCount/count');
  });

  it('continues an unfinished recount on the resolve step', () => {
    const { history } = renderWith(
      <CycleCountDraftInfoBar tab={TO_RESOLVE_TAB} />,
      { store: storeWithDrafts() },
    );

    fireEvent.click(button('Continue'));

    expect(history.location.pathname).toBe('/openboxes/inventory/cycleCount/resolve');
  });

  it('discards only the draft of its own tab', () => {
    const { store } = renderWith(
      <CycleCountDraftInfoBar tab={TO_COUNT_TAB} />,
      { store: storeWithDrafts() },
    );

    fireEvent.click(button('Discard'));

    expect(store.getState().cycleCount.requests).toEqual({});
    expect(store.getState().cycleCount.cycleCounts).toEqual({ [HERE.id]: ['cc1'] });
  });
});

describe('CycleCountStockDiscrepancyInfoBar', () => {
  it('describes each line whose stock changed during the count', () => {
    renderWith(
      <CycleCountStockDiscrepancyInfoBar
        outOfStockItems={[
          {
            id: 'i1', binLocation: { name: 'A-01' }, inventoryItem: { lotNumber: 'LOT-1' }, quantityOnHand: 0, quantityCounted: 4,
          },
          {
            id: 'i2', binLocation: null, inventoryItem: { lotNumber: null }, quantityOnHand: 0, quantityCounted: 2,
          },
        ]}
      />,
    );

    expect(screen.getByText(
      'Inventory was updated for bin A-01, lot LOT-1 during your count. Discrepancy found QOH = 0 and quantity counted = 4',
    )).toBeInTheDocument();
    expect(screen.getByText(
      'Inventory was updated for bin DEFAULT, lot NO LOT during your count. Discrepancy found QOH = 0 and quantity counted = 2',
    )).toBeInTheDocument();
  });
});
