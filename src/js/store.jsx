import { applyMiddleware, createStore } from 'redux';
import { persistReducer, persistStore } from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import ReduxPromise from 'redux-promise';
import reduxThunk from 'redux-thunk';

import rootReducer from 'reducers';

const persistConfig = {
  key: 'reducer',
  storage,
};

const createStoreWithMiddleware = applyMiddleware(ReduxPromise, reduxThunk)(createStore);
const store = createStoreWithMiddleware(persistReducer(persistConfig, rootReducer));
const persistor = persistStore(store);

export const { dispatch } = store;

export { persistor };

export default store;
