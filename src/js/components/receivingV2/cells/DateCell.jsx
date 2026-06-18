import React from 'react';

import * as locales from 'date-fns/locale';
import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Memoized cell rendering a localized, formatted date with a matching tooltip.
 */
const DateCell = React.memo(({ value, localeKey, ariaLabel }) => {
  const formatted = formatDateToString({
    date: value,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    options: { locale: locales[localeKey] },
  });

  return (
    <TableCell className="rt-td" customTooltip tooltipLabel={formatted}>
      <div aria-label={ariaLabel}>
        {formatted}
      </div>
    </TableCell>
  );
});

DateCell.displayName = 'DateCell';

DateCell.propTypes = {
  value: PropTypes.string,
  localeKey: PropTypes.string,
  ariaLabel: PropTypes.string.isRequired,
};

DateCell.defaultProps = {
  value: undefined,
  localeKey: undefined,
};

export default DateCell;
