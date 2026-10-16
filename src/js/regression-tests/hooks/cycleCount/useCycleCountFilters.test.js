import { act, renderHook } from '@testing-library/react-hooks';
import queryString from 'query-string';

import useCycleCountFilters from 'hooks/cycleCount/useCycleCountFilters';
import apiClient from 'utils/apiClient';
import {
  fetchBins,
  fetchProductClassifications,
  fetchProductsCatalogs,
  fetchProductsCategories,
  fetchProductsTags,
  fetchUserById,
} from 'utils/option-utils';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  useSelector: (selector) => selector(mockState),
}));
const mockHistory = { location: { pathname: '/cycleCount', search: '' }, push: jest.fn() };
jest.mock('react-router-dom', () => ({
  useHistory: () => mockHistory,
}));
jest.mock('actions', () => ({
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'SET_SHOULD_REBUILD', flag })),
}));
jest.mock('hooks/useTranslate', () => jest.fn(() => (key, defaultMessage) => defaultMessage));
jest.mock('utils/option-utils', () => ({
  debouncePeopleFetch: jest.requireActual('utils/option-utils').debouncePeopleFetch,
  fetchBins: jest.fn(),
  fetchProductClassifications: jest.fn(),
  fetchProductsCatalogs: jest.fn(),
  fetchProductsCategories: jest.fn(),
  fetchProductsTags: jest.fn(),
  fetchUserById: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const FILTER_FIELDS = {
  searchTerm: {},
  catalogs: {},
  tags: {},
  categories: {},
  internalLocations: {},
  abcClasses: {},
};

const buildState = ({ locationId = 'loc-1', shouldRebuildParams = true } = {}) => ({
  session: {
    currentLocation: locationId ? { id: locationId } : null,
    searchConfig: { debounceTime: 300, minSearchLength: 3 },
  },
  filterForm: { shouldRebuildParams },
});

const pushedQuery = (call = 0) => queryString.parse(mockHistory.push.mock.calls[call][0].search);

describe('useCycleCountFilters', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockHistory.location.search = '';
    mockState = buildState();
    fetchProductsCategories.mockResolvedValue([{ id: 'cat-1', name: 'Medicines' }, { id: 'cat-2', name: 'Other' }]);
    fetchProductsTags.mockResolvedValue([{ id: 'tag-1', label: 'Cold' }, { id: 'tag-2', label: 'Hot' }]);
    fetchProductsCatalogs.mockResolvedValue([{ id: 'cl-1', label: 'Essentials' }]);
    fetchProductClassifications.mockResolvedValue([{ id: 'A', label: 'A' }, { id: 'B', label: 'B' }]);
    fetchBins.mockResolvedValue([
      {
        id: 'bin-1', name: 'A-01', zoneId: 'z-1', zoneName: 'Zone 1',
      },
      { id: 'bin-2', name: 'B-01' },
    ]);
    fetchUserById.mockImplementation(async (id) => ({ id, name: `User ${id}` }));
  });

  it('builds the default values and options from the query string', async () => {
    mockHistory.location.search = '?tab=TO_COUNT&catalogs=cl-1&tags=tag-1&tags=tag-2&categories=cat-2'
      + '&internalLocations=bin-1&abcClasses=B&countAssignees=u-1&recountAssignees=u-2&recountAssignees=u-3'
      + '&negativeQuantity=true&dateLastCount=01/01/2026&countDeadline=02/01/2026&recountDeadline=03/01/2026';

    const { result } = renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));
    expect(result.current.isLoading).toBe(true);
    await flushPromises();

    expect(fetchProductsTags).toHaveBeenCalledWith({ hideNumbers: true });
    expect(fetchProductsCatalogs).toHaveBeenCalledWith({ hideNumbers: true });
    expect(fetchProductClassifications).toHaveBeenCalledWith('loc-1');
    expect(fetchBins).toHaveBeenCalledWith('loc-1', [], 'sortOrder,locationType,name');
    expect(fetchUserById.mock.calls.map(([id]) => id)).toEqual(['u-1', 'u-2', 'u-3']);
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SET_SHOULD_REBUILD', flag: false });

    const { defaultFilterValues } = result.current;
    expect(result.current.isLoading).toBe(false);
    expect(defaultFilterValues).toEqual(expect.objectContaining({
      tab: 'TO_COUNT',
      searchTerm: '',
      negativeQuantity: 'true',
      dateLastCount: '01/01/2026',
      countDeadline: '02/01/2026',
      recountDeadline: '03/01/2026',
      catalogs: [{
        id: 'cl-1', label: 'Essentials', name: 'Essentials', value: 'cl-1',
      }],
      tags: [
        {
          id: 'tag-1', label: 'Cold', name: 'Cold', value: 'tag-1',
        },
        {
          id: 'tag-2', label: 'Hot', name: 'Hot', value: 'tag-2',
        },
      ],
      categories: [{
        id: 'cat-2', label: 'Other', name: 'Other', value: 'cat-2',
      }],
      internalLocations: [{
        id: 'bin-1', label: 'A-01', name: 'A-01', value: 'bin-1',
      }],
      abcClasses: [{
        id: 'B', label: 'B', name: 'B', value: 'B',
      }],
    }));
    expect(defaultFilterValues.countAssignees).toEqual([{
      id: 'u-1', name: 'User u-1', label: 'User u-1', value: 'u-1',
    }]);
    expect(defaultFilterValues.recountAssignees.map(({ value }) => value)).toEqual(['u-2', 'u-3']);

    expect(result.current.categories).toHaveLength(2);
    expect(result.current.tags).toHaveLength(2);
    expect(result.current.catalogs).toHaveLength(1);
    expect(result.current.abcClasses).toHaveLength(2);
    expect(result.current.countAssignees).toBe(defaultFilterValues.countAssignees);
    expect(result.current.internalLocations
      .map(({ id, options }) => [id, options.map((o) => o.name)]))
      .toEqual([['zone-z-1', ['Zone 1: A-01']], ['zone-no-zone', ['B-01']]]);
  });

  it('leaves unselected filters empty and fetches no users without assignees in the query', async () => {
    mockHistory.location.search = '?tab=ALL';
    const { result } = renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));
    await flushPromises();

    expect(fetchUserById).not.toHaveBeenCalled();
    expect(result.current.defaultFilterValues).toEqual({
      tab: 'ALL',
      searchTerm: '',
      catalogs: null,
      tags: null,
      categories: null,
      internalLocations: null,
      abcClasses: null,
    });
    expect(result.current.countAssignees).toBeUndefined();
    expect(result.current.negativeQuantity).toBe(false);
    expect(result.current.dateLastCount).toBeNull();
  });

  it('does not initialize without a current location or without a rebuild request', async () => {
    mockState = buildState({ locationId: null });
    renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));
    await flushPromises();
    expect(fetchBins).not.toHaveBeenCalled();
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SET_SHOULD_REBUILD', flag: false });

    mockDispatch.mockClear();
    mockState = buildState({ shouldRebuildParams: false });
    renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));
    await flushPromises();
    expect(fetchBins).not.toHaveBeenCalled();
    expect(mockDispatch).not.toHaveBeenCalled();
  });

  it('setFilterValues pushes the selected ids to the url and stores the values', () => {
    mockState = buildState({ shouldRebuildParams: false });
    const { result } = renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));
    const values = {
      tab: 'TO_COUNT',
      tags: [{ id: 'tag-1' }, { id: 'tag-2' }],
      countAssignees: [{ id: 'u-1' }],
      negativeQuantity: true,
      dateLastCount: '01/01/2026',
      searchTerm: 'not a url filter',
      categories: [],
    };

    act(() => result.current.setFilterValues(values));

    expect(mockHistory.push).toHaveBeenCalledTimes(1);
    expect(mockHistory.push.mock.calls[0][0].pathname).toBe('/cycleCount');
    expect(pushedQuery()).toEqual({
      tab: 'TO_COUNT',
      tags: ['tag-1', 'tag-2'],
      countAssignees: 'u-1',
      negativeQuantity: 'true',
      dateLastCount: '01/01/2026',
    });
    expect(result.current.filterParams).toBe(values);
  });

  it('setFilterValues with no values keeps the url', () => {
    mockState = buildState({ shouldRebuildParams: false });
    const { result } = renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));

    act(() => result.current.setFilterValues({}));

    expect(mockHistory.push).not.toHaveBeenCalled();
    expect(result.current.filterParams).toEqual({});
  });

  it('resetForm keeps only the tab in the url and requests a rebuild', () => {
    mockState = buildState({ shouldRebuildParams: false });
    mockHistory.location.search = '?tab=TO_RESOLVE&tags=tag-1';
    const { result } = renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));

    result.current.resetForm();

    expect(pushedQuery()).toEqual({ tab: 'TO_RESOLVE' });
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'SET_SHOULD_REBUILD', flag: true });
  });

  it('clears the url before re-initializing once filters were initialized', async () => {
    mockHistory.location.search = '?tab=ALL&tags=tag-1';
    const { result, rerender } = renderHook(
      () => useCycleCountFilters({ filterFields: FILTER_FIELDS }),
    );
    await flushPromises();
    expect(mockHistory.push).not.toHaveBeenCalled();

    mockState = buildState({ shouldRebuildParams: false });
    rerender();
    mockState = buildState({ shouldRebuildParams: true });
    rerender();
    await flushPromises();

    expect(pushedQuery()).toEqual({ tab: 'ALL' });
    expect(fetchBins).toHaveBeenCalledTimes(2);
    expect(result.current.isLoading).toBe(false);
  });

  describe('debouncedPeopleFetch', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('searches people once the term is long enough, after the debounce time', async () => {
      mockState = buildState({ shouldRebuildParams: false });
      apiClient.get.mockResolvedValue({ data: { data: [{ id: 'p-1', name: 'Ann' }] } });
      const { result } = renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));
      const callback = jest.fn();

      result.current.debouncedPeopleFetch('Ann', callback);
      expect(apiClient.get).not.toHaveBeenCalled();
      jest.advanceTimersByTime(300);
      await flushPromises();

      expect(apiClient.get).toHaveBeenCalledWith('/api/persons', { params: { name: 'Ann', status: true } });
      expect(callback).toHaveBeenCalledWith([{
        id: 'p-1', name: 'Ann', value: 'p-1', label: 'Ann',
      }]);
    });

    it('answers with no options for a short term', () => {
      mockState = buildState({ shouldRebuildParams: false });
      const { result } = renderHook(() => useCycleCountFilters({ filterFields: FILTER_FIELDS }));
      const callback = jest.fn();

      result.current.debouncedPeopleFetch('An', callback);
      jest.advanceTimersByTime(300);

      expect(apiClient.get).not.toHaveBeenCalled();
      expect(callback).toHaveBeenCalledWith([]);
    });
  });
});
