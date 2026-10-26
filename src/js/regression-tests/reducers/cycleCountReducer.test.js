import {
  ERASE_DRAFT,
  FETCH_BIN_LOCATIONS,
  FETCH_CYCLE_COUNT_REASON_CODES,
  START_COUNT,
  START_RESOLUTION,
  UPDATE_CYCLE_COUNT_IDS,
} from 'actions/types';
import { TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import cycleCountReducer from 'reducers/cycleCountReducer';

import deepFreeze from './deepFreeze';

const filledState = () => deepFreeze({
  requests: { loc1: ['r1'], loc2: ['r2'] },
  cycleCounts: { loc1: ['c1'], loc2: ['c2'] },
  reasonCodes: [{ id: 'old' }],
  binLocations: [{ id: 'bin-old' }],
});

describe('cycleCountReducer', () => {
  it('starts empty', () => {
    expect(cycleCountReducer(undefined, { type: 'UNKNOWN' })).toEqual({
      requests: {}, cycleCounts: {}, reasonCodes: [], binLocations: [],
    });
  });

  it('returns the same state for an unknown action', () => {
    const previous = filledState();

    expect(cycleCountReducer(previous, { type: 'UNKNOWN' })).toBe(previous);
  });

  it('keeps only the requests of the location a count was started for', () => {
    const next = cycleCountReducer(filledState(), {
      type: START_COUNT,
      payload: { locationId: 'loc3', requests: ['r3', 'r4'] },
    });

    expect(next.requests).toEqual({ loc3: ['r3', 'r4'] });
    expect(next.cycleCounts).toEqual({ loc1: ['c1'], loc2: ['c2'] });
  });

  it('keeps only the cycle counts of the location a resolution was started for', () => {
    const next = cycleCountReducer(filledState(), {
      type: START_RESOLUTION,
      payload: { locationId: 'loc1', cycleCounts: ['c9'] },
    });

    expect(next.cycleCounts).toEqual({ loc1: ['c9'] });
    expect(next.requests).toEqual({ loc1: ['r1'], loc2: ['r2'] });
  });

  it('stores the reason codes and bin locations', () => {
    const withCodes = cycleCountReducer(filledState(), {
      type: FETCH_CYCLE_COUNT_REASON_CODES,
      payload: [{ id: 'DAMAGED' }],
    });
    const withBins = cycleCountReducer(deepFreeze(withCodes), {
      type: FETCH_BIN_LOCATIONS,
      payload: [{ id: 'bin1' }],
    });

    expect(withBins.reasonCodes).toEqual([{ id: 'DAMAGED' }]);
    expect(withBins.binLocations).toEqual([{ id: 'bin1' }]);
  });

  it('erases the count draft of one location on the to count tab', () => {
    const next = cycleCountReducer(filledState(), {
      type: ERASE_DRAFT,
      payload: { locationId: 'loc1', tab: TO_COUNT_TAB },
    });

    expect(next.requests).toEqual({ loc2: ['r2'] });
    expect(next.cycleCounts).toEqual({ loc1: ['c1'], loc2: ['c2'] });
  });

  it('erases the resolution draft of one location on the to resolve tab', () => {
    const next = cycleCountReducer(filledState(), {
      type: ERASE_DRAFT,
      payload: { locationId: 'loc2', tab: TO_RESOLVE_TAB },
    });

    expect(next.cycleCounts).toEqual({ loc1: ['c1'] });
    expect(next.requests).toEqual({ loc1: ['r1'], loc2: ['r2'] });
  });

  it('ignores an erase for any other tab', () => {
    const previous = filledState();

    expect(cycleCountReducer(previous, {
      type: ERASE_DRAFT,
      payload: { locationId: 'loc1', tab: 'ALL' },
    })).toBe(previous);
  });

  it('replaces the cycle count ids of one location and keeps the others', () => {
    const next = cycleCountReducer(filledState(), {
      type: UPDATE_CYCLE_COUNT_IDS,
      payload: { locationId: 'loc2', cycleCounts: ['c7', 'c8'] },
    });

    expect(next.cycleCounts).toEqual({ loc1: ['c1'], loc2: ['c7', 'c8'] });
  });
});
