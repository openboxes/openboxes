import { addTranslationForLanguage, initialize } from 'react-localize-redux';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

// A real store with English active (so translate() answers the default messages, and the date
// formats the localized dates use) and a signed-in session; `session` overrides any of its fields.
const testStore = (session = {}) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({ languages: [{ name: 'English', code: 'en' }], options: { renderToStaticMarkup: false } }));
  store.dispatch(addTranslationForLanguage({
    'react.default.commonDate.format': 'DD/MMM/YYYY',
    'react.default.defaultDate.format': 'MM/DD/YYYY',
  }, 'en'));
  store.dispatch({
    type: 'FETCH_SESSION_INFO',
    payload: {
      data: {
        data: {
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: {
            id: 'loc1',
            name: 'Main Warehouse',
            supportedActivities: ['MANAGE_INVENTORY'],
            locationType: { description: 'Depot', locationTypeCode: 'DEPOT' },
          },
          supportedActivities: ['MANAGE_INVENTORY'],
          currentLocationRoles: [],
          highestRole: 'Superuser',
          isSuperuser: true,
          isUserAdmin: true,
          menuItems: [],
          supportedLocales: [],
          activeLanguage: 'en',
          ...session,
        },
      },
    },
  });
  return store;
};

export default testStore;
