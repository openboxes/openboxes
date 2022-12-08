import React from 'react';

import { BrowserRouter as Router } from 'react-router-dom';
import renderer from 'react-test-renderer';
import { render, screen } from '@testing-library/react';

import Menu from 'components/Layout/menu/Menu';
import MenuConfigurationSubsection from 'components/Layout/menu/MenuConfigurationSubsection';
import MenuItem from 'components/Layout/menu/MenuItem';

import store from '../../store';

let renderedMenu;
let renderedMenuConfigurationSubsection;
let renderedMenuItem;
let subsection;

describe('test menu component', () => {
  beforeEach(() => {
    const menuConfig = [{
      id: 'configuration',
      label: 'Configuration',
      href: '/openboxes/configuration/index',
    }];
    renderedMenu = renderer.create(<Router><Menu
      menuConfig={menuConfig}
      location={[]}
      store={store}
    />
    </Router>);
  });

  it('test if menu component matches snapshot', () => {
    expect(renderedMenu.toJSON())
      .toMatchSnapshot();
  });
});


describe('test menuConfigurationSubsection component', () => {
  beforeEach(() => {
    subsection = {
      label: 'Administration',
      menuItems: [{
        label: 'Settings',
        href: '/openboxes/admin/showSettings',
      }],
    };
    renderedMenuConfigurationSubsection = renderer
      .create(<Router><MenuConfigurationSubsection subsection={subsection} /></Router>);
  });

  it('test if menuConfigurationSubsection component correctly mapped menu items', () => {
    render(<Router><MenuConfigurationSubsection subsection={subsection} /></Router>);
    expect(screen.findByText('Settings'))
      .toBeTruthy();
  });

  it('test if menuConfigurationSubsection matches snapshot', () => {
    expect(renderedMenuConfigurationSubsection.toJSON())
      .toMatchSnapshot();
  });
});


describe('test menuItem component', () => {
  beforeEach(() => {
    renderedMenuItem = renderer
      .create(<Router><MenuItem section={{}} active={false} /></Router>);
  });

  it('test if MenuItem matches snapshot', () => {
    expect(renderedMenuItem.toJSON())
      .toMatchSnapshot();
  });

  it('test if collapseMenuItem and dropdownMenuItem are correctly rendering', () => {
    expect(renderedMenuItem.root.findByProps({ className: 'collapse-nav-item nav-item justify-content-center align-items-center d-flex d-md-none' }))
      .toBeTruthy();
    expect(renderedMenuItem.root.findByProps({ className: 'nav-item dropdown d-none d-md-flex justify-content-center align-items-center false' }))
      .toBeTruthy();
  });
});
