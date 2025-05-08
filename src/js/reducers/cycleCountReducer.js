import {
  ERASE_DRAFT,
  FETCH_BIN_LOCATIONS,
  FETCH_CYCLE_COUNT_REASON_CODES,
  START_COUNT,
  START_RESOLUTION,
  UPDATE_CYCLE_COUNT_IDS,
} from 'actions/types';

const initialState = {
  requests: [],
  cycleCounts: [],
  reasonCodes: [],
  binLocations: [],
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
    case FETCH_BIN_LOCATIONS:
      return {
        ...state,
        binLocations: action.payload,
      };
    case ERASE_DRAFT:
      if (action.payload === 'count') {
        return {
          ...state,
          requests: [],
        };
      }
      if (action.payload === 'resolve') {
        return {
          ...state,
          cycleCounts: [],
        };
      }
      return state;
    case UPDATE_CYCLE_COUNT_IDS:
      return {
        ...state,
        cycleCounts: action.payload,
      };
    default:
      return state;
  }
}
