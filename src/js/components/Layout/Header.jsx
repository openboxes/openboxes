import React from 'react';

import Logo from 'components/Layout/Logo';
import Menu from 'components/Layout/menu/Menu';
import NavbarIcons from 'components/Layout/NavbarIcons';
import LocationChooser from 'components/location/LocationChooser';

import 'components/Layout/HeaderStyles.scss';

const Header = () => (
  <nav className="navbar navbar-expand navbar-light bg-light navbar-inverse navbar-fixed-top flex-column w-100 p-0 bg-white">
    <div className="main-wrapper">
      <div className="d-flex align-items-center">
        <Logo />
        <LocationChooser />
      </div>
      <Menu />
      <NavbarIcons />
    </div>
  </nav>
);

export default Header;
