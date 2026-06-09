import _ from 'lodash';

import {
  ERASE_DRAFT,
  FETCH_BIN_LOCATIONS,
  FETCH_CYCLE_COUNT_REASON_CODES,
  START_COUNT,
  START_RESOLUTION,
  UPDATE_CYCLE_COUNT_IDS,
} from 'actions/types';
import { TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';

const initialState = {
  requests: {},
  cycleCounts: {},
  reasonCodes: [],
  binLocations: [],
};

export default function cycleCountReducer(state = initialState, action) {
  switch (action.type) {
    case START_COUNT:
      return {
        ...state,
        requests: {
          [action.payload.locationId]: action.payload.requests,
        },
      };
    case START_RESOLUTION:
      return {
        ...state,
        cycleCounts: {
          [action.payload.locationId]: action.payload.cycleCounts,
        },
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
      if (action.payload.tab === TO_COUNT_TAB) {
        return {
          ...state,
          requests: _.omit(state.requests, action.payload.locationId),
        };
      }
      if (action.payload.tab === TO_RESOLVE_TAB) {
        return {
          ...state,
          cycleCounts: _.omit(state.cycleCounts, action.payload.locationId),
        };
      }
      return state;
    case UPDATE_CYCLE_COUNT_IDS:
      return {
        ...state,
        cycleCounts: {
          ...state.cycleCounts,
          [action.payload.locationId]: action.payload.cycleCounts,
        },
      };
    default:
      return state;
  }
}
