import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import configureStore from 'redux-mock-store';

import { DISABLE_LOCALIZATION } from 'api/urls';
import Footer from 'components/Layout/Footer';

jest.mock('utils/Translate', () => ({ defaultMessage }) => defaultMessage);
jest.mock('actions', () => ({
  changeCurrentLocale: (locale) => ({ type: 'CHANGE_LOCALE', locale }),
}));

const supportedLocales = [{ code: 'en', name: 'English' }, { code: 'fr', name: 'French' }];

const createStore = (localizationModeEnabled = false) => configureStore()({
  session: {
    activeLanguage: localizationModeEnabled ? 'ach' : 'en',
    supportedLocales,
    localizationModeLocale: 'ach',
    localizationModeEnabled,
    grailsVersion: '',
    appVersion: '',
    branchName: '',
    buildNumber: '',
    environment: '',
    buildDate: '',
    hostname: '',
    timezone: '',
    ipAddress: '',
  },
  // Translation resources include Crowdin, but the selectable languages must not.
  localize: {
    languages: [...supportedLocales, { code: 'ach', name: 'Acholi' }],
  },
});

describe('footer locale choices', () => {
  it('offers only supported user locales even when Crowdin is registered for translation', () => {
    const store = createStore();
    render(<Provider store={store}><Footer /></Provider>);

    expect(screen.queryByText('Acholi')).toBeNull();
    expect(screen.getAllByRole('button').map((button) => button.textContent))
      .toEqual(['English', 'French']);

    fireEvent.click(screen.getByRole('button', { name: 'French' }));
    expect(store.getActions()).toContainEqual({ type: 'CHANGE_LOCALE', locale: 'fr' });
  });

  it('keeps ordinary languages available to leave localization mode', () => {
    render(<Provider store={createStore(true)}><Footer /></Provider>);

    expect(screen.queryByText('Acholi')).toBeNull();
    expect(screen.getByRole('link', { name: 'French' }).getAttribute('href'))
      .toBe(DISABLE_LOCALIZATION('fr'));
    expect(screen.getByRole('link', { name: 'English' }).getAttribute('href'))
      .toBe(DISABLE_LOCALIZATION('en'));
  });
});
