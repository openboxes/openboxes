import React from 'react';

import { render, screen } from '@testing-library/react';
import { BrowserRouter as Router } from 'react-router-dom';
import renderer from 'react-test-renderer';

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
    renderedMenu = renderer.create(<Router><Menu
      menuConfig={menuConfig}
      location={[]}
      store={store}
    />
    </Router>);
  });

  it('should match snapshot', () => {
    expect(renderedMenu.toJSON())
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
    renderedMenuConfigurationSubsection = renderer
      .create(<Router><MenuConfigurationSubsection subsection={subsection} /></Router>);
  });

  it('should map menu items correctly', () => {
    render(<Router><MenuConfigurationSubsection subsection={subsection} /></Router>);
    expect(screen.findByText('Settings'))
      .toBeTruthy();
  });

  it('should match snapshot', () => {
    expect(renderedMenuConfigurationSubsection.toJSON())
      .toMatchSnapshot();
  });
});


describe('menuItem component', () => {
  beforeEach(() => {
    renderedMenuItem = renderer
      .create(<Router><MenuItem section={{}} active={false} /></Router>);
  });

  it('should match snapshot', () => {
    expect(renderedMenuItem.toJSON())
      .toMatchSnapshot();
  });

  it('should render collapseMenuItem and dropdownMenuItem correctly', () => {
    expect(renderedMenuItem.root.findByProps({ 'data-testid': 'collapseMenuItem' }))
      .toBeTruthy();
    expect(renderedMenuItem.root.findByProps({ 'data-testid': 'dropdownMenuItem' }))
      .toBeTruthy();
  });
});
