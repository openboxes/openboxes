import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import renderer from 'react-test-renderer';
import store from 'store';

import Logo from 'components/Layout/Logo';

let renderedLogo;
describe('test logo component', () => {
  beforeEach(() => {
    renderedLogo = renderer.create(<Router><Logo logoUrl="" store={store}/></Router>);

  });

  it('test if logo matches snapshot', () => {
    expect(renderedLogo.toJSON())
      .toMatchSnapshot();
  });

  it('test if logo correctly rendering', () => {
    expect(renderedLogo.root.findByProps({ className: 'd-flex align-items-center logo-wrapper' }))
      .toBeTruthy();
  });

  it('test if logo is present after redirecting to main page', () => {
    render(<Router><Logo logoUrl="" store={store}/></Router>);
    const image = screen.getByAltText('Openboxes');
    expect(image)
      .toBeTruthy();
    fireEvent.click(image);
    expect(image)
      .toBeTruthy();
  });
});

