import React from 'react';

import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import renderer from 'react-test-renderer';

import ActionDots from 'utils/ActionDots';

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
      leftIcon: <React.Fragment>leftIconTest</React.Fragment>,
    }];
    actionDots = (<ActionDots
      id="1"
      actions={actions}
    />);
  });

  it('should match snapshot', () => {
    const renderedActionDots = renderer.create(actionDots);
    expect(renderedActionDots.toJSON())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    const renderedActionDots = renderer.create(actionDots);
    expect(renderedActionDots.root.findByProps({ 'data-testid': 'action-dots-component' }))
      .toBeTruthy();
  });

  it('should have button after clicking it', () => {
    const testId = 'dropdown-toggle';
    render(actionDots);
    expect(screen.findByTestId(testId))
      .toBeTruthy();
    fireEvent.click(screen.getByTestId(testId));
    expect(screen.findByTestId(testId))
      .toBeTruthy();
  });

  it('should have hidden dropdown-menu', () => {
    render(actionDots);
    expect(screen.queryByTestId('dropdown-toggle'))
      .toBeTruthy();
    expect(screen.getByTestId('dropdown-menu'))
      .toHaveStyle({ display: 'none' });
  });

  it('should have visible dropdown-menu', async () => {
    const testId = 'dropdown-menu';
    const { rerender } = render(actionDots);
    expect(screen.queryByTestId(testId))
      .toHaveStyle({ display: 'none' });
    fireEvent.click(screen.getByTestId(testId));
    rerender(<ActionDots
      id="1"
      actions={actions}
      dropdownClasses="show"
    />);
    // after rerender, previously injected styles are deleted, so I had to include it one more time.
    applyStyles();
    await waitFor(() => {
      expect(screen.queryByTestId(testId))
        .toHaveStyle({ display: 'block' });
    });
  });
});
