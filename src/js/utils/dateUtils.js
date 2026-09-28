import {
  format,
  getDate,
  getMonth,
  getYear,
  isAfter,
  isValid,
  parse,
  parseISO,
  startOfDay,
} from 'date-fns';
import * as locales from 'date-fns/locale';
import moment from 'moment';

import { DateFormat, DateFormatDateFns } from 'consts/timeFormat';

/**
 * @deprecated This method uses moment.js which is deprecated in favor of date-fns.
 */
const dateWithoutTimeZone = ({
  date,
  currentDateFormat,
  outputDateFormat = DateFormat.MM_DD_YYYY,
  locale = 'en',
}) => {
  if (!date) {
    return null;
  }

  const parsedDate = currentDateFormat
    ? moment(date, currentDateFormat, locale).utcOffset(0, true)
    : moment(date).utcOffset(0, true);
  return parsedDate.format(outputDateFormat);
};

/**
 * Removes the timezone offset from a date string.
 * For example: '01/Jan/2000 00:00:00+07:00' becomes '01/Jan/2000 00:00:00'
 *
 * @param {string} date - The date string to check. Ex: '01/Jan/2000 00:00:00+07:00'
 * @return {string} the date string without the timezone offset
 */
const stripTimezoneFromDateString = (date) => date.replace(/([+-]\d{2}:\d{2}|Z)$/, '');

/**
 * Removes the timezone offset from a date format.
 * For example: 'dd/MMM/yyyy HH:mm:ssXXX' becomes 'dd/MMM/yyyy HH:mm:ss'
 *
 * @param {string} dateFormat - The date format to check. Ex: 'dd/MMM/yyyy HH:mm:ssXXX'
 * @return {string} the date format without the timezone offset
 */
const stripOffsetFromDateFormat = (dateFormat) => dateFormat.replace('XXX', '');

/**
 * Converts a date string to a Date object.
 *
 * If dateOnly is true, will strip any time and zone information from the string. This functionality
 * exists due to a desync between the frontend and the backend. Some old fields that still use
 * java.util.Date on the backend treat what should be date-only fields as full date + time + zone
 * objects. To avoid off-by-one-day errors when comparing or displaying date-only dates, we strip
 * time and zone from the string.
 *
 * If you're parsing a string from an API response, use {@link #parseApiDate} instead since it
 * more gracefully handles ISO strings.
 *
 * @param params
 * @param {string} params.date - The date string to convert
 * @param {string} params.currentDateFormat - the format the given string is in currently
 * @param {boolean} [params.dateOnly=false] - if true, time + zone will be stripped from the string
 * @returns {Date | null}
 */
export const parseStringToDate = ({
  date,
  currentDateFormat,
  dateOnly = false,
}) => {
  if (typeof date !== 'string' || !date) {
    return null;
  }

  if (!currentDateFormat) {
    throw new Error('currentDateFormat is required');
  }

  // Conditionally strip out timezone offset. See the docstring for details.
  const [dateToParse, formatToParse] = dateOnly
    ? [stripTimezoneFromDateString(date), stripOffsetFromDateFormat(currentDateFormat)]
    : [date, currentDateFormat];

  const parsedDate = parse(dateToParse, formatToParse, new Date());

  if (!isValid(parsedDate)) {
    throw new Error('Invalid date string or provided format');
  }

  // And also conditionally strip out time. We do this after creating the Date object because it
  // is less error-prone than stripping the characters from the given string.
  return dateOnly
    ? new Date(
      parsedDate.getFullYear(),
      parsedDate.getMonth(),
      parsedDate.getDate(),
    )
    : parsedDate;
};

/**
 * Resolves a date-fns locale object for the given locale code.
 * `enUS` is a fallback when the locale is missing or unsupported — the 'ar' locale
 * crashes the date picker, so it is intentionally fallback.
 * @param {String} localeCode - locale code
 * @returns {Locale} date-fns locale object
 */
export const getDateFnsLocale = (localeCode) => {
  if (!localeCode || ['en', 'ar'].includes(localeCode)) {
    return locales.enUS;
  }

  return locales[localeCode] ?? locales.enUS;
};

/**
 * Converts a date to a string in specified format
 * @param {Object} params
 * @param {Object} options
 * @param {Date} params.date - date object
 * @param {String} params.dateFormat - output date format
 * @param {String} options.locale - output locale
 * @returns {String}
 */
export const formatDateToString = ({
  date,
  dateFormat = DateFormatDateFns.MMM_DD_YYYY,
  options = {
    locale: locales.enUS,
  },
}) => {
  if (!date) {
    return null;
  }

  return format(date, dateFormat, {
    locale: options.locale,
  });
};

/**
 A method for converting Date to an ISO-formatted date-time string (for formatting API
 request fields)
 */
export const formatDateToZonedDateTimeString = (date) => formatDateToString({
  date,
  dateFormat: DateFormatDateFns.MM_DD_YYYY_HH_MM_Z,
});

/**
 * Converts a date string into an ISO-formatted datetime string, which is the format that APIs
 * binding a java.time.Instant expect.
 *
 * @param {string} date - the date string to convert
 * @param {string} currentDateFormat - the format the given string is in currently
 * @returns {string|null} An ISO-formatted datetime + zone string. Ex: '2000-01-01T00:00:00Z'
 */
export const formatStringToInstant = (date, currentDateFormat) => {
  const parsedDate = parseStringToDate({ date, currentDateFormat, dateOnly: false });
  return !parsedDate ? null : parsedDate.toISOString();
};

/**
 * Converts a date string into an ISO-formatted date-only string, which is the format that APIs
 * binding a java.time.LocalDate expect.
 *
 * @param {string} date - the date string to convert
 * @param {string} currentDateFormat - the format the given string is in currently
 * @returns {string|null} An ISO-formatted date-only string. Ex: '2000-01-01'
 */
export const formatStringToLocalDate = (date, currentDateFormat) => {
  const parsedDate = parseStringToDate({ date, currentDateFormat, dateOnly: true });
  return !parsedDate ? null : formatDateToString({
    date: parsedDate,
    dateFormat: DateFormatDateFns.YYYY_MM_DD,
  });
};

/**
 * A method for converting Date to a localized date string for display (shifted time by timezone
 * differences)
 */
export const formatDateToDatetimeString = (date, locale = locales.enUS) => formatDateToString({
  date,
  dateFormat: DateFormatDateFns.DD_MMM_YYYY,
  options: {
    locale,
  },
});

/**
 * A method for converting Date to a localized date string for display (skipping timezone)
 */
export const formatDateToDateOnlyString = (date, locale = locales.enUS) => {
  const dateWithoutTimezone = new Date(
    getYear(date),
    getMonth(date),
    getDate(date),
  );

  return formatDateToString({
    date: dateWithoutTimezone,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    options: {
      locale,
    },
  });
};

/**
 * A method for formating ISO date string to date in another format
 */
export const formatISODate = (date, dateFormat) => format(parseISO(date), dateFormat);

/**
 * A method for parsing a date-only string as APIs send it (e.g. '2026-09-19') to a Date.
 * @param {String} date - date-only string in the yyyy-MM-dd format
 * @returns {Date|null} the parsed date, or null when the value is empty or not a valid date
 */
export const parseApiDate = (date) => {
  if (typeof date !== 'string' || !date) {
    return null;
  }

  const parsedDate = parseISO(date);
  return isValid(parsedDate) ? parsedDate : null;
};

/**
 * A method for converting a date-only string as APIs send it (e.g. '2026-09-19') to a string in
 * the given format, without the timezone shift formatting the string directly would introduce.
 * @param {Object} params
 * @param {String} params.date - date-only string in the yyyy-MM-dd format
 * @param {String} params.dateFormat - output date format
 * @param {Object} params.options
 * @param {Locale} params.options.locale - output locale
 * @returns {String|null}
 */
export const formatApiDateToString = ({ date, dateFormat, options }) => formatDateToString({
  date: parseApiDate(date),
  dateFormat,
  options,
});

/**
 * Get timezone offset, defaulting to the user's timezone offset
 * @param {Number} timezoneOffset
 * @returns {string}
 */
export const displayTimezoneOffset = (timezoneOffset = new Date().getTimezoneOffset()) => {
  // timezoneOffset = difference in minutes comparing to utc (timezoneOffset is an argument for
  // testing purposes, because we can't force Date object to use different timezone that the user's)
  if (timezoneOffset === 0) {
    return 'Z';
  }

  const offsetMinutes = -timezoneOffset;
  const sign = offsetMinutes >= 0 ? '+' : '-';

  const absMinutes = Math.abs(offsetMinutes);
  const hours = Math.floor(absMinutes / 60);
  const minutes = absMinutes % 60;

  // parsing hours / minutes to appropriate format: hours: 02, 01, 14, minutes: 02, 01, 59
  const paddedHours = hours.toString().padStart(2, '0');
  const paddedMinutes = minutes.toString().padStart(2, '0');

  return `${sign}${paddedHours}:${paddedMinutes}`;
};

/**
 * Get current date string for filename usage
 * @returns {string}
 */
export const getFilenameDateString = () => format(new Date(), DateFormatDateFns.YYYYMMDD_HHMMSS);

export default dateWithoutTimeZone;

export const validateFutureDateFns = (date) => {
  const today = startOfDay(new Date());
  return !isAfter(startOfDay(date), today);
};
