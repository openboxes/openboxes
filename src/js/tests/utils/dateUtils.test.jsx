import * as locales from 'date-fns/locale';

import { DateFormatDateFns } from 'consts/timeFormat';
import {
  displayTimezoneOffset,
  formatApiDateToString,
  formatDateToDateOnlyString,
  formatDateToDatetimeString,
  formatDateToString,
  formatDateToZonedDateTimeString,
  formatStringToInstant,
  formatStringToLocalDate,
  getFilenameDateString,
  parseApiDate,
  parseStringToDate,
} from 'utils/dateUtils';

const DATE_WITH_DAY = new Date(2025, 8, 19);
const DATE_WITH_SECONDS = new Date(2025, 8, 19, 15, 10, 5);

describe('parseStringToDate()', () => {
  it.each([
    ['null', null],
    ['empty string', ''],
    ['Date', DATE_WITH_DAY],
    ['timestamp', 1758153600000],
  ])('should return null if date is (%s)', (_, date) => {
    expect(parseStringToDate({ date, currentDateFormat: DateFormatDateFns.DD_MMM_YYYY }))
      .toBe(null);
  });

  it.each([
    ['not a valid date', 'not a date'],
    ['does not match format', '19/Sep/2025'],
  ])('should throw if date is (%s)', (_, date) => {
    expect(() => parseStringToDate({ date, currentDateFormat: DateFormatDateFns.YYYY_MM_DD }))
      .toThrow('Invalid date string or provided format');
  });

  it('should throw if no date format is given', () => {
    expect(() => parseStringToDate({ date: '18/Sep/2025' }))
      .toThrow('currentDateFormat is required');
  });

  it.each([
    [DateFormatDateFns.DD_MMM_YYYY, '19/Sep/2025'],
    [DateFormatDateFns.YYYY_MM_DD, '2025-09-19'],
  ])('should return correct date if date-only has format (%s)', (format, dateString) => {
    const date = parseStringToDate({ date: dateString, currentDateFormat: format });
    expect(date).toEqual(DATE_WITH_DAY);
    expect(date.getFullYear()).toBe(2025);
    expect(date.getMonth()).toBe(8); // zero-indexed
    expect(date.getDate()).toBe(19);
    expect(date.getHours()).toBe(0);
    expect(date.getMinutes()).toBe(0);
    expect(date.getSeconds()).toBe(0);
  });

  it.each([
    [DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS, '19/Sep/2025 15:10:05'],
    [DateFormatDateFns.YYYY_MM_DD_HH_MM_SS, '2025-09-19T15:10:05'],
  ])('should return correct date if datetime has format (%s)', (format, dateString) => {
    const date = parseStringToDate({ date: dateString, currentDateFormat: format });
    expect(date).toEqual(DATE_WITH_SECONDS);
    expect(date.getFullYear()).toBe(2025);
    expect(date.getMonth()).toBe(8); // zero-indexed
    expect(date.getDate()).toBe(19);
    expect(date.getHours()).toBe(15);
    expect(date.getMinutes()).toBe(10);
    expect(date.getSeconds()).toBe(5);
  });

  it.each([
    ['+00:00', 0],
    ['Z', 0],
    ['+01:00', 1],
    ['-01:00', -1],
  ])('should return correct date if datetime + zone has zone (%s)', (zone, hourOffset) => {
    const expectedHour = 15 - hourOffset;
    const date = parseStringToDate({
      date: `2025-09-19T15:10${zone}`,
      currentDateFormat: DateFormatDateFns.YYYY_MM_DD_HH_MM_Z,
    });
    expect(date.toISOString()).toBe(`2025-09-19T${expectedHour}:10:00.000Z`);

    // Use getUTC* to avoid tests flaking depending on the timezone of the environment
    // that they're running on.
    expect(date.getUTCFullYear()).toBe(2025);
    expect(date.getUTCMonth()).toBe(8); // zero-indexed
    expect(date.getUTCDate()).toBe(19);
    expect(date.getUTCHours()).toBe(expectedHour);
    expect(date.getUTCMinutes()).toBe(10);
    expect(date.getUTCSeconds()).toBe(0);
  });

  it.each([
    [DateFormatDateFns.YYYY_MM_DD, '2025-09-19'],
    [DateFormatDateFns.YYYY_MM_DD_HH_MM_SS, '2025-09-19T15:10:05'],
    [DateFormatDateFns.YYYY_MM_DD_HH_MM_Z, '2025-09-19T00:00+07:00'],
    [DateFormatDateFns.YYYY_MM_DD_HH_MM_Z, '2025-09-19T23:59-07:00'],
    [DateFormatDateFns.YYYY_MM_DD_HH_MM_Z, '2025-09-19T12:00Z'],
  ])('should return date with time and zone stripped if dateOnly and format (%s)', (format, dateString) => {
    const date = parseStringToDate({ date: dateString, currentDateFormat: format, dateOnly: true });
    expect(date).toEqual(DATE_WITH_DAY);
  });
});

describe('formatDateToString()', () => {
  it('should return null if date is empty', () => {
    const nullDate = formatDateToString({
      date: null,
    });
    const emptyStringDate = formatDateToString({
      date: '',
    });
    expect(nullDate).toBe(null);
    expect(emptyStringDate).toBe(null);
  });

  it('should format date properly', () => {
    const date = formatDateToString({
      date: DATE_WITH_DAY,
      dateFormat: DateFormatDateFns.MM_DD_YYYY,
    });
    expect(date).toBe('09/19/2025');
  });

  it('should format date in specified locale properly', () => {
    const esDate = formatDateToString({
      date: DATE_WITH_DAY,
      dateFormat: DateFormatDateFns.MMM_DD_YYYY,
      options: { locale: locales.es },
    });
    const frDate = formatDateToString({
      date: DATE_WITH_DAY,
      dateFormat: DateFormatDateFns.MMM_DD_YYYY,
      options: { locale: locales.fr },
    });
    const plDate = formatDateToString({
      date: DATE_WITH_DAY,
      dateFormat: DateFormatDateFns.MMM_DD_YYYY,
      options: { locale: locales.pl },
    });
    expect(esDate).toBe('sep 19, 2025');
    expect(frDate).toBe('sept. 19, 2025');
    expect(plDate).toBe('wrz 19, 2025');
  });

  it('should format date with time component properly', () => {
    const date = formatDateToString({
      date: DATE_WITH_SECONDS,
      dateFormat: DateFormatDateFns.MMM_DD_YYYY_HH_MM_SS,
    });
    expect(date).toBe('Sep 19, 2025 15:10:05');
  });

  it('should format date with time component in specified locale properly', () => {
    const esDate = formatDateToString({
      date: DATE_WITH_SECONDS,
      dateFormat: DateFormatDateFns.MMM_DD_YYYY_HH_MM_SS,
      options: { locale: locales.es },
    });
    const frDate = formatDateToString({
      date: DATE_WITH_SECONDS,
      dateFormat: DateFormatDateFns.MMM_DD_YYYY_HH_MM_SS,
      options: { locale: locales.fr },
    });
    expect(esDate).toBe('sep 19, 2025 15:10:05');
    expect(frDate).toBe('sept. 19, 2025 15:10:05');
  });
});

describe('formatDateToZonedDateTimeString()', () => {
  it('should return ISO-formatted date', () => {
    const date = formatDateToZonedDateTimeString(DATE_WITH_DAY);
    // examples that should pass the following comparison:
    // 09/19/2025 00:00 +02:00
    // 09/19/2025 00:00 -05:00
    expect(date).toBe(`09/19/2025 00:00 ${displayTimezoneOffset()}`);
  });
});

describe('formatStringToInstant()', () => {
  it('should convert a date string in the given format to an ISO instant', () => {
    const instant = formatStringToInstant(
      '19/Sep/2025 15:10:05',
      DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS,
    );
    // The string holds a local time, so the instant is the same moment in UTC
    expect(instant).toBe(DATE_WITH_SECONDS.toISOString());
  });

  it.each([
    ['null', null],
    ['empty string', ''],
    ['Date', DATE_WITH_DAY],
    ['timestamp', 1758153600000],
  ])('should return null if date is (%s)', (_, date) => {
    expect(formatStringToInstant(date, DateFormatDateFns.DD_MMM_YYYY_HH_MM_SS)).toBe(null);
  });

  it.each([
    ['not a valid date', 'not a date'],
    ['does not match format', '19/Sep/2025'],
  ])('should throw if date is (%s)', (_, date) => {
    expect(() => formatStringToInstant(date, DateFormatDateFns.YYYY_MM_DD))
      .toThrow('Invalid date string or provided format');
  });
});

describe('formatStringToLocalDate()', () => {
  it('should convert a date string in the given format to an ISO date', () => {
    expect(formatStringToLocalDate('19/Sep/2025', DateFormatDateFns.DD_MMM_YYYY))
      .toBe('2025-09-19');
  });

  it.each([
    '+00:00',
    'Z',
    '+01:00',
    '-01:00',
  ])('should not shift the day when the string has zone(%s)', (zone) => {
    expect(formatStringToLocalDate(`2025-09-19T00:00${zone}`, DateFormatDateFns.YYYY_MM_DD_HH_MM_Z))
      .toBe('2025-09-19');
  });

  it.each([
    ['null', null],
    ['empty string', ''],
    ['Date', DATE_WITH_DAY],
    ['timestamp', 1758153600000],
  ])('should return null if date is (%s)', (_, date) => {
    expect(formatStringToLocalDate(date, DateFormatDateFns.DD_MMM_YYYY)).toBe(null);
  });
});

describe('formatDateToDatetimeString()', () => {
  it('should return date string without timezone', () => {
    const date = formatDateToDatetimeString(DATE_WITH_DAY);

    expect(date).toBe('19/Sep/2025');
  });

  it('should return date in proper locale', () => {
    const esDate = formatDateToDatetimeString(
      DATE_WITH_DAY,
      locales.es,
    );

    const frDate = formatDateToDatetimeString(
      DATE_WITH_DAY,
      locales.fr,
    );

    expect(esDate).toBe('19/sep/2025');
    expect(frDate).toBe('19/sept./2025');
  });

  it('should return date-only when date with time component is passed', () => {
    const date = formatDateToDatetimeString(
      DATE_WITH_SECONDS,
    );

    expect(date).toBe('19/Sep/2025');
  });
});

describe('formatDateToDateOnlyString()', () => {
  it('should parse date correctly', () => {
    const date = formatDateToDateOnlyString(DATE_WITH_DAY);
    expect(date).toBe('19/Sep/2025');
  });

  it('should parse datetime correctly', () => {
    const formattedDate = formatDateToDateOnlyString(DATE_WITH_SECONDS);
    expect(formattedDate).toBe('19/Sep/2025');
  });

  it('should return date in proper locale', () => {
    const esDate = formatDateToDateOnlyString(
      DATE_WITH_DAY,
      locales.es,
    );

    const frDate = formatDateToDateOnlyString(
      DATE_WITH_DAY,
      locales.fr,
    );

    expect(esDate).toBe('19/sep/2025');
    expect(frDate).toBe('19/sept./2025');
  });

  describe('displayTimezoneOffset()', () => {
    it('should return offset behind UTC', () => {
      const offset = displayTimezoneOffset(-120);
      expect(offset).toBe('+02:00');
    });

    it('should return offset after UTC', () => {
      const offset = displayTimezoneOffset(180);
      expect(offset).toBe('-03:00');
    });

    it('should return offset with minutes greater than 0', () => {
      const offset = displayTimezoneOffset(-150);
      expect(offset).toBe('+02:30');
    });

    it('should return 0 offset', () => {
      const offset = displayTimezoneOffset(0);
      expect(offset).toBe('Z');
    });
  });
});

describe('getFilenameDateString()', () => {
  it('returns exact formatted string for a fixed date', () => {
    jest.useFakeTimers('modern');
    jest.setSystemTime(new Date(2025, 8, 19, 15, 10, 5));

    const filename = getFilenameDateString();
    expect(filename).toBe('20250919-151005');

    jest.useRealTimers();
  });

  it('returns a string matching YYYYMMDD_HHMMSS pattern', () => {
    const filename = getFilenameDateString();
    expect(filename).toMatch(/^\d{8}-\d{6}$/);
  });
});

describe('parseApiDate()', () => {
  it('should return null if date is empty', () => {
    expect(parseApiDate(null)).toBe(null);
    expect(parseApiDate('')).toBe(null);
  });

  it('should return null if the value is not a date the API could have sent', () => {
    expect(parseApiDate('19/Sep/2026')).toBe(null);
    expect(parseApiDate('not a date')).toBe(null);
  });

  it('should return null rather than throw for a value that is not a string at all', () => {
    expect(parseApiDate(new Date(2026, 8, 19))).toBe(null);
    expect(parseApiDate(1789000000000)).toBe(null);
  });

  it('should parse a date-only string to local midnight, so the day never shifts west of UTC', () => {
    const date = parseApiDate('2026-09-19');

    expect(date.getFullYear()).toBe(2026);
    expect(date.getMonth()).toBe(8);
    expect(date.getDate()).toBe(19);
    expect(date.getHours()).toBe(0);
  });
});

describe('formatApiDateToString()', () => {
  it('should return null if date is empty', () => {
    const date = formatApiDateToString({
      date: null,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    });

    expect(date).toBe(null);
  });

  it('should format a date-only string without shifting the day', () => {
    const date = formatApiDateToString({
      date: '2026-09-19',
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    });

    expect(date).toBe('19/Sep/2026');
  });

  it('should round-trip a date-only string back to the format the API sent', () => {
    const date = formatApiDateToString({
      date: '2026-09-19',
      dateFormat: DateFormatDateFns.YYYY_MM_DD,
    });

    expect(date).toBe('2026-09-19');
  });

  it('should format in the given locale', () => {
    const date = formatApiDateToString({
      date: '2026-09-19',
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
      options: { locale: locales.es },
    });

    expect(date).toBe('19/sep/2026');
  });
});
