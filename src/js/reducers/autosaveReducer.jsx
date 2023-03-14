import { ADD_LINES } from 'actions/types';

const initialState = {
  outbound: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case ADD_LINES:
      return {
        ...state,
        [action.payload.workflow]: action.payload.lines,
      };
    default:
      return state;
  }
}
