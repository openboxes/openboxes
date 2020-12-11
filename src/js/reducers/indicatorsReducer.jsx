import { arrayMove } from 'react-sortable-hoc';
import update from 'immutability-helper';
import { loadGraphColors, loadGraphOptions } from '../consts/dataFormat/graphConfig';
import {
  ADD_TO_INDICATORS,
  FETCH_GRAPHS,
  FETCH_NUMBERS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
  RESET_INDICATORS,
  FETCH_CONFIG,
  SET_ACTIVE_CONFIG,
} from '../actions/types';
import { loadNumbersOptions } from '../consts/dataFormat/customGraphConfig';

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
    if (array[i] && array[i].id === id) {
      return i;
    }
  }
  return false;
}

const initialState = {
  allData: [],
  config: {},
  activeConfig: sessionStorage.getItem('dashboardKey') || 'personal',
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_GRAPHS: {
      const { payload } = action;
      // Data formatting
      if (payload.type === 'numbers' || payload.type === 'numberTable') {
        payload.options = loadNumbersOptions(payload);
      }
      if (payload.type === 'bar' || payload.type === 'doughnut' || payload.type === 'horizontalBar' || payload.type === 'line') {
        payload.data.datasets = loadGraphColors(payload);
        payload.options = loadGraphOptions(payload);
      }
      // new reference to array so the component is re-rendered when value changes
      const newData = [].concat(state.allData);
      const index = findInArray(payload.id, state.allData);
      if (index === false) {
        newData[action.payload.id - 1] = action.payload;
      } else {
        newData[index] = payload;
      }
      return {
        ...state,
        allData: newData,
      };
    }
    case FETCH_NUMBERS: {
      // new reference to array so the component is re-rendered when value changes
      const newData = [].concat(state.allData);
      const index = findInArray(action.payload.id, state.allData);
      if (index === false) {
        newData[action.payload.id - 1] = action.payload;
      } else {
        newData[index] = action.payload;
      }
      return {
        ...state,
        allData: newData,
      };
    }
    case RESET_INDICATORS:
      return {
        ...state,
        allData: [],
      };
    case REORDER_INDICATORS: {
      return {
        ...state,
        allData: arrayMove(
          state.allData,
          action.payload.oldIndex,
          action.payload.newIndex,
        ),
      };
    }
    case ADD_TO_INDICATORS:
      return {
        ...state,
        allData: arrayUnarchive(state.allData, action.payload.index),
      };
    case REMOVE_FROM_INDICATORS: {
      return {
        ...state,
        allData: arrayArchive(state.allData, action.payload.index),
      };
    }
    case FETCH_CONFIG:
      return {
        ...state,
        config: action.payload.data,
      };
    case SET_ACTIVE_CONFIG:
      return {
        ...state,
        activeConfig: action.payload.data,
      };
    default:
      return state;
  }
}
