import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import useProductFilters from 'hooks/list-pages/product/useProductFilters';
import {
  fetchHandlingRequirements,
  fetchProductGroups,
  fetchProductsCatalogs,
  fetchProductsCategories,
  fetchProductsGlAccounts,
  fetchProductsTags,
} from 'utils/option-utils';

jest.mock('react-redux', () => ({
  ...jest.requireActual('react-redux'),
  useSelector: jest.fn(),
  useDispatch: jest.fn(),
}));
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useHistory: jest.fn(),
}));
jest.mock('actions', () => ({
  setShouldRebuildFilterParams: jest.fn((flag) => ({ type: 'REBUILD', flag })),
}));
jest.mock('utils/option-utils', () => ({
  ...jest.requireActual('utils/option-utils'),
  fetchHandlingRequirements: jest.fn(),
  fetchProductGroups: jest.fn(),
  fetchProductsCatalogs: jest.fn(),
  fetchProductsCategories: jest.fn(),
  fetchProductsGlAccounts: jest.fn(),
  fetchProductsTags: jest.fn(),
}));

const flushPromises = async () => {
  for (let i = 0; i < 10; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const option = (id) => ({ id, label: `Label ${id}` });
const selected = (id) => ({
  id, label: `Label ${id}`, name: `Label ${id}`, value: id,
});

describe('useProductFilters', () => {
  let state;
  let history;

  beforeEach(() => {
    jest.clearAllMocks();
    state = {
      session: { currentLocation: { id: 'loc-1' } },
      filterForm: { shouldRebuildParams: true },
    };
    history = { location: { pathname: '/product/list', search: '' }, push: jest.fn() };
    useSelector.mockImplementation((selector) => selector(state));
    useDispatch.mockReturnValue(jest.fn());
    useHistory.mockReturnValue(history);
    fetchProductsCategories.mockResolvedValue([option('cat-1'), option('cat-2')]);
    fetchProductsCatalogs.mockResolvedValue([option('catalog-1')]);
    fetchProductsTags.mockResolvedValue([option('tag-1'), option('tag-2')]);
    fetchProductsGlAccounts.mockResolvedValue([option('gl-1')]);
    fetchProductGroups.mockResolvedValue([option('family-1')]);
    fetchHandlingRequirements.mockResolvedValue([option('cold')]);
  });

  it('sets the defaults right away and loads every option list', async () => {
    history.location.search = 'includeInactive=true&includeCategoryChildren=true'
      + '&createdAfter=2026-01-01&createdBefore=2026-02-01';
    const { result } = renderHook(() => useProductFilters());

    expect(result.current.defaultFilterValues).toEqual({
      categoryId: '',
      glAccountsId: '',
      catalogId: '',
      tagId: '',
      productFamilyId: '',
      createdAfter: '2026-01-01',
      createdBefore: '2026-02-01',
      includeInactive: 'true',
      includeCategoryChildren: 'true',
      handlingRequirementId: '',
    });
    await flushPromises();

    expect(fetchProductsGlAccounts).toHaveBeenCalledWith({ active: true });
    expect(result.current.categories).toEqual([option('cat-1'), option('cat-2')]);
    expect(result.current.catalogs).toEqual([option('catalog-1')]);
    expect(result.current.tags).toEqual([option('tag-1'), option('tag-2')]);
    expect(result.current.glAccounts).toEqual([option('gl-1')]);
    expect(result.current.productGroups).toEqual([option('family-1')]);
    expect(result.current.handlingRequirements).toEqual([option('cold')]);
  });

  it('waits for the option lists and selects the ids from the query', async () => {
    history.location.search = 'catalogId=catalog-1&tagId=tag-2&tagId=missing&categoryId=cat-1'
      + '&glAccountsId=gl-1&productFamilyId=family-1&handlingRequirementId=cold';
    const { result } = renderHook(() => useProductFilters());
    expect(result.current.defaultFilterValues).toEqual({});

    await flushPromises();

    expect(result.current.defaultFilterValues).toMatchObject({
      catalogId: [selected('catalog-1')],
      tagId: [selected('tag-2')],
      categoryId: [selected('cat-1')],
      glAccountsId: [selected('gl-1')],
      productFamilyId: [selected('family-1')],
      handlingRequirementId: [selected('cold')],
      includeInactive: '',
    });
  });

  it('pushes the mapped filters to the url and stores the params', () => {
    const { result } = renderHook(() => useProductFilters());
    const values = {
      includeInactive: true,
      catalogId: [{ id: 'catalog-1' }],
      handlingRequirementId: [{ id: 'cold' }, { id: 'hazmat' }],
      createdAfter: '2026-01-01',
    };

    act(() => result.current.setFilterValues(values));

    expect(history.push).toHaveBeenCalledWith({
      pathname: '/product/list',
      search: 'catalogId=catalog-1&createdAfter=2026-01-01&handlingRequirementId=cold'
        + '&handlingRequirementId=hazmat&includeInactive=true',
    });
    expect(result.current.filterParams).toBe(values);
  });

  it('skips the url for empty values and clears it on a rebuild after initialization', async () => {
    const { result, rerender } = renderHook(() => useProductFilters());
    await flushPromises();
    act(() => result.current.setFilterValues({}));
    expect(history.push).not.toHaveBeenCalled();

    state.filterForm.shouldRebuildParams = false;
    rerender();
    state.filterForm.shouldRebuildParams = true;
    rerender();

    expect(history.push).toHaveBeenCalledWith({ pathname: '/product/list' });
  });
});
