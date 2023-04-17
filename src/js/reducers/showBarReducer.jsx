import {
  ADD_INFO_BAR,
  CLOSE_INFO_BAR,
  HIDE_INFO_BAR,
  SHOW_INFO_BAR,
} from 'actions/types';
import { InfoBar } from 'consts/infoBar';

const initialState = Object.keys(InfoBar).reduce((acc, bar) => ({ ...acc, [bar]: false }), {});

export default function showBarReducer(state = initialState, action) {
  switch (action.type) {
    case ADD_INFO_BAR:
      return { ...state, [action.payload.name]: true };
    case HIDE_INFO_BAR:
      return { ...state, [action.payload.name]: false };
    case SHOW_INFO_BAR:
      return { ...state, [action.payload.name]: true };
    case CLOSE_INFO_BAR:
      return { ...state, [action.payload.name]: false };
    default:
      return state;
  }
}
