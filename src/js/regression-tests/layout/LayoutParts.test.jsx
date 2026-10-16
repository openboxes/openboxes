import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import Alert from 'react-s-alert';
import testStore from 'regression-tests/data-table/testStore';

import notification from 'components/Layout/notifications/notification';
import Section from 'components/Layout/v2/Section';
import Subsection from 'components/Layout/v2/Subsection';
import VerticalTabs from 'components/Layout/VerticalTabs';
import NotificationType from 'consts/notificationTypes';

import '@testing-library/jest-dom';

// The real client imports the store, which needs the reducers this test is still loading.
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());

jest.mock('react-s-alert', () => ({
  success: jest.fn(), warning: jest.fn(), error: jest.fn(), info: jest.fn(),
}));

const withStore = (ui) => <Provider store={testStore()}>{ui}</Provider>;

describe('notification', () => {
  afterEach(() => jest.clearAllMocks());

  it.each([
    [NotificationType.SUCCESS, 'success'],
    [NotificationType.WARNING, 'warning'],
    [NotificationType.ERROR_OUTLINED, 'error'],
    [NotificationType.ERROR_FILLED, 'error'],
    [NotificationType.INFO, 'info'],
    [undefined, 'info'],
  ])('shows a %s notification as an alert of type %s with a default icon', (type, method) => {
    notification(type)({ message: 'Saved', details: 'All lines', detailsArray: ['a'] });

    expect(Alert[method]).toHaveBeenCalledTimes(1);
    const [message, { customFields }] = Alert[method].mock.calls[0];
    expect(message).toBe('Saved');
    expect(customFields.details).toBe('All lines');
    expect(customFields.detailsArray).toEqual(['a']);
    expect(React.isValidElement(customFields.icon)).toBe(true);
  });

  it('keeps the icon it is given', () => {
    const icon = <span>custom</span>;
    notification(NotificationType.ERROR_FILLED)({ message: 'Failed', icon });

    expect(Alert.error.mock.calls[0][1].customFields.icon).toBe(icon);
  });

  it('tells the two error styles apart by their default icon', () => {
    notification(NotificationType.ERROR_OUTLINED)({ message: 'One' });
    notification(NotificationType.ERROR_FILLED)({ message: 'Two' });

    const [outlined, filled] = Alert.error.mock.calls
      .map(([, { customFields }]) => customFields.icon.type);
    expect(outlined).not.toBe(filled);
  });
});

describe('VerticalTabs', () => {
  it('shows no content until a tab is chosen, then the chosen tab', () => {
    const tabs = { First: <p>First content</p>, Second: <p>Second content</p> };
    render(withStore(<VerticalTabs tabs={tabs} />));

    expect(screen.queryByText('First content')).toBeNull();
    fireEvent.click(screen.getByText('Second'));
    expect(screen.getByText('Second content')).toBeInTheDocument();
    expect(screen.getByText('Second').closest('[role="button"]')).toHaveClass('active');
    expect(screen.getByText('First').closest('[role="button"]')).not.toHaveClass('active');

    fireEvent.keyPress(screen.getByText('First'), { key: 'Enter', charCode: 13 });
    expect(screen.getByText('First content')).toBeInTheDocument();
  });

  it('opens the first tab when the set of tabs changes', () => {
    const { rerender } = render(withStore(<VerticalTabs tabs={{ Only: <p>Only content</p> }} />));

    rerender(withStore(
      <VerticalTabs tabs={{ New: <p>New content</p>, Other: <p>Other content</p> }} />,
    ));
    expect(screen.getByText('New content')).toBeInTheDocument();
  });
});

describe('Section', () => {
  it('shows the translated title above the content', () => {
    render(withStore(
      <Section title={{ label: 'react.section.title', defaultMessage: 'Details' }} className="extra">
        <p>Body</p>
      </Section>,
    ));

    expect(screen.getByText('Details')).toHaveClass('v2-section-title');
    expect(screen.getByText('Body').parentElement).toHaveClass('v2-section', 'extra');
  });

  it('can leave the title out', () => {
    render(withStore(
      <Section showTitle={false} title={{ label: 'react.section.title', defaultMessage: 'Details' }}>
        <p>Body</p>
      </Section>,
    ));

    expect(screen.queryByText('Details')).toBeNull();
  });
});

describe('Subsection', () => {
  const body = () => screen.getByText('Body').parentElement;

  it('collapses and expands on click and on key press when collapsable', () => {
    render(withStore(
      <Subsection title={{ label: 'react.sub.title', defaultMessage: 'Pricing' }}>
        <p>Body</p>
      </Subsection>,
    ));

    expect(body()).toHaveClass('subsection-body-expanded');
    fireEvent.click(screen.getByRole('button'));
    expect(body()).not.toHaveClass('subsection-body-expanded');
    fireEvent.keyDown(screen.getByRole('button'), { key: 'Enter' });
    expect(body()).toHaveClass('subsection-body-expanded');
    expect(screen.getByText('Pricing')).toBeInTheDocument();
  });

  // Adapted for OBAF (OBAF lacks the code): OBAF's Subsection takes only a
  // { label, defaultMessage } title, so the element-title assertion is left out.
  it('starts collapsed when asked, and accepts an element as the title', () => {
    render(withStore(
      <Subsection title={{ label: 'react.sub.title', defaultMessage: 'Custom title' }} expandedByDefault={false}>
        <p>Body</p>
      </Subsection>,
    ));

    expect(screen.getByText('Custom title')).toBeInTheDocument();
    expect(body()).not.toHaveClass('subsection-body-expanded');
  });

  it('stays as it is when not collapsable, and renders no title without a message', () => {
    render(withStore(
      <Subsection title={{ label: 'react.sub.title', defaultMessage: '' }} collapsable={false}>
        <p>Body</p>
      </Subsection>,
    ));

    fireEvent.click(screen.getByRole('button'));
    expect(body()).toHaveClass('subsection-body-expanded');
    expect(screen.getByRole('button')).toHaveTextContent('');
  });
});
