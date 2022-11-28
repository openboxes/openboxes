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
      if (section.menuItems) {
        return {
          ...acc,
          [section.label]: section.menuItems.flatMap(({ href }) => href),
        };
      }
    }
    return acc;
  }, {});

  const checkActiveSection = (menuUrls, path) => {
    const { pathname, search } = path;
    const matchedPath = Object.keys(menuUrls)
      .find((section) => {
        // find matching URL from sections
        const foundURL = menuUrls[section].find((url) => {
          const [sectionPath, sectionSearch] = url.split('?');
          if (sectionPath !== pathname) return false;
          // if found matching pathname
          // then check if all parameters of section path match with current path parameters
          if (sectionSearch) {
            const searchParams = sectionSearch.split('&');
            return search && searchParams.every(param => search.includes(param));
          }
          return true;
        });
        return !!foundURL;
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
          .filter(section => section.id !== 'configuration')
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

const menuItemPropType = PropTypes.shape({
  label: PropTypes.string,
  href: PropTypes.string,
});

const subsectionPropTypes = PropTypes.shape({
  label: PropTypes.string,
  menuItems: PropTypes.arrayOf(menuItemPropType),
});

const sectionPropTypes = PropTypes.shape({
  label: PropTypes.string,
  href: PropTypes.string,
  subsections: PropTypes.arrayOf(subsectionPropTypes),
  menuItems: PropTypes.arrayOf(menuItemPropType),
});

Menu.propTypes = {
  location: PropTypes.shape({
    pathname: PropTypes.string,
    search: PropTypes.string,
  }).isRequired,
  menuConfig: PropTypes.arrayOf(sectionPropTypes).isRequired,
};
