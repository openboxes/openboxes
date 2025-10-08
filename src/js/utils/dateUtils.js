import {
  format,
  getDate,
  getMonth,
  getYear,
  isValid,
  parse,
  parseISO,
} from 'date-fns';
import * as locales from 'date-fns/locale';
import moment from 'moment';

import { DateFormat, DateFormatDateFns } from 'consts/timeFormat';

const dateWithoutTimeZone = ({
  date,
  currentDateFormat,
  outputDateFormat = DateFormat.MM_DD_YYYY,
}) => {
  if (!date) {
    return null;
  }

  const parsedDate = currentDateFormat
    ? moment(date, currentDateFormat).utcOffset(0, true)
    : moment(date).utcOffset(0, true);
  return parsedDate.format(outputDateFormat);
};

export const parseDateAndStripTimezone = (date, providedFormat) => {
  const dateWithoutOffset = date.replace(/([+-]\d{2}:\d{2}|Z)$/, '');
  const formatWithoutOffset = providedFormat.replace('XXX', '');
  return parse(dateWithoutOffset, formatWithoutOffset, new Date());
};

/**
 * Converts an ISO date string to a Date object or a date-only Date without timezone information
 * @param {Object} params
 * @param {Object} params.options
 * @param {string} params.date - ISO date string (e.g. '2025-09-18' or '2025-09-18T14:30:00')
 * @param {Boolean} params.dateOnly - if true, returns the full date-time; if false, returns
 * date with time zeroed
 * @param {String} params.options.providedFormat - format of the passed date
 * @returns {Date | null}
 */
export const parseStringToDate = ({
  date,
  dateOnly = false,
  options = {
    providedDateFormat: DateFormatDateFns.MMM_DD_YYYY,
  },
}) => {
  if (!date) {
    return null;
  }

  if (dateOnly && !options.providedDateFormat) {
    throw new Error('ProvidedDateFormat is required when dateOnly is set to true');
  }

  // If dateOnly is set to true, we are converting the date to the date object with the appropriate
  // timezone. In another case, we need to pass the provided date format and strip the timezone
  // information to avoid unexpected date changes.
  const parsedDate = dateOnly
    ? parseDateAndStripTimezone(date, options.providedDateFormat)
    : parseISO(date);

  if (!isValid(parsedDate)) {
    throw new Error('Invalid ISO date string or provided format');
  }

  // Date object is automatically converted to the user's browser timezone, so the user doesn't have
  // to set it on their own
  return dateOnly
    ? new Date(
      parsedDate.getFullYear(),
      parsedDate.getMonth(),
      parsedDate.getDate(),
    )
    : parsedDate;
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

export default dateWithoutTimeZone;
