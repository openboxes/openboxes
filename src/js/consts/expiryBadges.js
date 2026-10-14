import { RiCalendarLine, RiCalendarTodoLine } from 'react-icons/ri';

import ExpiryStatus from 'consts/expiryStatus';

const EXPIRY_BADGES = {
  [ExpiryStatus.EXPIRED]: {
    Icon: RiCalendarTodoLine,
    label: 'react.receiving.expirationDate.expired.label',
    defaultLabel: 'Expired',
  },
  [ExpiryStatus.EXPIRING]: {
    Icon: RiCalendarLine,
    label: 'react.receiving.expirationDate.expiring.label',
    defaultLabel: 'Expiring',
  },
};

export default EXPIRY_BADGES;
