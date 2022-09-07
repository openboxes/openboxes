import React, { useMemo } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import MenuItem from 'components/Layout/menu/MenuItem';
import MenuSection from 'components/Layout/menu/MenuSection';
import MenuSubsection from 'components/Layout/menu/MenuSubsection';


const Menu = ({ menuConfig, location }) => {
  const getAllMenuUrls = () => Object.entries(menuConfig).reduce((acc, [, section]) => {
    if (!acc[section.label]) {
      if (section.href) {
        return {
          ...acc,
          [section.label]: [section.href],
        };
      }
      if (section.subsections) {
        return {
          ...acc,
          // eslint-disable-next-line max-len
          [section.label]: section.subsections.flatMap(subsection => subsection.menuItems.map(item => item.href)),
        };
      }
    }
    return acc;
  }, {});

  const checkActiveSection = (menuUrls, path) => {
    // eslint-disable-next-line max-len
    // Concat currentPath, because react-router recognizes params (?exampleParam) and it moves this part from pathname and moves it to search
    // eslint-disable-next-line max-len
    // e.g. /openboxes/stockMovement/createInbound?directon=INBOUND => pathname: /openboxes/stockMovement/createInbound/ search: ?direction=INBOUND
    const currentPath = path.pathname.concat('', path.search);
    // eslint-disable-next-line max-len
    // slice currentPath to max length of 4, so we look only at e.g. /openboxes/invoice/create/, so we don't need to check additional /invoicing/create/:id/:param1/:param2, etc.
    const splittedCurrentPath = currentPath.split('/').slice(0, 4);
    const matchedPath = Object.keys(menuUrls).find((section) => {
      const splittedSectionUrls = menuUrls[section].map(url => url.split('/').slice(0, 4));
      // Check if any of section's spllited url matches currentPath
      return splittedSectionUrls.some((url) => {
        if (splittedCurrentPath.length === url.length) {
          return !!splittedCurrentPath.every((el, idx) => el === url[idx]);
        }
        return false;
      });
    });
    return matchedPath || 'Dashboard';
  };
  const allMenuUrls = useMemo(() => getAllMenuUrls(menuConfig), [menuConfig]);
  const activeSection = useMemo(() =>
    checkActiveSection(allMenuUrls, location), [allMenuUrls, location]);


  return (
    <div className="menu-wrapper" id="navbarSupportedContent">
      <ul className="d-flex align-items-center navbar-nav mr-auto flex-wrap">
        { _.chain(menuConfig)
          .filter(section => section.label !== 'Configuration')
          .map((section) => {
            if (section.href) {
              return (
                <MenuSection
                  section={section}
                  key={`${section.label}-menu-section`}
                  active={activeSection === section.label}
                />
              );
            }
            if (section.subsections) {
              return (
                <MenuSubsection
                  section={section}
                  key={`${section.label}-menu-subsection`}
                  active={activeSection === section.label}
                />
              );
            }
            if (section.menuItems) {
              return (
                <MenuItem
                  section={section}
                  key={`${section.label}-menuItem`}
                  active={activeSection === section.label}
                />
              );
            }
            return null;
          })
          .value()
      }
      </ul>
    </div>
  );
};

const mapStateToProps = state => ({
  menuConfig: state.session.menuConfig,
});

export default withRouter(connect(mapStateToProps)(Menu));

Menu.propTypes = {
  location: PropTypes.shape({
    pathname: PropTypes.string,
    search: PropTypes.string,
  }).isRequired,
  menuConfig: PropTypes.shape([]).isRequired,
};
