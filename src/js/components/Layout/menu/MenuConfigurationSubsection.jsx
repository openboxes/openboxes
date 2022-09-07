import React from 'react';

import PropTypes from 'prop-types';


const MenuConfigurationSubsection = ({ subsection, key }) => (
  <div className="padding-8" key={key}>
    <span className="subsection-title">{subsection.label && subsection.label}</span>
    {subsection.menuItems && subsection.menuItems.map(menuItem => (
      <a className="dropdown-item" key={`${menuItem.label}-menuItem`} href={menuItem.href} target={menuItem.target}>
        {menuItem.label}
      </a>
    ))}
  </div>
);


export default MenuConfigurationSubsection;


MenuConfigurationSubsection.propTypes = {
  subsection: PropTypes.shape({
    menuItems: PropTypes.shape([]),
  }).isRequired,
  key: PropTypes.string,
};

MenuConfigurationSubsection.defaultProps = {
  key: '',
};

