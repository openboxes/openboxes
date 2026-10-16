import React from 'react';

import {
  act, fireEvent, render, screen, waitFor,
} from '@testing-library/react';
import { initialize, localizeReducer } from 'react-localize-redux';
import { Provider } from 'react-redux';
import configureStore from 'redux-mock-store';

import '@testing-library/jest-dom';

// The bundle provides lodash as the global "_"; the cards and the color mapping rely on it.
global._ = jest.requireActual('lodash');

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
}));

// The action creators record their arguments; the tests assert what the dashboard dispatches.
jest.mock('actions', () => {
  const creator = (type) => jest.fn((...args) => ({ type, args }));
  return {
    addToIndicators: creator('addToIndicators'),
    fetchConfig: creator('fetchConfig'),
    fetchConfigAndData: creator('fetchConfigAndData'),
    fetchIndicators: creator('fetchIndicators'),
    reloadIndicator: creator('reloadIndicator'),
    reorderIndicators: creator('reorderIndicators'),
    resetIndicators: creator('resetIndicators'),
  };
});

// Charts need a canvas jsdom does not have.
jest.mock('react-chartjs-2', () => {
  const chart = (kind) => () => <div data-testid={`chart-${kind}`} />;
  return {
    defaults: { scale: { ticks: {} } },
    Line: chart('line'),
    Bar: chart('bar'),
    Doughnut: chart('doughnut'),
    HorizontalBar: chart('horizontalBar'),
  };
});

// Dragging is not something jsdom can do: the sortable containers hand their props to the
// tests, which end a sort the way the library would.
const mockSortables = {};
jest.mock('react-sortable-hoc', () => ({
  SortableContainer: (Component) => (props) => {
    mockSortables[props.loadIndicator ? 'graph' : 'number'] = props;
    return <Component {...props} />;
  },
  SortableElement: (Component) => Component,
  sortableHandle: (Component) => Component,
}));

/* eslint-disable import/first,import/order */
import Dashboard from 'components/dashboard/Dashboard';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const localize = localizeReducer(undefined, initialize({
  languages: [{ name: 'English', code: 'en' }],
  options: { renderToStaticMarkup: false },
}));

const CONFIG = {
  dashboard: {
    personal: {
      name: 'My dashboard',
      filters: { Category: { endpoint: '/api/categories' } },
      widgets: [{ widgetId: 'g1', order: 1 }, { widgetId: 'n1', order: 1 }],
    },
    warehouse: { name: 'Warehouse', widgets: [] },
  },
  dashboardWidgets: {
    g1: {
      title: 'Graph one', type: 'graph', graphType: 'bar', enabled: true,
    },
    n1: { title: 'Number one', type: 'number', enabled: true },
    n2: { title: 'Number two', type: 'number', enabled: true },
  },
};

const GRAPHS = [{
  id: 1, widgetId: 'g1', title: 'Graph one', type: 'error', data: [],
}];
const NUMBERS = [{
  id: 2, widgetId: 'n1', title: 'Number one', numberType: 'number', number: 7,
}];

const createStore = ({
  config = CONFIG, activeConfig = 'personal', locationId = 'loc1', graphs = GRAPHS, numbers = NUMBERS,
} = {}) => configureStore([])({
  localize,
  indicators: {
    data: graphs, numberData: numbers, config, activeConfig,
  },
  session: { currentLocation: { id: locationId }, user: { id: 'u1' }, currencyCode: 'USD' },
});

const renderDashboard = (store, configId = 'index') => {
  const utils = render(
    <Provider store={store}>
      <Dashboard match={{ params: { configId } }} />
    </Provider>,
  );
  return { ...utils, store };
};

const types = (store) => store.getActions().map((action) => action.type);
const actionOf = (store, type) => store.getActions().find((action) => action.type === type);

describe('Dashboard', () => {
  beforeEach(() => {
    sessionStorage.clear();
    apiClient.get.mockImplementation((url) => {
      if (url === '/api/dashboard/fillRateDestinations') {
        return Promise.resolve({ data: { data: [{ id: 'b', name: 'B' }, { id: 'a', name: 'A' }] } });
      }
      if (url.endsWith('/subdashboardKeys')) {
        return Promise.resolve({ data: ['personal', 'warehouse'] });
      }
      return Promise.resolve({ data: { data: [] } });
    });
    apiClient.post.mockResolvedValue({});
  });

  afterEach(() => jest.clearAllMocks());

  it('loads the config and data of the stored sub-dashboard when no config is loaded yet', async () => {
    sessionStorage.setItem('dashboardKey', 'warehouse');
    const { store } = renderDashboard(createStore({ config: {}, numbers: [] }));

    await waitFor(() => expect(types(store)).toContain('fetchConfigAndData'), { timeout: 5000 });
    expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/mainDashboard/subdashboardKeys');
    expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/fillRateDestinations');
    expect(types(store)).toEqual(['resetIndicators', 'fetchConfigAndData']);
    expect(actionOf(store, 'fetchConfigAndData').args).toEqual(['loc1', 'warehouse', 'u1', 'mainDashboard']);
    expect(sessionStorage.getItem('dashboardKey')).toBe('warehouse');
    // No number data yet: the number cards are loading placeholders.
    expect(document.querySelectorAll('.loader-div')).toHaveLength(4);
  });

  it('ignores a stored key that is not one of the sub-dashboards and uses the one in the URL', async () => {
    sessionStorage.setItem('dashboardKey', 'other');
    const first = renderDashboard(createStore({ config: {} }));
    await waitFor(() => expect(types(first.store)).toContain('fetchConfigAndData'));
    expect(actionOf(first.store, 'fetchConfigAndData').args[1]).toBe('personal');
    first.unmount();

    const second = renderDashboard(createStore({ config: {} }), 'warehouse');
    await waitFor(() => expect(types(second.store)).toContain('fetchConfigAndData'));
    expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/warehouse/subdashboardKeys');
    expect(actionOf(second.store, 'fetchConfigAndData').args).toEqual(['loc1', 'warehouse', 'u1', 'warehouse']);
  });

  it('does not fetch indicators without a current location', async () => {
    const { store } = renderDashboard(createStore({ locationId: '' }));
    await waitFor(() => expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/fillRateDestinations'));
    expect(apiClient.get).not.toHaveBeenCalledWith(expect.stringMatching(/subdashboardKeys/));
    expect(types(store)).toEqual([]);
  });

  it('fetches the indicators of a loaded config and switches sub-dashboards from the list', async () => {
    const { store } = renderDashboard(createStore());
    await waitFor(() => expect(types(store)).toContain('fetchIndicators'));
    expect(actionOf(store, 'fetchIndicators').args).toEqual([CONFIG, 'personal', 'loc1', 'u1']);

    expect(screen.getByText('Graph one')).toBeInTheDocument();
    expect(screen.getByText('Number one')).toBeInTheDocument();
    // The personal dashboard has page filters, so the filter bar shows.
    expect(screen.getByText('+ Add filter')).toBeInTheDocument();

    store.clearActions();
    fireEvent.click(screen.getByText('Warehouse'));
    expect(types(store)).toEqual(['resetIndicators', 'fetchIndicators']);
    expect(actionOf(store, 'fetchIndicators').args[1]).toBe('warehouse');
    expect(sessionStorage.getItem('dashboardKey')).toBe('warehouse');
  });

  it('opens and closes the navigation', () => {
    const { container } = renderDashboard(createStore());
    const nav = container.querySelector('.configs-left-nav');
    const initiallyHidden = nav.classList.contains('hidden');
    fireEvent.click(container.querySelector('.toggle-nav'));
    expect(nav.classList.contains('hidden')).toBe(!initiallyHidden);
    fireEvent.click(container.querySelector('.overlay'));
    expect(nav.classList.contains('hidden')).toBe(initiallyHidden);
  });

  it('reloads a failed widget with its merged configuration', () => {
    const { store, container } = renderDashboard(createStore());
    fireEvent.click(container.querySelector('.fa-repeat').closest('button'));
    expect(actionOf(store, 'reloadIndicator').args).toEqual([
      { ...CONFIG.dashboardWidgets.g1, widgetId: 'g1', order: 1 },
      undefined,
      'loc1',
    ]);
  });

  it('saves a reordered personal layout with the new widget order', async () => {
    const { store } = renderDashboard(createStore());

    act(() => mockSortables.number.onSortStart());
    expect(document.querySelector('.archive-div')).not.toHaveClass('hide-archive');

    act(() => mockSortables.graph.onSortEnd(
      { oldIndex: 0, newIndex: 1 },
      { target: {}, clientY: 0 },
    ));
    expect(actionOf(store, 'reorderIndicators').args).toEqual([
      { oldIndex: 0, newIndex: 1 }, { target: {}, clientY: 0 }, 'graph',
    ]);
    expect(document.querySelector('.archive-div')).toHaveClass('hide-archive');
    expect(screen.getByText('The dashboard layout has been edited')).toBeInTheDocument();

    fireEvent.click(screen.getByText('Save configuration'));
    expect(apiClient.post).toHaveBeenCalledWith('/api/dashboard/config', {
      ...CONFIG.dashboard,
      personal: { ...CONFIG.dashboard.personal, widgets: [{ widgetId: 'g1', order: 1 }, { widgetId: 'n1', order: 1 }] },
    });
    await waitFor(() => expect(types(store)).toContain('fetchConfig'));
    expect(actionOf(store, 'fetchConfig').args).toEqual(['mainDashboard']);
    expect(screen.queryByText('The dashboard layout has been edited')).not.toBeInTheDocument();
  });

  it('archives a card dropped at the bottom of the screen, even without moving it', () => {
    const { store } = renderDashboard(createStore());
    const event = { target: {}, clientY: window.innerHeight };
    act(() => mockSortables.number.onSortEnd({ oldIndex: 0, newIndex: 0 }, event));
    expect(event.target.id).toBe('archive');
    expect(actionOf(store, 'reorderIndicators').args[2]).toBe('number');
    expect(screen.getByText('The dashboard layout has been edited')).toBeInTheDocument();
  });

  it('does not mark a shared dashboard as edited, and offers no archive there', () => {
    const { store, container } = renderDashboard(createStore({ activeConfig: 'warehouse' }));
    act(() => mockSortables.graph.onSortEnd(
      { oldIndex: 0, newIndex: 1 },
      { target: {}, clientY: 0 },
    ));
    expect(types(store)).toContain('reorderIndicators');
    expect(screen.queryByText('The dashboard layout has been edited')).not.toBeInTheDocument();
    expect(container.querySelector('.unarchived-items')).not.toBeInTheDocument();
    expect(container.querySelector('.drag-handler')).not.toBeInTheDocument();
  });

  it('unarchives a widget at the end of its kind and marks the layout as edited', () => {
    const { store, container } = renderDashboard(createStore());
    const popout = container.querySelector('.unarchived-items');
    expect(screen.getByText(/Archived Indicators/)).toHaveTextContent('Archived Indicators (1)');

    fireEvent.click(container.querySelector('.unarchive'));
    expect(popout).toHaveClass('popover-active');
    fireEvent.click(container.querySelector('.close-button'));
    expect(popout).not.toHaveClass('popover-active');

    fireEvent.click(screen.getByText('Unarchive'));
    expect(actionOf(store, 'addToIndicators').args).toEqual([
      { ...CONFIG.dashboardWidgets.n2, widgetId: 'n2', order: 2 }, 'loc1', 'u1',
    ]);
    expect(screen.getByText('The dashboard layout has been edited')).toBeInTheDocument();
    expect(popout).toHaveClass('popover-active');
  });

  it('keeps the archive closed when every widget is on the dashboard', () => {
    const config = {
      ...CONFIG,
      dashboardWidgets: { g1: CONFIG.dashboardWidgets.g1, n1: CONFIG.dashboardWidgets.n1 },
    };
    const { container } = renderDashboard(createStore({ config }));
    fireEvent.click(container.querySelector('.unarchive'));
    expect(container.querySelector('.unarchived-items')).not.toHaveClass('popover-active');
  });

  it('refetches when the current location changes', async () => {
    const view = renderDashboard(createStore());
    await waitFor(() => expect(types(view.store)).toContain('fetchIndicators'));

    const moved = createStore({ locationId: 'loc2' });
    view.rerender(
      <Provider store={moved}>
        <Dashboard match={{ params: { configId: 'index' } }} />
      </Provider>,
    );
    await waitFor(() => expect(types(moved)).toContain('fetchIndicators'));
    expect(actionOf(moved, 'fetchIndicators').args[2]).toBe('loc2');
  });
});
