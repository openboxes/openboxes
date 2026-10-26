// OBAF-new: the putaway task filter config and OBAF's debounced internal-location search
// (utils/option-utils debounceInternalLocationsFetch) that feeds its container/destination filters.
import filterFields from 'components/putaway-task/list/FilterFields';
import { DateFormat } from 'consts/timeFormat';
import apiClient from 'utils/apiClient';
import { debounceInternalLocationsFetch } from 'utils/option-utils';

// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('putaway task FilterFields (OBAF)', () => {
  it('declares the filters in display order', () => {
    expect(Object.keys(filterFields)).toEqual([
      'statusCategory', 'status', 'container', 'destination', 'createdAfter', 'createdBefore',
    ]);
  });

  it('offers OPEN/CLOSED categories and the five task statuses (multi-select)', () => {
    expect(filterFields.statusCategory.getDynamicAttr().options.map(({ id }) => id))
      .toEqual(['OPEN', 'CLOSED']);
    expect(filterFields.status.attributes.multi).toBe(true);
    expect(filterFields.status.getDynamicAttr().options.map(({ id }) => id))
      .toEqual(['PENDING', 'STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED']);
  });

  it('wires container and destination to their own async loaders, without client filtering', () => {
    const debouncedContainerFetch = jest.fn();
    const debouncedDestinationFetch = jest.fn();
    const props = { debouncedContainerFetch, debouncedDestinationFetch };

    expect(filterFields.container.getDynamicAttr(props).loadOptions).toBe(debouncedContainerFetch);
    expect(filterFields.destination.getDynamicAttr(props).loadOptions)
      .toBe(debouncedDestinationFetch);
    const options = [{ id: 'a' }];
    expect(filterFields.container.attributes.filterOptions(options)).toBe(options);
    expect(filterFields.destination.attributes.filterOptions(options)).toBe(options);
    expect(filterFields.container.attributes.async).toBe(true);
  });

  it('sends the created dates as month/day/year', () => {
    expect(filterFields.createdAfter.attributes.dateFormat).toBe(DateFormat.MM_DD_YYYY);
    expect(filterFields.createdBefore.attributes.dateFormat).toBe(DateFormat.MM_DD_YYYY);
  });
});

describe('debounceInternalLocationsFetch (OBAF)', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => jest.useRealTimers());

  it('searches the internal locations of the facility with the extra params, once per burst', async () => {
    apiClient.get.mockResolvedValue({ data: { data: [{ id: 'c1', name: 'Cart 1' }] } });
    const fetch = debounceInternalLocationsFetch(300, 3, 'loc1', { activityCodes: 'PUTAWAY_CART' });
    const callback = jest.fn();

    fetch('Car', callback);
    fetch('Cart', callback);
    jest.advanceTimersByTime(300);
    await flushPromises();

    expect(apiClient.get).toHaveBeenCalledTimes(1);
    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe('/api/internalLocations/search');
    expect(config.params).toEqual({
      searchTerm: 'Cart', 'parentLocation.id': 'loc1', activityCodes: 'PUTAWAY_CART',
    });
    expect(config.paramsSerializer({ a: ['x', 'y'] })).toBe('a=x&a=y');
    expect(callback).toHaveBeenCalledWith([{
      id: 'c1', value: 'c1', name: 'Cart 1', label: 'Cart 1',
    }]);
  });

  it('loads the default options for an empty search, without a searchTerm param', async () => {
    apiClient.get.mockResolvedValue({ data: { data: [] } });
    const fetch = debounceInternalLocationsFetch(100, 3, 'loc1');
    const callback = jest.fn();

    fetch('', callback);
    jest.advanceTimersByTime(100);
    await flushPromises();

    expect(apiClient.get.mock.calls[0][1].params).toEqual({ searchTerm: undefined, 'parentLocation.id': 'loc1' });
    expect(callback).toHaveBeenCalledWith([]);
  });

  it('answers nothing for a search shorter than the minimum, without calling the API', () => {
    const fetch = debounceInternalLocationsFetch(100, 3, 'loc1');
    const callback = jest.fn();

    fetch('ab', callback);
    jest.advanceTimersByTime(100);

    expect(apiClient.get).not.toHaveBeenCalled();
    expect(callback).toHaveBeenCalledWith([]);
  });

  it('answers an empty list when the search fails', async () => {
    apiClient.get.mockRejectedValue(new Error('down'));
    const fetch = debounceInternalLocationsFetch(100, 1, 'loc1');
    const callback = jest.fn();

    fetch('A', callback);
    jest.advanceTimersByTime(100);
    await flushPromises();

    expect(callback).toHaveBeenCalledWith([]);
  });
});
