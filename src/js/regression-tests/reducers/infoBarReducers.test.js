import {
  ADD_INFO_BAR,
  CLOSE_INFO_BAR,
  HIDE_INFO_BAR,
  HIDE_INFO_BAR_MODAL,
  SHOW_INFO_BAR,
  SHOW_INFO_BAR_MODAL,
} from 'actions/types';
import { InfoBar } from 'consts/infoBar';
import infoBarReducer from 'reducers/infoBarReducer';
import infoBarVisibilityReducer from 'reducers/infoBarVisibilityReducer';

import deepFreeze from './deepFreeze';

const BAR = InfoBar.FULL_OUTBOUND_IMPORT;
const OTHER = InfoBar.STOCK_TRANSFER_DESCRIPTION;

describe('infoBarReducer', () => {
  it('starts with no bars and ignores unknown actions', () => {
    const initial = infoBarReducer(undefined, { type: 'UNKNOWN' });
    expect(initial).toEqual({ bars: {} });

    const previous = deepFreeze({ bars: { [BAR]: { closed: false } } });
    expect(infoBarReducer(previous, { type: 'UNKNOWN' })).toBe(previous);
  });

  it('adds a bar from the payload, always opened', () => {
    const next = infoBarReducer(deepFreeze({ bars: { [OTHER]: { name: OTHER, closed: true } } }), {
      type: ADD_INFO_BAR,
      payload: {
        name: BAR, isCloseable: true, title: { label: 'x' }, closed: true,
      },
    });

    expect(next.bars[BAR]).toEqual({
      name: BAR, isCloseable: true, title: { label: 'x' }, closed: false,
    });
    expect(next.bars[OTHER]).toEqual({ name: OTHER, closed: true });
  });

  it('re-adding a closed bar opens it again', () => {
    const next = infoBarReducer(deepFreeze({ bars: { [BAR]: { name: BAR, closed: true } } }), {
      type: ADD_INFO_BAR,
      payload: { name: BAR },
    });

    expect(next.bars[BAR].closed).toBe(false);
  });

  it('closes a bar permanently and keeps its other fields', () => {
    const previous = deepFreeze({
      bars: { [BAR]: { name: BAR, closed: false, isCloseable: true } },
    });

    const next = infoBarReducer(previous, {
      type: CLOSE_INFO_BAR,
      payload: { name: BAR },
    });

    expect(next.bars[BAR]).toEqual({ name: BAR, closed: true, isCloseable: true });
  });

  it('opens and hides the modal of a bar', () => {
    const previous = deepFreeze({ bars: { [BAR]: { name: BAR, isModalOpen: false } } });

    const shown = infoBarReducer(previous, {
      type: SHOW_INFO_BAR_MODAL,
      payload: { name: BAR },
    });
    expect(shown.bars[BAR].isModalOpen).toBe(true);

    const hidden = infoBarReducer(deepFreeze(shown), {
      type: HIDE_INFO_BAR_MODAL,
      payload: { name: BAR },
    });
    expect(hidden.bars[BAR].isModalOpen).toBe(false);
  });
});

describe('infoBarVisibilityReducer', () => {
  it('starts with every known bar hidden', () => {
    expect(infoBarVisibilityReducer(undefined, { type: 'UNKNOWN' })).toEqual({
      FULL_OUTBOUND_IMPORT: false,
      STOCK_TRANSFER_DESCRIPTION: false,
      STOCK_REPLENISHMENT_DESCRIPTION: false,
    });
  });

  it('returns the same state for an unknown action', () => {
    const previous = deepFreeze({ [BAR]: true });

    expect(infoBarVisibilityReducer(previous, { type: 'UNKNOWN' })).toBe(previous);
  });

  it.each([
    [ADD_INFO_BAR, false, true],
    [SHOW_INFO_BAR, false, true],
    [HIDE_INFO_BAR, true, false],
    [CLOSE_INFO_BAR, true, false],
  ])('%s turns the bar from %s to %s and leaves the others', (type, before, after) => {
    const previous = deepFreeze({ [BAR]: before, [OTHER]: before });

    const next = infoBarVisibilityReducer(previous, { type, payload: { name: BAR } });

    expect(next).toEqual({ [BAR]: after, [OTHER]: before });
  });
});
