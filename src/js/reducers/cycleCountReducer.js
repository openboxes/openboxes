import { START_COUNT, START_RESOLUTION } from 'actions/types';

const initialState = {
  requests: [],
  cycleCounts: [],
};

export default function cycleCountReducer(state = initialState, action) {
  switch (action.type) {
    case START_COUNT:
      return {
        ...state,
        requests: action.payload,
      };
    case START_RESOLUTION:
      return {
        ...state,
        cycleCounts: action.payload,
      };
    default:
      return state;
  }
}
