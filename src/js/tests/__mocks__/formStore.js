import { localizeReducer } from 'react-localize-redux';
import { legacy_configureStore as configureStore } from 'redux-mock-store';

// Store for connected form components, with no translations loaded
const store = configureStore()({
  localize: localizeReducer(undefined, { type: '@@INIT' }),
  session: { fetchedTranslations: {} },
});

export default store;
