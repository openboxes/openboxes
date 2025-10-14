import { combineReducers } from 'redux';

import {
  ADD_EMPTY_ROW,
  FETCH_CYCLE_COUNTS,
  REMOVE_ROW, SET_UPDATED,
  UPDATE_DATE_COUNTED,
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
    default:
      return state;
  }
}

function countedByReducer(state = {}, action) {
  switch (action.type) {
    case FETCH_CYCLE_COUNTS:
      return normalizeCycleCounts(action.payload).countedBy;
    default:
      return state;
  }
}

export default combineReducers({
  entities: entitiesReducer,
  dateCounted: dateCountedReducer,
  countedBy: countedByReducer,
});
