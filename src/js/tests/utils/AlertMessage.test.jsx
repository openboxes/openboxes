import React from 'react';

import { render, screen, waitFor } from '@testing-library/react';
import renderer from 'react-test-renderer';

import AlertMessage from 'utils/AlertMessage';

import '@testing-library/jest-dom';

let alertMessage;

describe('alert message', () => {
  beforeAll(() => {
    alertMessage = (<AlertMessage message="test message" show />);
  });

  it('should match snapshot', () => {
    const renderedAlertMessage = renderer.create(alertMessage);
    expect(renderedAlertMessage.toJSON()).toMatchSnapshot();
  });

  it('should render properly', () => {
    render(alertMessage);
    expect(screen.getByTestId('alert-message')).toBeInTheDocument();
  });

  it('should not be displayed when show is set to false', async () => {
    const { rerender } = render(alertMessage);
    expect(screen.getByTestId('alert-message')).toBeInTheDocument();
    await waitFor(() => rerender(<AlertMessage message="test message" show={false} />));
    expect(screen.queryByTestId('alert-message')).not.toBeInTheDocument();
  });

  it('should display alert message', () => {
    render(alertMessage);
    expect(screen.findByText('test message')).toBeTruthy();
  });
});
