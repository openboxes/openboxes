import { isUnifiedLayout } from 'utils/unifiedLayout';

/**
 * The React bundle is a single build serving both the original and the
 * unified design, so components that differ between them read this flag at
 * render time. layouts/react.gsp sets window.UNIFIED_LAYOUT from
 * openboxes.unifiedLayout.enabled.
 */
describe('isUnifiedLayout', () => {
  afterEach(() => {
    delete window.UNIFIED_LAYOUT;
  });

  it('is false when the layout never set the flag', () => {
    // The shipped default: an instance that has not opted in emits no
    // <script> setting this, so the global is simply absent.
    expect(isUnifiedLayout()).toBe(false);
  });

  it('is true only when the flag is exactly true', () => {
    window.UNIFIED_LAYOUT = true;
    expect(isUnifiedLayout()).toBe(true);
  });

  it('is false when the flag is false', () => {
    window.UNIFIED_LAYOUT = false;
    expect(isUnifiedLayout()).toBe(false);
  });

  it.each([
    ['the string "false"', 'false'],
    ['the string "true"', 'true'],
    ['a number', 1],
    ['null', null],
    ['undefined', undefined],
  ])('is false for %s rather than coercing it', (_label, value) => {
    // GSP interpolates a Groovy boolean into JavaScript. If that ever emits a
    // quoted value, `Boolean('false')` would be true — hence the strict
    // comparison this asserts.
    window.UNIFIED_LAYOUT = value;
    expect(isUnifiedLayout()).toBe(false);
  });

  it('is read per call, not captured at import time', () => {
    // Components call this during render. Reading once at module scope would
    // freeze the answer before the layout's inline script had any effect in
    // some bundling orders, and would leak between tests.
    expect(isUnifiedLayout()).toBe(false);
    window.UNIFIED_LAYOUT = true;
    expect(isUnifiedLayout()).toBe(true);
    window.UNIFIED_LAYOUT = false;
    expect(isUnifiedLayout()).toBe(false);
  });
});
