import React from 'react';

import * as locales from 'date-fns/locale';
import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import EXPIRY_BADGES from 'consts/expiryBadges';
import { DateFormatDateFns } from 'consts/timeFormat';
import useTranslate from 'hooks/useTranslate';
import { formatDateToString } from 'utils/dateUtils';
import getExpiryStatus from 'utils/expirationStatus';

/**
 * Memoized cell rendering a localized, formatted expiration date. When `showExpiryStatus` is set,
 * it also highlights the date and shows an "expiring"/"expired" badge.
 */
const ExpirationDateCell = React.memo(({
  value, localeKey, label, defaultLabel, showExpiryStatus,
}) => {
  const translate = useTranslate();

  const formatted = formatDateToString({
    date: value,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    options: { locale: locales[localeKey] },
  });

  const status = showExpiryStatus ? getExpiryStatus(value) : null;
  const { Icon, label: badgeLabel, defaultLabel: badgeDefaultLabel } = EXPIRY_BADGES[status] || {};

  const tooltipLabel = formatted
    ? translate('react.default.expirationDate.tooltip.label', `This item expires on ${formatted}`, [formatted])
    : undefined;

  return (
    <TableCell className="rt-td" customTooltip tooltipLabel={tooltipLabel}>
      <div
        className={`expiration-cell ${status ? `expiration-cell--${status}` : ''}`}
        aria-label={translate(label, defaultLabel)}
      >
        <div className="expiration-cell__date">{formatted}</div>
        {Icon && (
          <span className={`expiry-badge expiry-badge--${status}`}>
            <Icon size={14} />
            {translate(badgeLabel, badgeDefaultLabel)}
          </span>
        )}
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
  // When true, highlights the date and shows an expiring/expired badge.
  showExpiryStatus: PropTypes.bool,
};

ExpirationDateCell.defaultProps = {
  value: undefined,
  localeKey: undefined,
  showExpiryStatus: false,
};

export default ExpirationDateCell;
