import { START_COUNT } from 'actions/types';

const initialState = {
  toCount: [],
};

export default function cycleCountReducer(state = initialState, action) {
  switch (action.type) {
    case START_COUNT:
      return {
        ...state,
        toCount: action.payload,
      };
    default:
      return state;
  }
}
