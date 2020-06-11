import _ from 'lodash';
import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

const Menu = ({ menuConfig }) => {
  function getSectionComponent(section, key) {
    return (
      <li className="nav-item" key={key}>
        <a className="nav-link" href={section.href}>
          {section.label}
        </a>
      </li>
    );
  }
  function getSubsectionComponent(section, key) {
    return (
      <li className="nav-item dropdown" key={key}>
        <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
          {section.label}
        </a>
        <div className="dropdown-menu" aria-labelledby="navbarDropdown">
          <div className="dropdown-menu-subsections">
            {_.map(section.subsections, (subsection, subsectionKey) => (
              <div className="px-2 py-1" key={subsectionKey}>
                <span className="subsection-title">{subsection.label}</span>
                {_.map(subsection.menuItems, (menuItem, menuItemKey) => (
                  <a className="dropdown-item" key={menuItemKey} href={menuItem.href} target={menuItem.target}>
                    {menuItem.label}
                  </a>
                ))}
              </div>
            ))}
          </div>
        </div>
      </li>
    );
  }
  function getMenuItemsComponent(section, key) {
    return (
      <li className="nav-item dropdown" key={key}>
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
  }

  return (
    <div className="collapse navbar-collapse w-100 menu-container" id="navbarSupportedContent">
      <ul className="navbar-nav mr-auto flex-wrap">
        { _.map(menuConfig, (section, key) => {
          if (section.href) {
            return getSectionComponent(section, key);
          }
          if (section.subsections) {
            return getSubsectionComponent(section, key);
          }
          if (section.menuItems) {
            return getMenuItemsComponent(section, key);
          }
          return null;
        })
      }
      </ul>
    </div>
  );
};

const mapStateToProps = state => ({
  menuConfig: state.session.menuConfig,
});

export default connect(mapStateToProps)(Menu);

Menu.propTypes = {
  menuConfig: PropTypes.shape({}).isRequired,
};
