/**
 * Infrastructure smoke for the web UAT. NOT a UAT flow: it proves the stack, the sealed browser, the
 * evidence settings and the helpers (step screenshots, AUDIT, START STATE, the per-flow data factory)
 * work, so that acceptance flows can be written on top.
 */
import { expect, smoke, test } from '../lib/uat';
import { pages } from '../lib/pages';
import { reads } from '../lib/audit-db';

/** Seconds since the user's last login date (OBAF stores it in UTC; the database session is UTC). */
const SECONDS_SINCE_LOGIN = 'SELECT TIMESTAMPDIFF(SECOND, last_login_date, UTC_TIMESTAMP()) FROM `user` WHERE username = ?';

test('SMOKE: the seed admin signs in through the login page, chooses a location and sees the dashboard', smoke(), async ({ page, step, audit }) => {
  await step('open the login page', async () => {
    await pages.login.open(page);
  });
  await step('sign in as the seed admin', async () => {
    await pages.login.signIn(page, 'admin', 'password');
    await pages.chooseLocation.expectShown(page);
  });
  await step('choose Main Warehouse', async () => {
    await pages.chooseLocation.choose(page, 'Main Warehouse');
  });
  await step('the dashboard loads in Main Warehouse', async () => {
    await pages.dashboard.expectLoaded(page, 'Main Warehouse');
  });
  await audit('the sign-in is recorded: admin\'s last login date was set in the last 2 minutes', async (db) => {
    expect(Number(await db.scalar(SECONDS_SINCE_LOGIN, ['admin']))).toBeLessThan(120);
  });
});

test('SMOKE: helpers: a flow-scoped depot, bin, stocked product and user; declared start state; the user signs in and chooses the depot', smoke(), async ({ page, step, audit, data, startState }) => {
  // setup (factory, before the first step)
  const depot = await data.depot('DC');
  const bin = await data.bin(depot.id, 'STOR', ['PICK_STOCK', 'PUTAWAY_STOCK']);
  const product = await data.product('P');
  await data.stock(depot.id, product.id, [[bin.id, 5]]);
  const user = await data.user('clerk', { roles: ['ROLE_MANAGER'] });
  expect(depot.name).toMatch(/^USMOKE-DC-/);

  await startState({
    [`${product.productCode} on hand in ${bin.number}`]: { expect: { [bin.number]: 5 }, read: () => reads.onHandByBin(depot.id, product.id) },
    [`user ${user.username}`]: { expect: { active: true, roles: ['ROLE_MANAGER'] }, read: () => reads.user(user.username) },
    [`user ${user.username} has never signed in`]: { expect: null, read: (db) => db.scalar('SELECT last_login_date FROM `user` WHERE username = ?', [user.username]) },
    [`depot ${depot.name} exists`]: { expect: depot.name, read: () => reads.locationName(depot.id) },
  });

  await step(`sign in as ${user.username}`, async () => {
    await pages.login.open(page);
    await pages.login.signIn(page, user.username, user.password);
    await pages.chooseLocation.expectShown(page);
  });
  await step(`choose ${depot.name}`, async () => {
    await pages.chooseLocation.choose(page, depot.name);
  });
  await step(`the dashboard loads in ${depot.name}`, async () => {
    await pages.dashboard.expectLoaded(page, depot.name);
  });
  await audit(`the sign-in is recorded: ${user.username}'s last login date was set in the last 2 minutes`, async (db) => {
    expect(Number(await db.scalar(SECONDS_SINCE_LOGIN, [user.username]))).toBeLessThan(120);
  });
  await audit(`the stock the factory recorded is unchanged: 5 in ${bin.number}`, async () => {
    expect(await reads.onHandByBin(depot.id, product.id)).toEqual({ [bin.number]: 5 });
  });
  await expectFactorySealed(data);
});

test('SMOKE: a legacy GSP page (custom layout) runs its CDN libraries from the mirror: jQuery, a select2 picker and a DataTables table', smoke(), async ({ page, step, uat }) => {
  await step('sign in as the seed admin in Main Warehouse', async () => {
    await pages.login.open(page);
    await pages.login.signIn(page, 'admin', 'password');
    await pages.chooseLocation.choose(page, 'Main Warehouse');
    await pages.dashboard.expectLoaded(page, 'Main Warehouse');
  });
  await step('open Reporting > Transaction Report', async () => {
    await pages.transactionReport.open(page);
  });
  await step('jQuery 1.7.2, select2 and DataTables initialised: the product picker and the report table are built', async () => {
    expect(await pages.transactionReport.libraries(page)).toEqual({ jquery: '1.7.2', select2: 'function', dataTable: 'function' });
    await expect(pages.transactionReport.productPicker(page)).toBeVisible();
    await expect(pages.transactionReport.table(page)).toBeVisible();
  });
  await step('the select2 product picker opens its search dropdown', async () => {
    await pages.transactionReport.openProductPicker(page);
  });
  const served = uat.mirror.pages.get('report/showTransactionReport');
  expect(served, 'the sealed browser saw the page\'s third-party requests').toBeDefined();
  expect([...served!.missing], 'third-party URLs missing from the CDN mirror').toEqual([]);
  for (const lib of ['/npm/select2@4.0.13/dist/js/select2.min.js', '/ajax/libs/datatables/1.9.4/jquery.dataTables.js', '/ajax/libs/jquery/1.7.2/jquery.min.js']) {
    expect([...served!.served].some((u) => u.endsWith(lib)), `${lib} served from the mirror`).toBe(true);
  }
});

/** After the first step the factory refuses to create data (flows drive the screens from then on). */
async function expectFactorySealed(data: import('../lib/data').FlowData) {
  await expect(data.product('LATE')).rejects.toThrow(/before the first step only/);
}
