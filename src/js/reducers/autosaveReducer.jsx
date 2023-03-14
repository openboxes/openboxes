import _ from 'lodash';

import { ADD_NOT_SAVED_LINE, REMOVE_SAVED_LINE } from 'actions/types';

const initialState = {
  outbound: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case ADD_NOT_SAVED_LINE:
      return {
        ...state,
        [action.payload.workflow]:
          [...state[action.payload.workflow], action.payload.line],
      };
    case REMOVE_SAVED_LINE:
      return {
        ...state,
        [action.payload.workflow]:
          _.without(
            state[action.payload.workflow],
            _.find(state[action.payload.workflow], action.payload.line, 0),
          ),
      };
    default:
      return state;
  }
}
