import PropTypes from 'prop-types';

import { DateFormat } from 'consts/timeFormat';
import dateWithoutTimeZone from 'utils/dateUtils';

const formatCycleCountDate = (date) => dateWithoutTimeZone({
  date,
  currentDateFormat: DateFormat.DD_MMM_YYYY,
  outputDateFormat: DateFormat.MM_DD_YYYY,
});

export default formatCycleCountDate;

formatCycleCountDate.propTypes = {
  date: PropTypes.instanceOf(Date),
};

formatCycleCountDate.defaultProps = {
  date: new Date(),
};
