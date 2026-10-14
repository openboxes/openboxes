import React from 'react';

import {
  act, fireEvent, render, screen, within,
} from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Switch } from 'react-router-dom';
import testStore from 'regression-tests/data-table/testStore';

import '@testing-library/jest-dom';

// The location chooser has tests of its own; here it only takes its place in the header.
jest.mock('components/location/LocationChooser', () => () => <div>Location chooser</div>);

// The info bars load their translations through the client; every call answers empty.
// The response helpers the actions use come from the real module, loaded on first use.
jest.mock('utils/apiClient', () => {
  const actual = (name) => (...args) => jest.requireActual('utils/apiClient')[name](...args);
  return {
    // eslint-disable-next-line global-require
    ...require('regression-tests/__mocks__/apiClient').default(),
    parseResponse: actual('parseResponse'),
    handleError: actual('handleError'),
  };
});

// 'react-icons/all' is an ES module jest does not transform; any icon renders nothing here.
jest.mock('react-icons/all', () => new Proxy({}, { get: () => () => null }));

// The collapsed menus build their ids with String.replaceAll, which every supported browser has
// but the Node the tests run on does not; a plain string version stands in for it.
if (!String.prototype.replaceAll) {
  // eslint-disable-next-line no-extend-native, func-names
  String.prototype.replaceAll = function (search, replacement) {
    return this.split(search).join(replacement);
  };
}

/* eslint-disable import/first,import/order */
import Menu from 'components/Layout/menu/Menu';
import MainLayoutRoute from 'components/Layout/v2/MainLayoutRoute';
import apiClient from 'utils/apiClient';
/* eslint-enable import/first,import/order */

const MENU_CONFIG = [
  { id: 'dashboard', label: 'Dashboard', href: '/openboxes/dashboard/index' },
  {
    id: 'inventory',
    label: 'Inventory',
    subsections: [{
      label: 'Stock',
      menuItems: [{ label: 'Browse', href: '/openboxes/inventory/browse', target: '_self' }],
    }],
  },
  {
    id: 'reporting',
    label: 'Reports',
    menuItems: [{ label: 'Stock report', href: '/openboxes/report/stock' }],
  },
  { id: 'empty', label: 'Empty section' },
  {
    id: 'configuration',
    label: 'Configuration',
    subsections: [{
      label: 'Admin',
      menuItems: [{ label: 'Settings', href: '/openboxes/admin/showSettings', target: '_blank' }],
    }],
  },
];

const PROFILE_ITEMS = [
  { label: 'Edit profile', linkAction: '/openboxes/user/edit', linkReactIcon: 'profile' },
  { label: 'Log out', linkAction: '/openboxes/auth/logout', linkReactIcon: 'logout' },
  { label: 'Flush cache', linkAction: '/openboxes/admin/flush', linkReactIcon: 'flush-cache' },
  { label: 'Localization', linkAction: '/openboxes/l10n', linkReactIcon: 'localization-mode' },
  { label: 'Other', linkAction: '/openboxes/other', linkReactIcon: 'unknown' },
];

const storeWith = (session = {}) => {
  const store = testStore({ highestRole: 'Admin', menuItems: PROFILE_ITEMS, ...session });
  store.dispatch({ type: 'FETCH_MENU_CONFIG', payload: { data: { data: { menuConfig: MENU_CONFIG } } } });
  return store;
};

// The whole header renders in every test, which takes a few seconds on a busy machine.
jest.setTimeout(30000);

const Page = () => <p>Page content</p>;

const renderLayout = (store, path = '/openboxes/inventory/browse') => render(
  <Provider store={store}>
    <MemoryRouter initialEntries={[path]}>
      <Switch>
        <MainLayoutRoute path="/openboxes" component={Page} />
      </Switch>
    </MemoryRouter>
  </Provider>,
);

describe('main layout', () => {
  let innerWidth;

  beforeEach(() => {
    ({ innerWidth } = window);
    apiClient.get.mockResolvedValue({ data: { data: [], messages: {} } });
  });

  afterEach(() => {
    window.innerWidth = innerWidth;
    jest.clearAllMocks();
  });

  it('wraps the page in the header with the menu, and marks the current section', async () => {
    renderLayout(storeWith());

    expect(screen.getByText('Page content').closest('main')).toHaveClass('page-layout__main');
    expect(screen.getByText('Location chooser')).toBeInTheDocument();
    const menu = document.querySelector('#navbarSupportedContent');
    // The plain link, the dropdown and the item sections; the configuration one goes in the icons
    expect(within(menu).getByRole('menuitem', { name: 'Dashboard' })).toHaveAttribute('href', '/openboxes/dashboard/index');
    expect(within(menu).getAllByText('Browse')[0]).toHaveAttribute('href', '/openboxes/inventory/browse');
    expect(within(menu).getAllByText('Stock report')[0]).toHaveAttribute('href', '/openboxes/report/stock');
    expect(within(menu).queryByText('Empty section')).toBeNull();
    expect(within(menu).queryByText('Settings')).toBeNull();
    // The current page is in the inventory section
    expect(within(menu).getByRole('menuitem', { name: 'Inventory' }).closest('li')).toHaveClass('active-section');
    expect(within(menu).getByRole('menuitem', { name: 'Dashboard' }).closest('li')).not.toHaveClass('active-section');

    // Hovering a dropdown measures the room on its right (jsdom has no layout: all the room),
    // and aligns it to the left when it fits
    const dropdown = within(menu).getByRole('menuitem', { name: 'Inventory' }).closest('li');
    const dropdownMenu = dropdown.querySelector('.dropdown-menu');
    expect(dropdownMenu).toHaveClass('dropdown-menu-right');
    fireEvent.mouseEnter(dropdown);
    expect(dropdownMenu).toHaveClass('dropdown-menu-left');
    fireEvent.mouseLeave(dropdown);
    expect(dropdownMenu).toHaveClass('dropdown-menu-right');
  });

  it('shows no banner for a regular session', async () => {
    renderLayout(storeWith());

    expect(screen.queryByRole('alert', { name: 'impersonate' })).toBeNull();
    expect(screen.queryByRole('alert', { name: 'localization' })).toBeNull();
  });

  it('warns when impersonating a user or in localization mode', async () => {
    renderLayout(storeWith({ isImpersonated: true, localizationModeEnabled: true }));

    const impersonating = screen.getByRole('alert', { name: 'impersonate' });
    expect(impersonating).toHaveTextContent('You are impersonating user');
    expect(within(impersonating).getByText('admin')).toBeInTheDocument();
    expect(within(impersonating).getByRole('link')).toHaveAttribute('href', '/openboxes/auth/logout');

    const localization = screen.getByRole('alert', { name: 'localization' });
    expect(localization).toHaveTextContent('The localization mode is active');
    expect(within(localization).getByRole('link').getAttribute('href')).toMatch(/disableLocalizationMode/);
  });

  it('shows the info bars that are visible', async () => {
    const store = storeWith();
    store.dispatch({
      type: 'ADD_INFO_BAR',
      payload: {
        name: 'RECEIVING',
        show: true,
        title: { label: 'react.infoBar.receiving.title', defaultLabel: 'New receiving' },
        versionLabel: { label: 'react.infoBar.receiving.version', defaultLabel: 'New' },
      },
    });
    renderLayout(store);

    expect(screen.getByText('New receiving')).toBeInTheDocument();
    act(() => { store.dispatch({ type: 'HIDE_INFO_BAR', payload: { name: 'RECEIVING' } }); });
    expect(screen.queryByText('New receiving')).toBeNull();
  });

  it('shows the profile and the configuration menus as icons on a wide screen', async () => {
    window.innerWidth = 1200;
    renderLayout(storeWith());

    const icons = screen.getAllByTestId('navbar-icon').map((icon) => icon.getAttribute('aria-label'));
    expect(icons).toEqual(['search', 'help', 'configuration', 'profile']);
    const profile = screen.getByLabelText('profile');
    expect(profile).toHaveTextContent('admin (Admin)');
    expect(within(profile).getByRole('menuitem', { name: /Edit profile/ })).toHaveAttribute('href', '/openboxes/user/edit');
    expect(within(profile).getAllByRole('menuitem')).toHaveLength(PROFILE_ITEMS.length);
    const configuration = within(screen.getByLabelText('configuration'));
    expect(configuration.getByText('Settings')).toBeInTheDocument();
    fireEvent.mouseEnter(configuration.getByRole('menu'));
    fireEvent.mouseLeave(configuration.getByRole('menu'));

    // Hovering an open menu holds its tooltip back; leaving lets it show again
    const menu = within(profile).getByRole('menu');
    fireEvent.mouseEnter(menu);
    fireEvent.mouseLeave(menu);
    expect(menu).toBeInTheDocument();
  });

  it('hides the configuration icon when there is nothing to configure', async () => {
    window.innerWidth = 1200;
    const store = testStore({ highestRole: 'Admin', menuItems: [] });
    store.dispatch({ type: 'FETCH_MENU_CONFIG', payload: { data: { data: { menuConfig: MENU_CONFIG.slice(0, 1) } } } });
    renderLayout(store);

    const icons = screen.getAllByTestId('navbar-icon').map((icon) => icon.getAttribute('aria-label'));
    expect(icons).toEqual(['search', 'help', 'profile']);
  });

  it('collapses the icons into the menu on a narrow screen', async () => {
    window.innerWidth = 600;
    renderLayout(storeWith());

    expect(screen.queryAllByTestId('navbar-icon')).toHaveLength(0);
    const profile = document.querySelector('#collapse-profile');
    expect(profile).toHaveTextContent('admin (Admin)');
    expect(within(profile).getByText('Log out').closest('a')).toHaveAttribute('href', '/openboxes/auth/logout');
    const configuration = document.querySelector('#collapse-configuration');
    expect(within(configuration).getByText('Admin')).toHaveClass('subsection-section-title');
    expect(within(configuration).getByText('Settings')).toHaveAttribute('target', '_blank');
  });
});

describe('menu', () => {
  it('falls back to the section from the url parts, then to the dashboard', () => {
    const store = storeWith();
    store.dispatch({
      type: 'FETCH_MENU_CONFIG',
      payload: { data: { data: { menuConfig: MENU_CONFIG, menuSectionsUrlParts: { reporting: ['/report/'] } } } },
    });
    const renderMenu = (path) => render(
      <Provider store={store}>
        <MemoryRouter initialEntries={[path]}>
          <Menu />
        </MemoryRouter>
      </Provider>,
    );

    const { unmount } = renderMenu('/openboxes/report/other');
    expect(screen.getAllByTestId('dropdownMenuItem')[0]).toHaveClass('active-section');
    unmount();

    renderMenu('/openboxes/nowhere');
    expect(screen.getByRole('menuitem', { name: 'Dashboard' }).closest('li')).toHaveClass('active-section');
  });
});
