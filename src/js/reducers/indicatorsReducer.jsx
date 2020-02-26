import { arrayMove } from 'react-sortable-hoc';
import {
  ADD_TO_INDICATORS,
  FETCH_INDICATORS,
  REMOVE_FROM_INDICATORS,
  REORDER_INDICATORS,
} from '../actions/types';
import { indicatorsFetched } from '../../assets/dataFormat/indicators';

function arrayArchive(array, index) {
  array[index].archived = 1;
  return arrayMove(array, index, 0);
}
function arrayUnarchive(array, index) {
  array[index].archived = 0;
  return array;
}

const initialState = {
  data: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_INDICATORS:
      const indicators = indicatorsFetched;
      return {
        ...state,
        data: indicators,
      };
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
