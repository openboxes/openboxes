import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import useTranslate from 'hooks/useTranslate';
import CustomTooltip from 'wrappers/CustomTooltip';

/**
 * Memoized cell rendering a row of action buttons. Each action provides its own
 * icon, click handler and aria label, plus an optional badge, disabled state and tooltip.
 * The whole cell can be disabled through the `disabled` prop.
 */
const ActionsCell = React.memo(({
  actions, label, defaultLabel, disabled,
}) => {
  const translate = useTranslate();

  return (
    <TableCell className="rt-td">
      <div className="actions-cell" aria-label={translate(label, defaultLabel)}>
        {actions.map((action) => {
          const isDisabled = disabled || Boolean(action.disabled);
          // A disabled action is marked with aria-disabled instead of the native attribute:
          // a natively disabled button receives no mouse events, so neither its tooltip nor
          // its "not allowed" cursor would ever show. Dropping the handler is what actually
          // makes it inert.
          const button = (
            <button
              key={action.key}
              type="button"
              className="actions-cell__button"
              onClick={isDisabled ? undefined : action.onClick}
              aria-disabled={isDisabled}
              aria-label={translate(action.label, action.defaultLabel)}
            >
              {action.icon}
              {action.badge}
            </button>
          );
          return action.tooltipLabel
            ? (
              <CustomTooltip
                key={action.key}
                content={translate(action.tooltipLabel, action.defaultTooltipLabel)}
              >
                {button}
              </CustomTooltip>
            )
            : button;
        })}
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
    disabled: PropTypes.bool,
    tooltipLabel: PropTypes.string,
    defaultTooltipLabel: PropTypes.string,
  })).isRequired,
  label: PropTypes.string.isRequired,
  defaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
};

ActionsCell.defaultProps = {
  disabled: false,
};

export default ActionsCell;
