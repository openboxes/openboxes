import React from 'react';

import { render } from '@testing-library/react';
import { Provider } from 'react-redux';
import { legacy_configureStore as configureStore } from 'redux-mock-store';

import Footer from 'components/Layout/Footer';

import '@testing-library/jest-dom';

jest.mock('react-localize-redux', () => ({
  getLanguages: () => [
    { code: 'en', name: 'English' },
    { code: 'fr', name: 'French' },
  ],
  getTranslate: () => (id) => id,
  setActiveLanguage: (code) => ({ type: 'SET_ACTIVE_LANGUAGE', payload: code }),
}));

jest.mock('actions', () => ({
  changeCurrentLocale: (locale) => ({ type: 'CHANGE_CURRENT_LOCALE', payload: locale }),
}));

jest.mock('utils/Translate', () => ({
  __esModule: true,
  // Render the id so assertions can find labelled sections without pulling in
  // the real localization store.
  default: ({ id, defaultMessage }) => defaultMessage || id,
  translateWithDefaultMessage: () => (id, defaultMessage) => defaultMessage || id,
}));

const mockStore = configureStore();

const session = {
  activeLanguage: 'en',
  grailsVersion: '3.3.16',
  appVersion: '0.9.5',
  branchName: 'develop',
  buildNumber: '1234',
  environment: 'development',
  buildDate: '2026-01-01',
  hostname: 'localhost',
  timezone: 'UTC',
  ipAddress: '127.0.0.1',
  localizationModeEnabled: false,
  localizationModeLocale: 'en',
};

const renderFooter = () => render(
  <Provider store={mockStore({ session, localize: {} })}>
    <Footer />
  </Provider>,
);

/**
 * The footer is the one React component the unified design restructures
 * rather than merely restyles: the build and environment detail moves behind
 * a CSS-only disclosure. Both versions ship in the same bundle and are chosen
 * at render time, so the classic one has to survive untouched.
 */
describe('Footer layout gating', () => {
  afterEach(() => {
    delete window.UNIFIED_LAYOUT;
  });

  describe('when the instance has not opted in', () => {
    it('renders the upstream footer with no disclosure control', () => {
      const { container } = renderFooter();

      expect(container.querySelector('.footer')).toBeInTheDocument();
      expect(container.querySelector('#footer-details-toggle')).not.toBeInTheDocument();
      expect(container.querySelector('.footer-details')).not.toBeInTheDocument();
      expect(container.querySelector('.footer-summary')).not.toBeInTheDocument();
    });

    it('keeps the upstream border-top class', () => {
      // The classic footer draws its own separator. The unified one drops it
      // because the surrounding chrome supplies the boundary — losing it in
      // the classic state would be a visible regression for instances that
      // never opted in.
      const { container } = renderFooter();

      expect(container.querySelector('.footer')).toHaveClass('border-top');
    });

    it('shows the build detail inline, not hidden behind a toggle', () => {
      const { container } = renderFooter();

      expect(container.textContent).toContain('3.3.16');
      expect(container.textContent).toContain('1234');
    });
  });

  describe('when the instance has opted in', () => {
    it('renders the disclosure structure', () => {
      window.UNIFIED_LAYOUT = true;

      const { container } = renderFooter();

      const toggle = container.querySelector('#footer-details-toggle');
      expect(toggle).toBeInTheDocument();
      expect(toggle).toHaveAttribute('type', 'checkbox');
      expect(container.querySelector('.footer-summary')).toBeInTheDocument();
      expect(container.querySelector('.footer-details')).toBeInTheDocument();
    });

    it('drops the upstream border-top', () => {
      window.UNIFIED_LAYOUT = true;

      const { container } = renderFooter();

      expect(container.querySelector('.footer')).not.toHaveClass('border-top');
    });

    it('still renders the build detail, only collapsed', () => {
      // The disclosure is CSS-only: the content is in the DOM either way, so
      // nothing is lost for anyone reading the page without the stylesheet.
      window.UNIFIED_LAYOUT = true;

      const { container } = renderFooter();

      expect(container.querySelector('.footer-details').textContent).toContain('3.3.16');
      expect(container.querySelector('.footer-details').textContent).toContain('1234');
    });

    it('drives the disclosure from a label, with no script', () => {
      window.UNIFIED_LAYOUT = true;

      const { container } = renderFooter();

      const label = container.querySelector('label[for="footer-details-toggle"]');
      expect(label).toBeInTheDocument();
      // the accessible name goes through the localization layer, not a
      // hard-coded string (the GSP footer uses the same message key)
      expect(label).toHaveAttribute('aria-label', 'Build information');
    });
  });

  it('offers the language switcher in both states', () => {
    // Whatever else changes, the footer's one piece of real functionality has
    // to be present either way.
    const { container: classic } = renderFooter();
    expect(classic.querySelectorAll('a, button, select').length).toBeGreaterThan(0);

    window.UNIFIED_LAYOUT = true;
    const { container: unified } = renderFooter();
    expect(unified.querySelectorAll('a, button, select').length).toBeGreaterThan(0);
  });
});
