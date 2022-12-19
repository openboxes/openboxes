import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import Filter from 'utils/Filter';

import '@testing-library/jest-dom';

let filter;
let filterWithValue;
let onChangeFunc;

describe('filter', () => {
  beforeEach(() => {
    onChangeFunc = jest.fn();
    filter = (<Filter onChange={onChangeFunc} />);
    filterWithValue = (<Filter onChange={onChangeFunc} filter={{ value: 'testValue' }} />);
  });

  it('should match snapshot', () => {
    const renderedFilter = renderer.create(filter);
    expect(renderedFilter.toJSON())
      .toMatchSnapshot();
  });

  it('should render properly', () => {
    render(filter);
    expect(screen.getByTestId('filter-input'))
      .toBeInTheDocument();
  });

  it('should call function when data changes', () => {
    render(filter);
    expect(onChangeFunc)
      .toHaveBeenCalledTimes(0);
    fireEvent.change(screen.getByTestId('filter-input'), { target: { value: 'a' } });
    expect(onChangeFunc)
      .toHaveBeenCalledTimes(1);
    expect(onChangeFunc)
      .toHaveBeenCalledWith('a');
  });

  it('should have starting value when filter.value is present', () => {
    render(filterWithValue);
    expect(screen.getByTestId('filter-input'))
      .toHaveAttribute('value', 'testValue');
  });
});
