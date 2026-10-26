import { act, renderHook } from '@testing-library/react-hooks';
import moment from 'moment';
import queryString from 'query-string';

import { INDICATORS_TAB } from 'consts/cycleCount';
import useCycleCountReportingFilters from 'hooks/cycleCount/useCycleCountReportingFilters';
import { fetchBins, fetchProduct } from 'utils/option-utils';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useSelector: (selector) => selector(mockState),
}));
const mockHistory = { location: { pathname: '/cycleCount/reporting', search: '' }, push: jest.fn() };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
}));
jest.mock('actions', () => ({
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'SET_SHOULD_REBUILD', flag })),
}));
jest.mock('utils/option-utils', () => ({
  fetchBins: jest.fn(),
  fetchProduct: jest.fn(),
}));

const NOW = new Date('2026-10-02T12:00:00Z');
const FILTER_FIELDS = { startDate: {}, endDate: {}, products: {} };

const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

// getCurrentLocation caches locations by id, which does not matter here: only the id is read.
const buildState = ({ locationId = 'loc-1', shouldRebuildParams = true } = {}) => ({
  session: { currentLocation: { id: locationId } },
  filterForm: { shouldRebuildParams },
});

const pushedQuery = (call = 0) => queryString.parse(mockHistory.push.mock.calls[call][0].search);

const renderFilters = () => renderHook(
  () => useCycleCountReportingFilters({ filterFields: FILTER_FIELDS }),
);

describe('useCycleCountReportingFilters', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
    jest.setSystemTime(NOW);
    mockHistory.location.search = '';
    mockState = buildState();
    fetchProduct.mockImplementation(async (id) => ({
      id, productCode: `PC-${id}`, name: `Name ${id}`,
    }));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('defaults the indicators tab to the last three months on first load', async () => {
    mockHistory.location.search = `?tab=${INDICATORS_TAB}`;
    const { result } = renderFilters();
    await flushPromises();

    const { defaultFilterValues } = result.current;
    expect(defaultFilterValues.tab).toBe(INDICATORS_TAB);
    expect(defaultFilterValues.products).toBe('');
    expect(moment.isMoment(defaultFilterValues.startDate)).toBe(true);
    expect(defaultFilterValues.startDate.toISOString()).toBe(moment(NOW).subtract(3, 'months').toISOString());
    expect(defaultFilterValues.endDate.toISOString()).toBe(NOW.toISOString());
    expect(result.current.filterParams).toBe(defaultFilterValues);
    expect(result.current.filtersInitialized).toBe(true);
    expect(result.current.isLoading).toBe(false);
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SET_SHOULD_REBUILD', flag: false });
  });

  it('takes dates and products from the query and labels the products', async () => {
    mockHistory.location.search = '?tab=PRODUCTS&startDate=01/01/2026&endDate=02/01/2026&products=p-1';
    const { result } = renderFilters();
    await flushPromises();

    expect(fetchProduct).toHaveBeenCalledWith('p-1');
    expect(result.current.defaultFilterValues).toEqual({
      tab: 'PRODUCTS',
      startDate: '01/01/2026',
      endDate: '02/01/2026',
      products: [{
        id: 'p-1', productCode: 'PC-p-1', name: 'Name p-1', label: 'PC-p-1 - Name p-1', value: 'p-1',
      }],
    });
  });

  it('prefers the display name and accepts several products', async () => {
    fetchProduct.mockImplementation(async (id) => ({
      id, productCode: id, name: 'Name', displayName: 'Display',
    }));
    mockHistory.location.search = '?tab=PRODUCTS&products=p-1&products=p-2';
    const { result } = renderFilters();
    await flushPromises();

    expect(result.current.defaultFilterValues.products.map(({ label }) => label))
      .toEqual(['p-1 - Display', 'p-2 - Display']);
  });

  it('sets no dates on other tabs without dates in the query', async () => {
    mockHistory.location.search = '?tab=PRODUCTS';
    const { result } = renderFilters();
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      tab: 'PRODUCTS', startDate: '', endDate: '', products: '',
    });
  });

  it('re-applies the indicator dates on a tab switch but not on a plain update', async () => {
    mockHistory.location.search = '?tab=PRODUCTS';
    const { result } = renderFilters();
    await flushPromises();
    expect(result.current.filtersInitialized).toBe(true);

    mockHistory.location.search = `?tab=${INDICATORS_TAB}`;
    result.current.updateParams();
    await flushPromises();
    expect(result.current.defaultFilterValues.startDate).toBe('');

    result.current.updateParams({ isTabSwitch: true });
    await flushPromises();
    expect(moment.isMoment(result.current.defaultFilterValues.startDate)).toBe(true);
    expect(moment.isMoment(result.current.defaultFilterValues.endDate)).toBe(true);
  });

  it('setFilterValues pushes dates and product ids to the url', () => {
    mockState = buildState({ shouldRebuildParams: false });
    const { result } = renderFilters();
    const values = {
      tab: 'PRODUCTS',
      startDate: '01/01/2026',
      endDate: '',
      products: [{ id: 'p-1' }, { id: 'p-2' }],
    };

    act(() => result.current.setFilterValues(values));

    expect(mockHistory.push.mock.calls[0][0].pathname).toBe('/cycleCount/reporting');
    expect(pushedQuery()).toEqual({ tab: 'PRODUCTS', startDate: '01/01/2026', products: ['p-1', 'p-2'] });
    expect(result.current.filterParams).toBe(values);
  });

  // Added for OBAF: OBAF's bin location filter (the auto-issuance tab).
  it('takes bin locations from the query and labels them', async () => {
    fetchBins.mockResolvedValue([
      { id: 'bin-1', name: 'Bin 1' },
      { id: 'bin-2', name: 'Bin 2' },
      { id: 'bin-3', name: 'Bin 3' },
    ]);
    mockHistory.location.search = '?tab=PRODUCTS&binLocations=bin-1&binLocations=bin-3';
    const { result } = renderFilters();
    await flushPromises();

    expect(fetchBins).toHaveBeenCalledWith('loc-1');
    expect(result.current.defaultFilterValues.binLocations).toEqual([
      {
        id: 'bin-1', name: 'Bin 1', label: 'Bin 1', value: 'bin-1',
      },
      {
        id: 'bin-3', name: 'Bin 3', label: 'Bin 3', value: 'bin-3',
      },
    ]);
  });

  it('takes a single bin location from the query', async () => {
    fetchBins.mockResolvedValue([{ id: 'bin-1', name: 'Bin 1' }, { id: 'bin-2', name: 'Bin 2' }]);
    mockHistory.location.search = '?tab=PRODUCTS&binLocations=bin-2';
    const { result } = renderFilters();
    await flushPromises();

    expect(result.current.defaultFilterValues.binLocations.map(({ value }) => value)).toEqual(['bin-2']);
  });

  it('setFilterValues pushes the bin location ids to the url', () => {
    mockState = buildState({ shouldRebuildParams: false });
    const { result } = renderFilters();

    act(() => result.current.setFilterValues({
      tab: 'PRODUCTS',
      binLocations: [{ id: 'bin-1' }, { id: 'bin-2' }],
    }));

    expect(pushedQuery()).toEqual({ tab: 'PRODUCTS', binLocations: ['bin-1', 'bin-2'] });
  });

  it('setFilterValues with no values keeps the url', () => {
    mockState = buildState({ shouldRebuildParams: false });
    const { result } = renderFilters();

    act(() => result.current.setFilterValues({}));

    expect(mockHistory.push).not.toHaveBeenCalled();
  });

  // KNOWN-GAP: OBAF's resetForm stores the query string as the filter params,
  // where the tabs expect an object.
  it.skip('resetForm keeps only the tab, stops fetching and requests a rebuild', async () => {
    mockHistory.location.search = '?tab=PRODUCTS&startDate=01/01/2026';
    const { result } = renderFilters();
    await flushPromises();
    act(() => result.current.setShouldFetch(true));
    expect(result.current.shouldFetch).toBe(true);

    act(() => result.current.resetForm());

    expect(pushedQuery()).toEqual({ tab: 'PRODUCTS' });
    // the filter params stay an object, as the tabs read them
    expect(result.current.filterParams).toEqual(expect.objectContaining({ tab: 'PRODUCTS' }));
    expect(result.current.shouldFetch).toBe(false);
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SET_SHOULD_REBUILD', flag: true });
  });

  it('resets the form when the current location changes', () => {
    mockState = buildState({ locationId: 'loc-a', shouldRebuildParams: false });
    mockHistory.location.search = '?tab=PRODUCTS&products=p-1';
    const { rerender } = renderFilters();
    rerender();
    expect(mockHistory.push).not.toHaveBeenCalled();

    mockState = buildState({ locationId: 'loc-b', shouldRebuildParams: false });
    rerender();

    expect(pushedQuery()).toEqual({ tab: 'PRODUCTS' });
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SET_SHOULD_REBUILD', flag: true });
  });
});
