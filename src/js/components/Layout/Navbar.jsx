import React from 'react';

import Breadcrumbs from 'components/Layout/Breadcrumbs';
import Header from 'components/Layout/Header';
import Menu from 'components/Layout/Menu';


const Navbar = () => (
  <nav className="navbar navbar-expand navbar-light bg-light navbar-inverse navbar-fixed-top flex-column w-100 p-0 bg-white">
    <Header />
    <Menu />
    <Breadcrumbs />
  </nav>
);

export default Navbar;
