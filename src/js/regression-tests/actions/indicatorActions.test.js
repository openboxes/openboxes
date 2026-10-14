import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  addToIndicators,
  fetchConfig,
  fetchConfigAndData,
  fetchIndicators,
  reloadIndicator,
} from 'actions';
import {
  FETCH_CONFIG,
  FETCH_CONFIG_AND_SET_ACTIVE,
  FETCH_GRAPHS,
  FETCH_NUMBERS,
  SET_ACTIVE_CONFIG,
} from 'actions/types';
import apiClient from 'utils/apiClient';

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
  parseResponse: jest.fn(),
}));

const createStore = () => configureStore([thunk])({});

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const graphConfig = {
  type: 'graph',
  order: 2,
  widgetId: 'expiring',
  endpoint: '/api/dashboard/expiring',
  title: 'Expiring stock',
  info: 'Items that expire soon',
  graphType: 'bar',
  timeFilter: true,
  yearTypeFilter: false,
  locationFilter: false,
  timeLimit: 12,
  legend: true,
  doubleAxeY: false,
  stacked: true,
  datalabel: false,
  colors: { palette: 'dark' },
  columnsSize: 4,
  truncationLength: 20,
  disableTruncation: false,
  size: 'big',
};

const numberConfig = {
  type: 'number',
  order: 1,
  widgetId: 'inProgress',
  endpoint: '/api/dashboard/inProgress',
  title: 'In progress',
  info: 'Shipments in progress',
  subtitle: 'shipments',
  numberType: 'number',
};

const loadingGraph = (config) => ({
  type: FETCH_GRAPHS,
  payload: {
    id: config.order,
    widgetId: config.widgetId,
    title: 'Loading...',
    info: 'Loading...',
    type: 'loading',
    data: [],
  },
});

describe('dashboard indicator actions', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    window.sessionStorage.clear();
  });

  describe('addToIndicators', () => {
    it('shows a graph as loading, then with the indicator data and its display settings', async () => {
      apiClient.get.mockResolvedValue({ data: { data: { datasets: [] }, link: '/stock/expiring' } });
      const store = createStore();

      store.dispatch(addToIndicators(graphConfig, 'loc1'));
      expect(store.getActions()).toEqual([loadingGraph(graphConfig)]);
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/expiring?locationId=loc1');
      expect(store.getActions()[1]).toEqual({
        type: FETCH_GRAPHS,
        payload: {
          id: 2,
          widgetId: 'expiring',
          title: 'Expiring stock',
          info: 'Items that expire soon',
          type: 'bar',
          data: { datasets: [] },
          timeFilter: true,
          yearTypeFilter: false,
          locationFilter: false,
          timeLimit: 12,
          link: '/stock/expiring',
          legend: true,
          doubleAxeY: false,
          config: {
            stacked: true,
            datalabel: false,
            colors: { palette: 'dark' },
            columnsSize: 4,
            truncationLength: 20,
            disableTruncation: false,
          },
          size: 'big',
        },
      });
    });

    it('shows an error card when the graph cannot be loaded', async () => {
      apiClient.get.mockRejectedValue(new Error('500'));
      const store = createStore();

      store.dispatch(addToIndicators(graphConfig, 'loc1'));
      await flushPromises();

      expect(store.getActions()).toEqual([
        loadingGraph(graphConfig),
        {
          type: FETCH_GRAPHS,
          payload: {
            id: 2,
            widgetId: 'expiring',
            title: 'Indicator could not be loaded',
            type: 'error',
            data: [],
          },
        },
      ]);
    });

    it('loads a number for the user and merges its value with the display settings', async () => {
      apiClient.get.mockResolvedValue({ data: { number: 12, link: '/shipments' } });
      const store = createStore();

      store.dispatch(addToIndicators(numberConfig, 'loc1', 'u1'));
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/inProgress?locationId=loc1&userId=u1');
      expect(store.getActions()).toEqual([{
        type: FETCH_NUMBERS,
        payload: {
          number: 12,
          link: '/shipments',
          id: 1,
          widgetId: 'inProgress',
          title: 'In progress',
          info: 'Shipments in progress',
          subtitle: 'shipments',
          numberType: 'number',
        },
      }]);
    });

    it('ignores widgets of any other type', () => {
      const store = createStore();

      store.dispatch(addToIndicators({ ...graphConfig, type: 'table' }, 'loc1'));

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(store.getActions()).toEqual([]);
    });
  });

  it('adds the filters saved for the active dashboard to the indicator url', async () => {
    window.sessionStorage.setItem('dashboardKey', 'warehouse');
    window.sessionStorage.setItem('pageConfig', JSON.stringify({
      warehouse: { category: [{ id: 'c1' }, { id: 'c2' }], supplier: [{ id: 's1' }] },
      personal: { ignored: [{ id: 'x' }] },
    }));
    apiClient.get.mockResolvedValue({ data: {} });

    createStore().dispatch(addToIndicators(numberConfig, 'loc1'));
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/dashboard/inProgress?locationId=loc1'
      + '&listFiltersSelected=category&listFiltersSelected=supplier&value=c1&value=c2&value=s1',
    );
  });

  it('reloadIndicator sends the extra params before the location and does not change the given config', async () => {
    apiClient.get.mockResolvedValue({ data: { data: [] } });
    const config = { ...graphConfig, colors: { palette: 'dark' } };
    const store = createStore();

    store.dispatch(reloadIndicator(config, 'timeFilter=6', 'loc1'));
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/expiring?timeFilter=6&locationId=loc1');
    expect(config).toEqual({ ...graphConfig, colors: { palette: 'dark' } });
    // The dispatched payload carries a copy, not the caller's objects
    expect(store.getActions()[1].payload.config.colors).not.toBe(config.colors);
  });

  const dashboardConfig = {
    dashboard: {
      personal: {
        widgets: [
          { widgetId: 'expiring', order: 3 },
          { widgetId: 'inProgress', order: 1 },
          { widgetId: 'disabled', order: 2 },
        ],
      },
    },
    dashboardWidgets: {
      expiring: { ...graphConfig, enabled: true },
      inProgress: { ...numberConfig, enabled: true },
      disabled: { ...numberConfig, endpoint: '/api/dashboard/disabled', enabled: false },
    },
  };

  describe('fetchIndicators', () => {
    it('activates the dashboard and loads its enabled widgets in order', async () => {
      apiClient.get.mockResolvedValue({ data: { data: [] } });
      const store = createStore();

      store.dispatch(fetchIndicators(dashboardConfig, 'personal', 'loc1', 'u1'));
      await flushPromises();

      expect(store.getActions()[0]).toEqual({ type: SET_ACTIVE_CONFIG, payload: { data: 'personal' } });
      expect(apiClient.get.mock.calls.map(([url]) => url)).toEqual([
        '/api/dashboard/inProgress?locationId=loc1&userId=u1',
        '/api/dashboard/expiring?locationId=loc1',
      ]);
      // The widget order from the dashboard wins over the order in the widget definition
      expect(store.getActions()[1]).toEqual(loadingGraph({ ...graphConfig, order: 3 }));
    });

    it('loads nothing for a dashboard that is not configured', () => {
      const store = createStore();

      store.dispatch(fetchIndicators(dashboardConfig, 'unknown', 'loc1', 'u1'));

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(store.getActions()).toEqual([{ type: SET_ACTIVE_CONFIG, payload: { data: 'unknown' } }]);
    });
  });

  it('fetchConfigAndData stores the configuration with the active dashboard, then loads its widgets', async () => {
    apiClient.get.mockImplementation((url) => Promise.resolve({
      data: url === '/api/dashboard/main/config' ? dashboardConfig : { data: [] },
    }));
    const store = createStore();

    store.dispatch(fetchConfigAndData('loc1', undefined, 'u1', 'main'));
    await flushPromises();

    expect(apiClient.get.mock.calls[0]).toEqual(['/api/dashboard/main/config']);
    expect(store.getActions()[0]).toEqual({
      type: FETCH_CONFIG_AND_SET_ACTIVE,
      payload: { data: dashboardConfig, activeConfig: 'personal' },
    });
    expect(apiClient.get.mock.calls.slice(1).map(([url]) => url)).toEqual([
      '/api/dashboard/inProgress?locationId=loc1&userId=u1',
      '/api/dashboard/expiring?locationId=loc1',
    ]);
  });

  it('fetchConfig stores the dashboard configuration', async () => {
    apiClient.get.mockResolvedValue({ data: dashboardConfig });
    const store = createStore();

    store.dispatch(fetchConfig('main'));
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/main/config');
    expect(store.getActions())
      .toEqual([{ type: FETCH_CONFIG, payload: { data: dashboardConfig } }]);
  });
});
