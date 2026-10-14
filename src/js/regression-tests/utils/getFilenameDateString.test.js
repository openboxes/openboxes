import { getFilenameDateString } from 'utils/dateUtils';

// Adapted for OBAF: OBAF's getFilenameDateString takes no argument and formats the
// current time, so the clock is set to the date instead.
describe('getFilenameDateString', () => {
  afterEach(() => {
    jest.useRealTimers();
  });

  it.each([
    [new Date(2026, 9, 2, 14, 5, 9), '20261002-140509'],
    [new Date(2026, 0, 5, 9, 7, 3), '20260105-090703'],
    [new Date(2026, 11, 31, 23, 59, 59), '20261231-235959'],
  ])('formats %s as %s, the month counted from 1 and every part zero-padded', (date, expected) => {
    jest.useFakeTimers({ now: date });
    expect(getFilenameDateString()).toBe(expected);
  });
});
