import { arrayMove } from 'react-sortable-hoc';
import update from 'immutability-helper';
import {
  ADD_TO_INDICATORS,
  FETCH_GRAPHS,
  FETCH_NUMBERS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
  RESET_INDICATORS,
} from '../actions/types';

function arrayArchive(array = [], index) {
  const newArray = update(array, { [index]: { archived: { $set: 1 } } });
  return newArray;
}

function arrayUnarchive(array = [], index) {
  const newArray = update(array, { [index]: { archived: { $set: 0 } } });
  return newArray;
}

function findInArray(id, array = []) {
  for (let i = 0; i < array.length; i += 1) {
    if (array[i].id === id) {
      return i;
    }
  }
  return false;
}

const initialState = {
  data: [],
  numberData: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_GRAPHS: {
      // new reference to array so the component is re-rendered when value changes
      const newState = [].concat(state.data);
      const index = findInArray(action.payload.id, state.data);
      if (index === false) {
        newState.push(action.payload);
      } else {
        newState[index] = action.payload;
      }
      return {
        ...state,
        data: newState,
      };
    }
    case FETCH_NUMBERS:
      return {
        ...state,
        numberData: action.payload.data,
      };
    case RESET_INDICATORS:
      return initialState;
    case REORDER_INDICATORS: {
      if (action.payload.type === 'graph') {
        return {
          ...state,
          data: arrayMove(
            state.data,
            action.payload.oldIndex,
            action.payload.newIndex,
          ),
        };
      }
      if (action.payload.type === 'number') {
        return {
          ...state,
          numberData: arrayMove(
            state.numberData,
            action.payload.oldIndex,
            action.payload.newIndex,
          ),
        };
      }
      return state;
    }
    case ADD_TO_INDICATORS:
      if (action.payload.type === 'graph') {
        return {
          ...state,
          data: arrayUnarchive(state.data, action.payload.index),
        };
      }
      if (action.payload.type === 'number') {
        return {
          ...state,
          numberData: arrayUnarchive(state.numberData, action.payload.index),
        };
      }
      return state;
    case REMOVE_FROM_INDICATORS: {
      if (action.payload.type === 'graph') {
        return {
          ...state,
          data: arrayArchive(state.data, action.payload.index),
        };
      }
      if (action.payload.type === 'number') {
        return {
          ...state,
          numberData: arrayArchive(state.numberData, action.payload.index),
        };
      }
      return state;
    }
    default:
      return state;
  }
}
