import { ADD_STOCK_MOVEMENT_DRAFT, REMOVE_STOCK_MOVEMENT_DRAFT } from 'actions/types';
import stockMovementDraftReducer from 'reducers/stockMovementDraftReducer';

import deepFreeze from './deepFreeze';

const NOW = new Date('2026-03-04T05:06:07Z');

describe('stockMovementDraftReducer', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(NOW);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('starts without drafts and ignores unknown actions', () => {
    expect(stockMovementDraftReducer(undefined, { type: 'UNKNOWN' })).toEqual({});

    const previous = deepFreeze({ sm1: { id: 'sm1' } });
    expect(stockMovementDraftReducer(previous, { type: 'UNKNOWN' })).toBe(previous);
  });

  it('stores a draft by id, stamped with the current time', () => {
    const previous = deepFreeze({ sm0: { id: 'sm0' } });

    const next = stockMovementDraftReducer(previous, {
      type: ADD_STOCK_MOVEMENT_DRAFT,
      payload: {
        id: 'sm1', lineItems: [{ id: 'li1' }], statusCode: 'PENDING', workflow: 'outbound',
      },
    });

    expect(next).toEqual({
      sm0: { id: 'sm0' },
      sm1: {
        id: 'sm1', lineItems: [{ id: 'li1' }], lastUpdated: NOW, statusCode: 'PENDING',
      },
    });
  });

  it('replaces an older draft of the same stock movement', () => {
    const previous = deepFreeze({
      sm1: {
        id: 'sm1', lineItems: [{ id: 'old' }], lastUpdated: new Date('2020-01-01'), statusCode: 'PENDING',
      },
    });

    const next = stockMovementDraftReducer(previous, {
      type: ADD_STOCK_MOVEMENT_DRAFT,
      payload: { id: 'sm1', lineItems: [{ id: 'new' }], statusCode: 'REQUESTED' },
    });

    expect(next.sm1.lineItems).toEqual([{ id: 'new' }]);
    expect(next.sm1.lastUpdated).toEqual(NOW);
  });

  it('removes a draft by id', () => {
    const previous = deepFreeze({ sm1: { id: 'sm1' }, sm2: { id: 'sm2' } });

    const next = stockMovementDraftReducer(previous, {
      type: REMOVE_STOCK_MOVEMENT_DRAFT,
      payload: { id: 'sm1' },
    });

    expect(next).toEqual({ sm2: { id: 'sm2' } });
  });
});
