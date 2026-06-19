import React from 'react';

import * as locales from 'date-fns/locale';
import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Memoized cell rendering a localized, formatted expiration date.
 */
const ExpirationDateCell = React.memo(({
  value, localeKey, label, defaultLabel,
}) => {
  const translate = useTranslate();

  const formatted = formatDateToString({
    date: value,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    options: { locale: locales[localeKey] },
  });

  const tooltipLabel = formatted
    ? translate('react.receiving.expirationDate.tooltip.label', `This item expires on ${formatted}`, [formatted])
    : undefined;

  return (
    <TableCell className="rt-td" customTooltip tooltipLabel={tooltipLabel}>
      <div aria-label={translate(label, defaultLabel)}>
        {formatted}
      </div>
    </TableCell>
  );
});

ExpirationDateCell.displayName = 'ExpirationDateCell';

ExpirationDateCell.propTypes = {
  value: PropTypes.string,
  localeKey: PropTypes.string,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
};

ExpirationDateCell.defaultProps = {
  value: undefined,
  localeKey: undefined,
};

export default ExpirationDateCell;
