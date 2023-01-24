import { checkActiveSection, getAllMenuUrls } from 'utils/menu-utils';

describe('getAllMenuUrls', () => {
  it('should return section with url', () => {
    const menuConfig = [{
      id: 'Dashboard',
      href: '/openboxes/dashboard/index',
    }];
    const menuUrls = { Dashboard: ['/openboxes/dashboard/index'] };
    expect(getAllMenuUrls(menuConfig)).toStrictEqual(menuUrls);
  });
  it('should return subsection with url', () => {
    const menuConfig = [{
      id: 'Inventory',
      subsections: [
        {
          id: 'Browse inventory',
          menuItems: [
            {
              id: 'Browse inventory',
              href: '/openboxes/inventory/browse?resetSearch=true',
            },
          ],
        },
      ],
    }];
    const menuUrls = { Inventory: ['/openboxes/inventory/browse?resetSearch=true'] };
    expect(getAllMenuUrls(menuConfig)).toStrictEqual(menuUrls);
  });
  it('should return menu item with url', () => {
    const menuConfig = [{
      id: 'Stocklists',
      menuItems: [
        {
          id: 'List stock lists',
          href: '/openboxes/requisitionTemplate/list',
        },
      ],
    }];
    const menuUrls = { Stocklists: ['/openboxes/requisitionTemplate/list'] };
    expect(getAllMenuUrls(menuConfig)).toStrictEqual(menuUrls);
  });
});

describe('checkActiveSection', () => {
  let menuUrls;
  let params;
  beforeAll(() => {
    const menuConfig = [{
      id: 'dashboard',
      href: '/openboxes/dashboard/index',
    },
    {
      id: 'inbound',
      subsections: [
        {
          label: 'stock movements',
          menuItems: [
            {
              label: 'create inbound movement',
              href: '/openboxes/stockMovement/createInbound?direction=INBOUND',
            },
          ],
        },
        {
          id: 'putaways',
          menuItems: [
            {
              label: 'Create Putaway',
              href: '/openboxes/putAway/create',
            },
            {
              label: 'List Putaways',
              href: '/openboxes/order/list?orderType=PUTAWAY_ORDER&status=PENDING',
            },
          ],
        },
      ],
    },
    ];
    menuUrls = getAllMenuUrls(menuConfig);
    params = { 0: 'openboxes/', 1: '' };
  });
  it('should return dashboard when there is no matching url', () => {
    const path = { pathname: '/openboxes/test/urls', search: '' };
    expect(checkActiveSection({
      menuUrls, path, params, menuSectionsUrlParts: {},
    })).toBe('dashboard');
  });
  it('should return dashboard when there is no matching query params', () => {
    const path = { pathname: '/openboxes/order/list', search: '?testKey=testValue' };
    expect(checkActiveSection({
      menuUrls, path, params, menuSectionsUrlParts: {},
    })).toBe('dashboard');
  });
  it('should match path with params', () => {
    const path = { pathname: '/openboxes/order/list', search: '?orderType=PUTAWAY_ORDER&status=PENDING' };
    expect(checkActiveSection({
      menuUrls, path, params, menuSectionsUrlParts: {},
    })).toBe('inbound');
  });
  it('should skip id in path', () => {
    params = { ...params, id: 'randomId' };
    const path = { pathname: '/openboxes/putAway/create', search: '' };
    expect(checkActiveSection({
      menuUrls, path, params, menuSectionsUrlParts: {},
    })).toBe('inbound');
  });
  it('should match path without params', () => {
    const path = { pathname: '/openboxes/putAway/create', search: '' };
    expect(checkActiveSection({
      menuUrls, path, params, menuSectionsUrlParts: {},
    })).toBe('inbound');
  });
  it('should skip direction in path', () => {
    const path = { pathname: '/openboxes/stockMovement/createInbound', search: '' };
    expect(checkActiveSection({
      menuUrls, path, params, menuSectionsUrlParts: {},
    })).toBe('inbound');
  });
  it('should match section from config', () => {
    const path = { pathname: '/openboxes/testPath/', search: '' };
    expect(checkActiveSection({
      menuUrls, path, params, menuSectionsUrlParts: { test: ['testPath'] },
    })).toBe('test');
  });
});
