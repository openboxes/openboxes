import React from 'react';

import Logo from 'components/Layout/Logo';
import Menu from 'components/Layout/Menu';
import NavbarIcons from 'components/Layout/NavbarIcons';

import 'components/Layout/HeaderStyles.scss';

const Header = () => (
  <div className="main-wrapper">
    <Logo />
    <Menu />
    <NavbarIcons />
  </div>
);

export default Header;
