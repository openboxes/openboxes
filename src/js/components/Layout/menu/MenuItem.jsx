import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

const MenuItem = ({ section, active }) => (
  <li className={`nav-item dropdown d-flex justify-content-center align-items-center ${active && 'active-section'}`} >
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

MenuItem.defaultProps = {
  active: false,
};
