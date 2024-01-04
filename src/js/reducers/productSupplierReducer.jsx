import { FETCH_PREFERENCE_TYPES } from 'actions/types';

const initialState = {
  preferenceTypes: [],
};

export default function productSupplierReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_PREFERENCE_TYPES:
      return {
        ...state,
        preferenceTypes: action.payload,
      };
    default:
      return state;
  }
}
