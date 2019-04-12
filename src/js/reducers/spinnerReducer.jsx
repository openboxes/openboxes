import { SHOW_SPINNER, HIDE_SPINNER } from '../actions/types';

const initialState = {
  show: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case SHOW_SPINNER:
      return { ...state, show: true };
    case HIDE_SPINNER:
      return { ...state, show: false };
    default:
      return state;
  }
}
