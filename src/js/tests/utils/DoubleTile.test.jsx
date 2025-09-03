import React from 'react';

import { render, screen, within } from '@testing-library/react';

import DoubleTile from 'utils/DoubleTile';

import '@testing-library/jest-dom';
import renderer from 'react-test-renderer';

const mockTranslate = jest.fn((key, defaultValue) => defaultValue);

jest.mock('hooks/useTranslate', () => () => mockTranslate);

jest.mock('wrappers/CustomTooltip', () => ({ children }) => <div>{children}</div>);

describe('DoubleTile component', () => {
  const defaultProps = {
    cardTitle: 'react.doubleTile.title',
    cardTitleDefaultValue: 'Card Title',
    cardFirstValue: 75,
    cardSecondValue: 1500,
    cardFirstSubtitle: 'react.doubleTile.firstSubtitle',
    cardDefaultFirstSubtitle: 'First Subtitle',
    cardSecondSubtitle: 'react.doubleTile.secondSubtitle',
    cardDefaultSecondSubtitle: 'Second Subtitle',
    cardInfo: 'react.doubleTile.info',
    cardInfoDefaultValue: 'Info Text',
    currencyCode: 'USD',
    showFirstValuePercentSign: false,
    formatSecondValueAsCurrency: false,
  };

  it('should match snapshot', () => {
    const doubleTile = renderer.create(<DoubleTile {...defaultProps} />).toJSON();
    expect(doubleTile).toMatchSnapshot();
  });

  it('renders title, first value, second value, and subtitles correctly', () => {
    render(<DoubleTile {...defaultProps} />);
    const { getByText } = within(screen.getByTestId('double-tile'));
    expect(getByText('Card Title')).toBeInTheDocument();
    expect(getByText('75')).toBeInTheDocument();
    expect(getByText('First Subtitle')).toBeInTheDocument();
    expect(getByText('1500')).toBeInTheDocument();
    expect(getByText('Second Subtitle')).toBeInTheDocument();
  });

  it('renders first value with percent sign when showFirstValuePercentSign is true', () => {
    render(<DoubleTile {...defaultProps} showFirstValuePercentSign />);
    const { getByText } = within(screen.getByTestId('double-tile'));
    expect(getByText('75%')).toBeInTheDocument();
  });

  it('formats second value as currency when formatSecondValueAsCurrency is true', () => {
    render(<DoubleTile {...defaultProps} formatSecondValueAsCurrency />);
    const { getByText } = within(screen.getByTestId('double-tile'));
    expect(getByText('1,500.00 USD')).toBeInTheDocument();
  });

  it('formats second value as million when value is >= 1,000,000', () => {
    render(<DoubleTile {...defaultProps} cardSecondValue={1500000} formatSecondValueAsCurrency />);
    const { getByText } = within(screen.getByTestId('double-tile'));
    expect(getByText('1.500 million USD')).toBeInTheDocument();
  });

  it('uses translate function for title, subtitles, and tooltip', () => {
    render(<DoubleTile {...defaultProps} />);
    expect(mockTranslate).toHaveBeenCalledWith('react.doubleTile.title', 'Card Title');
    expect(mockTranslate).toHaveBeenCalledWith('react.doubleTile.firstSubtitle', 'First Subtitle');
    expect(mockTranslate).toHaveBeenCalledWith('react.doubleTile.secondSubtitle', 'Second Subtitle');
    expect(mockTranslate).toHaveBeenCalledWith('react.doubleTile.info', 'Info Text');
  });

  it('renders correctly when optional props are undefined', () => {
    const propsWithOptionalDefaults = {
      ...defaultProps,
      currencyCode: undefined,
      showFirstValuePercentSign: undefined,
      formatSecondValueAsCurrency: undefined,
    };
    render(<DoubleTile {...propsWithOptionalDefaults} />);
    const { getByText, queryByText } = within(screen.getByTestId('double-tile'));
    expect(getByText('75')).toBeInTheDocument();
    expect(getByText('1500')).toBeInTheDocument();
    expect(queryByText('1,500.00 USD')).not.toBeInTheDocument();
  });
});
