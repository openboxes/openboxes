/** Browser helpers for the OBAF web app (the flows project's page is signed in as admin: setup project). */
import { expect, Page } from '@playwright/test';

/**
 * Signs this test's browser in as admin in its own server session and chooses the location. Tests run
 * in parallel and the server keeps the current location in the session, so a session shared through
 * a saved storageState would let one test switch another's location mid-flow.
 */
export async function chooseLocation(page: Page, locationId: string) {
  await page.context().clearCookies();
  const login = await page.request.post('api/login', { data: { username: 'admin', password: 'password' } });
  expect(login.status(), 'sign in').toBe(200);
  const r = await page.request.put(`api/chooseLocation/${locationId}`);
  expect(r.status(), 'choose location').toBe(200);
}

/** The stock movement page's status badge text, e.g. "Status: Shipped, Fulfillment Status: Issued". */
export async function stockMovementStatus(page: Page, id: string): Promise<string> {
  await page.goto(`stockMovement/show/${id}`);
  const badge = page.locator('[title^="Status:"]').first();
  await expect(badge).toBeVisible();
  return (await badge.getAttribute('title')) ?? '';
}

/**
 * The Outbound wizard from where the order stands (stockMovement/edit/{id}): on the Pick step,
 * "Next"; on the Send step, a shipment type other than Default (the page refuses Default), an
 * expected delivery date picked in the date picker, "Send shipment". Ends on the stock movement page.
 */
export async function sendFromWizard(page: Page, id: string) {
  await page.goto(`stockMovement/edit/${id}`);
  const send = page.getByRole('button', { name: 'Send shipment' });
  const next = page.getByRole('button', { name: 'Next' });
  await expect(send.or(next).first()).toBeVisible();
  await page.waitForLoadState('networkidle');
  if (!(await send.isVisible())) {
    // Pick step: "Next" validates the picklist and moves the order to PICKED (a click before the
    // step has loaded its items is ignored, hence the retry)
    await expect(async () => {
      if (!(await send.isVisible())) await next.click();
      await expect(send).toBeVisible({ timeout: 5000 });
    }).toPass({ timeout: 30000 });
    await page.waitForLoadState('networkidle');
  }
  const type = page.locator('[data-testid="form-field"][aria-label="Shipment type"]');
  await type.locator('input[id^="react-select"]').first().pressSequentially('Land');
  await page.locator('.react-select__option').filter({ hasText: /^Land$/ }).first().click();
  await expect(type).toContainText('Land');
  const date = page.getByRole('textbox', { name: /Expected Delivery Date/ });
  await date.click();
  const target = new Date(Date.now() + 2 * 86400000);
  if (target.getUTCMonth() !== new Date().getUTCMonth()) await page.locator('.react-datepicker__navigation--next').click();
  await page.locator(`.react-datepicker__day[aria-label="day-${target.getUTCDate()}"]:not(.react-datepicker__day--outside-month)`).click();
  await expect(date).not.toHaveValue('');
  await send.click();
  await page.waitForURL(/stockMovement\/show\//, { timeout: 30000 });
}
