/** Sign-in and session context, exactly as the app does it. */
import { contractTest, expect, test } from '../../lib/ts/test';
import { MobileApp, CallResult } from '../../lib/ts/app';
import { url } from '../../lib/ts/contract';
import { MAIN_WAREHOUSE } from '../../lib/ts/env';

test.describe('auth', () => {
  contractTest('POST /login with the app body opens a session', 'src/apis/auth.ts#login', async () => {
    const app = await MobileApp.anonymous();
    const r = await app.call('src/apis/auth.ts#login', {}, { username: 'admin', password: 'password' });
    r.holds();
    const cookies = await app.ctx.storageState();
    expect(cookies.cookies.map((c) => c.name)).toContain('JSESSIONID');
    await app.dispose();
  });

  test('POST /login with a wrong password is refused with 401', async () => {
    const app = await MobileApp.anonymous();
    const r = await app.raw('POST', url('src/apis/auth.ts#login'), { username: 'admin', password: 'not-the-password' });
    expect(r.status()).toBe(401);
    await app.dispose();
  });

  contractTest('GET /getAppContext answers the user and the chosen location', 'src/apis/auth.ts#getSession', async () => {
    const app = await MobileApp.signIn(MAIN_WAREHOUSE);
    const r = await app.call('src/apis/auth.ts#getSession');
    r.holds();
    expect(r.data.data.location.id).toBe(MAIN_WAREHOUSE);
    expect(r.data.data.user.username).toBe('admin');
    await app.dispose();
  });

  contractTest('PUT /chooseLocation/{id} switches the session location', 'src/apis/locations.ts#setCurrentLocation', async ({ fx }) => {
    const app = await MobileApp.signIn(MAIN_WAREHOUSE);
    const dc = await fx.mobileDc();
    const r = await app.call('src/apis/locations.ts#setCurrentLocation', { 'location.id': dc });
    const ctx = await app.call('src/apis/auth.ts#getSession');
    r.holds();
    expect(ctx.data.data.location.id).toBe(dc);
    await app.dispose();
  });

  contractTest('POST /logout with no body ends the session', 'src/apis/auth.ts#logout', async () => {
    const app = await MobileApp.signIn(MAIN_WAREHOUSE);
    const r: CallResult = await app.call('src/apis/auth.ts#logout');
    const after = await app.raw('GET', '/getAppContext');
    r.holds();
    expect(after.status()).not.toBe(200);
    await app.dispose();
  });
});
