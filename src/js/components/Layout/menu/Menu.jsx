import React, { useMemo } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { useParams, withRouter } from 'react-router-dom';

import MenuItem from 'components/Layout/menu/MenuItem';
import MenuSection from 'components/Layout/menu/MenuSection';
import MenuSubsection from 'components/Layout/menu/MenuSubsection';
import { checkActiveSection, getAllMenuUrls } from 'utils/menu-utils';

const Menu = ({ menuConfig, location, menuSectionsUrlParts }) => {
  const params = useParams();

  const allMenuUrls = useMemo(() => getAllMenuUrls(menuConfig), [menuConfig]);
  const activeSection = useMemo(() =>
    checkActiveSection({
      menuUrls: allMenuUrls,
      path: location,
      params,
      menuSectionsUrlParts,
    }), [allMenuUrls, location]);

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
                  active={activeSection === section.id}
                />
              );
            }
            if (section.subsections) {
              return (
                <MenuSubsection
                  section={section}
                  key={`${section.label}-menu-subsection`}
                  active={activeSection === section.id}
                />
              );
            }
            if (section.menuItems) {
              return (
                <MenuItem
                  section={section}
                  key={`${section.label}-menuItem`}
                  active={activeSection === section.id}
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
  menuSectionsUrlParts: state.session.menuSectionsUrlParts,
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
  menuSectionsUrlParts: PropTypes.shape({
    inventory: PropTypes.arrayOf(PropTypes.string),
    products: PropTypes.arrayOf(PropTypes.string),
    purchasing: PropTypes.arrayOf(PropTypes.string),
    invoicing: PropTypes.arrayOf(PropTypes.string),
    inbound: PropTypes.arrayOf(PropTypes.string),
    outbound: PropTypes.arrayOf(PropTypes.string),
    requisitionTemplate: PropTypes.arrayOf(PropTypes.string),
    configuration: PropTypes.arrayOf(PropTypes.string),
    injectedDirectly: PropTypes.arrayOf(PropTypes.string),
  }).isRequired,
  menuConfig: PropTypes.arrayOf(sectionPropTypes).isRequired,
};
