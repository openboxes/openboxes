import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { HelpScout, LiveChatLoaderProvider } from 'react-live-chat-loader';
import { connect } from 'react-redux';

import GlobalSearch from 'components/GlobalSearch';
import LocationChooser from 'components/location/LocationChooser';
import UserActionMenu from 'components/user/UserActionMenu';
import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';

class Header extends Component {
  constructor(props) {
    super(props);
    this.state = {
      logoUrl: '',
    };
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.logoUrl !== this.props.logoUrl) {
      this.setLogoUrl(nextProps.logoUrl);
    }
  }

  setLogoUrl(logoUrl) {
    this.setState({ logoUrl });
  }

  logoutImpersonatedUser = () => {
    const url = '/openboxes/api/logout';

    apiClient.post(url)
      .then(() => {
        window.location = '/openboxes/dashboard/index';
      });
  }

  render() {
    let helpScoutBeacon = <div />;
    if (this.props.isHelpScoutEnabled) {
      helpScoutBeacon = (
        <LiveChatLoaderProvider provider="helpScout" providerKey={this.props.helpScoutKey}>
          <HelpScout
            color={this.props.helpScoutColor}
            horizontalPosition="right"
            icon="question"
            idlePeriod="0"
            zIndex="1050"
          />
        </LiveChatLoaderProvider>
      );
    }

    return (
      <div className="w-100">
        {this.props.isImpersonated ?
          <div className="d-flex notice">
            <div className="ml-1">
              <Translate id="react.default.impersonate.label" defaultMessage="You are impersonating user" />
            </div>
            <div className="ml-1"><b>{this.props.username}</b></div>
            <div className="ml-1">
              <a
                href="#"
                onClick={() => this.logoutImpersonatedUser()}
              >
                <Translate id="react.default.logout.label" defaultMessage="Logout" />
              </a>
            </div>
          </div> : null}
        <div className="d-flex align-items-center justify-content-between flex-wrap">
          <div className="logo-header">
            <a
              href={this.props.highestRole === 'Authenticated' ? '/openboxes/stockMovement/list?direction=INBOUND' : '/openboxes'}
              className="navbar-brand brand-name"
            >
              { this.state.logoUrl !== '' ?
                <img alt="Openboxes" src={this.state.logoUrl} onError={(e) => { e.target.onerror = null; e.target.src = 'https://openboxes.com/img/logo_30.png'; }} /> : null
            }
            </a>
            { this.props.logoLabel.trim() !== '' ? <span>{this.props.logoLabel} </span> : null }
          </div>
          <div className="d-flex flex-wrap">
            <GlobalSearch />
            <UserActionMenu />
            <LocationChooser />
          </div>
        </div>
        {helpScoutBeacon}
      </div>
    );
  }
}

const mapStateToProps = state => ({
  helpScoutColor: state.session.helpScoutColor,
  helpScoutKey: state.session.helpScoutKey,
  highestRole: state.session.highestRole,
  isHelpScoutEnabled: state.session.isHelpScoutEnabled,
  isImpersonated: state.session.isImpersonated,
  logoLabel: state.session.logoLabel,
  logoUrl: state.session.logoUrl,
  username: state.session.user.username,
});

export default connect(mapStateToProps)(Header);

Header.propTypes = {
  helpScoutColor: PropTypes.string,
  helpScoutKey: PropTypes.string,
  highestRole: PropTypes.string.isRequired,
  isHelpScoutEnabled: PropTypes.string,
  isImpersonated: PropTypes.bool.isRequired,
  logoLabel: PropTypes.string.isRequired,
  logoUrl: PropTypes.string.isRequired,
  username: PropTypes.string.isRequired,
};

Header.defaultProps = {
  helpScoutColor: '',
  helpScoutKey: '',
  isHelpScoutEnabled: false,
};
