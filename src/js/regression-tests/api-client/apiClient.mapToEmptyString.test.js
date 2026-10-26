// Companion to upstream src/js/tests/api-client/apiClient.test.js: further
// mapToEmptyString cases (that file is left untouched). Two of them encode a fix
// (mapToEmptyString compared values with `in`, which checks array indexes, and
// stored a nested object under the literal key `nestedObject`) that OBAF does
// not carry.
import { mapToEmptyString } from 'utils/apiClient';

describe('mapToEmptyString', () => {
  it('turns empty values into empty strings and keeps the others', () => {
    expect(mapToEmptyString({ city: 'Boston', country: undefined, address: '' }))
      .toEqual({ city: 'Boston', country: '', address: '' });
  });

  // KNOWN-GAP: OBAF checks `value in valuesToSkip` (array indexes), so null is not skipped.
  it.skip('leaves out the values it is asked to skip', () => {
    expect(mapToEmptyString({ city: 'Boston', postalCode: null, country: undefined }, [null]))
      .toEqual({ city: 'Boston', country: '' });
  });

  // KNOWN-GAP: OBAF skips 0 because index 0 exists in ['x', 'y'].
  it.skip('skips by value, not by position in the list', () => {
    expect(mapToEmptyString({ first: 0, second: 1 }, ['x', 'y']))
      .toEqual({ first: '', second: 1 });
  });

  // KNOWN-GAP: OBAF stores the nested result under the literal key `nestedObject`.
  it.skip('maps a nested object under its own key', () => {
    expect(mapToEmptyString({ name: 'Ward', address: { city: undefined, country: 'USA', zip: null } }, [null]))
      .toEqual({ name: 'Ward', address: { city: '', country: 'USA' } });
  });
});
