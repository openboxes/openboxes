import { arrayMove } from 'react-sortable-hoc';
import {
  ADD_TO_INDICATORS,
  FETCH_INDICATORS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
} from '../actions/types';

function arrayArchive(array, index) {
  const newArray = array;
  newArray[index].archived = 1;
  return newArray;
}

function arrayUnarchive(array, index) {
  const newArray = array;
  newArray[index].archived = 0;
  return newArray;
}

function findInArray(id, array) {
  for (let i = 0; i < array.length; i += 1) {
    if (array[i].id === id) {
      return i;
    }
  }
  return false;
}

const initialState = {
  data: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_INDICATORS: {
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
    case REORDER_INDICATORS:
      return {
        ...state,
        data: arrayMove(
          state.data,
          action.payload.oldIndex,
          action.payload.newIndex,
        ),
      };
    case ADD_TO_INDICATORS:
      return {
        ...state,
        data: arrayUnarchive(state.data, action.payload.index),
      };
    case REMOVE_FROM_INDICATORS:
      return {
        ...state,
        data: arrayArchive(state.data, action.payload.index),
      };
    default:
      return state;
  }
}
