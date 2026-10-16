// Shared set-up for the screen tests (returns, receiving, put-away, stock transfer, replenishment,
// invoice): a real store signed in at one location, and a mocked API that answers by URL. The
// API module itself is mocked with tests/__mocks__/screenApiClient (kept apart: this file imports
// the reducers, which import the API).
import React from 'react';

import { configure, render } from '@testing-library/react';
import { initialize } from 'react-localize-redux';
import { Provider } from 'react-redux';
import { MemoryRouter, Route } from 'react-router-dom';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

// The screens load through several chained requests; the default 1s wait is too short in jsdom.
configure({ asyncUtilTimeout: 5000 });
jest.setTimeout(30000);

// The date formats the backend serves with every translation catalogue (messages.properties).
export const DATE_FORMATS = {
  'react.default.defaultDate.format': 'MM/DD/YYYY HH:mm:ss Z',
  'react.default.commonDate.format': 'MM/DD/YYYY',
  'react.default.expiryDate.format': 'MM/YYYY',
  'react.default.displayDate.format': 'MMM DD, YYYY',
};

export const HERE = {
  id: 'loc1',
  name: 'Main Warehouse',
  locationType: { description: 'Depot', locationTypeCode: 'DEPOT' },
};

/** A store with translations initialized and a signed-in superuser at `location`. */
export const sessionStore = ({ location = HERE, session = {} } = {}) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({
    languages: [{ name: 'English', code: 'en' }],
    options: { renderToStaticMarkup: false },
  }));
  store.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: {
            supportedActivities: ['MANAGE_INVENTORY', 'PUTAWAY_STOCK', 'PICK_STOCK'],
            ...location,
          },
          supportedActivities: ['MANAGE_INVENTORY', 'PUTAWAY_STOCK', 'PICK_STOCK'],
          currentLocationRoles: [],
          highestRole: 'Superuser',
          isSuperuser: true,
          isUserAdmin: true,
          isUserManager: true,
          pageSize: 10,
          isPaginated: false,
          menuItems: [],
          supportedLocales: [],
          activeLanguage: 'en',
          activeLanguageTag: 'en',
          currencyCode: 'USD',
          ...session,
        },
      },
    },
  });
  return store;
};

/**
 * Makes `method` of the mocked client answer from `routes`: a list of [matcher, body] where the
 * matcher is a string (exact URL, or prefix when it ends with '*') or a function of the URL, and
 * the body is the response body or a function of (url, config). Unmatched URLs answer
 * { data: [] }. Translations answer a catalogue holding only the date formats, so other labels
 * render as their keys (or their default message).
 */
export const answer = (method, routes = []) => {
  method.mockImplementation((url, ...rest) => {
    if (url.startsWith('/api/localizations')) {
      return Promise.resolve({ data: { messages: DATE_FORMATS, currentLocale: 'en' }, headers: {} });
    }
    const route = routes.find(([matcher]) => {
      if (typeof matcher === 'function') {
        return matcher(url);
      }
      if (matcher.endsWith('*')) {
        return url.startsWith(matcher.slice(0, -1));
      }
      return url === matcher;
    });
    if (!route) {
      return Promise.resolve({ data: { data: [] }, headers: {} });
    }
    const body = typeof route[1] === 'function' ? route[1](url, ...rest) : route[1];
    if (body instanceof Error) {
      return Promise.reject(body);
    }
    return Promise.resolve({ data: body, headers: {} });
  });
};

/**
 * Renders `component` at `path` (initially at `entry`) inside the store and a memory router; the
 * returned `currentPath()` reads where the router is now.
 */
export const renderAt = ({
  component, path, entry, store = sessionStore(),
}) => {
  let pathname = entry;
  let search = '';
  const utils = render(
    <Provider store={store}>
      <MemoryRouter initialEntries={[entry]}>
        <Route path={path} component={component} />
        <Route
          path="*"
          render={({ location }) => {
            pathname = location.pathname;
            search = location.search;
            return null;
          }}
        />
      </MemoryRouter>
    </Provider>,
  );
  return {
    ...utils, store, currentPath: () => pathname, currentSearch: () => search,
  };
};
