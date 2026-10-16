import React from 'react';

import {
  fireEvent, render, screen, waitFor,
} from '@testing-library/react';

import '@testing-library/jest-dom';

jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
}));

/* eslint-disable import/first,import/order */
import Filter from 'components/dashboard/Filter';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const PAGE_FILTERS = [{ name: 'Category', endpoint: '/api/categories' }];
const CONFIGS = {
  personal: { filters: { Category: { endpoint: '/api/categories' } } },
  warehouse: {},
};

const storedConfig = () => JSON.parse(sessionStorage.getItem('pageConfig'));

const renderFilter = (props = {}) => {
  const fetchData = jest.fn();
  const utils = render(
    <Filter
      configs={CONFIGS}
      activeConfig="personal"
      fetchData={fetchData}
      pageFilters={PAGE_FILTERS}
      {...props}
    />,
  );
  return { ...utils, fetchData };
};

describe('dashboard Filter', () => {
  beforeEach(() => {
    sessionStorage.clear();
    apiClient.get.mockResolvedValue({
      data: {
        data: [
          { id: 'c2', name: 'Vaccines' },
          { id: 'c1', name: 'Antibiotics' },
          { id: 'c3', name: 'Medical devices' },
        ],
      },
    });
  });

  afterEach(() => jest.clearAllMocks());

  it('renders nothing when the page has no filters', () => {
    const { container } = renderFilter({ pageFilters: [] });
    expect(container).toBeEmptyDOMElement();
  });

  it('initializes the stored page config for the active dashboard', () => {
    renderFilter();
    expect(storedConfig()).toEqual({ personal: {} });
  });

  it('adds a filter picked from the category values, storing it and refreshing the data', async () => {
    const { container, fetchData } = renderFilter();
    const popup = container.querySelector('.add-category-popup');
    expect(popup).not.toBeVisible();

    fireEvent.click(screen.getByText('+ Add filter'));
    expect(popup).toBeVisible();

    fireEvent.click(screen.getByText('Category'));
    expect(apiClient.get).toHaveBeenCalledWith('/api/categories');
    await screen.findByText('Antibiotics');
    // Values are listed alphabetically.
    expect([...container.querySelectorAll('.category-value-to-select')].map((el) => el.textContent))
      .toEqual(['Antibiotics', 'Medical devices', 'Vaccines']);

    fireEvent.change(container.querySelector('input'), { target: { value: 'MED' } });
    expect([...container.querySelectorAll('.category-value-to-select')].map((el) => el.textContent))
      .toEqual(['Medical devices']);

    fireEvent.click(screen.getByText('Medical devices'));
    expect(storedConfig()).toEqual({
      personal: { Category: [{ id: 'c3', name: 'Medical devices', nameCategory: 'Category' }] },
    });
    expect(fetchData).toHaveBeenCalledWith('personal');
    expect(container.querySelector('.category-item .category-title')).toHaveTextContent('Medical devices');
    expect(popup).not.toBeVisible();
  });

  it('loads the stored filters and leaves them out of the values to pick', async () => {
    sessionStorage.setItem('pageConfig', JSON.stringify({
      personal: { Category: [{ id: 'c1', name: 'Antibiotics', nameCategory: 'Category' }] },
    }));
    const { container } = renderFilter();
    expect(container.querySelector('.category-title')).toHaveTextContent('Antibiotics');

    fireEvent.click(screen.getByText('+ Add filter'));
    fireEvent.click(screen.getByText('Category'));
    await screen.findByText('Vaccines');
    expect(container.querySelectorAll('.category-value-to-select')).toHaveLength(2);
  });

  it('removes a filter, dropping the category from storage once it is empty', () => {
    sessionStorage.setItem('pageConfig', JSON.stringify({
      personal: {
        Category: [
          { id: 'c1', name: 'Antibiotics', nameCategory: 'Category' },
          { id: 'c2', name: 'Vaccines', nameCategory: 'Category' },
        ],
      },
    }));
    const { container, fetchData } = renderFilter();

    fireEvent.click(container.querySelectorAll('.delete-button')[0]);
    expect(storedConfig().personal.Category).toEqual([{ id: 'c2', name: 'Vaccines', nameCategory: 'Category' }]);
    expect(fetchData).toHaveBeenCalledWith('personal');
    expect(screen.queryByText('Antibiotics')).not.toBeInTheDocument();

    fireEvent.click(container.querySelectorAll('.delete-button')[0]);
    expect(storedConfig()).toEqual({ personal: {} });
    expect(container.querySelectorAll('.category-title')).toHaveLength(0);
  });

  it('shows no values when they cannot be loaded, and goes back to the categories', async () => {
    apiClient.get.mockRejectedValue(new Error('down'));
    const { container } = renderFilter();
    fireEvent.click(screen.getByText('+ Add filter'));
    fireEvent.click(screen.getByText('Category'));
    await waitFor(() => expect(apiClient.get).toHaveBeenCalled());
    expect(container.querySelectorAll('.category-value-to-select')).toHaveLength(0);
    expect(container.querySelector('input')).toBeVisible();

    // "<" returns to the category list
    fireEvent.click(screen.getByText('<'));
    expect(container.querySelector('input')).not.toBeVisible();
    expect(screen.getByText('Category')).toBeInTheDocument();

    // "X" closes the popup
    fireEvent.click(screen.getByText('X'));
    expect(container.querySelector('.add-category-popup')).not.toBeVisible();
  });

  it('reloads the stored filters when the active dashboard changes', () => {
    const { rerender, container } = renderFilter();
    sessionStorage.setItem('pageConfig', JSON.stringify({
      personal: {},
      warehouse: { Category: [{ id: 'c2', name: 'Vaccines', nameCategory: 'Category' }] },
    }));
    rerender(
      <Filter configs={CONFIGS} activeConfig="warehouse" fetchData={jest.fn()} pageFilters={PAGE_FILTERS} />,
    );
    expect(container.querySelector('.category-title')).toHaveTextContent('Vaccines');
  });
});
