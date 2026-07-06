import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';

/**
 * Memoized cell rendering a row of action buttons. Each action provides its own
 * icon, click handler and aria label, plus an optional badge.
 */
const ActionsCell = React.memo(({
  actions, label, defaultLabel, disabled,
}) => {
  const translate = useTranslate();

  return (
    <TableCell className="rt-td">
      <div className="actions-cell" aria-label={translate(label, defaultLabel)}>
        {actions.map((action) => (
          <button
            key={action.key}
            type="button"
            className="actions-cell__button"
            onClick={action.onClick}
            disabled={disabled}
            aria-label={translate(action.label, action.defaultLabel)}
          >
            {action.icon}
            {action.badge}
          </button>
        ))}
      </div>
    </TableCell>
  );
});

ActionsCell.displayName = 'ActionsCell';

ActionsCell.propTypes = {
  actions: PropTypes.arrayOf(PropTypes.shape({
    key: PropTypes.string.isRequired,
    icon: PropTypes.node.isRequired,
    onClick: PropTypes.func,
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    badge: PropTypes.node,
  })).isRequired,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
};

ActionsCell.defaultProps = {
  disabled: false,
};

export default ActionsCell;
