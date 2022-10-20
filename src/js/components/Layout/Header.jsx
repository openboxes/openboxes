import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ImpersonateInfo from 'components/Layout/ImpersonateInfo';
import Logo from 'components/Layout/Logo';
import Menu from 'components/Layout/menu/Menu';
import NavbarIcons from 'components/Layout/NavbarIcons';
import LocationChooser from 'components/location/LocationChooser';

import 'components/Layout/HeaderStyles.scss';

const Header = ({ isImpersonated }) => (
  <nav className="navbar navbar-expand navbar-light bg-light navbar-inverse navbar-fixed-top flex-column w-100 p-0 bg-white">
    {isImpersonated && <ImpersonateInfo />}
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

const mapStateToProps = state => ({
  isImpersonated: state.session.isImpersonated,
});

export default connect(mapStateToProps)(Header);

Header.propTypes = {
  isImpersonated: PropTypes.bool.isRequired,
};
