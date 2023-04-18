import {
  ADD_INFO_BAR,
  CLOSE_INFO_BAR,
  HIDE_INFO_BAR,
  SHOW_INFO_BAR,
} from 'actions/types';
import { InfoBar } from 'consts/infoBar';

/**
 * Initial state for Info Bar Visibility reducer
 * @type {{ [BAR_NAME]: boolean }}
 */
const initialState = Object.keys(InfoBar).reduce((acc, bar) => ({ ...acc, [bar]: false }), {});

/**
 * The purpose of this reducer is to control the visibility of the InfoBar across different pages
 * @param state
 * @param action
 * @returns {{BAR_NAME?: boolean}}
 */
export default function infoBarVisibilityReducer(state = initialState, action) {
  switch (action.type) {
    /**
     * This action performs an initial infoBar visibility state assignment.
     */
    case ADD_INFO_BAR:
      return { ...state, [action.payload.name]: true };
    /**
     * This action performs a hiding from an infoBar across different pages.
     * If the InfoBar is visible calling this action should hide the InfoBar from all pages
     */
    case HIDE_INFO_BAR:
      return { ...state, [action.payload.name]: false };
    /**
     * This action performs a conditional rendering of an InforBar on a page.
     * If the InfoBar is invisible and is not permanently closed (InfoBarReducer { closed: false })
     * calling this action should render the InfoBar on all pages.
     */
    case SHOW_INFO_BAR:
      return { ...state, [action.payload.name]: true };
    /**
     * This action is activated as a side effect to InfoBarReducer(action: CLOSE_INFO_BAR)
     * Which hides the InfoBar if it is permanently closed.
     */
    case CLOSE_INFO_BAR:
      return { ...state, [action.payload.name]: false };
    default:
      return state;
  }
}
