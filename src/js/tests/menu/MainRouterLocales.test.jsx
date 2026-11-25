import React from 'react';

import { render, waitFor } from '@testing-library/react';
import {
  getActiveLanguage, initialize, localizeReducer, setActiveLanguage,
} from 'react-localize-redux';

import MainRouter from '../../MainRouter';

jest.mock('components/Router', () => () => null);
jest.mock('actions', () => ({}));
jest.mock('react-redux/es/connect/connect', () => () => (component) => component);
jest.mock('react-localize-redux', () => ({
  ...jest.requireActual('react-localize-redux'),
  withLocalize: (component) => component,
}));

describe('translation mode language registration', () => {
  it('can activate Crowdin without adding it to selectable user locales', async () => {
    const supportedLocales = [{ code: 'en', name: 'English' }, { code: 'fr', name: 'French' }];
    let localizationState;
    const initializeLanguages = jest.fn((settings) => {
      localizationState = localizeReducer(undefined, initialize(settings));
    });
    const activateLanguage = jest.fn((code) => {
      localizationState = localizeReducer(localizationState, setActiveLanguage(code));
    });

    render(
      <MainRouter
        supportedLocales={supportedLocales}
        localizationModeLocale="ach"
        locale="ach"
        initialize={initializeLanguages}
        setActiveLanguage={activateLanguage}
        fetchSessionInfo={() => Promise.resolve()}
        fetchTranslations={jest.fn()}
        fetchMenuConfig={jest.fn()}
      />,
    );

    await waitFor(() => expect(activateLanguage).toHaveBeenCalledWith('ach'));
    expect(getActiveLanguage(localizationState).code).toBe('ach');
    expect(supportedLocales.map(({ code }) => code)).toEqual(['en', 'fr']);
  });
});
