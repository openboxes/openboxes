import React from 'react';

import { render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import Badge from 'utils/Badge';

import '@testing-library/jest-dom';

jest.mock('wrappers/CustomTooltip', () => ({ children }) => <>{children}</>);

let badgePrimary;
let badgeWithTooltip;
let badgeEmptyVariant;

describe('Badge component', () => {
  beforeEach(() => {
    badgePrimary = <Badge label="Primary Badge" variant="primary" />;
    badgeWithTooltip = <Badge label="Tooltip Badge" variant="warning" tooltip />;
    badgeEmptyVariant = <Badge label="Empty Variant" variant="" />;
  });

  it('should match snapshot', () => {
    const badge = renderer.create(badgeWithTooltip).toJSON();
    expect(badge).toMatchSnapshot();
  });

  it('renders the label text', () => {
    render(badgePrimary);
    expect(screen.getByText('Primary Badge')).toBeInTheDocument();
  });

  it('applies the correct variant class', () => {
    render(badgePrimary);
    const badgeElement = screen.getByTestId('badge');
    expect(badgeElement).toHaveClass('badge');
    expect(badgeElement).toHaveClass('primary');
  });

  it('renders nothing when label prop is undefined', () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    render(<Badge variant="primary" />);
    expect(screen.queryByTestId('badge')).not.toBeInTheDocument();
    consoleSpy.mockRestore();
  });

  it('applies only base class when variant is empty string', () => {
    render(badgeEmptyVariant);
    const badgeElement = screen.getByTestId('badge');
    expect(badgeElement).toHaveClass('badge');
    expect(badgeElement).not.toHaveClass('primary', 'success', 'info', 'warning', 'danger');
  });
});
