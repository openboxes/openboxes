import React from 'react';

import { render, screen } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';

import Menu from 'components/Layout/menu/Menu';
import MenuConfigurationSubsection from 'components/Layout/menu/MenuConfigurationSubsection';
import MenuItem from 'components/Layout/menu/MenuItem';

import store from '../../store';

let renderedMenu;
let renderedMenuConfigurationSubsection;
let renderedMenuItem;
let subsection;

describe('menu component', () => {
  beforeEach(() => {
    const menuConfig = [{
      id: 'configuration',
      label: 'Configuration',
      href: '/openboxes/configuration/index',
    }];
    renderedMenu = render(
      <Router>
        <Menu
          menuConfig={menuConfig}
          location={[]}
          store={store}
        />
      </Router>,
    );
  });

  it('should match snapshot', () => {
    expect(renderedMenu.asFragment())
      .toMatchSnapshot();
  });
});

describe('menuConfigurationSubsection component', () => {
  beforeEach(() => {
    subsection = {
      label: 'Administration',
      menuItems: [{
        label: 'Settings',
        href: '/openboxes/admin/showSettings',
      }],
    };
    renderedMenuConfigurationSubsection = render(
      <Router><MenuConfigurationSubsection subsection={subsection} /></Router>,
    );
  });

  it('should map menu items correctly', () => {
    expect(screen.getByText('Settings'))
      .toBeTruthy();
  });

  it('should match snapshot', () => {
    expect(renderedMenuConfigurationSubsection.asFragment())
      .toMatchSnapshot();
  });
});

describe('menuItem component', () => {
  beforeEach(() => {
    renderedMenuItem = render(<Router><MenuItem section={{}} active={false} /></Router>);
  });

  it('should match snapshot', () => {
    expect(renderedMenuItem.asFragment())
      .toMatchSnapshot();
  });

  it('should render collapseMenuItem and dropdownMenuItem correctly', () => {
    expect(renderedMenuItem.getByTestId('collapseMenuItem'))
      .toBeTruthy();
    expect(renderedMenuItem.getByTestId('dropdownMenuItem'))
      .toBeTruthy();
  });
});
