import {
  SET_ERRORS,
  SET_ERRORS_BY_ID,
  SUBMIT_FORM,
} from 'actions/types';

export default function errorsReducer(state = {
  isFormSubmitted: false,
  errors: {},
}, action) {
  switch (action.type) {
    case SET_ERRORS:
      return {
        ...state,
        errors: action.payload,
      };
    case SET_ERRORS_BY_ID:
      return {
        ...state,
        errors: {
          ...state.errors,
          [action.payload.id]: action.payload.errors,
        },
      };
    case SUBMIT_FORM:
      return {
        ...state,
        isFormSubmitted: true,
      };
    default:
      return state;
  }
}
