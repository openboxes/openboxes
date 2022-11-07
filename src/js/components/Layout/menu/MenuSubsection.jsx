import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { RiArrowDropDownLine } from 'react-icons/ri';

const DropdownMenu = ({ section, active }) => (
  <li className={`nav-item dropdown d-none d-md-flex justify-content-center align-items-center ${active && 'active-section'}`} >
    <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
      {section.label}
    </a>
    <div className={`dropdown-menu dropdown-menu-wrapper ${section.label === 'Reporting' && 'dropdown-menu-right'}`} aria-labelledby="navbarDropdown">
      <div className="dropdown-menu-content dropdown-menu-subsections">
        {_.map(section.subsections, (subsection, subsectionKey) => (
          <div className="padding-8" key={subsectionKey}>
            {subsection.label && <span className="subsection-section-title">{subsection.label}</span>}
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

const CollapseMenu = ({ section, active }) => (
  <li className="collapse-nav-item nav-item justify-content-center align-items-center d-flex d-md-none" >
    <a
      className={`nav-link d-flex justify-content-between align-items-center w-100 ${active && 'active-section'}`}
      data-toggle="collapse"
      href={`#collapse-${section.label}`}
      role="button"
      aria-expanded="false"
      aria-controls={`collapse-${section.label}`}
    >
      {section.label}
      <RiArrowDropDownLine className="collapse-arrow-icon" />
    </a>
    <div className="collapse w-100" id={`collapse-${section.label}`}>
      <div className="d-flex flex-row flex-wrap">
        {_.map(section.subsections, (subsection, subsectionKey) => (
          <div key={subsectionKey} className="d-flex flex-column m-3">
            {subsection.label && <span className="subsection-section-title">{subsection.label}</span>}
            {_.map(subsection.menuItems, (menuItem, menuItemKey) => (
              <a className="subsection-section-item" key={menuItemKey} href={menuItem.href} target={menuItem.target}>
                {menuItem.label}
              </a>
            ))}
          </div>
        ))}
      </div>
    </div>
  </li>
);

const MenuSubsection = props => (<>
  <DropdownMenu {...props} />
  <CollapseMenu {...props} />
</>);

export default MenuSubsection;

const menuItemPropType = PropTypes.shape({
  label: PropTypes.string,
  href: PropTypes.string,
});

const subsectionsPropTypes = PropTypes.shape({
  label: PropTypes.string,
  menuItems: PropTypes.arrayOf(menuItemPropType),
});


MenuSubsection.propTypes = {
  section: PropTypes.shape({
    label: PropTypes.string,
    menuItems: PropTypes.arrayOf(menuItemPropType),
    subsections: PropTypes.arrayOf(subsectionsPropTypes),
  }).isRequired,
  active: PropTypes.bool,
};

DropdownMenu.propTypes = { ...MenuSubsection.propTypes };
DropdownMenu.defaultProps = { ...MenuSubsection.defaultProps };

CollapseMenu.propTypes = { ...MenuSubsection.propTypes };
CollapseMenu.defaultProps = { ...MenuSubsection.defaultProps };

MenuSubsection.defaultProps = {
  active: false,
};
