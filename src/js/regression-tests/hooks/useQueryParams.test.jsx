import React from 'react';

import { renderHook } from '@testing-library/react-hooks';
import { MemoryRouter } from 'react-router-dom';

import useQueryParams from 'hooks/useQueryParams';

const renderAt = (url) => renderHook(() => useQueryParams(), {
  wrapper: ({ children }) => <MemoryRouter initialEntries={[url]}>{children}</MemoryRouter>,
}).result.current;

describe('useQueryParams', () => {
  it('parses the query of the current location', () => {
    expect(renderAt('/openboxes/stockMovement/list?direction=INBOUND&max=10'))
      .toEqual({ direction: 'INBOUND', max: '10' });
  });

  it('gathers repeated parameters into a list and decodes values', () => {
    expect(renderAt('/list?status=PENDING&status=SHIPPED&q=a%20b'))
      .toEqual({ status: ['PENDING', 'SHIPPED'], q: 'a b' });
  });

  it('returns no parameters for a location without a query', () => {
    expect(renderAt('/list')).toEqual({});
  });
});
