import { arrayMove } from 'react-sortable-hoc';
import { ADD_TO_INDICATORS, FETCH_INDICATORS, REMOVE_FROM_INDICATORS, REORDER_INDICATORS } from '../actions/types';

function arrayRemove(array, index) {
  array.splice(index, 1);
  return array;
}

const initialState = {
  data: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_INDICATORS:
      const indicators = [
        {
          title: 'Expiration summary',
          id: Math.random(),
          data: {
            labels: ['January', 'February', 'March', 'April', 'May', 'June'],
            datasets: [{
              label: 'Expiration summary',
              fill: true,
              data: [12, 30, 26, 7, 19, 17],
            }],
          },
        },
        {
          title: 'Expiration summary bis',
          id: Math.random(),
          data: {
            labels: ['January', 'February', 'March', 'April', 'May', 'June'],
            datasets: [{
              label: 'Expiration summary',
              fill: true,
              data: [12, 3, 26, 70, 18, 17],
            }],
          },
        },
        {
          title: 'Expiration summary ter',
          id: Math.random(),
          data: {
            labels: ['January', 'February', 'March', 'April', 'May', 'June'],
            datasets: [{
              label: 'Expiration summary',
              fill: true,
              data: [14, 30, 26, 7, 18, 17],
            }],
          },
        },
      ];
      return {
        ...state,
        data: indicators
      };
    case REORDER_INDICATORS:
      return {
        ...state,
        data: arrayMove(state.data, action.payload.oldIndex, action.payload.newIndex)
      };
    case ADD_TO_INDICATORS:
      return state;
    case REMOVE_FROM_INDICATORS:
      return {
        ...state,
        data: arrayRemove(state.data, action.payload.index)
      };
    default:
      return state;
  }
}
