import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { RiArrowDropDownLine } from 'react-icons/ri';

const DropdownMenuItem = ({ section, active }) => (
  <li className={`nav-item dropdown d-none d-md-flex justify-content-center align-items-center ${active && 'active-section'}`} data-testid="dropdownMenuItem">
    <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
      {section.label}
    </a>
    <div className="dropdown-menu dropdown-menu-wrapper" aria-labelledby="navbarDropdown">
      <div className="dropdown-menu-content padding-8">
        {_.map(section.menuItems, (menuItem, menuItemKey) => (
          <a className="dropdown-item" key={menuItemKey} href={menuItem.href} target={menuItem.target}>
            {menuItem.label}
          </a>
          ))}
      </div>
    </div>
  </li>
);

const CollapseMenuItem = ({ section, active }) => {
  const id = `collapse-${section?.label?.replaceAll(' ', '-')}`;
  return (
    <li className="collapse-nav-item nav-item justify-content-center align-items-center d-flex d-md-none" data-testid="collapseMenuItem">
      <a
        className={`nav-link d-flex justify-content-between align-items-center w-100 ${active && 'active-section'}`}
        data-toggle="collapse"
        href={`#${id}`}
        role="button"
        aria-expanded="false"
        aria-controls={id}
      >
        {section.label}
        <RiArrowDropDownLine className="collapse-arrow-icon" />
      </a>
      <div className="collapse w-100" id={id}>
        <div className="d-flex flex-row flex-wrap">
          {_.map(section.menuItems, (menuItem, menuItemKey) => (
            <a className="subsection-section-item" key={menuItemKey} href={menuItem.href} target={menuItem.target}>
              {menuItem.label}
            </a>
          ))}
        </div>
      </div>
    </li>
  );
};

const MenuItem = props => (<>
  <DropdownMenuItem {...props} />
  <CollapseMenuItem {...props} />
  </>);

export default MenuItem;

const menuItemPropType = PropTypes.shape({
  label: PropTypes.string,
  href: PropTypes.string,
});

MenuItem.propTypes = {
  section: PropTypes.shape({
    label: PropTypes.string,
    menuItems: PropTypes.arrayOf(menuItemPropType),
  }).isRequired,
  active: PropTypes.bool,
};

DropdownMenuItem.propTypes = { ...MenuItem.propTypes };
DropdownMenuItem.defaultProps = { ...MenuItem.defaultProps };

CollapseMenuItem.propTypes = { ...MenuItem.propTypes };
CollapseMenuItem.defaultProps = { ...MenuItem.defaultProps };

MenuItem.defaultProps = {
  active: false,
};
