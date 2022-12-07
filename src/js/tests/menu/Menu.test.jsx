import React from 'react';

import { BrowserRouter as Router } from 'react-router-dom';
import renderer from 'react-test-renderer';

import Menu from 'components/Layout/menu/Menu';
import MenuConfigurationSubsection from 'components/Layout/menu/MenuConfigurationSubsection';
import MenuItem from 'components/Layout/menu/MenuItem';

import store from '../../store';

it('test if menu component has sections', () => {
  const menuConfig = [{
    id: 'dashboard',
    label: 'Dashboard',
    href: '/openboxes/dashboard/index',
  }];
  const renderedMenu = renderer
    .create(<Router><Menu menuConfig={menuConfig} location={[]} store={store} /></Router>);
  const renderedMenuInstance = renderedMenu.root;
  expect(renderedMenuInstance.findByProps({ key: menuConfig.label }))
    .toBeTruthy();
});

it('test if menu component matches snapshot', () => {
  const menu = renderer
    .create(<Router><Menu menuConfig={{}} location={[]} store={store} /></Router>)
    .toJSON();
  expect(menu)
    .toMatchSnapshot();
});

it('test if menuConfigurationSubsection component correctly map menu items', () => {
  const subsection = {
    label: 'Administration',
    menuItems: [{
      label: 'Settings',
      href: '/openboxes/admin/showSettings',
    }],
  };
  const renderedMenuConfigurationSubsection = renderer
    .create(<Router><MenuConfigurationSubsection subsection={subsection} /></Router>);
  const renderedMenuInstance = renderedMenuConfigurationSubsection.root;
  expect(renderedMenuInstance.findByProps({ key: subsection.menuItems.label }))
    .toBeTruthy();
});

it('test if menuConfigurationSubsection matches snapshot', () => {
  const renderedMenuConfigurationSubsection = renderer
    .create(<Router><MenuConfigurationSubsection subsection={{}} /></Router>)
    .toJSON();
  expect(renderedMenuConfigurationSubsection)
    .toMatchSnapshot();
});


it('test if MenuItem component is correctly rendering', () => {
  const renderedMenuItem = renderer
    .create(<Router><MenuItem section={{}} active={false} /></Router>)
    .toJSON();
  expect(renderedMenuItem)
    .toMatchSnapshot();
});


it('test if menu component is correctly rendering', () => {
  const renderedMenu = renderer
    .create(<Router><Menu menuConfig={{}} location={[]} store={store} /></Router>)
    .toJSON();
  expect(renderedMenu)
    .toMatchSnapshot();
});

it('test if collapseMenuItem and dropdownMenuItem are correctly rendering', () => {
  const renderedMenuItem = renderer
    .create(<Router><MenuItem section={{}} active={false} /></Router>).root;
  expect(renderedMenuItem.findByProps({ className: 'collapse-nav-item nav-item justify-content-center align-items-center d-flex d-md-none' }))
    .toBeTruthy();
  expect(renderedMenuItem.findByProps({ className: 'nav-item dropdown d-none d-md-flex justify-content-center align-items-center false' }))
    .toBeTruthy();
});
