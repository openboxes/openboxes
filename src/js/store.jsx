import { applyMiddleware, createStore } from 'redux';
import { persistReducer, persistStore } from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

const persistConfig = {
  key: 'reducer',
  storage,
  // The indicators payload has some weird and complex structure,
  // that is causing "Error: createPersistoid: error serializing state"
  // (see reference in the comments section in the: OBPIH-4735),
  // hence there is need to temporarily disable the indicator reducer
  // from the persisted reducers.
  blacklist: ['indicators', 'spinner', 'connection', 'infoBarVisibility'],
};

const createStoreWithMiddleware = applyMiddleware(ReduxPromise, reduxThunk)(createStore);
const store = createStoreWithMiddleware(persistReducer(persistConfig, rootReducer));
const persistor = persistStore(store);

export const { dispatch } = store;

export { persistor };

export default store;
