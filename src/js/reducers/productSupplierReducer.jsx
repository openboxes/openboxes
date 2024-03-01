import {
  FETCH_ATTRIBUTES,
  FETCH_PREFERENCE_TYPES,
  FETCH_RATING_TYPE_OPTIONS,
} from 'actions/types';

const initialState = {
  preferenceTypes: [],
  ratingTypeCodes: [],
  attributes: [],
};

export default function productSupplierReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_PREFERENCE_TYPES:
      return {
        ...state,
        preferenceTypes: action.payload,
      };
    case FETCH_RATING_TYPE_OPTIONS:
      return {
        ...state,
        ratingTypeCodes: action.payload,
      };
    case FETCH_ATTRIBUTES:
      return {
        ...state,
        attributes: action.payload,
      };
    default:
      return state;
  }
}
