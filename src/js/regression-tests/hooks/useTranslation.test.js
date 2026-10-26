import { renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';

import * as actions from 'actions';
import useTranslation from 'hooks/useTranslation';

jest.mock('react-redux', () => ({
  useDispatch: jest.fn(),
  useSelector: jest.fn(),
  connect: jest.fn(() => (Component) => Component),
}));

describe('useTranslation', () => {
  const dispatch = jest.fn();
  let activeLanguage;

  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(actions, 'fetchTranslations').mockImplementation((locale, prefix) => ({ locale, prefix }));
    useDispatch.mockReturnValue(dispatch);
    useSelector.mockImplementation((selector) => selector({ session: { activeLanguage } }));
    activeLanguage = 'en';
  });

  it('fetches every prefix in the active language', () => {
    renderHook(() => useTranslation('stockMovement', 'default'));

    expect(dispatch.mock.calls).toEqual([
      [{ locale: 'en', prefix: 'stockMovement' }],
      [{ locale: 'en', prefix: 'default' }],
    ]);
  });

  it('fetches again only when the language changes', () => {
    const { rerender } = renderHook(() => useTranslation('stockMovement'));

    rerender();
    expect(dispatch).toHaveBeenCalledTimes(1);

    activeLanguage = 'fr';
    rerender();

    expect(dispatch).toHaveBeenCalledTimes(2);
    expect(dispatch).toHaveBeenLastCalledWith({ locale: 'fr', prefix: 'stockMovement' });
  });
});
