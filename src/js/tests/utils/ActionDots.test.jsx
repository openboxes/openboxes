import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';

import ContextMenu from 'utils/ContextMenu';

import '@testing-library/jest-dom';

let actionDots;
let actions;

// jest doesn't have access to bootstrap styles, so I included it here.
const applyStyles = () => {
  const style = document.createElement('style');
  style.innerHTML = ` 
           .dropdown-menu { 
              display: none;
           }
           .show {
              display: block !important;
           }`;
  document.body.appendChild(style);
};

describe('action dots', () => {
  beforeEach(() => {
    applyStyles();
    actions = [{
      variant: 'danger',
      label: 'testLabel',
      defaultLabel: 'defaultLabelTest',
      leftIcon: <>leftIconTest</>,
    }];
    actionDots = (
      <ContextMenu
        id="1"
        actions={actions}
      />
    );
  });

  it('should match snapshot', () => {
    const renderedActionDots = render(actionDots);
    expect(renderedActionDots.asFragment())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    const renderedActionDots = render(actionDots);
    expect(renderedActionDots.getByTestId('dropdown-toggle'))
      .toBeTruthy();
  });

  it('should have button after clicking it', () => {
    const testId = 'dropdown-toggle';
    render(actionDots);
    expect(screen.getByTestId(testId))
      .toBeTruthy();
    fireEvent.click(screen.getByTestId(testId));
    expect(screen.getByTestId(testId))
      .toBeTruthy();
  });
});
