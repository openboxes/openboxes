import { differenceInCalendarDays } from 'date-fns';

import ExpiryStatus from 'consts/expiryStatus';

const getExpiryStatus = (date) => {
  if (!date) {
    return null;
  }
  const daysToExpiry = differenceInCalendarDays(date, new Date());
  if (daysToExpiry < 0) {
    return ExpiryStatus.EXPIRED;
  }
  // Items expiring within 90 days are flagged as "expiring soon".
  if (daysToExpiry <= 90) {
    return ExpiryStatus.EXPIRING;
  }
  return null;
};

export default getExpiryStatus;
