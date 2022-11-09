import React from 'react';

import PropTypes from 'prop-types';

const MenuConfigurationSubsection = ({ subsection }) => (
  <div className="padding-8" >
    <span className="subsection-section-title">{subsection.label && subsection.label}</span>
    {subsection.menuItems && subsection.menuItems.map(menuItem => (
      <a className="dropdown-item" key={`${menuItem.label}-menuItem`} href={menuItem.href} target={menuItem.target}>
        {menuItem.label}
      </a>
    ))}
  </div>
);


export default MenuConfigurationSubsection;

const menuItemPropType = PropTypes.shape({
  label: PropTypes.string,
  href: PropTypes.string,
});

MenuConfigurationSubsection.propTypes = {
  subsection: PropTypes.shape({
    label: PropTypes.string,
    menuItems: PropTypes.arrayOf(menuItemPropType),
  }).isRequired,
};

