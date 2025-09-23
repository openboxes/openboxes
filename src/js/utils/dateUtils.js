import {
  format, isValid, parse, parseISO,
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
 * Converts an ISO date string to a Date object or a "local date"
 * @param {Object} params
 * @param {string} params.date - ISO date string (e.g. '2025-09-18' or '2025-09-18T14:30:00')
 * @param {Boolean} params.localDate - if true, returns the full date-time; if false, returns
 * date with time zeroed
 * @param {Object} options
 * @param {String} params.options.providedFormat - format of the passed date
 * @returns {Date | null}
 */
export const parseStringToDate = ({
  date,
  localDate = true,
  options = {
    providedDateFormat: DateFormatDateFns.MMM_DD_YYYY,
  },
}) => {
  if (!date) {
    return null;
  }

  if (!localDate && !options.providedDateFormat) {
    throw new Error('ProvidedDateFormat is required when localDate is set to false');
  }

  // If localDate is set to true, we are converting the date to the date object with the appropriate
  // timezone. In another case, we need to pass the provided date format and strip the timezone
  // information to avoid unexpected date changes.
  const parsedDate = localDate
    ? parseISO(date)
    : parseDateAndStripTimezone(date, options.providedDateFormat);

  if (!isValid(parsedDate)) {
    throw new Error('Invalid ISO date string or provided format');
  }

  // Date object is automatically converted to the user's browser timezone, so the user doesn't have
  // to set it on their own
  return localDate
    ? parsedDate
    : new Date(
      parsedDate.getFullYear(),
      parsedDate.getMonth(),
      parsedDate.getDate(),
    );
};

/**
 * Converts an ISO date string to a Date object or a "local date"
 * @param {Object} params
 * @param {Date} params.date - date object
 * @param {String} params.dateFormat - desired date format
 * @param {String} options.locale - desired locale
 * @returns {Date | null}
 */
export const parseDateToString = ({
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
export const formatDateToZonedDateTimeString = (date) => parseDateToString({
  date,
  dateFormat: DateFormatDateFns.MM_DD_YYYY_HH_MM_Z,
});

/**
 * A method for converting Date to a localized date string for display (shifted time by timezone
 * differences)
 */
export const formatDateToLocalDateString = (date, locale = locales.enUS) => parseDateToString({
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
  const dateStringWithTimezone = parseDateToString({
    date,
    dateFormat: DateFormatDateFns.MM_DD_YYYY_HH_MM_Z,
  });

  const dateWithoutTimezone = parseStringToDate({
    date: dateStringWithTimezone,
    localDate: false,
    options: {
      providedDateFormat: DateFormatDateFns.MM_DD_YYYY_HH_MM_Z,
    },
  });

  return parseDateToString({
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

export default dateWithoutTimeZone;
