import React from 'react';

import { render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import Badge from 'utils/Badge';

import '@testing-library/jest-dom';

const mockCustomTooltip = jest.fn(({ children }) => <>{children}</>);

jest.mock('wrappers/CustomTooltip', () => (props) => mockCustomTooltip(props));

describe('Badge component', () => {
  it('should match snapshot', () => {
    const badge = renderer.create(<Badge label="Tooltip Badge" variant="warning" tooltip />).toJSON();
    expect(badge).toMatchSnapshot();
  });

  it('renders the label text', () => {
    render(<Badge label="Primary Badge" variant="primary" />);
    expect(screen.getByText('Primary Badge')).toBeInTheDocument();
  });

  it('applies the correct variant class', () => {
    render(<Badge label="Primary Badge" variant="primary" />);
    const badgeElement = screen.getByTestId('badge');
    expect(badgeElement).toHaveClass('badge', 'primary');
  });

  it('renders nothing when label prop is undefined', () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    render(<Badge variant="primary" />);
    expect(screen.queryByTestId('badge')).not.toBeInTheDocument();
    consoleSpy.mockRestore();
  });

  it('applies only base class when variant is empty string', () => {
    render(<Badge label="Empty Variant" variant="" />);
    const badgeElement = screen.getByTestId('badge');
    expect(badgeElement).toHaveClass('badge');
    expect(badgeElement).not.toHaveClass('primary', 'success', 'info', 'warning', 'danger');
  });

  it('passes correct props to CustomTooltip when tooltip is enabled', () => {
    render(<Badge label="Tooltip Badge" variant="warning" tooltip />);
    expect(mockCustomTooltip).toHaveBeenCalledWith(
      expect.objectContaining({
        content: 'Tooltip Badge',
        show: true,
        children: expect.any(Object),
      }),
    );
  });
});
