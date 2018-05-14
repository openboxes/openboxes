import React from 'react';
import Header from './Header';
import Menu from './Menu';
import Breadcrumbs from './Breadcrumbs';

const Navbar = () => (
  <nav className="navbar navbar-expand navbar-light bg-light navbar-inverse navbar-fixed-top flex-column w-100 p-0 bg-white">
    <Header />
    <Menu />
    <Breadcrumbs />
  </nav>
);

export default Navbar;
