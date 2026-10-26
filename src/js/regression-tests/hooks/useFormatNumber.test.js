import { renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useFormatNumber from 'hooks/useFormatNumber';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));

describe('useFormatNumber', () => {
  const formatIn = (activeLanguage) => {
    useSelector.mockImplementation((selector) => selector({ session: { activeLanguage } }));
    return renderHook(() => useFormatNumber()).result.current;
  };

  it('formats in the locale of the session', () => {
    expect(formatIn('en')(1234.5)).toBe('1,234.5');
    expect(formatIn('de')(1234.5)).toBe('1.234,5');
    // French groups the thousands with a narrow no-break space
    expect(formatIn('fr')(1234.5)).toBe('1\u202f234,5');
  });

  it('formats in English when the session has no locale', () => {
    expect(formatIn(undefined)(1234.5)).toBe('1,234.5');
  });

  it('maps a locale the formatter does not know to the one it does', () => {
    expect(formatIn('es_MX')(12345.6)).toBe('12.345,6');
  });

  it('formats in the given locale over the session one', () => {
    const format = formatIn('en');

    expect(format(1234.5, 'de')).toBe('1.234,5');
    expect(format(12345.6, 'es_mx')).toBe('12.345,6');
  });
});
