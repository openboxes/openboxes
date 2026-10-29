import React from 'react';

import { render, screen } from '@testing-library/react';

import ExpirationDateCell from 'utils/cells/ExpirationDateCell';

import '@testing-library/jest-dom';

jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);

// Both the rendered date and the badge are read west of UTC, where a date-only string formatted
// as-is lands on the previous day. The badge is relative to today, so the clock is fixed too.
const TODAY = new Date(2026, 8, 11);

const renderCell = (value) => render(
  <ExpirationDateCell
    value={value}
    label="react.receiving.expirationDate.short.label"
    defaultLabel="Exp Date"
    showExpiryStatus
  />,
);

const badgeText = () => document.querySelector('.expiry-badge')?.textContent ?? null;

describe('ExpirationDateCell', () => {
  beforeEach(() => {
    jest.useFakeTimers('modern');
    jest.setSystemTime(TODAY);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('should render the date the API sent', () => {
    renderCell('2026-09-19');

    expect(screen.getByText('19/Sep/2026')).toBeInTheDocument();
  });

  it('should render nothing for a lot with no expiration date', () => {
    renderCell(null);

    expect(document.querySelector('.expiration-cell__date').textContent).toBe('');
    expect(badgeText()).toBe(null);
  });

  it('should not call a lot expiring today expired', () => {
    renderCell('2026-09-11');

    expect(badgeText()).toBe('Expiring');
  });

  it('should call a lot that expired yesterday expired', () => {
    renderCell('2026-09-10');

    expect(badgeText()).toBe('Expired');
  });

  it('should call a lot expiring in 90 days expiring', () => {
    renderCell('2026-12-10');

    expect(badgeText()).toBe('Expiring');
  });

  it('should show no badge for a lot expiring in 91 days', () => {
    renderCell('2026-12-11');

    expect(badgeText()).toBe(null);
  });
});
