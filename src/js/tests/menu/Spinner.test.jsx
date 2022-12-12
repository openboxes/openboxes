import React from 'react';

import { render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import Spinner from 'components/spinner/Spinner';

describe('spinner component', () => {
  it('should match snapshot', () => {
    const renderedSpinner = renderer.create(<Spinner />)
      .toJSON();
    expect(renderedSpinner)
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    render(<Spinner />);
    const spinnerText = screen.getByText('Loading...');
    expect(spinnerText)
      .toBeTruthy();
  });
});
