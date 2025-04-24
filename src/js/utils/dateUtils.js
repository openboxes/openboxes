import moment from 'moment';

const dateWithoutTimeZone = ({
  date,
  currentDateFormat,
  outputDateFormat,
}) => {
  if (!date) {
    return null;
  }

  const parsedDate = moment(date, currentDateFormat).utcOffset(0, true);
  return parsedDate.format(outputDateFormat);
};

export default dateWithoutTimeZone;
