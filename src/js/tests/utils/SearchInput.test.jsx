import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import SearchInput from 'utils/SearchInput';

import '@testing-library/jest-dom';


let searchInput;
let searchInputWithValue;
let onChangeFunc;

describe('search input', () => {
  beforeEach(() => {
    onChangeFunc = jest.fn();
    searchInput = (<SearchInput value="" onChange={onChangeFunc} />);
    searchInputWithValue = (<SearchInput value="test" onChange={onChangeFunc} />);
  });

  it('should match snapshot', () => {
    const renderedInput = renderer.create(searchInput);
    expect(renderedInput.toJSON())
      .toMatchSnapshot();
  });

  it('should render properly', () => {
    render(searchInput);
    expect(screen.getByTestId('search-input-div'))
      .toBeInTheDocument();
  });

  it('should not render button element', () => {
    render(searchInput);
    expect(screen.queryByRole('button'))
      .not
      .toBeInTheDocument();
  });

  it('should render button element', () => {
    render(searchInputWithValue);
    expect((screen.queryByRole('button')))
      .toBeInTheDocument();
  });

  it('should call handleClear on click', () => {
    render(searchInputWithValue);
    expect(onChangeFunc)
      .toHaveBeenCalledTimes(0);
    fireEvent.click(screen.getByRole('button'));
    expect(onChangeFunc)
      .toHaveBeenCalledWith('');
    expect(onChangeFunc)
      .toHaveBeenCalledTimes(1);
  });
});
