import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import {
  RiLoginBoxLine, RiMapPinLine,
  RiRefreshLine,
  RiSearchLine,
  RiSettings5Line,
  RiUser3Line,
} from 'react-icons/ri';
import { connect } from 'react-redux';

import GlobalSearch from 'components/GlobalSearch/GlobalSearch';
import MenuConfigurationSubsection from 'components/Layout/menu/MenuConfigurationSubsection';
import NavbarIcon from 'components/Layout/NavbarIcon';
import HelpScout from 'components/support-button/HelpScout';

const NavbarIcons = ({
  username, highestRole, menuItems, configurationMenuSection,
}) => {
  const findIcon = (icon) => {
    switch (icon) {
      case 'localization-mode':
        return <RiMapPinLine />;
      case 'profile':
        return <RiUser3Line />;
      case 'flush-cache':
        return <RiRefreshLine />;
      case 'logout':
        return <RiLoginBoxLine />;
      default:
        return '';
    }
  };

  const configurationMenuSubsections = _.get(configurationMenuSection, 'subsections', []);

  const iconsList = [
    {
      name: 'search',
      tooltip: 'Search',
      component: renderProps => (<GlobalSearch
        renderButton={({ showSearchbar, isVisible }) => {
          renderProps.setIsTooltipDisabled(isVisible);
          return (
            <button onClick={showSearchbar} className="menu-icon">
              <RiSearchLine />
            </button>);
        }}
      />),
    },
    {
      name: 'help',
      tooltip: 'Help',
      component: () => (<div className="menu-icon"><HelpScout /></div>),
    },
    {
      name: 'configuration',
      tooltip: 'Configuration',
      hide: configurationMenuSubsections.length === 0,
      component: renderProps => (
        <div className="btn-group">
          <div
            className="dropdown-toggle menu-icon"
            data-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
          >
            <RiSettings5Line />
          </div>
          <div
            className="dropdown-menu dropdown-menu-wrapper dropdown-menu-right nav-item padding-8"
            onMouseEnter={() => renderProps.setIsTooltipDisabled(true)}
            onMouseLeave={() => renderProps.setIsTooltipDisabled(false)}
          >
            <div className="dropdown-menu-subsections dropdown-menu-content conf-subsections">
              {configurationMenuSubsections
                .map(subsection =>
                  (<MenuConfigurationSubsection
                    key={`${subsection.label}-subsection`}
                    subsection={subsection}
                  />))}
            </div>
          </div>
        </div>
      ),
    },
    {
      name: 'profile',
      tooltip: 'Profile',
      component: renderProps => (
        <div className="btn-group">
          <div
            className="dropdown-toggle menu-icon"
            data-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
          >
            <RiUser3Line />
          </div>
          <div
            className="dropdown-menu dropdown-menu-wrapper dropdown-menu-right nav-item padding-8"
            onMouseEnter={() => renderProps.setIsTooltipDisabled(true)}
            onMouseLeave={() => renderProps.setIsTooltipDisabled(false)}
          >
            <div className="dropdown-menu-content">
              <span className="subsection-title">
                {username && username} {highestRole && `(${highestRole})`}
              </span>
              {menuItems && menuItems.map(item => (
                <a className="dropdown-item" key={item.label} href={item.linkAction}>
                  <span className="icon">
                    {findIcon(item.linkReactIcon)}
                  </span> {item.label}
                </a>
              ))}
            </div>
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="d-flex align-items-center justify-content-end navbar-icons">
      {iconsList
        .filter(({ hide }) => !hide)
        .map(({ name, ...restProps }) =>
        (<NavbarIcon key={name} {...restProps} />))}
    </div>
  );
};


const mapStateToProps = state => ({
  username: state.session.user.username,
  highestRole: state.session.highestRole,
  menuItems: state.session.menuItems,
  configurationMenuSection: _.find(state.session.menuConfig, section => section.label === 'Configuration'),
  localizedHelpScoutKey: state.session.localizedHelpScoutKey,
  isHelpScoutEnabled: state.session.isHelpScoutEnabled,
});

export default connect(mapStateToProps)(NavbarIcons);


NavbarIcons.propTypes = {
  username: PropTypes.string.isRequired,
  highestRole: PropTypes.string.isRequired,
  menuItems: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.string.isRequired,
    linkIcon: PropTypes.string.isRequired,
    linkAction: PropTypes.string.isRequired,
    linkReactIcon: PropTypes.string.isRequired,
  }).isRequired).isRequired,
  configurationMenuSection: PropTypes.shape({
    label: PropTypes.string,
    subsections: PropTypes.array,
  }),
};

NavbarIcons.defaultProps = {
  configurationMenuSection: {},
};

