import React from 'react';
import _ from 'lodash';
import { Scrollbars } from 'react-custom-scrollbars';
import translations from '../../en';

const { navbar } = translations;

const Menu = () => (
  <div className="collapse navbar-collapse w-100 menu-container" id="navbarSupportedContent">
    <ul className="navbar-nav mr-auto flex-wrap">
      { _.map(navbar, (section, key) => {
        if (!section.subsections) {
          return (
            <li className="nav-item" key={key}>
              <a className="nav-link" href={section.link}>{section.label}</a>
            </li>
          );
        }
        return (
          <li className="nav-item dropdown" key={key}>
            <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
              {section.label}
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
                    {subsection.label}
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
