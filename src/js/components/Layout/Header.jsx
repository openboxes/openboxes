import React from 'react';

import PropTypes from 'prop-types';
import { RiMenuLine } from 'react-icons/ri';
import { connect } from 'react-redux';

import InfoBar from 'components/infoBar/InfoBar';
import ImpersonateInfo from 'components/Layout/ImpersonateInfo';
import LocalizationModeInfo from 'components/Layout/LocalizationModeInfo';
import Logo from 'components/Layout/Logo';
import Menu from 'components/Layout/menu/Menu';
import NavbarIcons from 'components/Layout/NavbarIcons';
import LocationChooser from 'components/location/LocationChooser';

import 'components/Layout/HeaderStyles.scss';

const Header = ({
  isImpersonated, localizationModeEnabled, infoBarVisibility, bars,
}) => (
  <div className="navbar p-0">
    {isImpersonated && <ImpersonateInfo />}
    {localizationModeEnabled && <LocalizationModeInfo />}
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
    {Object.entries(bars)?.map((([bar, values]) => (infoBarVisibility[values.name]
      ? <InfoBar {...values} key={bar} />
      : null
    )))}
  </div>);

const mapStateToProps = state => ({
  isImpersonated: state.session.isImpersonated,
  localizationModeEnabled: state.session.localizationModeEnabled,
  bars: state.infoBar.bars,
  infoBarVisibility: state.infoBarVisibility,
});

export default connect(mapStateToProps)(Header);

Header.propTypes = {
  isImpersonated: PropTypes.bool.isRequired,
  localizationModeEnabled: PropTypes.bool.isRequired,
  bars: PropTypes.arrayOf(PropTypes.shape({
    name: PropTypes.string.isRequired,
    show: PropTypes.bool.isRequired,
    closed: PropTypes.bool,
    title: PropTypes.shape({
      label: PropTypes.string.isRequired,
      defaultLabel: PropTypes.string.isRequired,
    }),
    versionLabel: PropTypes.shape({
      label: PropTypes.string.isRequired,
      defaultLabel: PropTypes.string.isRequired,
    }),
  })).isRequired,
  infoBarVisibility: PropTypes.shape(Object.keys(InfoBar)
    .reduce((acc, bar) => ({ ...acc, [bar]: PropTypes.string }), {})).isRequired,
};
