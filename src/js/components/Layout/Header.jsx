import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import Translate from '../../utils/Translate';
import translations from '../../en';
import GlobalSearch from '../GlobalSearch';
import LocationChooser from '../location/LocationChooser';
import apiClient from '../../utils/apiClient';

const { dashboard } = translations.navbar;

/** Logs out impersonated user and redirects to dashboard */
function logoutImpersonatedUser() {
  const url = '/openboxes/api/logout';

  apiClient.post(url)
    .then(() => {
      window.location = '/openboxes/dashboard/index';
    });
}

const Header = ({ username, isImpersonated }) => (
  <div className="w-100">
    {isImpersonated ?
      <div className="d-flex notice">
        <div className="ml-1"><Translate id="react.default.impersonate.label" defaultMessage="You are impersonating user" /></div>
        <div className="ml-1"><b>{username}</b></div>
        <div className="ml-1">
          <a
            href="#"
            onClick={() => logoutImpersonatedUser()}
          >
            <Translate id="react.default.logout.label" defaultMessage="Logout" />
          </a>
        </div>
      </div> : null}
    <div className="d-flex align-items-center justify-content-between flex-wrap">
      <a
        href={dashboard.link}
        className="navbar-brand brand-name"
      >
        Openboxes
      </a>
      <div className="d-flex flex-wrap">
        <GlobalSearch />
        <LocationChooser />
      </div>
    </div>
  </div>
);

const mapStateToProps = state => ({
  username: state.session.user.username,
  isImpersonated: state.session.isImpersonated,
});

export default connect(mapStateToProps)(Header);

Header.propTypes = {
  /** Active user's username */
  username: PropTypes.string.isRequired,
  /** Indicator if active user is impersonated */
  isImpersonated: PropTypes.bool.isRequired,
};
