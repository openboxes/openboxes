import React from 'react';

import { render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import Spinner from 'components/spinner/Spinner';

describe('test spinner component', () => {
  it('test if spinner matches snapshot', () => {
    const renderedSpinner = renderer.create(<Spinner/>)
      .toJSON();
    expect(renderedSpinner)
      .toMatchSnapshot();
  });

  it('test if spinner is correctly rendering', () => {
    render(<Spinner/>);
    const spinnerText = screen.getByText('Loading...');
    expect(spinnerText)
      .toBeTruthy();
  });
});
