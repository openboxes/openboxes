import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';

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
