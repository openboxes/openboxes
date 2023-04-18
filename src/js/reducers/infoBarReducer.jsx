import update from 'immutability-helper';

import {
  ADD_INFO_BAR,
  CLOSE_INFO_BAR,
  HIDE_INFO_BAR_MODAL,
  SHOW_INFO_BAR_MODAL,
} from 'actions/types';


/**
 * Initial state for Info Bar reducer
 * @type {{bars: {
 *   [BAR_NAME]: {
 *     isModalOpen: boolean,
 *     closed: boolean,
 *    title: {
 *       label: string,
 *       defaultMessage: string,
 *     },
 *     versionLabel: {
 *       label: string,
 *       defaultMessage: string,
 *     }
 *   }
 * }}}
 */
const initialState = {
  bars: {},
};
/**
 * The purpose of this reducer is to control the content of the InfoBars
 * that are being displayed under the Header.
 * @param state
 * @param action
 * @returns state
 */
export default function infoBarReducer(state = initialState, action) {
  switch (action.type) {
    /**
     * This action performs a creation/addition of a new InfoBar
     * with parameters that are passed inside of payload.
     */
    case ADD_INFO_BAR:
      return update(
        state,
        { bars: { [action.payload.name]: { $set: { ...action.payload, closed: false } } } },
      );
    /**
     * This action performs a permanent closing of selected InfoBar so that if user closes this bar
     * he won't see it on his next visit of the page.
     * State of this closing is being persisted in localStorage using redux-persist.
     */
    case CLOSE_INFO_BAR:
      return update(
        state,
        { bars: { [action.payload.name]: { closed: { $set: true } } } },
      );
    /**
     * This action performs a state of opening an InfoBar modal.
     */
    case SHOW_INFO_BAR_MODAL:
      return update(
        state,
        { bars: { [action.payload.name]: { isModalOpen: { $set: true } } } },
      );
    /**
     * This action performs a state of closing an InfoBar modal.
     */
    case HIDE_INFO_BAR_MODAL:
      return update(
        state,
        { bars: { [action.payload.name]: { isModalOpen: { $set: false } } } },
      );
    default:
      return state;
  }
}
