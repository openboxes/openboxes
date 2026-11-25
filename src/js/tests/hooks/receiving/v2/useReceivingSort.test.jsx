import React from 'react';

import { act, renderHook } from '@testing-library/react-hooks';
import { Provider } from 'react-redux';
import { combineReducers, createStore } from 'redux';

import useReceivingSort from 'hooks/receiving/v2/useReceivingSort';
import partialReceiving from 'reducers/partialReceivingReducer';

import '@testing-library/jest-dom';

const createReceivingStore = () => createStore(combineReducers({ partialReceiving }));

const renderSort = (store = createReceivingStore()) => {
  const wrapper = ({ children }) => <Provider store={store}>{children}</Provider>;
  return { ...renderHook(() => useReceivingSort(), { wrapper }), store };
};

const toggle = (result, columnId) => act(() => {
  result.current.sortableProps.toggleSort(columnId)();
});

describe('useReceivingSort', () => {
  it('starts with no column sorted, which means the shipment order', () => {
    const { result } = renderSort();

    expect(result.current.sort).toBeNull();
    expect(result.current.order).toBeNull();
  });

  it('sorts a newly clicked column ascending', () => {
    const { result } = renderSort();

    toggle(result, 'productCode');

    expect(result.current.sort).toBe('productCode');
    expect(result.current.order).toBe('asc');
  });

  it('flips the direction when the sorted column is clicked again', () => {
    const { result } = renderSort();

    toggle(result, 'productCode');
    toggle(result, 'productCode');

    expect(result.current.order).toBe('desc');

    toggle(result, 'productCode');

    expect(result.current.order).toBe('asc');
  });

  it('starts ascending again when another column is clicked', () => {
    const { result } = renderSort();

    toggle(result, 'productCode');
    toggle(result, 'productCode');
    toggle(result, 'lotNumber');

    expect(result.current.sort).toBe('lotNumber');
    expect(result.current.order).toBe('asc');
  });

  it('resets back to the shipment order', () => {
    const { result } = renderSort();

    toggle(result, 'productCode');
    act(() => {
      result.current.resetSort();
    });

    expect(result.current.sort).toBeNull();
    expect(result.current.order).toBeNull();
  });

  it('marks only the sorted column with its direction', () => {
    const { result } = renderSort();

    toggle(result, 'productCode');

    const { dynamicClassName } = result.current.sortableProps;
    expect(dynamicClassName('productCode')).toBe('-sort-asc');
    expect(dynamicClassName('lotNumber')).toBeNull();
  });

  it('shares the sorting between two instances of the hook', () => {
    const store = createReceivingStore();
    const { result: receiving } = renderSort(store);
    const { result: check } = renderSort(store);

    toggle(receiving, 'expirationDate');
    toggle(receiving, 'expirationDate');

    expect(check.current.sort).toBe('expirationDate');
    expect(check.current.order).toBe('desc');
  });
});
