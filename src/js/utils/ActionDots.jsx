import React from 'react';

import PropTypes from 'prop-types';
import { RiMoreLine } from 'react-icons/ri';
import { Link } from 'react-router-dom';

import Translate from 'utils/Translate';

const actionItemType = {
  LINK: 'LINK',
  REACT_LINK: 'REACT_LINK',
  BUTTON: 'BUTTON',
};

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

  const getActionItemType = (action) => {
    if (action.href) {
      if (action.reactLink) {
        return actionItemType.REACT_LINK;
      }
      return actionItemType.LINK;
    }
    return actionItemType.BUTTON;
  };

  return (
    <div className={`btn-group ${getPositionClass()}`} data-testid="action-dots-component">
      <button
        data-testid="dropdown-toggle"
        className="action-dots dropdown-toggle d-flex align-items-center justify-content-center"
        data-toggle="dropdown"
        aria-haspopup="true"
        aria-expanded="false"
      >
        <RiMoreLine />
      </button>
      <div data-testid="dropdown-menu" className={`${dropdownClasses} dropdown-menu dropdown-menu-right nav-item padding-8`}>
        {actions && actions.map((action) => {
          const itemClasses = `d-flex align-items-center gap-8 dropdown-item ${action.variant === 'danger' ? 'font-red-ob' : ''}`;
          const itemValue = (
            <React.Fragment>
              {action.leftIcon && action.leftIcon}
              {action.label &&
              <Translate id={action.label} defaultMessage={action.defaultLabel} />}
            </React.Fragment>
          );
          const elementType = getActionItemType(action);
          let link = '';
          if (elementType === actionItemType.LINK || actionItemType.REACT_LINK) {
            link = action.href + (action.appendId === false ? '' : `/${id}`);
          }

          return (
            <React.Fragment key={action.href ? action.href : action.label}>
              { elementType === actionItemType.BUTTON && (
                <button
                  onClick={() => action.onClick(id)}
                  className={itemClasses}
                >
                  {itemValue}
                </button>)}
              { elementType === actionItemType.LINK && (
                <a href={link} className={itemClasses}>
                  {itemValue}
                </a>)}
              { elementType === actionItemType.REACT_LINK && (
                <Link to={link} className={itemClasses}>
                  {itemValue}
                </Link>)}
            </React.Fragment>
          );
        })}
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
  actions: PropTypes.arrayOf(PropTypes.shape({
    leftIcon: PropTypes.element.isRequired,
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    href: PropTypes.string,
    reactLink: PropTypes.bool,
    appendId: PropTypes.bool,
    variant: PropTypes.string,
    onClick: PropTypes.func,
  })).isRequired,
  dropdownPlacement: PropTypes.oneOf(['top', 'bottom', 'left', 'right']),
  id: PropTypes.string.isRequired,
  dropdownClasses: PropTypes.string,
};

