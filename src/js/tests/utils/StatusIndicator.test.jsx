import React from 'react';

import { render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import StatusIndicator from 'utils/StatusIndicator';

import '@testing-library/jest-dom';

let statusIndicator;

describe('status indicator', () => {
  beforeEach(() => {
    statusIndicator = (<StatusIndicator variant="test" status="test_status_to_display" />);
  });

  it('should match snapshot', () => {
    const renderedStatusIndicator = renderer.create(statusIndicator);
    expect(renderedStatusIndicator.toJSON()).toMatchSnapshot();
  });

  it('should render properly', () => {
    render(statusIndicator);
    expect(screen.getByTestId('status-indicator')).toBeInTheDocument();
  });

  it('should map status', () => {
    render(statusIndicator);
    expect(screen.getByText('test status to display')).toBeInTheDocument();
  });
});
