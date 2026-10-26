// Store for the product source screens: the real reducers, a signed-in admin with the product
// manager role (or without it), and the English translations initialized the way the app does it
// (a missing translation falls back to its id, which is how date formats are looked up).
import { initialize } from 'react-localize-redux';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

const productSupplierStore = ({ canManageProducts = true } = {}) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({
    languages: [{ name: 'English', code: 'en' }],
    options: {
      renderToStaticMarkup: false,
      onMissingTranslation: ({ translationId }) => translationId,
    },
  }));
  store.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: {
            id: 'u1', username: 'admin', name: 'Admin', roles: canManageProducts ? ['ROLE_PRODUCT_MANAGER'] : [],
          },
          location: {
            id: 'loc1',
            name: 'Main Warehouse',
            supportedActivities: ['MANAGE_INVENTORY'],
            locationType: { description: 'Depot', locationTypeCode: 'DEPOT' },
          },
          supportedActivities: ['MANAGE_INVENTORY'],
          currentLocationRoles: [],
          highestRole: 'Admin',
          isSuperuser: false,
          isUserAdmin: true,
          isUserManager: true,
          pageSize: 10,
          isPaginated: true,
          menuItems: [],
          supportedLocales: [],
          activeLanguage: 'en',
          activeLanguageTag: 'en',
          currencyCode: 'USD',
        },
      },
    },
  });
  return store;
};

export default productSupplierStore;
