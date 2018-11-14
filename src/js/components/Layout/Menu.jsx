import _ from 'lodash';
import React from 'react';
import { connect } from 'react-redux';
import { Scrollbars } from 'react-custom-scrollbars';
import { Translate } from 'react-localize-redux';
import PropTypes from 'prop-types';

import en from '../../en';

const { navbar } = en;

const Menu = ({
  hasBinLocationSupport, isSuperuser, isUserAdmin, menuConfig, supportedActivities,
}) => {
  const isPutAwayDisabled = subKey => subKey === 'createPutAway' && !hasBinLocationSupport;
  const isEnabled = sectionKey => (
    menuConfig ? _.get(menuConfig, sectionKey, {}).enabled || isSuperuser : true
  );
  const isSupported = ({ activity }) => (
    activity && supportedActivities ? !!_.intersection(supportedActivities, activity).length : true
  );
  const isDisplayed = ({ adminOnly }) => (adminOnly ? isUserAdmin : true);
  const canBeRendered = (key, section) =>
    isEnabled(key) && isSupported(section) && isDisplayed(section);

  return (
    <div className="collapse navbar-collapse w-100 menu-container" id="navbarSupportedContent">
      <ul className="navbar-nav mr-auto flex-wrap">
        { _.map(navbar, (section, key) => {
        if (!section.subsections && canBeRendered(key, section)) {
          return (
            <li className="nav-item" key={key}>
              <a className="nav-link" href={section.link}>
                <Translate id={`navbar.${key}.label`} />
              </a>
            </li>
          );
        }
        if (canBeRendered(key, section)) {
          return (
            <li className="nav-item dropdown" key={key}>
              <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
                <Translate id={`navbar.${key}.label`} />
              </a>
              <div className="dropdown-menu" aria-labelledby="navbarDropdown">
                <Scrollbars
                  autoHeight
                  autoHeightMin={0}
                  autoHeightMax="50vh"
                  hideTracksWhenNotNeeded
                  autoHide
                >
                  {
                    _.map(section.subsections, (subsection, subKey) => {
                      if (isDisplayed(subsection)) {
                        return (
                          <a
                            className={`dropdown-item ${isPutAwayDisabled(subKey) ? 'disabled' : ''}`}
                            key={subKey}
                            href={isPutAwayDisabled(subKey) ? '#' : subsection.link}
                          >
                            <Translate id={`navbar.${key}.subsections.${subKey}.label`} />
                          </a>
                        );
                      }
                      return null;
                    })
                  }
                </Scrollbars>
              </div>
            </li>
          );
        }
        return null;
        })
      }
      </ul>
    </div>
  );
};

const mapStateToProps = state => ({
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  isSuperuser: state.session.isSuperuser,
  isUserAdmin: state.session.isUserAdmin,
  menuConfig: state.session.menuConfig,
  supportedActivities: state.session.supportedActivities,
});

export default connect(mapStateToProps)(Menu);

Menu.propTypes = {
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  isSuperuser: PropTypes.bool.isRequired,
  isUserAdmin: PropTypes.bool.isRequired,
  menuConfig: PropTypes.shape({}).isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
};
