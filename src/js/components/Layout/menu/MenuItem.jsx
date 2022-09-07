import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

const MenuItem = ({ section, key, active }) => (
  <li className={`nav-item dropdown d-flex justify-content-center align-items-center ${active && 'active-section'}`} key={key}>
    <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
      {section.label}
    </a>
    <div className="dropdown-menu" aria-labelledby="navbarDropdown">
      <div style={{ maxHeight: '60vh', overflow: 'auto' }} className="px-3 py-1">
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

MenuItem.propTypes = {
  section: PropTypes.shape({
    label: PropTypes.string,
    menuItems: PropTypes.shape([]),
  }).isRequired,
  key: PropTypes.string,
  active: PropTypes.bool,
};

MenuItem.defaultProps = {
  active: false,
  key: '',
};
