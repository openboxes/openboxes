import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import store from 'store';

import Logo from 'components/Layout/Logo';

let renderedLogo;
describe('logo component', () => {
  beforeEach(() => {
    renderedLogo = render(<Router><Logo logoUrl="" store={store} /></Router>);
  });

  it('should match snapshot', () => {
    expect(renderedLogo.asFragment())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedLogo.getByTestId('logo-wrapper'))
      .toBeTruthy();
  });

  it('should include logo after redirecting to the main page', () => {
    const image = screen.getByAltText('Openboxes');
    expect(image)
      .toBeTruthy();
    fireEvent.click(image);
    expect(image)
      .toBeTruthy();
  });
});
