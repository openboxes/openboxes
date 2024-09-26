import { SET_SCROLL_TO_BOTTOM } from 'actions/types';

const initialState = {
  scrollToBottom: false,
};

export default function outboundImportReducer(state = initialState, action) {
  switch (action.type) {
    case SET_SCROLL_TO_BOTTOM:
      return {
        ...state,
        scrollToBottom: action.payload,
      };
    default:
      return state;
  }
}
