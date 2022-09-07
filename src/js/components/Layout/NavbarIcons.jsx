import React, { useState } from 'react';

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
import { Tooltip } from 'react-tippy';

import MenuConfigurationSubsection from 'components/Layout/menu/MenuConfigurationSubsection';
import HelpScout from 'components/support-button/HelpScout';

const NavbarIcons = ({
  username, highestRole, menuItems, configurationMenuSection,
}) => {
  const [disableProfileTooltip, setDisableProfileTooltip] = useState(false);
  const [disableConfigurationTooltip, setDisableConfigurationTooltip] = useState(false);
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
  const iconsList = [
    {
      component: RiSearchLine,
      tooltip: 'Search',
    },
    {
      component: HelpScout,
      tooltip: 'Help',
    },
    {
      component: RiSettings5Line,
      tooltip: 'Configuration',
      isTooltipDisabled: disableConfigurationTooltip,
      dropdown: (
        <div className="btn-group">
          <RiSettings5Line
            className="dropdown-toggle"
            data-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
          />
          <div
            className="dropdown-menu dropdown-menu-right nav-item padding-8 margin-top-17"
            onMouseEnter={() => setDisableConfigurationTooltip(true)}
            onMouseLeave={() => setDisableConfigurationTooltip(false)}
          >
            <div className="dropdown-menu-subsections conf-subsections">
              {configurationMenuSection &&
                configurationMenuSection.subsections
                && configurationMenuSection.subsections.map(subsection => (
                  <MenuConfigurationSubsection subsection={subsection} key={`${subsection.label}-subsection`} />
              ))}
            </div>
          </div>
        </div>
      ),
    },
    {
      component: RiUser3Line,
      tooltip: 'Profile',
      isTooltipDisabled: disableProfileTooltip,
      dropdown: (
        <div className="btn-group">
          <RiUser3Line
            className="dropdown-toggle"
            data-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
          />
          <div
            className="dropdown-menu dropdown-menu-right nav-item padding-8"
            onMouseEnter={() => setDisableProfileTooltip(true)}
            onMouseLeave={() => setDisableProfileTooltip(false)}
          >
            <span className="subsection-title">{username && username} {highestRole && `(${highestRole})`}</span>
            {menuItems && menuItems.map(item => (
              <a className="dropdown-item" key={item.label} href={item.linkAction}>
                <span className="icon">
                  {findIcon(item.linkReactIcon)}
                </span> {item.label}
              </a>
              ))}
          </div>
        </div>
      ),
    },
  ];


  const renderIcon = (icon, idx) => {
    const Icon = icon.component;
    return (
      <Tooltip
        html={<div className="navbar-buttons-tooltip">{icon.tooltip}</div>}
        theme="transparent"
        key={idx}
        disabled={icon.isTooltipDisabled ? icon.isTooltipDisabled : false}
      >
        <React.Fragment>
          <div className="menu-icon">
            {icon.dropdown ? icon.dropdown : <Icon />}
          </div>
        </React.Fragment>
      </Tooltip>
    );
  };

  return (
    <div className="d-flex align-items-center justify-content-end navbar-icons">
      {iconsList.map((icon, idx) => renderIcon(icon, idx))}
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

