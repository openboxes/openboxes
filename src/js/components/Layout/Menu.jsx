import React from 'react';
import translations from '../../en';

const { navbar } = translations;

const Menu = () => (
  <div className="collapse navbar-collapse w-100 menu-container" id="navbarSupportedContent">
    <ul className="navbar-nav mr-auto flex-wrap">
      { Object.entries(navbar).map((section) => {
        if (!section[1].subsections) {
          return (
            <li className="nav-item" key={section[0]}>
              <a className="nav-link" href={section[1].link}>{section[1].label}</a>
            </li>
          );
        }
        return (
          <li className="nav-item dropdown" key={section[0]}>
            <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
              {section[1].label}
            </a>
            <div className="dropdown-menu" aria-labelledby="navbarDropdown">
              {
                Object.entries(section[1].subsections).map(subsection => (
                  <a
                    className="dropdown-item"
                    key={subsection[0]}
                    href={subsection[1].link}
                  >
                    {subsection[1].label}
                  </a>
                ))
              }
            </div>
          </li>
        );
        })
      }
    </ul>
  </div>
);

export default Menu;
