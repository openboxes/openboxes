// OBAF-modified: dashboard links open in the same tab by default (linkTarget '_self'), configurable
// per dashboard config (config.linkTarget) and per widget (value.linkTarget); only '_blank' links
// get rel="noopener noreferrer". Covers Dashboard, GraphCard, NumberCard and TableCard.
import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { initialize, localizeReducer } from 'react-localize-redux';
import { Provider } from 'react-redux';
import configureStore from 'redux-mock-store';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';

import '@testing-library/jest-dom';

// The bundle provides lodash as the global "_"; the cards rely on it.
global._ = jest.requireActual('lodash');

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(() => Promise.resolve({ data: { data: [] } })),
    post: jest.fn(() => Promise.resolve({})),
    put: jest.fn(),
    delete: jest.fn(),
  },
}));
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
jest.mock('react-sortable-hoc', () => ({
  SortableContainer: (Component) => Component,
  SortableElement: (Component) => Component,
  sortableHandle: (Component) => Component,
}));

/* eslint-disable import/first,import/order */
import Dashboard from 'components/dashboard/Dashboard';
import NumberCard from 'components/dashboard/NumberCard';
import TableCard from 'components/dashboard/TableCard';
/* eslint-enable import/first,import/order */

installNavigationSpy();

const localize = localizeReducer(undefined, initialize({
  languages: [{ name: 'English', code: 'en' }],
  options: { renderToStaticMarkup: false },
}));

const CONFIG = {
  dashboard: {
    personal: {
      name: 'My dashboard',
      widgets: [{ widgetId: 'g1', order: 1 }, { widgetId: 'n1', order: 1 }, { widgetId: 'n2', order: 2 }],
    },
  },
  dashboardWidgets: {
    g1: {
      title: 'Graph one', type: 'graph', graphType: 'table', enabled: true,
    },
    n1: { title: 'Number one', type: 'number', enabled: true },
    n2: { title: 'Number two', type: 'number', enabled: true },
  },
};

const GRAPHS = [{
  id: 1,
  widgetId: 'g1',
  title: 'Graph one',
  type: 'table',
  link: '/graph/link',
  data: {
    number: 'Code',
    name: 'Product',
    value: 'Qty',
    body: [{
      number: 'P-1', numberLink: '/n/1', name: 'Gauze', link: '/row/1', value: '3',
    }],
  },
}];
const NUMBERS = [
  {
    id: 2, widgetId: 'n1', title: 'Number one', numberType: 'number', number: 7, link: '/number/one',
  },
  {
    id: 3, widgetId: 'n2', title: 'Number two', numberType: 'number', number: 8, link: '/number/two', linkTarget: '_blank',
  },
];

const renderDashboard = (config = CONFIG) => render(
  <Provider
    store={configureStore([])({
      localize,
      indicators: {
        data: GRAPHS, numberData: NUMBERS, config, activeConfig: 'personal',
      },
      session: { currentLocation: { id: 'loc1' }, user: { id: 'u1' }, currencyCode: 'USD' },
    })}
  >
    <Dashboard match={{ params: { configId: 'index' } }} />
  </Provider>,
);

const anchor = (text) => screen.getByText(text).closest('a');

describe('dashboard link target (OBAF)', () => {
  beforeEach(() => sessionStorage.clear());
  afterEach(() => jest.clearAllMocks());

  it('opens graph, number and table links in the same tab by default', () => {
    renderDashboard();

    const graphTitle = anchor('Graph one');
    expect(graphTitle).toHaveAttribute('href', '/graph/link');
    expect(graphTitle).toHaveAttribute('target', '_self');
    expect(graphTitle).not.toHaveAttribute('rel');

    const numberOne = anchor('Number one');
    expect(numberOne).toHaveAttribute('href', '/number/one');
    expect(numberOne).toHaveAttribute('target', '_self');
    expect(numberOne).not.toHaveAttribute('rel');

    expect(anchor('P-1')).toHaveAttribute('target', '_self');
    fireEvent.click(screen.getByText('Gauze').closest('tr'));
    expect(followLink).toHaveBeenCalledWith('/row/1');
  });

  it('lets a widget ask for a new tab, with noopener', () => {
    renderDashboard();

    const numberTwo = anchor('Number two');
    expect(numberTwo).toHaveAttribute('target', '_blank');
    expect(numberTwo).toHaveAttribute('rel', 'noopener noreferrer');
  });

  it('follows the dashboard config linkTarget', () => {
    renderDashboard({ ...CONFIG, linkTarget: '_blank' });

    expect(anchor('Graph one')).toHaveAttribute('target', '_blank');
    expect(anchor('Graph one')).toHaveAttribute('rel', 'noopener noreferrer');
    expect(anchor('Number one')).toHaveAttribute('target', '_blank');
    expect(anchor('P-1')).toHaveAttribute('target', '_blank');
    fireEvent.click(screen.getByText('Gauze').closest('tr'));
    expect(followLink).toHaveBeenCalledWith('/row/1', { newTab: true });
  });
});

describe('TableCard and NumberCard link target (OBAF)', () => {
  it('TableCard puts the target on the name links of an item', () => {
    render(
      <TableCard
        linkTarget="_top"
        data={{
          number: 'Code',
          name: 'Product',
          value: 'Qty',
          body: [{ number: 'P-2', nameDataList: ['A'], nameLinksList: ['/a'] }],
        }}
      />,
    );
    expect(anchor('A')).toHaveAttribute('target', '_top');
    expect(anchor('P-2')).toHaveAttribute('target', '_top');
  });

  it('NumberCard without a link renders no anchor', () => {
    const { container } = render(
      <Provider store={configureStore([])({ localize, session: { currencyCode: 'USD' } })}>
        <NumberCard
          index={0}
          cardTitle="Plain"
          cardNumber={1}
          cardNumberType="number"
          hideDraghandle
          linkTarget="_blank"
        />
      </Provider>,
    );
    expect(container.querySelector('a.number-card')).toBeNull();
    expect(container.querySelector('div.number-card')).toBeInTheDocument();
  });
});
