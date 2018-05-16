import React from 'react';
import _ from 'lodash';
import { Scrollbars } from 'react-custom-scrollbars';
import { Translate } from 'react-localize-redux';
import en from '../../en';

const { navbar } = en;

const Menu = () => (
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
                    className="dropdown-item"
                    key={subKey}
                    href={subsection.link}
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

export default Menu;
