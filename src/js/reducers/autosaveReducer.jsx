import { ADD_LINES, REMOVE_LINES } from 'actions/types';

const initialState = {
  outbound: { id: null, lineItems: [], lastUpdateDate: null },
};

export default function (state = initialState, action) {
  switch (action.type) {
    case ADD_LINES:
      return {
        ...state,
        [action.payload.workflow]: {
          id: action.payload.id,
          lineItems: action.payload.lines,
          lastUpdated: new Date(),
        },
      };
    case REMOVE_LINES:
      return {
        ...state,
        [action.payload.workflow]: initialState[action.payload.workflow],
      };
    default:
      return state;
  }
}
