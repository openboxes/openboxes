import { SET_OFFLINE, SET_ONLINE } from 'actions/types';

const initialState = {
  online: navigator.onLine,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case SET_ONLINE:
      return { ...state, online: true };
    case SET_OFFLINE:
      return { ...state, online: false };
    default:
      return state;
  }
}
