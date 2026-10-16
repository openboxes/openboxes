import React from 'react';

import { renderHook } from '@testing-library/react-hooks';
import {
  addTranslationForLanguage, initialize, localizeReducer,
} from 'react-localize-redux';
import { Provider } from 'react-redux';
import { combineReducers, createStore } from 'redux';

import useTranslate from 'hooks/useTranslate';

// tests/__mocks__ replaces this hook in every other test; here the real one is under test.
jest.unmock('hooks/useTranslate');

const createLocalizeStore = (languages) => {
  const store = createStore(combineReducers({ localize: localizeReducer }));
  store.dispatch(initialize({ languages, options: { renderToStaticMarkup: false } }));
  return store;
};

const renderTranslate = (store) => renderHook(() => useTranslate(), {
  wrapper: ({ children }) => <Provider store={store}>{children}</Provider>,
});

describe('useTranslate', () => {
  it('translates with the messages of the active language', () => {
    const store = createLocalizeStore([{ name: 'French', code: 'fr' }]);
    store.dispatch(addTranslationForLanguage({ 'react.default.save.label': 'Enregistrer' }, 'fr'));

    const { result } = renderTranslate(store);

    expect(result.current('react.default.save.label', 'Save')).toBe('Enregistrer');
  });

  it('falls back to the default message, then to the code, for a missing translation', () => {
    const store = createLocalizeStore([{ name: 'English', code: 'en' }]);

    const { result } = renderTranslate(store);

    expect(result.current('react.default.missing.label', 'Missing')).toBe('Missing');
    expect(result.current('react.default.missing.label')).toBe('react.default.missing.label');
  });

  it('fills in the data of the message', () => {
    const store = createLocalizeStore([{ name: 'English', code: 'en' }]);
    // eslint-disable-next-line no-template-curly-in-string
    store.dispatch(addTranslationForLanguage({ 'react.x.label': 'Shipment ${id} saved' }, 'en'));

    const { result } = renderTranslate(store);

    expect(result.current('react.x.label', 'x', { id: 'S-1' })).toBe('Shipment S-1 saved');
  });

  it('translates nothing while no language is active', () => {
    const store = createStore(combineReducers({ localize: localizeReducer }));

    const { result } = renderTranslate(store);

    expect(result.current('react.default.save.label', 'Save')).toBeUndefined();
  });
});
