import update from 'immutability-helper';
import _ from 'lodash';
import { arrayMove } from 'react-sortable-hoc';

import {
  FETCH_CONFIG,
  FETCH_CONFIG_AND_SET_ACTIVE,
  FETCH_GRAPHS,
  FETCH_NUMBERS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
  RESET_INDICATORS, SET_ACTIVE_CONFIG,
} from 'actions/types';
import { loadNumbersOptions } from 'consts/dataFormat/customGraphConfig';
import { loadGraphColors, loadGraphOptions } from 'consts/dataFormat/graphConfig';


function arrayArchive(array = [], index) {
  let newArray = update(array, { $splice: [[index, 1]] });
  newArray = _.map(newArray, (value, ind) => {
    if (ind < index) {
      return value;
    }

    return { ...value, id: value.id - 1 };
  });
  return newArray;
}

function findInArray(widgetId, array = []) {
  for (let i = 0; i < array.length; i += 1) {
    if (array[i] && array[i].widgetId === widgetId) {
      return i;
    }
  }
  return false;
}

const initialState = {
  data: [],
  numberData: [],
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
      const newState = [].concat(state.data);
      const index = findInArray(payload.widgetId, state.data);
      if (index === false) {
        newState[action.payload.id - 1] = action.payload;
      } else {
        newState[index] = payload;
      }
      return {
        ...state,
        data: newState,
      };
    }
    case FETCH_NUMBERS: {
      // new reference to array so the component is re-rendered when value changes
      const newState = [].concat(state.numberData);
      const index = findInArray(action.payload.widgetId, state.numberData);
      if (index === false) {
        newState[action.payload.id - 1] = action.payload;
      } else {
        newState[index] = action.payload;
      }
      return {
        ...state,
        numberData: newState,
      };
    }
    case RESET_INDICATORS:
      return {
        ...state,
        data: [],
        numberData: [],
      };
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
    case FETCH_CONFIG_AND_SET_ACTIVE:
      return {
        ...state,
        config: action.payload.data,
        activeConfig: action.payload.activeConfig,
      };
    default:
      return state;
  }
}
