import { START_COUNT } from 'actions/types';

const initialState = {
  requests: [],
};

export default function cycleCountReducer(state = initialState, action) {
  switch (action.type) {
    case START_COUNT:
      return {
        ...state,
        requests: action.payload,
      };
    default:
      return state;
  }
}
