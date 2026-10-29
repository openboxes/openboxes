import {
  format, getYear, isValid, parse,
} from 'date-fns';

import { DateFormatDateFns } from 'consts/timeFormat';

// Formats accepted when the user types a date by hand, ordered by preference - the first
// one that parses wins, so an ambiguous date like 10/12/2026 is read as month/day (Oct 12).
// Each yyyy format goes before its yy counterpart: a two digit year parsed as yyyy (e.g. 26
// becomes year 0026) is rejected by MIN_YEAR, so it falls through to the yy format (2026).
const TYPED_DATE_FORMATS = [
  // Preferred: month/day/year, e.g. 10/12/2026 or 10/12/26
  DateFormatDateFns.MM_DD_YYYY,
  DateFormatDateFns.MM_DD_YY,
  // Fallback for dates that can't be month/day, e.g. 25/12/2026 or 25/12/26
  DateFormatDateFns.DD_MM_YYYY,
  DateFormatDateFns.DD_MM_YY,
  // The format the field displays, e.g. 12/Oct/2026 (month name in the current locale)
  DateFormatDateFns.DD_MMM_YYYY,
];

const MIN_YEAR = 2000;

/**
 * Parses the full text typed into the expiration date input. Returns the date formatted
 * as DD/MMM/YYYY, or null when the text isn't a date, so that it gets erased.
 */
const parseTypedExpirationDate = (text, locale) => {
  const parsedDate = TYPED_DATE_FORMATS
    .map((dateFormat) => parse(text.trim(), dateFormat, new Date(), { locale }))
    .find((date) => isValid(date) && getYear(date) >= MIN_YEAR);

  return parsedDate ? format(parsedDate, DateFormatDateFns.DD_MMM_YYYY) : null;
};

export default parseTypedExpirationDate;
