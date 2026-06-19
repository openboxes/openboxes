import {
  createNormalizedState,
  denormalizeData,
  getNormalizedItem,
  normalizeData,
  removeNormalizedItem,
  resolveKey,
  updateNormalizedItem,
  upsertNormalizedItem,
} from 'utils/normalizationUtils';

import '@testing-library/jest-dom';

describe('resolveKey()', () => {
  it('should read the key from a single field by default', () => {
    expect(resolveKey({ id: 'a' }))
      .toBe('a');
  });

  it('should read the key from a custom string field', () => {
    expect(resolveKey({ shipmentItemId: 1, receiptItemId: 2 }, 'shipmentItemId'))
      .toBe(1);
  });

  it('should combine multiple fields into a composite key', () => {
    expect(resolveKey({ shipmentItemId: 1, receiptItemId: 2 }, ['shipmentItemId', 'receiptItemId']))
      .toBe('1:2');
  });

  it('should support a function descriptor', () => {
    expect(resolveKey({ a: 1, b: 2 }, (item) => item.a + item.b))
      .toBe(3);
  });
});

describe('createNormalizedState()', () => {
  it('should return an empty normalized state', () => {
    expect(createNormalizedState())
      .toEqual({ entities: {}, ids: [] });
  });
});

describe('normalizeData()', () => {
  it('should normalize an array using the default key field', () => {
    const normalized = normalizeData([{ id: 'a', name: 'A' }, { id: 'b', name: 'B' }]);
    expect(normalized.ids)
      .toEqual(['a', 'b']);
    expect(normalized.entities.a)
      .toEqual({ id: 'a', name: 'A' });
  });

  it('should normalize using a custom key field', () => {
    const normalized = normalizeData([{ shipmentItemId: 1, name: 'A' }], 'shipmentItemId');
    expect(normalized.ids)
      .toEqual([1]);
    expect(normalized.entities[1].name)
      .toBe('A');
  });

  it('should preserve the original order', () => {
    const normalized = normalizeData([{ id: 3 }, { id: 1 }, { id: 2 }]);
    expect(normalized.ids)
      .toEqual([3, 1, 2]);
  });

  it('should normalize using a composite key', () => {
    const normalized = normalizeData(
      [{ shipmentItemId: 1, receiptItemId: 2, name: 'A' }],
      ['shipmentItemId', 'receiptItemId'],
    );
    const compositeKey = '1:2';
    expect(normalized.ids)
      .toEqual([compositeKey]);
    expect(normalized.entities[compositeKey].name)
      .toBe('A');
  });

  it('should return an empty state for a non-array input', () => {
    expect(normalizeData(null))
      .toEqual({ entities: {}, ids: [] });
    expect(normalizeData(undefined))
      .toEqual({ entities: {}, ids: [] });
  });
});

describe('denormalizeData()', () => {
  it('should denormalize back to an ordered array', () => {
    const state = { entities: { a: { id: 'a' }, b: { id: 'b' } }, ids: ['b', 'a'] };
    expect(denormalizeData(state))
      .toEqual([{ id: 'b' }, { id: 'a' }]);
  });

  it('should return an empty array for an empty state', () => {
    expect(denormalizeData(undefined))
      .toEqual([]);
    expect(denormalizeData(createNormalizedState()))
      .toEqual([]);
  });
});

describe('getNormalizedItem()', () => {
  it('should return the entity for an existing id', () => {
    const state = { entities: { a: { id: 'a', name: 'A' } }, ids: ['a'] };
    expect(getNormalizedItem(state, 'a'))
      .toEqual({ id: 'a', name: 'A' });
  });

  it('should return undefined for a missing id', () => {
    expect(getNormalizedItem(createNormalizedState(), 'a'))
      .toBeUndefined();
  });
});

describe('updateNormalizedItem()', () => {
  it('should shallow-merge new data into the entity', () => {
    const state = { entities: { a: { id: 'a', qty: 1, name: 'A' } }, ids: ['a'] };
    const updated = updateNormalizedItem(state, 'a', { qty: 5 });
    expect(updated.entities.a)
      .toEqual({ id: 'a', qty: 5, name: 'A' });
  });

  it('should not mutate the original state', () => {
    const state = { entities: { a: { id: 'a', qty: 1 } }, ids: ['a'] };
    updateNormalizedItem(state, 'a', { qty: 5 });
    expect(state.entities.a.qty)
      .toBe(1);
  });

  it('should return the same state reference when the id does not exist', () => {
    const state = { entities: { a: { id: 'a' } }, ids: ['a'] };
    expect(updateNormalizedItem(state, 'b', { qty: 5 }))
      .toBe(state);
  });
});

describe('upsertNormalizedItem()', () => {
  it('should insert a new entity and append its id', () => {
    const state = { entities: { a: { id: 'a' } }, ids: ['a'] };
    const result = upsertNormalizedItem(state, { id: 'b', name: 'B' });
    expect(result.ids)
      .toEqual(['a', 'b']);
    expect(result.entities.b)
      .toEqual({ id: 'b', name: 'B' });
  });

  it('should merge into an existing entity without duplicating the id', () => {
    const state = { entities: { a: { id: 'a', name: 'A' } }, ids: ['a'] };
    const result = upsertNormalizedItem(state, { id: 'a', qty: 3 });
    expect(result.ids)
      .toEqual(['a']);
    expect(result.entities.a)
      .toEqual({ id: 'a', name: 'A', qty: 3 });
  });

  it('should work on an empty state with a custom key field', () => {
    const result = upsertNormalizedItem(createNormalizedState(), { uuid: 'x' }, 'uuid');
    expect(result.ids)
      .toEqual(['x']);
    expect(result.entities.x)
      .toEqual({ uuid: 'x' });
  });

  it('should upsert using a composite key', () => {
    const keyField = ['shipmentItemId', 'receiptItemId'];
    const compositeKey = '1:2';
    const inserted = upsertNormalizedItem(
      createNormalizedState(),
      { shipmentItemId: 1, receiptItemId: 2, qty: 1 },
      keyField,
    );
    expect(inserted.ids)
      .toEqual([compositeKey]);

    const merged = upsertNormalizedItem(
      inserted,
      { shipmentItemId: 1, receiptItemId: 2, qty: 5 },
      keyField,
    );
    expect(merged.ids)
      .toEqual([compositeKey]);
    expect(merged.entities[compositeKey].qty)
      .toBe(5);
  });
});

describe('removeNormalizedItem()', () => {
  it('should remove the entity and its id', () => {
    const state = { entities: { a: { id: 'a' }, b: { id: 'b' } }, ids: ['a', 'b'] };
    const result = removeNormalizedItem(state, 'a');
    expect(result.ids)
      .toEqual(['b']);
    expect(result.entities)
      .toEqual({ b: { id: 'b' } });
  });

  it('should return the same state reference when the id does not exist', () => {
    const state = { entities: { a: { id: 'a' } }, ids: ['a'] };
    expect(removeNormalizedItem(state, 'b'))
      .toBe(state);
  });
});
