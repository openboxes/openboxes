/**
 * Page objects, one per screen, so a UI change is one edit. Role and visible text
 * first; CSS only where OBAF's legacy GSP pages leave no choice (said so at each use).
 */
import { expect, Page } from '@playwright/test';

/**
 * The accessible name of a link or button that starts with an icon font glyph. With the CDN mirror the
 * icon fonts load (Remix Icon, Font Awesome) and their glyph, a Private Use Area character, becomes part
 * of the name (glyph, space, "Main Warehouse"), as in a production browser; `exact` text matching would miss it.
 */
export function withIcon(text: string): RegExp {
  return new RegExp(`^[\\uE000-\\uF8FF\\s]*${text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\s*$`);
}

export const pages = {
  login: {
    async open(page: Page) {
      await page.goto('auth/login');
      await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
    },
    async signIn(page: Page, username: string, password: string) {
      // the GSP login form's inputs have no label or placeholder: ids are the only handle
      await page.locator('#username').fill(username);
      await page.locator('#password').fill(password);
      await page.getByRole('button', { name: 'Login' }).click();
    },
  },
  chooseLocation: {
    async expectShown(page: Page) {
      await page.waitForURL(/dashboard\/chooseLocation/);
      await expect(page.getByRole('heading', { name: 'Choose Location' })).toBeVisible();
    },
    async choose(page: Page, locationName: string) {
      await page.getByRole('link', { name: withIcon(locationName) }).click();
    },
  },
  dashboard: {
    /** The dashboard of `locationName`: the location chooser in the header shows it, the menu, the dashboard tabs and widgets. */
    async expectLoaded(page: Page, locationName: string) {
      await page.waitForURL((u) => /\/openboxes\/(dashboard(\/index)?)?(\?.*)?$/.test(u.pathname + u.search));
      await expect(page.getByRole('button', { name: 'location-chooser' })).toHaveText(locationName);
      await expect(page.getByRole('menuitem', { name: 'Dashboard', exact: true })).toBeVisible();
      await expect(page.getByRole('button', { name: 'My Dashboard' })).toBeVisible();
      await expect(page.getByRole('link', { name: /^Products in Receiving Bin/ })).toBeVisible();
      await expect(page.getByRole('link', { name: /^Your in Progress Shipments/ })).toBeVisible();
    },
  },
  /**
   * Reporting > Transaction Report (report/showTransactionReport): a legacy GSP page on the `custom`
   * layout with a select2 product picker (g:selectProductAjax) and a DataTables table. CSS where noted:
   * the widgets' own markup is the only handle on whether the libraries initialised.
   */
  transactionReport: {
    async open(page: Page) {
      await page.goto('report/showTransactionReport');
      await expect(page).toHaveTitle('Transaction Report');
    },
    /** What the page's libraries did: versions, plugin functions and the widgets they built. */
    async libraries(page: Page) {
      return page.evaluate(() => {
        const w = window as unknown as { jQuery?: { fn: { jquery: string; select2?: unknown; dataTable?: unknown } } };
        return {
          jquery: w.jQuery?.fn.jquery ?? null,
          select2: typeof w.jQuery?.fn.select2,
          dataTable: typeof w.jQuery?.fn.dataTable,
        };
      });
    },
    // CSS: select2 inserts its container right after the <select> it replaces
    productPicker: (page: Page) => page.locator('#products-select + .select2-container'),
    // CSS: DataTables wraps the table in <div id="<table id>_wrapper" class="dataTables_wrapper">
    table: (page: Page) => page.locator('#transactionReport_wrapper.dataTables_wrapper'),
    async openProductPicker(page: Page) {
      await this.productPicker(page).locator('.select2-selection').click();
      // select2 4 appends its dropdown (with a search field) to <body>
      await expect(page.locator('.select2-container--open .select2-search__field')).toBeVisible();
    },
  },
};
