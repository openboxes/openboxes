import React from 'react';

import PropTypes from 'prop-types';
import { RiMenuLine } from 'react-icons/ri';
import { connect } from 'react-redux';

import ImpersonateInfo from 'components/Layout/ImpersonateInfo';
import Logo from 'components/Layout/Logo';
import Menu from 'components/Layout/menu/Menu';
import NavbarIcons from 'components/Layout/NavbarIcons';
import LocationChooser from 'components/location/LocationChooser';

import 'components/Layout/HeaderStyles.scss';

const Header = ({ isImpersonated }) => (
  <div className="navbar p-0">
    {isImpersonated && <ImpersonateInfo />}
    <nav className="navbar navbar-expand-md navbar-light bg-light bg-white main-wrapper p-0 px-md-4">
      <div className="d-flex p-2 justify-content-between flex-1">
        <div className="d-flex align-items-center">
          <Logo />
          <LocationChooser />
        </div>
        <button
          className="navbar-toggler"
          type="button"
          data-toggle="collapse"
          data-target="#navbarToggler"
          aria-controls="navbarToggler"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <RiMenuLine />
        </button>
      </div>
      <div className="collapse navbar-collapse w-100" id="navbarToggler">
        <ul className="navbar-nav w-100">
          <Menu />
          <NavbarIcons />
        </ul>
      </div>
    </nav>
  </div>);

const mapStateToProps = state => ({
  isImpersonated: state.session.isImpersonated,
});

export default connect(mapStateToProps)(Header);

Header.propTypes = {
  isImpersonated: PropTypes.bool.isRequired,
};
