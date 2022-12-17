import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import Input from 'utils/Input';

import '@testing-library/jest-dom';

let input;
let onChangeFunc;
let arrowUpFunction;

describe('input', () => {
  beforeEach(() => {
    onChangeFunc = jest.fn();
    arrowUpFunction = jest.fn();
    input = (<Input onChange={onChangeFunc} arrowUp={arrowUpFunction} />);
  });

  it('should match snapshot', () => {
    const renderedInput = renderer.create(input);
    expect(renderedInput.toJSON()).toMatchSnapshot();
  });

  it('should render properly', () => {
    render(input);
    expect(screen.getByTestId('input')).toBeInTheDocument();
  });

  it('should trigger function on change', () => {
    render(input);
    expect(onChangeFunc).toHaveBeenCalledTimes(0);
    fireEvent.change(screen.getByTestId('input'), { target: { value: 'a' } });
    expect(onChangeFunc).toHaveBeenCalledTimes(1);
    expect(onChangeFunc).toHaveBeenCalledWith('a');
  });

  it('should trigger onKeyDown', () => {
    render(input);
    expect(arrowUpFunction).toHaveBeenCalledTimes(0);
    fireEvent.keyDown(screen.getByTestId('input'), { key: 'ArrowUp', which: 38, keyCode: 38 });
    expect(arrowUpFunction).toHaveBeenCalledTimes(1);
  });
});
