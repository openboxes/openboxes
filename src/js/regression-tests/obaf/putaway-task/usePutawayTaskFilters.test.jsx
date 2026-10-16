// OBAF-new: usePutawayTaskFilters, the address <-> filter values mapping of the putaway task list.
import React from 'react';

import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';

import usePutawayTaskFilters from 'hooks/list-pages/putaway-task/usePutawayTaskFilters';
import { fetchLocationById } from 'utils/option-utils';

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));
jest.mock('utils/option-utils', () => ({ fetchLocationById: jest.fn() }));

// React 16's test renderer has no async act, so the async initialisation is awaited by flushing.
const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

describe('usePutawayTaskFilters (OBAF)', () => {
  let state;
  let history;

  const renderFilters = (entry = '/openboxes/putawayTask/list') => renderHook(
    () => usePutawayTaskFilters(),
    {
      wrapper: ({ children }) => (
        <MemoryRouter initialEntries={[entry]}>
          {children}
          <Route
            path="*"
            render={(props) => {
              history = props.history;
              return null;
            }}
          />
        </MemoryRouter>
      ),
    },
  );

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      session: { currentLocation: { id: 'loc1' } },
      filterForm: { shouldRebuildParams: false },
    };
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(jest.fn());
  });

  it('defaults the status category to OPEN with every other filter blank', async () => {
    const { result } = renderFilters();
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      statusCategory: { id: 'OPEN', value: 'OPEN', label: 'Open' },
      status: '',
      container: '',
      destination: '',
      createdAfter: '',
      createdBefore: '',
    });
  });

  it('reads every filter from the address, resolving container and destination', async () => {
    fetchLocationById.mockImplementation(async (id) => ({ id, name: `Loc ${id}` }));
    const { result } = renderFilters(
      '/openboxes/putawayTask/list?statusCategory=CLOSED&status=PENDING&status=BOGUS&searchTerm=Gauze'
      + '&container=c1&destination=d1&createdAfter=09/01/2026&createdBefore=09/30/2026',
    );
    await flushPromises();

    expect(result.current.defaultFilterValues).toEqual({
      statusCategory: { id: 'CLOSED', value: 'CLOSED', label: 'Closed' },
      status: [{ id: 'PENDING', value: 'PENDING', label: 'Pending' }],
      searchTerm: 'Gauze',
      container: { id: 'c1', value: 'c1', label: 'Loc c1' },
      destination: { id: 'd1', value: 'd1', label: 'Loc d1' },
      createdAfter: '09/01/2026',
      createdBefore: '09/30/2026',
    });
  });

  it('leaves an unknown category, container or destination blank', async () => {
    fetchLocationById.mockResolvedValue(undefined);
    const { result } = renderFilters(
      '/openboxes/putawayTask/list?statusCategory=WHATEVER&container=gone&destination=gone',
    );
    await flushPromises();

    expect(result.current.defaultFilterValues).toMatchObject({
      statusCategory: '', container: '', destination: '',
    });
  });

  it('writes chosen filters to the address as ids', async () => {
    const { result } = renderFilters();
    await flushPromises();

    act(() => {
      result.current.setFilterValues({
        statusCategory: { id: 'OPEN' },
        status: [{ id: 'PENDING' }, { id: 'STARTED' }],
        container: { id: 'c1' },
        destination: { id: 'd1' },
        searchTerm: 'x',
        createdAfter: '09/01/2026',
      });
    });

    expect(history.location.search).toBe(
      '?container=c1&createdAfter=09%2F01%2F2026&destination=d1&searchTerm=x&status=PENDING&status=STARTED&statusCategory=OPEN',
    );
    expect(result.current.filterParams.container).toEqual({ id: 'c1' });
  });

  it('does not touch the address for an empty filter set', async () => {
    const { result } = renderFilters('/openboxes/putawayTask/list?searchTerm=keep');
    await flushPromises();

    act(() => result.current.setFilterValues({}));
    expect(history.location.search).toBe('?searchTerm=keep');
    expect(result.current.filterParams).toEqual({});
  });

  it('filters by one putaway order, clearing every other filter', async () => {
    const { result } = renderFilters('/openboxes/putawayTask/list?statusCategory=CLOSED');
    await flushPromises();

    act(() => result.current.filterByOrder('PUT-7'));

    const cleared = {
      statusCategory: '',
      status: '',
      container: '',
      destination: '',
      createdAfter: '',
      createdBefore: '',
      searchTerm: 'PUT-7',
    };
    expect(history.location.search).toBe('?searchTerm=PUT-7');
    expect(result.current.defaultFilterValues).toEqual(cleared);
    expect(result.current.filterParams).toEqual(cleared);
  });
});
