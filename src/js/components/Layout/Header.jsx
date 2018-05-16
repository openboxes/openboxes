import React from 'react';
import translations from '../../en';
import LanguageSelector from '../LanguageSelector';

const { dashboard } = translations.navbar;

const Header = () => (
  <div className="d-flex align-items-center justify-content-between w-100">
    <a
      href={dashboard.link}
      className="navbar-brand brand-name"
    >
      Openboxes
    </a>
    <div className="d-flex align-items-center justify-content-center">
      <form className="form-inline my-2 my-lg-1 align-self-end">
        <input className="form-control mr-sm-2" type="search" placeholder="Search" aria-label="Search" />
      </form>
      <LanguageSelector />
    </div>
  </div>
);

export default Header;
