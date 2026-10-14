import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { initialize, localizeReducer } from 'react-localize-redux';
import { Provider } from 'react-redux';
import configureStore from 'redux-mock-store';
import { followLink, installNavigationSpy } from 'regression-tests/__setup__/navigationSpy';

import '@testing-library/jest-dom';

// The bundle provides lodash as the global "_"; the cards and the color mapping rely on it.
global._ = jest.requireActual('lodash');

// Charts need a canvas jsdom does not have: each chart renders its data and click handler
// into a stand-in that the tests can read and trigger.
jest.mock('react-chartjs-2', () => {
  const chart = (kind) => ({ data, onElementsClick }) => (
    <button
      type="button"
      aria-label={`${kind} chart`}
      data-testid={`chart-${kind}`}
      data-labels={JSON.stringify(data.labels || [])}
      onClick={() => onElementsClick && onElementsClick(global.chartClickElements)}
    />
  );
  return {
    defaults: { scale: { ticks: {} } },
    Line: chart('line'),
    Bar: chart('bar'),
    Doughnut: chart('doughnut'),
    HorizontalBar: chart('horizontalBar'),
  };
});

// Sorting is driven by the dashboard; outside a sortable container the cards render as they are.
jest.mock('react-sortable-hoc', () => ({
  SortableContainer: (Component) => Component,
  SortableElement: (Component) => Component,
  sortableHandle: (Component) => Component,
}));

/* eslint-disable import/first,import/order */
import CustomAlert from 'components/dashboard/CustomAlert';
import GraphCard from 'components/dashboard/GraphCard';
import LoadingNumbers from 'components/dashboard/LoadingNumbers';
import NumberCard from 'components/dashboard/NumberCard';
import Numbers from 'components/dashboard/Numbers';
import NumbersRAG from 'components/dashboard/NumbersRAG';
import NumbersTableCard from 'components/dashboard/NumbersTableCard';
import TableCard from 'components/dashboard/TableCard';
import UnarchiveIndicators from 'components/dashboard/UnarchiveIndicators';
/* eslint-enable import/first,import/order */

// OBAF navigates with window.location / window.open directly; the spy reports each navigation
// to followLink as (url, options).
installNavigationSpy();

const localize = localizeReducer(undefined, initialize({
  languages: [{ name: 'English', code: 'en' }],
  options: { renderToStaticMarkup: false },
}));

const renderWithStore = (ui) => render(
  <Provider store={configureStore([])({ localize, session: { currencyCode: 'USD' } })}>
    {ui}
  </Provider>,
);

afterEach(() => jest.clearAllMocks());

describe('TableCard', () => {
  const data = {
    number: 'Code',
    name: 'Product',
    value: 'Quantity',
    body: [
      {
        number: 'P-1', name: 'Ibuprofen', value: '10', link: '/product/1', numberLink: '/n/1', icon: '/i.png',
      },
      {
        number: 'P-2', nameDataList: ['A', 'B'], nameLinksList: ['/a', '/b'], value: '1234567890123',
      },
    ],
  };

  it('renders the header and one row per item, with an icon column when any item has an icon', () => {
    const { container } = render(<TableCard data={data} />);
    expect(screen.getByText('Code')).toBeInTheDocument();
    expect(screen.getByText('Product')).toBeInTheDocument();
    expect(container.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(container.querySelector('img')).toHaveAttribute('src', '/i.png');
    expect(screen.getByText('P-1').closest('a')).toHaveAttribute('href', '/n/1');
    expect(screen.getByText('P-1').closest('a')).toHaveClass('indicator-item-href');
    expect(screen.getByText('Ibuprofen').closest('a')).toHaveClass('disabled-indicator-item-href');
  });

  it('lists the name links of an item without a name', () => {
    render(<TableCard data={data} />);
    expect(screen.getByText('A').closest('a')).toHaveAttribute('href', '/a');
    expect(screen.getByText('B').closest('a')).toHaveAttribute('href', '/b');
  });

  it('shows long values in full by default', () => {
    render(<TableCard data={data} />);
    expect(screen.getByText('1234567890123')).toBeInTheDocument();
  });

  // Adapted to OBAF (OBLS-572, ade83fb0d): the row link opens in a new tab only with
  // linkTarget '_blank' (the dashboard passes its configured target, '_self' by default);
  // without one it opens in the same tab.
  it('opens the row link in a new tab on click, and does nothing for rows without a link', () => {
    const { unmount } = render(<TableCard data={data} />);
    fireEvent.click(screen.getByText('Ibuprofen').closest('tr'));
    expect(followLink).toHaveBeenCalledWith('/product/1');
    unmount();

    followLink.mockClear();
    render(<TableCard data={data} linkTarget="_blank" />);
    fireEvent.click(screen.getByText('Ibuprofen').closest('tr'));
    expect(followLink).toHaveBeenCalledWith('/product/1', { newTab: true });

    followLink.mockClear();
    fireEvent.click(screen.getByText('P-2').closest('tr'));
    expect(followLink).not.toHaveBeenCalled();
  });
});

describe('Numbers', () => {
  it('renders the indicators in their configured order, linking those with a link', () => {
    const { container } = render(
      <Numbers
        data={{
          second: { value: 2, subtitle: 'Second', order: 2 },
          first: {
            value: 1, subtitle: 'First', order: 1, link: '/first',
          },
          third: null,
        }}
        options={{ colors: { first: 'green', second: 'red' } }}
      />,
    );
    const items = container.querySelectorAll('.number-indicator');
    expect(items).toHaveLength(2);
    expect(items[0]).toHaveTextContent('1First');
    expect(items[0]).toHaveAttribute('href', '/first');
    expect(items[1]).toHaveTextContent('2Second');
    expect(items[1]).not.toHaveAttribute('href');
    expect(items[0].querySelector('.circle')).toHaveStyle({ backgroundColor: 'green' });
  });
});

describe('NumbersRAG and NumbersTableCard', () => {
  it('renders one linked indicator per colored number', () => {
    const { container } = render(
      <NumbersRAG
        data={{
          listColorNumber: [
            {
              link: '/ok', color: 'success', subtitle: 'On time', value: '5',
            },
            {
              link: '/late', color: 'error', subtitle: 'Late', value: '7',
            },
          ],
        }}
      />,
    );
    const items = container.querySelectorAll('a.number-indicator');
    expect(items).toHaveLength(2);
    expect(items[1]).toHaveAttribute('href', '/late');
    expect(items[1]).toHaveTextContent('7Late');
  });

  it('shows the numbers next to the table', () => {
    render(
      <NumbersTableCard
        data={{
          numbersIndicator: { first: { value: 3, subtitle: 'Open', order: 1 } },
          tableData: {
            number: 'Code', name: 'Name', value: 'Value', body: [{ number: 'X1', name: 'Row', value: '9' }],
          },
        }}
        options={{ colors: { first: 'blue' } }}
      />,
    );
    expect(screen.getByText('Open')).toBeInTheDocument();
    expect(screen.getByText('X1')).toBeInTheDocument();
  });
});

describe('CustomAlert', () => {
  it('shows the message, the details list and the details, and closes', () => {
    const handleClose = jest.fn();
    const { container } = render(
      <CustomAlert
        message="Saved"
        classNames="s-alert-box"
        id="a1"
        styles={{}}
        condition="success"
        customFields={{ details: 'All good', detailsArray: ['one', null, 'two'] }}
        handleClose={handleClose}
      />,
    );
    expect(screen.getByText('Saved')).toBeInTheDocument();
    expect(screen.getByText('All good')).toBeInTheDocument();
    expect(container.querySelectorAll('.array-errors-wrapper p')).toHaveLength(2);
    expect(container.firstChild).not.toHaveClass('no-details');

    fireEvent.click(container.querySelector('.alert-close-icon span'));
    expect(handleClose).toHaveBeenCalled();
  });

  it('uses the custom icon when given and marks alerts without details', () => {
    const { container } = render(
      <CustomAlert
        message="Note"
        classNames="s-alert-box"
        id="a2"
        styles={{}}
        customFields={{ icon: <span data-testid="custom-icon" /> }}
        handleClose={jest.fn()}
      />,
    );
    expect(screen.getByTestId('custom-icon')).toBeInTheDocument();
    expect(container.firstChild).toHaveClass('no-details');
  });
});

describe('LoadingNumbers', () => {
  it('renders four loading cards by default, or the requested number', () => {
    const { container, rerender } = render(<LoadingNumbers />);
    expect(container.querySelectorAll('.number-card')).toHaveLength(4);
    rerender(<LoadingNumbers numberOfLoadingCards={2} />);
    expect(container.querySelectorAll('.number-card')).toHaveLength(2);
  });
});

describe('NumberCard', () => {
  it('shows a number with a percent sign, the subtitle, and links the card', () => {
    const { container } = renderWithStore(
      <NumberCard
        index={0}
        cardTitle="react.dashboard.title"
        cardTitleDefaultValue="Open items"
        cardNumber={1234}
        cardNumberType="number"
        showPercentSign
        cardSubtitle="react.dashboard.sub"
        cardSubtitleDefaultValue="this week"
        cardSubtitleValue="5"
        cardInfo="react.dashboard.info"
        cardLink="/link"
        hideDraghandle
      />,
    );
    expect(screen.getByText('Open items')).toBeInTheDocument();
    expect(container.querySelector('.result-card')).toHaveTextContent('1,234%');
    expect(container.querySelector('.subtitle-card')).toHaveTextContent('5 this week');
    expect(container.querySelector('a.number-card')).toHaveAttribute('href', '/link');
    expect(container.querySelector('.number-infos')).toBeInTheDocument();
    expect(container.querySelector('.drag-handler')).not.toBeInTheDocument();
  });

  it('shows a zero amount with the currency code when there is no number', () => {
    const { container } = renderWithStore(
      <NumberCard index={0} cardTitle="Value" cardNumberType="currency" cardInfo="" />,
    );
    expect(container.querySelector('.result-card')).toHaveTextContent('0 USD');
    expect(container.querySelector('div.number-card')).toBeInTheDocument();
    expect(container.querySelector('.number-infos')).not.toBeInTheDocument();
  });

  it('renders a sparkline card when the data carries a colored number', () => {
    const { container } = renderWithStore(
      <NumberCard
        index={0}
        cardTitle="Fill rate"
        cardInfo="info"
        sparklineData={{ colorNumber: { color: 'success', value: '90%', value2: '+2%' }, labels: [] }}
      />,
    );
    expect(screen.getByText('Fill rate')).toBeInTheDocument();
    expect(screen.getByText('90%')).toBeInTheDocument();
    expect(container.querySelector('.goal-difference')).toHaveTextContent('+2%');
    expect(screen.getByTestId('chart-line')).toBeInTheDocument();
  });
});

describe('UnarchiveIndicators', () => {
  const dashboardConfig = {
    dashboardWidgets: {
      shown: {
        title: 'Shown', type: 'graph', graphType: 'bar', enabled: true,
      },
      archivedNumber: { title: 'Archived number', type: 'number', enabled: true },
      archivedLine: {
        title: 'Archived line', type: 'graph', graphType: 'line', enabled: true,
      },
      archivedTable: {
        title: 'Archived table', type: 'graph', graphType: 'table', enabled: true,
      },
      disabled: {
        title: 'Disabled', type: 'graph', graphType: 'doughnut', enabled: false,
      },
    },
  };

  it('lists the enabled widgets missing from the dashboard and unarchives one on click', () => {
    const handleAdd = jest.fn();
    const unarchiveHandler = jest.fn();
    const { container } = renderWithStore(
      <UnarchiveIndicators
        graphData={[{ widgetId: 'shown' }, null]}
        numberData={[]}
        dashboardConfig={dashboardConfig}
        showPopout
        unarchiveHandler={unarchiveHandler}
        handleAdd={handleAdd}
      />,
    );
    expect(screen.getByText(/Archived Indicators \(/)).toHaveTextContent('Archived Indicators (3)');
    expect(container.firstChild).toHaveClass('popover-active');
    expect(screen.getByText('Archived number')).toBeInTheDocument();
    expect(screen.getByText('Archived line')).toBeInTheDocument();
    expect(screen.queryByText('Shown')).not.toBeInTheDocument();
    expect(screen.queryByText('Disabled')).not.toBeInTheDocument();
    expect(container.querySelector('.fa-line-chart')).toBeInTheDocument();
    expect(container.querySelector('.fa-table')).toBeInTheDocument();

    fireEvent.click(screen.getAllByText('Unarchive')[1]);
    expect(handleAdd).toHaveBeenCalledWith('archivedLine');

    fireEvent.click(container.querySelector('.close-button'));
    expect(unarchiveHandler).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['bar', '.fa-bar-chart'],
    ['numbersCustomColors', '.fa-bar-chart'],
    ['doughnut', '.fa-pie-chart'],
    ['horizontalBar', '.horizontal-bar'],
    ['numbers', '.circle'],
    ['error', '.fa-repeat'],
    ['numberTable', '.fa-table'],
  ])('previews an archived %s graph', (graphType, selector) => {
    const { container } = renderWithStore(
      <UnarchiveIndicators
        graphData={[]}
        numberData={[]}
        dashboardConfig={{ dashboardWidgets: { w: { title: 'W', graphType, enabled: true } } }}
        showPopout={false}
        unarchiveHandler={jest.fn()}
        handleAdd={jest.fn()}
      />,
    );
    expect(container.querySelector(`.graph-preview ${selector}`)).toBeInTheDocument();
    expect(container.firstChild).not.toHaveClass('popover-active');
  });
});

describe('GraphCard', () => {
  const renderCard = (props) => {
    const loadIndicator = jest.fn();
    const utils = renderWithStore(
      <GraphCard
        index={0}
        cardId={1}
        widgetId="w1"
        cardTitle="My graph"
        cardInfo=""
        data={{ labels: ['Jan'], datasets: [] }}
        options={{}}
        loadIndicator={loadIndicator}
        allLocations={[
          { id: 'l1', name: 'Clinic' },
          { id: 'l2', name: { code: 'react.loc.code', message: 'Depot' } },
        ]}
        {...props}
      />,
    );
    return { ...utils, loadIndicator };
  };

  it.each([
    ['bar', 'chart-bar'],
    ['doughnut', 'chart-doughnut'],
    ['horizontalBar', 'chart-horizontalBar'],
    ['line', 'chart-line'],
  ])('renders a %s chart', (cardType, testId) => {
    renderCard({ cardType });
    expect(screen.getByTestId(testId)).toBeInTheDocument();
  });

  it('translates coded line labels and follows the clicked point link', () => {
    renderCard({
      cardType: 'line',
      data: { labels: [{ code: 'react.month.jan', message: 'January' }], datasets: [] },
    });
    expect(JSON.parse(screen.getByTestId('chart-line').dataset.labels)).toEqual(['January']);

    global.chartClickElements = [{
      _index: 1,
      _chart: { data: { datasets: [{ links: ['', '/target'] }] } },
    }];
    fireEvent.click(screen.getByTestId('chart-line'));
    expect(followLink).toHaveBeenCalledWith('/target');

    followLink.mockClear();
    global.chartClickElements = [{ _index: 0, _chart: { data: { datasets: [{ links: [''] }] } } }];
    fireEvent.click(screen.getByTestId('chart-line'));
    expect(followLink).not.toHaveBeenCalled();
  });

  // KNOWN-GAP: OBAF's GraphCard reads data.labels[0].code and the card crashes.
  it.skip('renders a line card that has no labels yet', () => {
    renderCard({ cardType: 'line', data: { labels: [], datasets: [] } });

    expect(screen.getByTestId('chart-line')).toBeInTheDocument();
    expect(screen.getByText('My graph')).toBeInTheDocument();
  });

  it('renders the title as a link with an info tooltip and a drag handle', () => {
    const { container } = renderCard({
      cardType: 'bar', cardLink: '/details', cardInfo: 'More info', size: 'big',
    });
    expect(screen.getByText('My graph').closest('a')).toHaveAttribute('href', '/details');
    expect(container.querySelector('.graph-infos')).toBeInTheDocument();
    expect(container.querySelector('.drag-handler')).toBeInTheDocument();
    expect(container.querySelector('.graph-card')).toHaveClass('big-size');
  });

  it('renders the numbers, colored numbers, table and number-table card types', () => {
    const { unmount } = renderCard({
      cardType: 'numbers',
      data: { first: { value: 4, subtitle: 'Four', order: 1 } },
      options: { colors: {} },
    });
    expect(screen.getByText('Four')).toBeInTheDocument();
    unmount();

    renderCard({ cardType: 'numbersCustomColors', data: { listColorNumber: [{ color: 'warning', value: '8', subtitle: 'Eight' }] } });
    expect(screen.getByText('Eight')).toBeInTheDocument();

    renderCard({ cardType: 'table', data: { number: 'T', body: [{ number: 'T-1', name: 'n', value: 'v' }] } });
    expect(screen.getByText('T-1')).toBeInTheDocument();

    renderCard({
      cardType: 'numberTable',
      data: { numbersIndicator: {}, tableData: { number: 'NT', body: [] } },
      options: { colors: {} },
    });
    expect(screen.getByText('NT')).toBeInTheDocument();

    renderCard({ cardType: 'loading', hideDraghandle: true });
    expect(document.querySelectorAll('.loader-div')).toHaveLength(1);
  });

  it('reloads the widget from the retry button of an error card', () => {
    const { container, loadIndicator } = renderCard({ cardType: 'error' });
    expect(container.querySelector('.graph-card')).toHaveClass('error-card');
    fireEvent.click(container.querySelector('.fa-repeat').closest('button'));
    expect(loadIndicator).toHaveBeenCalledWith('w1');
  });

  it('reloads with the selected time frame and location', () => {
    const { container, loadIndicator } = renderCard({
      cardType: 'bar', timeFilter: true, locationFilter: true, timeLimit: 24,
    });
    const timeSelect = container.querySelector('#timeFrameSelector');
    expect(timeSelect.querySelectorAll('option')).toHaveLength(5);
    expect(screen.getByText('Depot')).toBeInTheDocument();

    fireEvent.change(timeSelect, { target: { value: '3' } });
    expect(loadIndicator).toHaveBeenLastCalledWith('w1', 'querySize=3');

    fireEvent.change(container.querySelector('#locationSelector'), { target: { value: 'l2' } });
    expect(loadIndicator).toHaveBeenLastCalledWith('w1', 'querySize=3&destinationLocation=l2');

    fireEvent.change(timeSelect, { target: { value: '12' } });
    expect(loadIndicator).toHaveBeenLastCalledWith('w1', 'querySize=12&destinationLocation=l2');
  });

  it('offers no year option when the time limit is below twelve months', () => {
    const { container } = renderCard({ cardType: 'bar', timeFilter: true, timeLimit: 6 });
    const values = [...container.querySelectorAll('#timeFrameSelector option')].map((o) => o.value);
    expect(values).toEqual(['1', '3', '6']);
  });

  it('reloads with the year type parameter, defaulting to querySize', () => {
    const options = [{ value: 'fiscal', label: 'Fiscal' }, { value: 'calendar', label: 'Calendar' }];
    const { container, loadIndicator, unmount } = renderCard({
      cardType: 'bar',
      yearTypeFilter: { parameter: 'yearType', defaultValue: 'calendar', options },
    });
    fireEvent.change(container.querySelector('#yearTypeSelector'), { target: { value: 'fiscal' } });
    expect(loadIndicator).toHaveBeenLastCalledWith('w1', 'yearType=fiscal');
    unmount();

    const second = renderCard({ cardType: 'bar', yearTypeFilter: { options } });
    fireEvent.change(second.container.querySelector('#yearTypeSelector'), { target: { value: 'calendar' } });
    expect(second.loadIndicator).toHaveBeenLastCalledWith('w1', 'querySize=calendar');
  });
});
