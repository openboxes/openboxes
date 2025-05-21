import moment from 'moment';

import { DateFormat } from 'consts/timeFormat';

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

export default dateWithoutTimeZone;
