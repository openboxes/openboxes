import React from 'react';

import { render, screen, within } from '@testing-library/react';

import Badge from 'utils/Badge';

import '@testing-library/jest-dom';

describe('Badge component', () => {
  it('matches snapshot', () => {
    const { asFragment } = render(<Badge label="Snapshot Test" variant="warning" tooltip />);
    expect(asFragment()).toMatchSnapshot();
  });

  it('renders the label text', () => {
    render(<Badge label="Test Label" variant="primary" />);
    const { getByText } = within(screen.getByTestId('badge'));
    expect(getByText('Test Label')).toBeInTheDocument();
  });

  it('applies the correct variant class', () => {
    render(<Badge label="Info" variant="info" />);
    const { getByText } = within(screen.getByTestId('badge'));
    expect(getByText('Info')).toHaveClass('badge');
    expect(getByText('Info')).toHaveClass('info');
  });

  it('renders nothing when label prop is undefined', () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    const propsWithoutLabel = {
      variant: 'primary',
      tooltip: undefined,
    };
    render(<Badge {...propsWithoutLabel} />);
    const { queryByTestId } = screen;
    expect(queryByTestId('badge')).not.toBeInTheDocument();
    consoleSpy.mockRestore();
  });

  it('applies only base class when variant is empty string', () => {
    render(<Badge label="Empty Variant" variant="" />);
    const { getByText } = within(screen.getByTestId('badge'));
    expect(getByText('Empty Variant')).toHaveClass('badge');
    expect(getByText('Empty Variant')).not.toHaveClass('primary', 'success', 'info', 'warning', 'danger');
  });
});
