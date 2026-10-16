import React from 'react';

import { render } from '@testing-library/react';

import useTranslateWithRedirect from 'hooks/useTranslateWithRedirect';

jest.mock('hooks/useTranslate', () => ({
  __esModule: true,
  default: () => (label, defaultLabel, options) => defaultLabel.replace('{code}', options.code),
}));

const Message = (props) => useTranslateWithRedirect()(props);

describe('useTranslateWithRedirect', () => {
  it('refuses a phrase the translation does not contain', () => {
    jest.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => render(<Message label="react.x" defaultLabel="Nothing here" redirects={[{ phrase: 'x', redirectTo: '/' }]} />))
      .toThrow('The phrase "x" was not found in the translation');
  });
});
