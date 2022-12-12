import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import renderer from 'react-test-renderer';
import store from 'store';

import Logo from 'components/Layout/Logo';

let renderedLogo;
describe('logo component', () => {
  beforeEach(() => {
    renderedLogo = renderer.create(<Router><Logo logoUrl="" store={store} /></Router>);
  });

  it('should match snapshot', () => {
    expect(renderedLogo.toJSON())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedLogo.root.findByProps({ 'data-testid': 'logo-wrapper' }))
      .toBeTruthy();
  });

  it('should include logo after redirecting to the main page', () => {
    render(<Router><Logo logoUrl="" store={store} /></Router>);
    const image = screen.getByAltText('Openboxes');
    expect(image)
      .toBeTruthy();
    fireEvent.click(image);
    expect(image)
      .toBeTruthy();
  });
});

