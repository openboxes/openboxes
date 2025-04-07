import moment from 'moment';

const dateWithoutTimeZone = ({
  date,
  currentDateFormat,
  outputDateFormat,
}) => {
  if (!date) {
    return null;
  }

  const parsedDate = currentDateFormat
    ? moment(date, currentDateFormat)
    : moment(date);

  return parsedDate.utcOffset(0, true).format(outputDateFormat);
};

export default dateWithoutTimeZone;
