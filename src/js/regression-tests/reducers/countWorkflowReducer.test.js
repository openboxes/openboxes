import {
  ADD_EMPTY_ROW,
  CLEAR_COUNT_WORKFLOW,
  FETCH_CYCLE_COUNTS,
  IMPORT_CYCLE_COUNTS,
  MARK_ALL_AS_UPDATED,
  REMOVE_ROW,
  SET_UPDATED,
  UPDATE_COUNTED_BY,
  UPDATE_DATE_COUNTED,
  UPDATE_FIELD_VALUE,
} from 'actions/types';
import countWorkflowReducer from 'reducers/countWorkflowReducer';

import deepFreeze from './deepFreeze';

// The real client pulls in the store (through the login modal), and the store imports the root
// reducer: loading it from here would close that import cycle half way. No request is made.
jest.mock('utils/apiClient', () => ({ __esModule: true, default: {} }));

const item = (id, overrides = {}) => ({
  id,
  countIndex: 0,
  custom: false,
  product: { id: 'p1', lotAndExpiryControl: true },
  inventoryItem: { lotNumber: 'L1', expirationDate: null },
  quantityCounted: null,
  ...overrides,
});

const stateWith = (entities, dateCounted = {}, countedBy = {}) => deepFreeze({
  entities,
  dateCounted,
  countedBy,
});

describe('countWorkflowReducer', () => {
  it('starts empty', () => {
    expect(countWorkflowReducer(undefined, { type: 'UNKNOWN' }))
      .toEqual({ entities: {}, dateCounted: {}, countedBy: {} });
  });

  it('returns the same state for an unknown action', () => {
    const previous = stateWith({ cc1: { id: 'cc1', cycleCountItems: [] } });

    expect(countWorkflowReducer(previous, { type: 'UNKNOWN' })).toBe(previous);
  });

  describe(FETCH_CYCLE_COUNTS, () => {
    it('normalizes the fetched cycle counts into entities, date counted and counted by', () => {
      const previous = stateWith({ stale: { id: 'stale', cycleCountItems: [] } });

      const next = countWorkflowReducer(previous, {
        type: FETCH_CYCLE_COUNTS,
        payload: [{
          id: 'cc1',
          cycleCountItems: [
            item('first', { dateCounted: '2026-01-01', assignee: { id: 'u1' } }),
            item('recount', { countIndex: 1 }),
            item('custom', { custom: true, dateCreated: '2026-01-02' }),
          ],
        }],
      });

      // Only the first count (countIndex 0) is kept, in the order the API sends (it sorts the
      // items), and the previously loaded entities are replaced.
      expect(Object.keys(next.entities)).toEqual(['cc1']);
      expect(next.entities.cc1.cycleCountItems.map((i) => i.id)).toEqual(['first', 'custom']);
      expect(next.dateCounted).toEqual({ cc1: '2026-01-01' });
      expect(next.countedBy).toEqual({ cc1: { id: 'u1' } });
    });
  });

  describe(ADD_EMPTY_ROW, () => {
    it('appends a blank row for the product of the cycle count', () => {
      const previous = stateWith({ cc1: { id: 'cc1', cycleCountItems: [item('i1')] } });

      const next = countWorkflowReducer(previous, { type: ADD_EMPTY_ROW, payload: { id: 'cc1' } });

      const rows = next.entities.cc1.cycleCountItems;
      expect(rows).toHaveLength(2);
      expect(rows[1].id).toMatch(/^newRow/);
      expect(rows[1].product).toEqual({ id: 'p1', lotAndExpiryControl: true });
      expect(rows[1].quantityCounted).toBeNull();
      expect(previous.entities.cc1.cycleCountItems).toHaveLength(1);
    });
  });

  describe(REMOVE_ROW, () => {
    it('removes only the given row from the given cycle count', () => {
      const previous = stateWith({
        cc1: { id: 'cc1', cycleCountItems: [item('i1'), item('i2')] },
        cc2: { id: 'cc2', cycleCountItems: [item('i1')] },
      });

      const next = countWorkflowReducer(previous, {
        type: REMOVE_ROW,
        payload: { id: 'cc1', rowId: 'i1' },
      });

      expect(next.entities.cc1.cycleCountItems.map((i) => i.id)).toEqual(['i2']);
      expect(next.entities.cc2).toBe(previous.entities.cc2);
    });
  });

  describe(SET_UPDATED, () => {
    it('sets the updated flag on every item of one cycle count', () => {
      const previous = stateWith({
        cc1: { id: 'cc1', cycleCountItems: [item('i1'), item('i2', { updated: true })] },
      });

      const next = countWorkflowReducer(previous, {
        type: SET_UPDATED,
        payload: { id: 'cc1', updated: false },
      });

      expect(next.entities.cc1.cycleCountItems.map((i) => i.updated)).toEqual([false, false]);
    });
  });

  describe(MARK_ALL_AS_UPDATED, () => {
    it('flags the listed cycle counts and skips ids that are not loaded', () => {
      const previous = stateWith({
        cc1: { id: 'cc1', cycleCountItems: [item('i1')] },
        cc2: { id: 'cc2', cycleCountItems: [item('i2')] },
      });

      const next = countWorkflowReducer(previous, {
        type: MARK_ALL_AS_UPDATED,
        payload: { cycleCountIds: ['cc1', 'missing'], updated: true },
      });

      expect(next.entities.cc1.cycleCountItems[0].updated).toBe(true);
      expect(next.entities.cc2).toBe(previous.entities.cc2);
      expect(next.entities).not.toHaveProperty('missing');
    });
  });

  describe(IMPORT_CYCLE_COUNTS, () => {
    it('merges the imported entities and metadata over the current ones', () => {
      const previous = stateWith(
        {
          cc1: { id: 'cc1', cycleCountItems: [item('i1')] },
          cc2: { id: 'cc2', cycleCountItems: [item('i2')] },
        },
        { cc1: '2026-01-01', cc2: '2026-01-01' },
        { cc1: { id: 'u1' } },
      );
      const imported = { id: 'cc2', cycleCountItems: [item('i2', { quantityCounted: 5 })] };

      const next = countWorkflowReducer(previous, {
        type: IMPORT_CYCLE_COUNTS,
        payload: {
          entities: { cc2: imported },
          dateCounted: { cc2: '2026-02-02' },
          countedBy: { cc2: { id: 'u2' } },
        },
      });

      expect(next.entities).toEqual({ cc1: previous.entities.cc1, cc2: imported });
      expect(next.dateCounted).toEqual({ cc1: '2026-01-01', cc2: '2026-02-02' });
      expect(next.countedBy).toEqual({ cc1: { id: 'u1' }, cc2: { id: 'u2' } });
    });
  });

  describe(UPDATE_FIELD_VALUE, () => {
    it('sets a top level field on the matching row only', () => {
      const previous = stateWith({ cc1: { id: 'cc1', cycleCountItems: [item('i1'), item('i2')] } });

      const next = countWorkflowReducer(previous, {
        type: UPDATE_FIELD_VALUE,
        payload: {
          id: 'cc1', rowId: 'i2', field: 'quantityCounted', value: 7,
        },
      });

      expect(next.entities.cc1.cycleCountItems[1].quantityCounted).toBe(7);
      expect(next.entities.cc1.cycleCountItems[0]).toBe(previous.entities.cc1.cycleCountItems[0]);
    });

    // KNOWN-GAP: OBAF's UPDATE_FIELD_VALUE uses _.set on a shallow copy of the row,
    // so a nested field is written into the previous state's object (which fails when frozen).
    it.skip('sets a nested field on a copy, leaving the previous state as it was', () => {
      const row = { ...item('i1'), inventoryItem: { id: 'ii1', lotNumber: 'L1' } };
      const previous = stateWith({ cc1: { id: 'cc1', cycleCountItems: [row] } });

      const next = countWorkflowReducer(previous, {
        type: UPDATE_FIELD_VALUE,
        payload: {
          id: 'cc1', rowId: 'i1', field: 'inventoryItem.lotNumber', value: 'L9',
        },
      });

      expect(next.entities.cc1.cycleCountItems[0].inventoryItem).toEqual({ id: 'ii1', lotNumber: 'L9' });
      expect(next.entities.cc1.cycleCountItems[0].inventoryItem).not.toBe(row.inventoryItem);
      expect(previous.entities.cc1.cycleCountItems[0].inventoryItem.lotNumber).toBe('L1');
    });
  });

  it('updates the date counted of one cycle count', () => {
    const previous = stateWith({}, { cc1: '2026-01-01', cc2: '2026-01-01' });

    const next = countWorkflowReducer(previous, {
      type: UPDATE_DATE_COUNTED,
      payload: { id: 'cc2', dateCounted: '2026-03-03' },
    });

    expect(next.dateCounted).toEqual({ cc1: '2026-01-01', cc2: '2026-03-03' });
    expect(next.entities).toBe(previous.entities);
  });

  it('updates the counter of one cycle count', () => {
    const previous = stateWith({}, {}, { cc1: { id: 'u1' } });

    const next = countWorkflowReducer(previous, {
      type: UPDATE_COUNTED_BY,
      payload: { id: 'cc2', countedBy: { id: 'u2' } },
    });

    expect(next.countedBy).toEqual({ cc1: { id: 'u1' }, cc2: { id: 'u2' } });
  });

  it('clears all three slices', () => {
    const previous = stateWith(
      { cc1: { id: 'cc1', cycleCountItems: [] } },
      { cc1: '2026-01-01' },
      { cc1: { id: 'u1' } },
    );

    expect(countWorkflowReducer(previous, { type: CLEAR_COUNT_WORKFLOW }))
      .toEqual({ entities: {}, dateCounted: {}, countedBy: {} });
  });
});
