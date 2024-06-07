import moment from 'moment';

/**
 * Method to if picked date is from the future
 * @return true if the date is not from the future; false if the date is from the future
 * @param pickedDate
 */
const validateFutureDate = (pickedDate) => {
  const date = moment(pickedDate);
  const today = moment(new Date());
  return date.startOf('day').isSameOrBefore(today.startOf('day'));
};

export default validateFutureDate;
