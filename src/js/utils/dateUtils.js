import moment from 'moment/moment';

const parseDateToUTC = ({ date, currentFormat }) => {
  if (!date) {
    return null;
  }

  const utcDate = moment.utc(date, currentFormat);

  if (utcDate.isValid()) {
    return utcDate.format();
  }

  return date;
};

export default parseDateToUTC;
