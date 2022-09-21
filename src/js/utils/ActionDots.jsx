import React from 'react';

import PropTypes from 'prop-types';
import { RiMoreLine } from 'react-icons/all';

import Translate from 'utils/Translate';

const ActionDots = ({
  actions, id, dropdownPlacement, dropdownClasses,
}) => {
  const getPositionClass = () => {
    switch (dropdownPlacement) {
      case 'top':
        return 'dropup';
      case 'left':
        return 'dropleft';
      case 'right':
        return 'dropright';
      default:
        return '';
    }
  };

  return (
    <div className={`btn-group ${getPositionClass()}`}>
      <div
        className="action-dots dropdown-toggle d-flex align-items-center justify-content-center"
        data-toggle="dropdown"
        aria-haspopup="true"
        aria-expanded="false"
      >
        <RiMoreLine />
      </div>
      <div className={`${dropdownClasses} dropdown-menu dropdown-menu-right nav-item padding-8`}>
        {actions && actions.map(action => (
          <React.Fragment key={action.href ? action.href : action.label}>
            {action.href && (
              <a
                key={action.href}
                href={`${action.href}/${id}`}
                className={`d-flex align-items-center gap-8 dropdown-item ${action.variant === 'danger' ? 'font-red-ob' : ''}`}
              >
                {action.leftIcon && action.leftIcon}
                {action.label &&
                <Translate id={action.label} defaultMessage={action.defaultLabel} />}
              </a>
            )}
            {action.onClickActionWithId && (
              <a
                key={action.label}
                href="#"
                onClick={() => action.onClickActionWithId(id)}
                className={`d-flex align-items-center gap-8 dropdown-item ${action.variant === 'danger' ? 'font-red-ob' : ''}`}
              >
                {action.leftIcon && action.leftIcon}
                {action.label &&
                <Translate id={action.label} defaultMessage={action.defaultLabel} />}
              </a>
            )}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
};

export default ActionDots;

ActionDots.defaultProps = {
  dropdownPlacement: undefined,
  dropdownClasses: '',
};

ActionDots.propTypes = {
  actions: PropTypes.shape([{
    leftIcon: PropTypes.element.isRequired,
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    href: PropTypes.string,
    variant: PropTypes.string,
    onClickActionWithId: PropTypes.func,
  }]).isRequired,
  dropdownPlacement: PropTypes.oneOf(['top', 'bottom', 'left', 'right']),
  id: PropTypes.string.isRequired,
  dropdownClasses: PropTypes.string,
};

