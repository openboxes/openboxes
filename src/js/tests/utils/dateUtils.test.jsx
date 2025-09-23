import * as locales from 'date-fns/locale';

import { DateFormatDateFns } from 'consts/timeFormat';
import {
  formatDateToDateOnlyString,
  formatDateToLocalDateString,
  formatDateToZonedDateTimeString,
  parseDateToString,
  parseStringToDate,
} from 'utils/dateUtils';

describe('parseStringToDate()', () => {
  it('should return null if date is empty', () => {
    const nullDate = parseStringToDate({
      date: null,
    });
    const emptyStringDate = parseStringToDate({
      date: '',
    });
    expect(nullDate).toBe(null);
    expect(emptyStringDate).toBe(null);
  });

  it('should return parsed date without time when date without time is passed', () => {
    const date = parseStringToDate({
      date: '2025-09-18',
      localDate: false,
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
      localDate: false,
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

  it('should return the same date when timezone is ahead of UTC', () => {
    const date = parseStringToDate({
      date: '2025-01-01T00:00+07:00',
      localDate: false,
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

  it('should return the previous day when timezone is ahead of UTC', () => {
    const date = parseStringToDate({
      date: '2025-01-01T00:00+07:00',
    });
    expect(date.getFullYear()).toBe(2024);
    expect(date.getMonth()).toBe(11);
    expect(date.getDate()).toBe(31);
    expect(date.getHours()).toBe(18);
    expect(date.getMinutes()).toBe(0);
    expect(date.getSeconds()).toBe(0);
  });
});

describe('parseDateToString()', () => {
  it('should return null if date is empty', () => {
    const nullDate = parseDateToString({
      date: null,
    });
    const emptyStringDate = parseDateToString({
      date: '',
    });
    expect(nullDate).toBe(null);
    expect(emptyStringDate).toBe(null);
  });

  it('should format date properly', () => {
    const date = parseDateToString({
      date: new Date(2025, 8, 19),
      dateFormat: DateFormatDateFns.MM_DD_YYYY,
    });
    expect(date).toBe('09/19/2025');
  });

  it('should format date properly', () => {
    const date = parseDateToString({
      date: new Date(2025, 8, 19),
      dateFormat: DateFormatDateFns.MM_DD_YYYY,
    });
    expect(date).toBe('09/19/2025');
  });

  it('should format date in specified locale properly', () => {
    const esDate = parseDateToString({
      date: new Date(2025, 8, 19),
      dateFormat: DateFormatDateFns.MMM_DD_YYYY,
      options: { locale: locales.es },
    });
    const frDate = parseDateToString({
      date: new Date(2025, 8, 19),
      dateFormat: DateFormatDateFns.MMM_DD_YYYY,
      options: { locale: locales.fr },
    });
    const plDate = parseDateToString({
      date: new Date(2025, 8, 19),
      dateFormat: DateFormatDateFns.MMM_DD_YYYY,
      options: { locale: locales.pl },
    });
    expect(esDate).toBe('sep 19, 2025');
    expect(frDate).toBe('sept. 19, 2025');
    expect(plDate).toBe('wrz 19, 2025');
  });
});

describe('formatDateToZonedDateTimeString()', () => {
  it('should return ISO-formatted date', () => {
    const date = formatDateToZonedDateTimeString(
      new Date(2025, 8, 19),
    );
    // examples that should match the following expression:
    // 09/19/2025 00:00 +02:00
    // 09/19/2025 00:00 -05:00
    expect(date).toMatch(/^09\/19\/2025 00:00 [+-]\d{2}:\d{2}$/);
  });
});

describe('formatDateToLocalDateString()', () => {
  it('should return date string without timezone', () => {
    const date = formatDateToLocalDateString(
      new Date(2025, 8, 19),
    );

    expect(date).not.toMatch(/^19\/Sep\/2025 00:00 [+-]\d{2}:\d{2}$/);
    expect(date).toMatch(/^19\/Sep\/2025/);
  });

  it('should return date in proper locale', () => {
    const esDate = formatDateToLocalDateString(
      new Date(2025, 8, 19),
      locales.es,
    );

    const frDate = formatDateToLocalDateString(
      new Date(2025, 8, 19),
      locales.fr,
    );

    expect(esDate).toBe('19/sep/2025');
    expect(frDate).toBe('19/sept./2025');
  });
});

describe('formatDateToDateOnlyString()', () => {
  it('should parse date correctly', () => {
    const date = formatDateToDateOnlyString(new Date(2025, 8, 19));
    expect(date).toBe('19/Sep/2025');
  });

  it('should skip timezone', () => {
    const date = parseStringToDate({
      date: '2025-09-19T1:22+07:00',
      localDate: false,
      options: {
        providedDateFormat: DateFormatDateFns.YYYY_MM_DD_HH_MM_Z,
      },
    });
    const formattedDate = formatDateToDateOnlyString(date);
    expect(formattedDate).toBe('19/Sep/2025');
  });

  it('should return date in proper locale', () => {
    const esDate = formatDateToDateOnlyString(
      new Date(2025, 8, 19),
      locales.es,
    );

    const frDate = formatDateToDateOnlyString(
      new Date(2025, 8, 19),
      locales.fr,
    );

    expect(esDate).toBe('19/sep/2025');
    expect(frDate).toBe('19/sept./2025');
  });
});
