import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import renderer from 'react-test-renderer';
import store from 'store';

import Logo from 'components/Layout/Logo';

it('test if logo matches snapshot', () => {
  const renderedLogo = renderer.create(<Router><Logo logoUrl="" store={store} /></Router>).toJSON();
  expect(renderedLogo).toMatchSnapshot();
});

it('test if logo correctly rendering', () => {
  const renderedLogo = renderer.create(<Router><Logo logoUrl="" store={store} /></Router>).root;
  expect(renderedLogo.findByProps({ className: 'd-flex align-items-center logo-wrapper' }))
    .toBeTruthy();
});

it('test logo after redirecting to main page', () => {
  render(<Router><Logo logoUrl="" store={store} /></Router>);
  const image = screen.getByAltText('Openboxes');
  expect(screen.getByAltText('Openboxes')).toBeTruthy();
  fireEvent.click(image);
  expect(screen.getByAltText('Openboxes')).toBeTruthy();
});
