import { FETCH_LOCATION_TYPES } from 'actions/types';

const initialState = {
  locationTypes: [],
};

export default function locationReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_LOCATION_TYPES:
      return {
        ...state,
        locationTypes: action.payload,
      };
    default:
      return state;
  }
}
