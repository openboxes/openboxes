import React from 'react';

import { render, waitFor } from '@testing-library/react';
import {
  getActiveLanguage, initialize, localizeReducer, setActiveLanguage,
} from 'react-localize-redux';

import MainRouter from '../../MainRouter';

vi.mock('components/Router', () => ({ default: () => null }));
vi.mock('actions', () => ({
  fetchMenuConfig: vi.fn(),
  fetchSessionInfo: vi.fn(),
  fetchTranslations: vi.fn(),
}));
vi.mock('react-redux', async () => ({
  ...(await vi.importActual('react-redux')),
  connect: () => (component) => component,
}));
vi.mock('react-localize-redux', async () => ({
  ...(await vi.importActual('react-localize-redux')),
  withLocalize: (component) => component,
}));

describe('translation mode language registration', () => {
  it('can activate Crowdin without adding it to selectable user locales', async () => {
    const supportedLocales = [{ code: 'en', name: 'English' }, { code: 'fr', name: 'French' }];
    let localizationState;
    const initializeLanguages = vi.fn((settings) => {
      localizationState = localizeReducer(undefined, initialize(settings));
    });
    const activateLanguage = vi.fn((code) => {
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
        fetchTranslations={vi.fn()}
        fetchMenuConfig={vi.fn()}
      />,
    );

    await waitFor(() => expect(activateLanguage).toHaveBeenCalledWith('ach'));
    expect(getActiveLanguage(localizationState).code).toBe('ach');
    expect(supportedLocales.map(({ code }) => code)).toEqual(['en', 'fr']);
  });
});
