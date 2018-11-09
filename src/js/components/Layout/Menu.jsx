import _ from 'lodash';
import React from 'react';
import { connect } from 'react-redux';
import { Scrollbars } from 'react-custom-scrollbars';
import { Translate } from 'react-localize-redux';
import PropTypes from 'prop-types';

import en from '../../en';

const { navbar } = en;

const Menu = ({ hasBinLocationSupport }) => {
  const isDisabled = subKey => subKey === 'createPutAway' && !hasBinLocationSupport;

  return (
    <div className="collapse navbar-collapse w-100 menu-container" id="navbarSupportedContent">
      <ul className="navbar-nav mr-auto flex-wrap">
        { _.map(navbar, (section, key) => {
        if (!section.subsections) {
          return (
            <li className="nav-item" key={key}>
              <a className="nav-link" href={section.link}>
                <Translate id={`navbar.${key}.label`} />
              </a>
            </li>
          );
        }
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
                _.map(section.subsections, (subsection, subKey) => (
                  <a
                    className={`dropdown-item ${isDisabled(subKey) ? 'disabled' : ''}`}
                    key={subKey}
                    href={isDisabled(subKey) ? '#' : subsection.link}
                  >
                    <Translate id={`navbar.${key}.subsections.${subKey}.label`} />
                  </a>
                ))
                }
              </Scrollbars>
            </div>
          </li>
        );
        })
      }
      </ul>
    </div>
  );
};

const mapStateToProps = state => ({
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
});

export default connect(mapStateToProps)(Menu);

Menu.propTypes = {
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
};
