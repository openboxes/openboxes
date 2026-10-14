// A real store for the cycle count screens: the app's reducers with English set up, a signed-in
// user at one location, and (optionally) cycle counts loaded into the count workflow.
import { initialize } from 'react-localize-redux';
import { applyMiddleware, createStore } from 'redux';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import { FETCH_CYCLE_COUNTS } from 'actions/types';
import rootReducer from 'reducers';

export const HERE = { id: 'loc1', name: 'Main Warehouse' };

export const cycleCountItem = (overrides = {}) => ({
  id: 'item1',
  countIndex: 0,
  custom: false,
  quantityCounted: 7,
  quantityOnHand: 7,
  comment: 'Shelf A',
  dateCounted: new Date('2026-09-30T12:00:00').toISOString(),
  assignee: { id: 'u1', name: 'Admin' },
  binLocation: { id: 'bin1', name: 'A-01' },
  product: { id: 'p1', productCode: 'P-0001', name: 'Ibuprofen 200mg' },
  inventoryItem: { lotNumber: 'LOT-1', expirationDate: '10/31/2027' },
  ...overrides,
});

export const cycleCountStore = ({
  cycleCounts = [], location = HERE, activities = ['MANAGE_INVENTORY'],
} = {}) => {
  const store = createStore(rootReducer, applyMiddleware(ReduxPromise, reduxThunk));
  store.dispatch(initialize({
    languages: [{ name: 'English', code: 'en' }],
    // As in the app (MainRouter): a missing key renders as the key itself, which is how the date
    // formats passed as keys reach moment.
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
          user: { id: 'u1', username: 'admin', name: 'Admin' },
          location: {
            ...location,
            supportedActivities: activities,
            locationType: { description: 'Depot', locationTypeCode: 'DEPOT' },
          },
          supportedActivities: activities,
          currentLocationRoles: [],
          highestRole: 'Superuser',
          isSuperuser: true,
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
  if (cycleCounts.length) {
    store.dispatch({ type: FETCH_CYCLE_COUNTS, payload: cycleCounts });
  }
  return store;
};

// The value of one field of one row of a cycle count, as the store holds it now.
export const storedItem = (store, cycleCountId, rowId) =>
  store.getState().countWorkflow.entities[cycleCountId].cycleCountItems
    .find((item) => item.id === rowId);
