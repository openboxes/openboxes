/** The label stub (e2e/docker/labelstub.py): what the "Zebra printer" and "labelary" received. */
import { request } from '@playwright/test';
import { LABEL_STUB_URL } from './env';

export async function labelStub(): Promise<{ printed: string[]; rendered: string[] }> {
  const ctx = await request.newContext();
  try {
    const printed = await (await ctx.get(`${LABEL_STUB_URL}/_printed`)).json();
    const rendered = await (await ctx.get(`${LABEL_STUB_URL}/_rendered`)).json();
    return { printed, rendered };
  } finally {
    await ctx.dispose();
  }
}
