import { fr } from 'date-fns/locale';

import parseTypedExpirationDate from 'utils/receiving/parseTypedExpirationDate';

import '@testing-library/jest-dom';

describe('parseTypedExpirationDate()', () => {
  it.each([
    ['12/Oct/2026', '12/Oct/2026'],
    ['10/12/2026', '12/Oct/2026'],
    ['10/12/26', '12/Oct/2026'],
    ['1/5/26', '05/Jan/2026'],
    [' 10/12/2026 ', '12/Oct/2026'],
    ['25/12/2026', '25/Dec/2026'],
    ['25/12/26', '25/Dec/2026'],
  ])('should parse "%s" as %s', (text, expected) => {
    expect(parseTypedExpirationDate(text)).toEqual(expected);
  });

  it.each([
    'abc',
    '10/35/2026',
    '13/13/2026',
    '10/3',
    '10/12/202',
    '10/12/1999',
    '',
  ])('should return null for "%s"', (text) => {
    expect(parseTypedExpirationDate(text)).toBeNull();
  });

  it('should parse the month name in the given locale', () => {
    expect(parseTypedExpirationDate('12/oct./2026', fr)).toEqual('12/Oct/2026');
  });
});
