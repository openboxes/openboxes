import React from 'react';

import PropTypes from 'prop-types';
import { RiMoreLine } from 'react-icons/ri';
import { Link } from 'react-router-dom';
import { Popover } from 'react-tiny-popover';

import actionItemType from 'consts/actionItemType';
import useContextMenu from 'hooks/useContextMenu';
import Translate from 'utils/Translate';

import './utils.scss';

const ContextMenu = ({
  positions, actions, dropdownClasses, popoverClasses, id,
}) => {
  const {
    getActionItemType,
    buildLink,
    popoverRef,
    isPopoverOpen,
    setIsPopoverOpen,
  } = useContextMenu();

  return (
    <Popover
      data-testid="action-dots-component"
      isOpen={isPopoverOpen}
      positions={positions}
      padding={10}
      onClickOutside={() => setIsPopoverOpen(false)}
      ref={popoverRef}
      content={() => (
        actions && (
        <div
          className={`${popoverClasses} context-menu padding-8`}
          data-testid="dropdown-menu"
        >
          {actions.map((action) => {
            const itemClasses = `${dropdownClasses} d-flex align-items-center gap-8 dropdown-item ${action.variant === 'danger' ? 'font-red-ob' : ''}`;
            const itemValue = (
              <>
                {action.leftIcon && action.leftIcon}
                {action.label
                  && <Translate id={action.label} defaultMessage={action.defaultLabel} />}
              </>
            );
            const elementType = getActionItemType(action);
            const link = elementType === actionItemType.LINK || actionItemType.REACT_LINK
              ? buildLink(action, id)
              : '';

            return (
              <React.Fragment key={action.label}>
                {elementType === actionItemType.BUTTON && (
                <button
                  type="button"
                  onClick={() => {
                    action.onClick(id);
                    setIsPopoverOpen(false);
                  }}
                  className={itemClasses}
                >
                  {itemValue}
                </button>
                )}
                {elementType === actionItemType.LINK && (
                <a href={link} className={itemClasses}>
                  {itemValue}
                </a>
                )}
                {elementType === actionItemType.REACT_LINK && (
                <Link to={link} className={itemClasses}>
                  {itemValue}
                </Link>
                )}
              </React.Fragment>
            );
          })}
        </div>
        )
      )}
    >
      <button
        data-testid="dropdown-toggle"
        className="action-dots dropdown-toggle d-flex align-items-center justify-content-center"
        type="button"
        onClick={() => setIsPopoverOpen((isOpen) => !isOpen)}
      >
        <RiMoreLine />
      </button>
    </Popover>
  );
};

export default ContextMenu;

ContextMenu.propTypes = {
  actions: PropTypes.arrayOf(PropTypes.shape({
    leftIcon: PropTypes.element.isRequired,
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    href: PropTypes.string,
    reactLink: PropTypes.bool,
    variant: PropTypes.string,
    onClick: PropTypes.func,
  })).isRequired,
  positions: PropTypes.arrayOf(PropTypes.string),
  id: PropTypes.string.isRequired,
  dropdownClasses: PropTypes.string,
  popoverClasses: PropTypes.string,
};

ContextMenu.defaultProps = {
  positions: ['top', 'right', 'left', 'bottom'],
  dropdownClasses: '',
  popoverClasses: '',
};
