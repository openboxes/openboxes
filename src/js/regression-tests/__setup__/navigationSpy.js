/**
 * Navigation spy for the regression tests.
 *
 * OBAF navigates directly: `window.location = url`,
 * `window.location.href = url`, `window.location.assign(url)` / `replace(url)`, and
 * `window.open(url, target)`. This shim installs a fake `window.location` and `window.open`
 * that report every navigation to one `followLink` jest.fn as `followLink(url, options)`, so
 * `expect(followLink).toHaveBeenCalledWith(...)` assertions check OBAF's real navigation:
 *   - same tab (location assignment, href, assign, replace): `followLink(url)`
 *   - `window.open(url)` / `window.open(url, '_blank')`: `followLink(url, { newTab: true })`
 *   - `window.open(url, otherTarget)`: `followLink(url, { target: otherTarget })`
 *   - `window.open()` without a URL returns a tab; setting its `location.href` (or `location`)
 *     later reports `followLink(url, { targetWindow: tab })`
 * `window.location.reload()` is recorded on `reloadPage`.
 *
 * Usage: import `followLink` and `installNavigationSpy` from
 * `regression-tests/__setup__/navigationSpy`, then call `installNavigationSpy()` once at the
 * top level of the test file (it registers beforeEach/afterEach hooks).
 */
export const followLink = jest.fn();
export const reloadPage = jest.fn();

const LOCATION_FIELDS = ['hash', 'host', 'hostname', 'origin', 'pathname', 'port', 'protocol', 'search'];

const fakeTab = () => {
  const tab = {
    close: () => {},
    focus: () => {},
    closed: false,
    opener: null,
    document: { write: () => {}, close: () => {} },
  };
  const tabLocation = {};
  Object.defineProperty(tabLocation, 'href', {
    configurable: true,
    get: () => 'about:blank',
    set: (url) => followLink(String(url), { targetWindow: tab }),
  });
  tabLocation.assign = (url) => followLink(String(url), { targetWindow: tab });
  tabLocation.replace = tabLocation.assign;
  Object.defineProperty(tab, 'location', {
    configurable: true,
    get: () => tabLocation,
    set: (url) => followLink(String(url), { targetWindow: tab }),
  });
  return tab;
};

const fakeOpen = (url, target) => {
  const tab = fakeTab();
  if (url === undefined || url === null || url === '') {
    return tab;
  }
  if (target === undefined || target === '_blank') {
    followLink(String(url), { newTab: true });
  } else {
    followLink(String(url), { target });
  }
  return tab;
};

export const installNavigationSpy = () => {
  let realLocationDescriptor;
  let realOpen;

  beforeEach(() => {
    realLocationDescriptor = Object.getOwnPropertyDescriptor(window, 'location');
    realOpen = window.open;
    const realLocation = window.location;

    const fakeLocation = {
      assign: (url) => followLink(String(url)),
      replace: (url) => followLink(String(url)),
      reload: (...args) => reloadPage(...args),
      toString: () => realLocation.href,
    };
    Object.defineProperty(fakeLocation, 'href', {
      configurable: true,
      enumerable: true,
      get: () => realLocation.href,
      set: (url) => followLink(String(url)),
    });
    LOCATION_FIELDS.forEach((field) => {
      Object.defineProperty(fakeLocation, field, {
        configurable: true,
        enumerable: true,
        get: () => realLocation[field],
        set: () => {},
      });
    });

    delete window.location;
    Object.defineProperty(window, 'location', {
      configurable: true,
      get: () => fakeLocation,
      // Under jest's vm context one `window.location = url` runs this setter twice: once on the
      // context's inner window and once on the test's `window`. Count only the latter.
      set(url) {
        if (this === window) {
          followLink(String(url));
        }
      },
    });
    window.open = fakeOpen;
    // A navigation recorded after a file's own clearAllMocks (e.g. restoring window.location in
    // its afterEach) must not leak into the next test.
    followLink.mockClear();
    reloadPage.mockClear();
  });

  afterEach(() => {
    delete window.location;
    if (realLocationDescriptor) {
      Object.defineProperty(window, 'location', realLocationDescriptor);
    }
    window.open = realOpen;
  });
};
