import { arrayMove } from 'react-sortable-hoc';
import update from 'immutability-helper';
import { loadColors, loadOptions } from '../consts/dataFormat/dataLoading';
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

function getGraphOptions(payload) {
  if (payload.type === 'bar') {
    return loadOptions(payload.method !== 'getFillRate');
  }
  if (payload.type === 'horizontalBar') {
    return loadOptions(null, true, 'right', Math.max(...payload.data.datasets[0].data));
  }
  return loadOptions();
}

const initialState = {
  data: [],
  numberData: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_GRAPHS: {
      const { payload } = action;
      // Data formatting
      if (payload.type === 'bar' || payload.type === 'doughnut' || payload.type === 'horizontalBar' || payload.type === 'line') {
        payload.data.datasets = loadColors(payload.data, payload.type);
        payload.options = getGraphOptions(payload);
      }
      // new reference to array so the component is re-rendered when value changes
      const newState = [].concat(state.data);
      const index = findInArray(payload.id, state.data);
      if (index === false) {
        newState.push(payload);
      } else {
        newState[index] = payload;
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
