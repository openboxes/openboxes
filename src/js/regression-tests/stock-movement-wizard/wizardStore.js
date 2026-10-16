// Shared setup for the stock movement wizard tests: a real store with the app's reducers, the
// English translations initialized (every label falls back to its default message) and a
// signed-in session at the given location.
import { initialize } from 'react-localize-redux';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

export const HERE = { id: 'loc1', name: 'Main Warehouse' };

export const DEPOT = { locationTypeCode: 'DEPOT', description: 'Depot' };

// The action the app dispatches once the session info has loaded.
export const sessionInfo = ({
  location = HERE,
  supportedActivities = ['MANAGE_INVENTORY', 'SEND_STOCK'],
  hasPackingSupport = false,
  isPaginated = false,
  user = { id: 'u1', username: 'admin', name: 'Admin' },
  // Further current location settings, e.g. hasCentralPurchasingEnabled.
  locationSettings = {},
} = {}) => ({
  type: 'FETCH_SESSION_INFO',
  payload: {
    data: {
      data: {
        user,
        location: {
          ...location,
          hasPackingSupport,
          supportedActivities,
          locationType: DEPOT,
          ...locationSettings,
        },
        supportedActivities,
        currentLocationRoles: [],
        highestRole: 'Superuser',
        isSuperuser: true,
        isUserAdmin: true,
        isUserManager: true,
        pageSize: 10,
        isPaginated,
        menuItems: [],
        supportedLocales: [],
        activeLanguage: 'en',
        activeLanguageTag: 'en',
        currencyCode: 'USD',
      },
    },
  },
});

export const wizardStore = (session) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  store.dispatch(sessionInfo(session));
  return store;
};

// The answer to every GET: the first route whose prefix the URL starts with (or which equals a
// regular expression match), wrapped as an axios response.
export const routeAnswers = (routes) => (url) => {
  const match = routes.find(([route]) => (
    route instanceof RegExp ? route.test(url) : url.startsWith(route)));
  const data = match ? match[1](url) : { data: [] };
  return Promise.resolve({ data, headers: {}, status: 200 });
};
