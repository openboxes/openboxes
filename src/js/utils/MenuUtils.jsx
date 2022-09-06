import React from 'react';

import _ from 'lodash';

export function getMenuItemsComponent(section, key, active = false) {
  return (
    <li className={`nav-item dropdown d-flex justify-content-center align-items-center ${active && 'active-section'}`} key={key}>
      <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
        {section.label}
      </a>
      <div className="dropdown-menu" aria-labelledby="navbarDropdown">
        <div style={{ maxHeight: '60vh', overflow: 'auto' }} className="px-3 py-1">
          {_.map(section.menuItems, (menuItem, menuItemKey) => (
            <a className="dropdown-item" key={menuItemKey} href={menuItem.href} target={menuItem.target}>
              {menuItem.label}
            </a>
          ))}
        </div>
      </div>
    </li>
  );
}

export function getSubsectionComponent(section, key, active = false) {
  return (
    <li className={`nav-item dropdown d-flex justify-content-center align-items-center ${active && 'active-section'}`} key={key}>
      <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
        {section.label}
      </a>
      <div className={`dropdown-menu ${section.label === 'Reporting' && 'dropdown-menu-right'}`} aria-labelledby="navbarDropdown">
        <div className="dropdown-menu-subsections">
          {_.map(section.subsections, (subsection, subsectionKey) => (
            <div className="padding-8" key={subsectionKey}>
              {subsection.label && <span className="subsection-title">{subsection.label}</span>}
              {_.map(subsection.menuItems, (menuItem, menuItemKey) => (
                <a className="dropdown-item" key={menuItemKey} href={menuItem.href} target={menuItem.target}>
                  {menuItem.label}
                </a>
              ))}
            </div>
          ))}
        </div>
      </div>
    </li>
  );
}

export function getSectionComponent(section, key, active = false) {
  return (
    <li className={`nav-item d-flex justify-content-center align-items-center ${active && 'active-section'}`} key={key}>
      <a className="nav-link" href={section.href}>
        {section.label}
      </a>
    </li>
  );
}

export function getConfigurationSubsections(subsection, key) {
  return (
    <div className="padding-8" key={key}>
      <span className="subsection-title">{subsection.label && subsection.label}</span>
      {subsection.menuItems && subsection.menuItems.map((menuItem, menuItemKey) => (
        // eslint-disable-next-line react/no-array-index-key
        <a className="dropdown-item" key={menuItemKey} href={menuItem.href} target={menuItem.target}>
          {menuItem.label}
        </a>
    ))}
    </div>
  );
}
