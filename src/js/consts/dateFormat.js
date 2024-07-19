// Enum for storing labels used in dates formatting.
// In case of adding new format we have to:
// 1. Add new format in message.properties file
// 2. Add the newly created label to this enum
// This enum is used in:
// 1. <FormatDate date={your date} formatName={property of this enum} /> component

const DateFormat = {
  DEFAULT: 'react.default.defaultDate.format',
  COMMON: 'react.default.commonDate.format',
  EXPIRY: 'react.default.expiryDate.format',
  DISPLAY: 'react.default.displayDate.format',
};

export default DateFormat;
