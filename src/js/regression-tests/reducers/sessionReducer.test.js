import {
  CHANGE_CURRENT_LOCALE,
  CHANGE_CURRENT_LOCATION,
  FETCH_MENU_CONFIG,
  FETCH_SESSION_INFO,
  START_FETCHING_TRANSLATIONS,
  TOGGLE_USER_ACTION_MENU,
  TRANSLATIONS_FETCHED,
} from 'actions/types';
import sessionReducer from 'reducers/sessionReducer';

import deepFreeze from './deepFreeze';

const initial = () => deepFreeze(sessionReducer(undefined, { type: 'UNKNOWN' }));

// Shape of the axios response of /api/getAppContext
const response = (data) => ({ data: { data } });

// Adapted for OBAF (OBAF lacks the code): the autosave tuning settings
// (autosaveBatchSize, autosaveDebounceTime, autosaveMaxRetries, autosaveRetryDelay,
// autosaveMaxRetryDelay) are not in OBAF's session, so they are left out of the expectations.

describe('sessionReducer', () => {
  it('starts with safe defaults', () => {
    const state = initial();

    expect(state.currentLocation.id).toBe('');
    expect(state.isSuperuser).toBe(false);
    expect(state.pageSize).toBe(50);
    expect(state.searchConfig).toEqual({ debounceTime: 500, minSearchLength: 3 });
    expect(Object.values(state.fetchedTranslations)).not.toContain(true);
    expect(state.displayDateFormat).toBe('MMM DD, yyyy');
  });

  it('returns the same state for an unknown action', () => {
    const previous = initial();

    expect(sessionReducer(previous, { type: 'UNKNOWN' })).toBe(previous);
  });

  describe(FETCH_SESSION_INFO, () => {
    it('copies the app context into the session and clears the loading flag', () => {
      const location = { id: 'loc1', name: 'Main warehouse' };
      const previous = deepFreeze({ ...initial(), loading: true });

      const next = sessionReducer(previous, {
        type: FETCH_SESSION_INFO,
        payload: response({
          location,
          currentLocationRoles: ['ROLE_MANAGER'],
          isSuperuser: true,
          isUserAdmin: true,
          isUserApprover: true,
          isUserManager: true,
          user: { id: 'u1', username: 'ann' },
          activeLanguage: 'fr',
          activeLanguageTag: 'fr-FR',
          pageSize: 25,
          supportedActivities: ['MANAGE_INVENTORY'],
          isAutosaveEnabled: true,
          autosaveBatchSize: 10,
          maxUploadFileSize: 1024,
          displayDateFormat: 'DD/MM/YYYY',
        }),
      });

      expect(next).toMatchObject({
        currentLocation: location,
        currentLocationRoles: ['ROLE_MANAGER'],
        isSuperuser: true,
        isUserAdmin: true,
        isUserApprover: true,
        isUserManager: true,
        user: { id: 'u1', username: 'ann' },
        activeLanguage: 'fr',
        activeLanguageTag: 'fr-FR',
        pageSize: 25,
        supportedActivities: ['MANAGE_INVENTORY'],
        isAutosaveEnabled: true,
        maxUploadFileSize: 1024,
        displayDateFormat: 'DD/MM/YYYY',
        loading: false,
      });
      // Keys the app context does not carry are kept
      expect(next.menuConfig).toBe(previous.menuConfig);
      expect(next.fetchedTranslations).toBe(previous.fetchedTranslations);
    });

    it('falls back to the documented defaults for settings missing from the context', () => {
      const next = sessionReducer(initial(), {
        type: FETCH_SESSION_INFO,
        payload: response({ location: { id: 'loc1' } }),
      });

      expect(next).toMatchObject({
        isUserApprover: false,
        isUserRequestApprover: false,
        isUserManager: false,
        localizationModeEnabled: false,
        localizationModeLocale: 'ach',
        displayDateFormat: 'MMM DD, yyyy',
        displayDateDefaultValue: '-',
        notificationAutohideDelay: 8000,
        browserConnectionTimeout: 0,
        isAutosaveEnabled: false,
        cycleCountMaxSelectedProducts: 50,
        maxUploadFileSize: 2097152,
      });
    });
  });

  it('stores the menu configuration', () => {
    const next = sessionReducer(initial(), {
      type: FETCH_MENU_CONFIG,
      payload: response({ menuConfig: [{ id: 'inventory' }], menuSectionsUrlParts: { inventory: ['stock'] } }),
    });

    expect(next.menuConfig).toEqual([{ id: 'inventory' }]);
    expect(next.menuSectionsUrlParts).toEqual({ inventory: ['stock'] });
  });

  it('switches the location and starts loading', () => {
    const location = { id: 'loc2', name: 'Depot' };

    const next = sessionReducer(initial(), { type: CHANGE_CURRENT_LOCATION, payload: location });

    expect(next.currentLocation).toBe(location);
    expect(next.loading).toBe(true);
  });

  it('switches the active language', () => {
    const next = sessionReducer(initial(), {
      type: CHANGE_CURRENT_LOCALE,
      payload: response({ activeLanguage: 'es', activeLanguageTag: 'es-ES' }),
    });

    expect(next.activeLanguage).toBe('es');
    expect(next.activeLanguageTag).toBe('es-ES');
  });

  it('tracks the fetch of one translation prefix without touching the others', () => {
    const started = sessionReducer(
      deepFreeze({ ...initial(), fetchedTranslations: { default: true, invoice: true } }),
      { type: START_FETCHING_TRANSLATIONS, payload: 'invoice' },
    );
    expect(started.fetchedTranslations).toEqual({ default: true, invoice: false });

    const fetched = sessionReducer(deepFreeze(started), { type: TRANSLATIONS_FETCHED, payload: 'invoice' });
    expect(fetched.fetchedTranslations).toEqual({ default: true, invoice: true });
  });

  it('opens and closes the user action menu', () => {
    const opened = sessionReducer(initial(), { type: TOGGLE_USER_ACTION_MENU, payload: true });
    expect(opened.userActionMenuOpen).toBe(true);

    const closed = sessionReducer(deepFreeze(opened), {
      type: TOGGLE_USER_ACTION_MENU,
      payload: false,
    });
    expect(closed.userActionMenuOpen).toBe(false);
  });
});
