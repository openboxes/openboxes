import React from 'react';

import PropTypes from 'prop-types';

const MenuSection = ({ section, key, active }) => (
  <li className={`nav-item d-flex justify-content-center align-items-center ${active && 'active-section'}`} key={key}>
    <a className="nav-link" href={section.href}>
      {section.label}
    </a>
  </li>
);

export default MenuSection;

MenuSection.propTypes = {
  section: PropTypes.shape({
    label: PropTypes.string,
    href: PropTypes.string,
  }).isRequired,
  key: PropTypes.string,
  active: PropTypes.bool,
};

MenuSection.defaultProps = {
  active: false,
  key: '',
};
