import { FETCH_CYCLE_COUNT_REASON_CODES, START_COUNT, START_RESOLUTION } from 'actions/types';

const initialState = {
  requests: [],
  cycleCounts: [],
  reasonCodes: [],
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
    case FETCH_CYCLE_COUNT_REASON_CODES:
      return {
        ...state,
        reasonCodes: action.payload,
      };
    default:
      return state;
  }
}
