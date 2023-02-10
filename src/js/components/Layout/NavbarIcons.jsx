import React, { useEffect, useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import {
  RiArrowDropDownLine,
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
import useElementSize from 'hooks/useElementSize';

const NavbarIcons = ({
  username, highestRole, menuItems, configurationMenuSection,
}) => {
  const windowSize = useElementSize(window);
  const [isMenuCollapsed, setIsMenuCollapsed] = useState(false);

  useEffect(() => {
    // We collapse the megamenu on medium (MD) breakpoint which starts at 768px of window size
    // Bootstrap 4 documentation on breakpoints
    // https://getbootstrap.com/docs/4.6/layout/overview/#responsive-breakpoints
    setIsMenuCollapsed(windowSize.width < 768);
  }, [windowSize.width]);

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
              <span className="subsection-section-title">
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
    <>
      {/* before MD (middle) breakpoint render below nav-icons (as collapsable) */}
      { isMenuCollapsed &&
        <div className=" px-3 mt-4">
          <li className="collapse-nav-item nav-item justify-content-center d-flex">
            <GlobalSearch visible className="w-100 my-2" />
          </li>
          <li className="collapse-nav-item nav-item justify-content-center align-items-center d-flex">
            <a
              className="nav-link d-flex justify-content-between align-items-center w-100"
              data-toggle="collapse"
              href="#collapse-profile"
              role="button"
              aria-expanded="false"
              aria-controls="collapse-profile"
            >
              <span className="d-flex align-items-center">
                <RiUser3Line className="mr-2" />
                Profile
              </span>
              <RiArrowDropDownLine className="collapse-arrow-icon" />
            </a>
            <div className="collapse w-100" id="collapse-profile">
              <div className="d-flex flex-column">
                <span className="subsection-section-title">
                  {username && username} {highestRole && `(${highestRole})`}
                </span>
                {menuItems && menuItems.map(item => (
                  <a className="subsection-section-item" key={item.label} href={item.linkAction}>
                    <span className="icon">
                      {findIcon(item.linkReactIcon)}
                    </span> {item.label}
                  </a>
                ))}
              </div>
            </div>
          </li>
          {configurationMenuSubsections.length > 0 &&
          <li className="collapse-nav-item nav-item justify-content-center align-items-center d-flex">
            <a
              className="nav-link d-flex justify-content-between align-items-center w-100"
              data-toggle="collapse"
              href="#collapse-configuration"
              role="button"
              aria-expanded="false"
              aria-controls="collapse-configuration"
            >
              <span className="d-flex align-items-center">
                <RiSettings5Line className="mr-2" />
                Configuration
              </span>
              <RiArrowDropDownLine className="collapse-arrow-icon" />
            </a>
            <div className="collapse w-100" id="collapse-configuration">
              <div className="d-flex flex-row flex-wrap">
                {_.map(configurationMenuSubsections, (subsection, subsectionKey) => (
                  <div key={subsectionKey} className="d-flex flex-column m-3">
                    {subsection.label &&
                    <span className="subsection-section-title">{subsection.label}</span>}
                    {_.map(subsection.menuItems, (menuItem, menuItemKey) => (
                      <a
                        className="subsection-section-item"
                        key={menuItemKey}
                        href={menuItem.href}
                        target={menuItem.target}
                      >
                        {menuItem.label}
                      </a>
                    ))}
                  </div>
              ))}
              </div>
            </div>
          </li>
        }
          <li className="nav-item collapse-nav-item d-flex dropdown justify-content-start justify-content-md-center align-items-center mb-2">
            <a href="#" className="nav-link w-100 d-flex align-items-center">
              <HelpScout className="d-flex align-items-center gap-8 w-100" text="Help" />
            </a>
          </li>
        </div>}
      {/* after MD (middle) breakpoint render below nav-icons (as dropdowns) */}
      { !isMenuCollapsed &&
      <div className="d-flex align-items-center justify-content-end navbar-icons">
        {iconsList
          .filter(({ hide }) => !hide)
          .map(({ name, ...restProps }) =>
            (<NavbarIcon key={name} {...restProps} />))}
      </div>}
      </>
  );
};


const mapStateToProps = state => ({
  username: state.session.user.username,
  highestRole: state.session.highestRole,
  menuItems: state.session.menuItems,
  configurationMenuSection: _.find(state.session.menuConfig, section => section.id === 'configuration'),
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

