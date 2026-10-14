import {
  FETCH_CONFIG,
  FETCH_CONFIG_AND_SET_ACTIVE,
  FETCH_GRAPHS,
  FETCH_NUMBERS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
  RESET_INDICATORS,
  SET_ACTIVE_CONFIG,
} from 'actions/types';
import indicatorsReducer from 'reducers/indicatorsReducer';

import deepFreeze from './deepFreeze';

const UNKNOWN = { type: 'UNKNOWN' };

const widget = (id, widgetId, extra = {}) => ({
  id, widgetId, type: 'loading', data: [], ...extra,
});

const stateWith = (overrides = {}) => deepFreeze({
  data: [],
  numberData: [],
  config: {},
  activeConfig: 'personal',
  ...overrides,
});

describe('indicatorsReducer', () => {
  describe('initial state', () => {
    afterEach(() => {
      window.sessionStorage.clear();
    });

    const loadInitialState = () => {
      let state;
      jest.isolateModules(() => {
        // eslint-disable-next-line global-require
        const reducer = require('reducers/indicatorsReducer').default;
        state = reducer(undefined, UNKNOWN);
      });
      return state;
    };

    it('uses the personal dashboard when nothing is stored', () => {
      expect(loadInitialState()).toEqual({
        data: [], numberData: [], config: {}, activeConfig: 'personal',
      });
    });

    it('uses the dashboard key kept in the session storage', () => {
      window.sessionStorage.setItem('dashboardKey', 'warehouse');

      expect(loadInitialState().activeConfig).toBe('warehouse');
    });
  });

  it('returns the same state for an unknown action', () => {
    const previous = stateWith();

    expect(indicatorsReducer(previous, UNKNOWN)).toBe(previous);
  });

  describe(FETCH_GRAPHS, () => {
    it('places a new widget at the position given by its order', () => {
      const previous = stateWith({ data: [widget(1, 'w1')] });
      const payload = widget(3, 'w3');

      const next = indicatorsReducer(previous, { type: FETCH_GRAPHS, payload });

      expect(next.data).toHaveLength(3);
      expect(next.data[0]).toBe(previous.data[0]);
      expect(next.data[1]).toBeUndefined();
      expect(next.data[2]).toBe(payload);
      expect(next.data).not.toBe(previous.data);
    });

    it('replaces a widget that is already shown, wherever it sits', () => {
      // w2 was moved to the first slot by the user; its order (id) still says 2
      const previous = stateWith({ data: [widget(2, 'w2'), widget(1, 'w1')] });
      const loaded = widget(2, 'w2', { type: 'error', title: 'Indicator could not be loaded' });

      const next = indicatorsReducer(previous, { type: FETCH_GRAPHS, payload: loaded });

      expect(next.data).toEqual([loaded, previous.data[1]]);
    });

    it('adds the number options to a numbers widget', () => {
      const payload = widget(1, 'w1', {
        type: 'numbers',
        config: {
          colors: undefined, columnsSize: 4, truncationLength: 10, disableTruncation: true,
        },
      });

      const next = indicatorsReducer(stateWith(), { type: FETCH_GRAPHS, payload });

      expect(next.data[0].options).toMatchObject({
        columnsSize: 4,
        truncationLength: 10,
        disableTruncation: true,
      });
      expect(Object.keys(next.data[0].options.colors)).toEqual(['first', 'second', 'third']);
    });

    it('colors the datasets of a bar graph and builds its chart options', () => {
      const payload = widget(1, 'w1', {
        type: 'bar',
        data: { labels: ['Jan', 'Feb'], datasets: [{ label: 'Stock', data: [1, 2] }] },
        config: {},
        legend: true,
        doubleAxeY: false,
      });

      const next = indicatorsReducer(stateWith(), { type: FETCH_GRAPHS, payload });

      const [dataset] = next.data[0].data.datasets;
      expect(dataset.data).toEqual([1, 2]);
      // The palette comes from a stylesheet that is stubbed in tests, so only the keys are checked
      expect(dataset).toHaveProperty('backgroundColor');
      expect(dataset).toHaveProperty('hoverBackgroundColor');
      expect(next.data[0].options.scales.xAxes[0].display).toBe(true);
    });
  });

  describe(FETCH_NUMBERS, () => {
    it('places a new number by its order and replaces a known one in place', () => {
      const previous = stateWith({ numberData: [widget(2, 'n2', { value: 1 })] });

      const added = indicatorsReducer(previous, {
        type: FETCH_NUMBERS,
        payload: widget(2, 'n9', { value: 5 }),
      });
      expect(added.numberData.map((n) => n.widgetId)).toEqual(['n2', 'n9']);

      const replaced = indicatorsReducer(deepFreeze(added), {
        type: FETCH_NUMBERS,
        payload: widget(2, 'n2', { value: 7 }),
      });
      expect(replaced.numberData[0].value).toBe(7);
      expect(replaced.numberData[1]).toBe(added.numberData[1]);
    });
  });

  it('resets graphs and numbers but keeps the configuration', () => {
    const previous = stateWith({
      data: [widget(1, 'w1')],
      numberData: [widget(1, 'n1')],
      config: { some: 'config' },
      activeConfig: 'warehouse',
    });

    expect(indicatorsReducer(previous, { type: RESET_INDICATORS })).toEqual({
      data: [],
      numberData: [],
      config: { some: 'config' },
      activeConfig: 'warehouse',
    });
  });

  describe(REORDER_INDICATORS, () => {
    const previous = stateWith({
      data: [widget(1, 'a'), widget(2, 'b'), widget(3, 'c')],
      numberData: [widget(1, 'x'), widget(2, 'y')],
    });

    it('moves a graph', () => {
      const next = indicatorsReducer(previous, {
        type: REORDER_INDICATORS,
        payload: { type: 'graph', oldIndex: 0, newIndex: 2 },
      });

      expect(next.data.map((w) => w.widgetId)).toEqual(['b', 'c', 'a']);
      expect(next.numberData).toBe(previous.numberData);
    });

    it('moves a number', () => {
      const next = indicatorsReducer(previous, {
        type: REORDER_INDICATORS,
        payload: { type: 'number', oldIndex: 1, newIndex: 0 },
      });

      expect(next.numberData.map((w) => w.widgetId)).toEqual(['y', 'x']);
      expect(next.data).toBe(previous.data);
    });

    it('ignores any other type', () => {
      expect(indicatorsReducer(previous, {
        type: REORDER_INDICATORS,
        payload: { type: 'table', oldIndex: 1, newIndex: 0 },
      })).toBe(previous);
    });
  });

  describe(REMOVE_FROM_INDICATORS, () => {
    const previous = stateWith({
      data: [widget(1, 'a'), widget(2, 'b'), widget(3, 'c')],
      numberData: [widget(1, 'x'), widget(2, 'y')],
    });

    it('removes a graph and shifts the order of the ones after it', () => {
      const next = indicatorsReducer(previous, {
        type: REMOVE_FROM_INDICATORS,
        payload: { type: 'graph', index: 1 },
      });

      expect(next.data.map((w) => [w.widgetId, w.id])).toEqual([['a', 1], ['c', 2]]);
      expect(next.data[0]).toBe(previous.data[0]);
    });

    it('removes a number', () => {
      const next = indicatorsReducer(previous, {
        type: REMOVE_FROM_INDICATORS,
        payload: { type: 'number', index: 0 },
      });

      expect(next.numberData.map((w) => [w.widgetId, w.id])).toEqual([['y', 1]]);
    });

    it('ignores any other type', () => {
      expect(indicatorsReducer(previous, {
        type: REMOVE_FROM_INDICATORS,
        payload: { type: 'table', index: 0 },
      })).toBe(previous);
    });
  });

  it('stores the dashboard configuration and the active dashboard', () => {
    const config = { dashboard: { personal: {} } };

    const withConfig = indicatorsReducer(stateWith(), {
      type: FETCH_CONFIG,
      payload: { data: config },
    });
    expect(withConfig.config).toBe(config);
    expect(withConfig.activeConfig).toBe('personal');

    const switched = indicatorsReducer(deepFreeze(withConfig), {
      type: SET_ACTIVE_CONFIG,
      payload: { data: 'warehouse' },
    });
    expect(switched.activeConfig).toBe('warehouse');
    expect(switched.config).toBe(config);

    const both = indicatorsReducer(deepFreeze(switched), {
      type: FETCH_CONFIG_AND_SET_ACTIVE,
      payload: { data: { other: true }, activeConfig: 'personal' },
    });
    expect(both.config).toEqual({ other: true });
    expect(both.activeConfig).toBe('personal');
  });
});
