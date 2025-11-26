import * as locales from 'date-fns/locale';

import { DateFormatDateFns } from 'consts/timeFormat';
import {
  displayTimezoneOffset,
  formatDateToDateOnlyString,
  formatDateToDatetimeString,
  formatDateToString,
  formatDateToZonedDateTimeString, getTimezone, getTimezoneOffset, getUserTimezone,
  parseStringToDate,
} from 'utils/dateUtils';

const DATE_WITH_DAY = new Date(2025, 8, 19);
const DATE_WITH_SECONDS = new Date(2025, 8, 19, 15, 10, 5);

describe('parseStringToDate()', () => {
  it('should return null if date is empty', () => {
    const nullDate = parseStringToDate({
      date: null,
      dateOnly: true,
    });
    const emptyStringDate = parseStringToDate({
      date: '',
      dateOnly: true,
    });
    expect(nullDate).toBe(null);
    expect(emptyStringDate).toBe(null);
  });

  it('should return parsed date without time when date without time is passed', () => {
    const date = parseStringToDate({
      date: '2025-09-18',
      dateOnly: true,
      options: {
        providedDateFormat: DateFormatDateFns.YYYY_MM_DD,
      },
    });
    expect(date.getFullYear()).toBe(2025);
    expect(date.getMonth()).toBe(8);
    expect(date.getDate()).toBe(18);
    expect(date.getHours()).toBe(0);
    expect(date.getMinutes()).toBe(0);
    expect(date.getSeconds()).toBe(0);
  });

  it('should return parsed date without time when date with time is passed', () => {
    const date = parseStringToDate({
      date: '2025-09-18T14:30:15',
      dateOnly: true,
      options: {
        providedDateFormat: DateFormatDateFns.YYYY_MM_DD_HH_MM_SS,
      },
    });
    expect(date.getFullYear()).toBe(2025);
    expect(date.getMonth()).toBe(8);
    expect(date.getDate()).toBe(18);
    expect(date.getHours()).toBe(0);
    expect(date.getMinutes()).toBe(0);
    expect(date.getSeconds()).toBe(0);
  });

  it('should return parsed date with time when date without time is passed', () => {
    const date = parseStringToDate({
      date: '2025-09-18',
    });
    expect(date.getFullYear()).toBe(2025);
    expect(date.getMonth()).toBe(8);
    expect(date.getDate()).toBe(18);
    expect(date.getHours()).toBe(0);
    expect(date.getMinutes()).toBe(0);
    expect(date.getSeconds()).toBe(0);
  });

  it('should return parsed date with time when date with time is passed', () => {
    const date = parseStringToDate({
      date: '2025-09-18T14:30:15',
    });
    expect(date.getFullYear()).toBe(2025);
    expect(date.getMonth()).toBe(8);
    expect(date.getDate()).toBe(18);
    expect(date.getHours()).toBe(14);
    expect(date.getMinutes()).toBe(30);
    expect(date.getSeconds()).toBe(15);
  });

  it('should return the same date when timezone is ahead of UTC (stripping timezone)', () => {
    const date = parseStringToDate({
      date: '2025-01-01T00:00+07:00',
      dateOnly: true,
      options: {
        providedDateFormat: DateFormatDateFns.YYYY_MM_DD_HH_MM_Z,
      },
    });
    expect(date.getFullYear()).toBe(2025);
    expect(date.getMonth()).toBe(0);
    expect(date.getDate()).toBe(1);
    expect(date.getHours()).toBe(0);
    expect(date.getMinutes()).toBe(0);
    expect(date.getSeconds()).toBe(0);
  });

  it('should return the appropriate date when timezone is ahead of UTC (with timezone)', () => {
    const date = parseStringToDate({
      date: '2025-01-01T00:00+07:00',
    });
    expect(date.getUTCFullYear()).toBe(2024);
    expect(date.getUTCMonth()).toBe(11);
    expect(date.getUTCDate()).toBe(31);
    expect(date.getUTCHours()).toBe(17);
    expect(date.getUTCMinutes()).toBe(0);
    expect(date.getUTCSeconds()).toBe(0);
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

  it('should skip timezone', () => {
    const date = parseStringToDate({
      date: '2025-09-19T1:22+07:00',
      dateOnly: true,
      options: {
        providedDateFormat: DateFormatDateFns.YYYY_MM_DD_HH_MM_Z,
      },
    });
    const formattedDate = formatDateToDateOnlyString(date);
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
