import _ from 'lodash';
import { combineReducers } from 'redux';

import {
  ADD_EMPTY_ROW,
  FETCH_CYCLE_COUNTS,
  IMPORT_CYCLE_COUNTS,
  MARK_ALL_AS_UPDATED,
  REMOVE_ROW, SET_UPDATED,
  UPDATE_COUNTED_BY,
  UPDATE_DATE_COUNTED,
  UPDATE_FIELD_VALUE,
} from 'actions/types';
import {
  addEmptyRow,
  normalizeCycleCounts,
  removeRow, setAllItemsUpdatedState,
} from 'utils/cycleCountUtils';

function entitiesReducer(state = {}, action) {
  switch (action.type) {
    case FETCH_CYCLE_COUNTS:
      return normalizeCycleCounts(action.payload).entities;
    case ADD_EMPTY_ROW:
      return {
        ...state,
        [action.payload.id]: addEmptyRow(state[action.payload.id]),
      };
    case REMOVE_ROW: {
      return {
        ...state,
        [action.payload.id]: removeRow(state[action.payload.id], action.payload.rowId),
      };
    }
    case SET_UPDATED: {
      return {
        ...state,
        [action.payload.id]: setAllItemsUpdatedState(
          state[action.payload.id],
          action.payload.updated,
        ),
      };
    }
    case MARK_ALL_AS_UPDATED: {
      const { cycleCountIds } = action.payload;
      const nextState = { ...state };
      cycleCountIds.forEach((id) => {
        if (state[id]) {
          nextState[id] = setAllItemsUpdatedState(state[id], action.payload.updated);
        }
      });
      return nextState;
    }
    case IMPORT_CYCLE_COUNTS: {
      return {
        ...state,
        ...action.payload.entities,
      };
    }
    case UPDATE_FIELD_VALUE: {
      const updatedCycleCountItems = state[action.payload.id].cycleCountItems.map((item) => {
        if (item.id === action.payload.rowId) {
          // Updating object through lodash to support nested fields (inventory item properties)
          return _.set({ ...item }, action.payload.field, action.payload.value);
        }
        return item;
      });
      return {
        ...state,
        [action.payload.id]: {
          ...state[action.payload.id],
          cycleCountItems: updatedCycleCountItems,
        },
      };
    }
    default:
      return state;
  }
}

function dateCountedReducer(state = {}, action) {
  switch (action.type) {
    case FETCH_CYCLE_COUNTS:
      return normalizeCycleCounts(action.payload).dateCounted;
    case UPDATE_DATE_COUNTED: {
      return {
        ...state,
        [action.payload.id]: action.payload.dateCounted,
      };
    }
    case IMPORT_CYCLE_COUNTS: {
      return {
        ...state,
        ...action.payload.dateCounted,
      };
    }
    default:
      return state;
  }
}

function countedByReducer(state = {}, action) {
  switch (action.type) {
    case FETCH_CYCLE_COUNTS:
      return normalizeCycleCounts(action.payload).countedBy;
    case UPDATE_COUNTED_BY: {
      return {
        ...state,
        [action.payload.id]: action.payload.countedBy,
      };
    }
    case IMPORT_CYCLE_COUNTS: {
      return {
        ...state,
        ...action.payload.countedBy,
      };
    }
    default:
      return state;
  }
}

export default combineReducers({
  entities: entitiesReducer,
  dateCounted: dateCountedReducer,
  countedBy: countedByReducer,
});
