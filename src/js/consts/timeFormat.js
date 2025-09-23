export const TimeFormat = {
  HH_MM: 'HH:mm',
  HH_MM_SS: 'HH:mm:ss',
};

export const DateFormat = {
  MM_DD_YYYY: 'MM/DD/yyyy',
  DD_MMM_YYYY: 'DD/MMM/yyyy',
  MMM_DD_YYYY: 'MMM DD, yyyy',
  MMM_DD_YYYY_HH_MM_SS: 'MMM DD, YYYY HH:mm:ss',
  MM_DD_YYYY_HH_MM_Z: 'MM/DD/yyyy HH:mm Z',
};

// date-fns library uses different date formats than moment.js
// see: https://github.com/date-fns/date-fns/blob/main/docs/unicodeTokens.md
export const DateFormatDateFns = {
  MM_DD_YYYY: 'MM/dd/yyyy',
  DD_MMM_YYYY: 'dd/MMM/yyyy',
  MMM_DD_YYYY: 'MMM dd, yyyy',
  MMM_DD_YYYY_HH_MM_SS: 'MMM dd, yyyy HH:mm:ss',
  MM_DD_YYYY_HH_MM_Z: 'MM/dd/yyyy HH:mm XXX',
  YYYY_MM_DD: 'yyyy-MM-dd',
  YYYY_MM_DD_HH_MM_Z: "yyyy-MM-dd'T'HH:mmXXX",
  YYYY_MM_DD_HH_MM_SS: "yyyy-MM-dd'T'HH:mm:ss",
};

// converter values from DateFormat enum to DateFormatDateFns enum
export const convertDateFormatToDateFns = (format) => {
  const map = {
    [DateFormat.MM_DD_YYYY]: DateFormatDateFns.MM_DD_YYYY,
    [DateFormat.DD_MMM_YYYY]: DateFormatDateFns.DD_MMM_YYYY,
    [DateFormat.MMM_DD_YYYY]: DateFormatDateFns.MMM_DD_YYYY,
    [DateFormat.MMM_DD_YYYY_HH_MM_SS]: DateFormatDateFns.MMM_DD_YYYY_HH_MM_SS,
    [DateFormat.MM_DD_YYYY_HH_MM_Z]: DateFormatDateFns.MM_DD_YYYY_HH_MM_Z,
  };

  return map[format] ?? DateFormatDateFns[format];
};
